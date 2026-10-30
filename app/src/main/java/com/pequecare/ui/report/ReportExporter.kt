package com.pequecare.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Locale

object ReportExporter {

    /**
     * Agrupa y formatea todos los datos pediátricos en un texto plano estructurado
     * para mensajes (WhatsApp, correo, SMS, etc.).
     */
    fun buildReportText(
        profile: ChildProfileEntity?,
        startDate: String,
        endDate: String,
        growthRecords: List<GrowthRecordEntity>,
        sleepRecords: List<SleepEntity>,
        mealRecords: List<MealEntity>,
        activityRecords: List<ActivityEntity>,
        appointments: List<MedicalAppointmentEntity>,
        temperatures: List<TemperatureRecordEntity>,
        treatments: List<MedicationTreatmentEntity>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("        REPORTE PEDIÁTRICO - PEQUECARE           ")
        sb.appendLine("==================================================")
        sb.appendLine("Paciente: ${profile?.name ?: "Sin nombre"} ${profile?.lastName ?: ""}".trim())
        sb.appendLine("Fecha de Nacimiento: ${profile?.birthDate ?: "N/D"}")
        sb.appendLine("Grupo Sanguíneo: ${profile?.bloodType?.ifBlank { "N/D" } ?: "N/D"}")
        sb.appendLine("Alergias: ${profile?.allergies?.ifEmpty { "Ninguna declarada" } ?: "Ninguna"}")
        sb.appendLine("Pediatra: ${profile?.pediatricianName?.ifBlank { "N/D" } ?: "N/D"}")
        sb.appendLine("Período del Reporte: $startDate a $endDate")
        sb.appendLine("==================================================")
        sb.appendLine()

        // 1. CRECIMIENTO Y METRICAS OMS
        sb.appendLine("--- 1. MÉTRICAS DE CRECIMIENTO (OMS) ---")
        if (growthRecords.isEmpty()) {
            sb.appendLine("No hay registros de crecimiento en el rango seleccionado.")
        } else {
            growthRecords.forEach { g ->
                val weightStr = String.format(Locale.US, "%.3f", g.weightKg).trimEnd('0').trimEnd('.')
                sb.appendLine("• [${g.date}] Peso: $weightStr kg (P${g.weightPercentile}) | Talla: ${g.heightCm} cm (P${g.heightPercentile}) | IMC: ${g.bmi} kg/m² (P${g.bmiPercentile} • ${g.bmiStatus})")
                if (!g.notes.isNullOrBlank()) sb.appendLine("  Notas: ${g.notes}")
            }
        }
        sb.appendLine()

        // 2. SALUD Y TRATAMIENTOS
        sb.appendLine("--- 2. SALUD Y TRATAMIENTOS MÉDICOS ---")
        sb.appendLine("[Tratamientos Farmacológicos]:")
        if (treatments.isEmpty()) {
            sb.appendLine("Sin medicamentos o tratamientos en este período.")
        } else {
            treatments.forEach { t ->
                sb.appendLine("• ${t.name} (${t.formula}) - Dosis: ${t.dosage} c/${t.frequencyHours}h | Estado: ${t.status}")
                if (!t.notes.isNullOrBlank()) sb.appendLine("  Indicaciones: ${t.notes}")
            }
        }

        sb.appendLine()
        sb.appendLine("[Registro de Fiebre y Temperatura]:")
        if (temperatures.isEmpty()) {
            sb.appendLine("Sin episodios de temperatura registrados.")
        } else {
            temperatures.forEach { temp ->
                val med = if (!temp.medicationGiven.isNullOrBlank()) " | Antipirético: ${temp.medicationGiven}" else ""
                sb.appendLine("• [${temp.date} ${temp.time} hs] ${temp.celsius}°C - Síntomas: ${temp.symptoms}$med")
            }
        }

        sb.appendLine()
        sb.appendLine("[Consultas y Turnos Médicos]:")
        if (appointments.isEmpty()) {
            sb.appendLine("Sin consultas médicas en el período.")
        } else {
            appointments.forEach { appt ->
                sb.appendLine("• [${appt.date} ${appt.time} hs] ${appt.doctorName} (${appt.specialty}) en ${appt.clinic} | Motivo: ${appt.reason}")
            }
        }
        sb.appendLine()

        // 3. RUTINAS
        sb.appendLine("--- 3. RUTINAS DIARIAS ---")
        sb.appendLine("[Sueño y Siestas]:")
        if (sleepRecords.isEmpty()) {
            sb.appendLine("Sin registros de descanso.")
        } else {
            sleepRecords.forEach { s ->
                sb.appendLine("• [${s.date}] ${s.type.uppercase()}: ${s.startTime} - ${s.endTime} (${s.durationMinutes} min) - Calidad: ${s.quality}")
            }
        }

        sb.appendLine()
        sb.appendLine("[Alimentación y Comidas]:")
        if (mealRecords.isEmpty()) {
            sb.appendLine("Sin registros de alimentación.")
        } else {
            mealRecords.forEach { m ->
                sb.appendLine("• [${m.date} ${m.time}] ${m.mealType.uppercase()}: ${m.foods} - Apetito: ${m.appetite}")
            }
        }

        sb.appendLine()
        sb.appendLine("[Actividades y Desarrollo]:")
        if (activityRecords.isEmpty()) {
            sb.appendLine("Sin actividades registradas.")
        } else {
            activityRecords.forEach { a ->
                sb.appendLine("• [${a.date} ${a.time}] ${a.title} (${a.category}) - ${a.durationMinutes} min")
            }
        }

        sb.appendLine()
        sb.appendLine("==================================================")
        sb.appendLine("Generado automáticamente por PequeCare")
        sb.appendLine("==================================================")

        return sb.toString()
    }

    /**
     * Genera un archivo .txt en el almacenamiento de la app y devuelve su Uri.
     */
    fun saveAsTxtFile(context: Context, reportContent: String): Uri {
        val file = File(context.cacheDir, "Reporte_Pediatrico_PequeCare.txt")
        file.writeText(reportContent)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Genera un archivo PDF con diseño personalizado y tipografía ARIAL.
     * Incluye encabezado ilustrado, ficha del niño, secciones destacadas y paginación.
     */
    fun generateStyledPdf(
        context: Context,
        profile: ChildProfileEntity?,
        startDate: String,
        endDate: String,
        growthRecords: List<GrowthRecordEntity>,
        sleepRecords: List<SleepEntity>,
        mealRecords: List<MealEntity>,
        activityRecords: List<ActivityEntity>,
        appointments: List<MedicalAppointmentEntity>,
        temperatures: List<TemperatureRecordEntity>,
        treatments: List<MedicationTreatmentEntity>
    ): Uri {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val arialRegular = Typeface.create("Arial", Typeface.NORMAL)
        val arialBold = Typeface.create("Arial", Typeface.BOLD)

        val headerBannerPaint = Paint().apply {
            color = Color.rgb(225, 29, 72) // RosePrimary
            isAntiAlias = true
        }

        val headerTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 17f
            typeface = arialBold
            isAntiAlias = true
        }

        val headerSubPaint = Paint().apply {
            color = Color.rgb(255, 228, 230) // Light rose
            textSize = 9.5f
            typeface = arialRegular
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252) // slate-50
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBorderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // slate-200
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val patientNamePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // slate-900
            textSize = 12.5f
            typeface = arialBold
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // slate-500
            textSize = 8.5f
            typeface = arialBold
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // slate-800
            textSize = 9f
            typeface = arialRegular
            isAntiAlias = true
        }

        val sectionBarPaint = Paint().apply {
            color = Color.rgb(255, 241, 242) // Rose-50
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(190, 18, 60) // Rose-700
            textSize = 11f
            typeface = arialBold
            isAntiAlias = true
        }

        val itemCardBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val itemTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = arialBold
            isAntiAlias = true
        }

        val itemDetailPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 8.5f
            typeface = arialRegular
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8f
            typeface = arialRegular
            isAntiAlias = true
        }

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        fun drawFooter() {
            canvas.drawLine(36f, pageHeight - 38f, 559f, pageHeight - 38f, dividerPaint)
            canvas.drawText("🌸 PequeCare • Informe Pediátrico Oficial • Página $pageNum", 36f, pageHeight - 24f, footerPaint)
            val dateLabel = "Fecha: " + LocalDate.now()
            val dateW = footerPaint.measureText(dateLabel)
            canvas.drawText(dateLabel, 559f - dateW, pageHeight - 24f, footerPaint)
        }

        fun drawPageHeader() {
            canvas.drawRect(0f, 0f, 595f, 65f, headerBannerPaint)
            canvas.drawText("🌸 PequeCare - Reporte Pediátrico Integral", 36f, 32f, headerTitlePaint)
            canvas.drawText("Período: $startDate al $endDate • Documento para el control pediátrico", 36f, 48f, headerSubPaint)
        }

        fun drawSubsequentHeader() {
            canvas.drawRect(0f, 0f, 595f, 32f, headerBannerPaint)
            val continuedTitle = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = arialBold
                isAntiAlias = true
            }
            val name = "${profile?.name ?: ""} ${profile?.lastName ?: ""}".trim()
            canvas.drawText("🌸 PequeCare • Reporte Pediátrico - $name (Continuación)", 36f, 20f, continuedTitle)
        }

        drawPageHeader()

        // Dibujar Ficha del Paciente (en Página 1)
        var currentY = 78f
        val patientBoxHeight = 58f
        canvas.drawRoundRect(36f, currentY, 559f, currentY + patientBoxHeight, 10f, 10f, cardBgPaint)
        canvas.drawRoundRect(36f, currentY, 559f, currentY + patientBoxHeight, 10f, 10f, cardBorderPaint)

        val childName = "${profile?.name ?: "Sin nombre"} ${profile?.lastName ?: ""}".trim()
        val genderIcon = if (profile?.gender == "male") "Niño 👦" else "Niña 👧"
        canvas.drawText("Paciente: $childName ($genderIcon)", 48f, currentY + 18f, patientNamePaint)

        val pFila1 = "F. Nac: ${profile?.birthDate ?: "N/D"}    |    Grupo Sang: ${profile?.bloodType?.ifBlank { "N/D" } ?: "N/D"}    |    Alergias: ${profile?.allergies?.ifBlank { "Ninguna declarada" } ?: "Ninguna"}"
        canvas.drawText(pFila1, 48f, currentY + 34f, valuePaint)

        val pFila2 = "Pediatra: ${profile?.pediatricianName?.ifBlank { "N/D" } ?: "N/D"}    |    Clínica / Centro: ${profile?.clinicName?.ifBlank { "N/D" } ?: "N/D"}"
        canvas.drawText(pFila2, 48f, currentY + 48f, valuePaint)

        currentY += patientBoxHeight + 16f

        fun ensureSpace(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - 50f) {
                drawFooter()
                pdfDocument.finishPage(currentPage)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                drawSubsequentHeader()
                currentY = 46f
            }
        }

        fun drawSectionHeader(title: String) {
            ensureSpace(34f)
            canvas.drawRoundRect(36f, currentY, 559f, currentY + 22f, 6f, 6f, sectionBarPaint)
            canvas.drawText(title, 46f, currentY + 15f, sectionTitlePaint)
            currentY += 28f
        }

        // --- 1. CRECIMIENTO OMS ---
        drawSectionHeader("📏 1. MÉTRICAS DE CRECIMIENTO Y CURVAS OMS")
        if (growthRecords.isEmpty()) {
            ensureSpace(20f)
            canvas.drawText("Sin mediciones de crecimiento en el período.", 46f, currentY + 10f, itemDetailPaint)
            currentY += 18f
        } else {
            growthRecords.forEach { g ->
                val cardH = if (!g.notes.isNullOrBlank()) 44f else 32f
                ensureSpace(cardH + 6f)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + cardH, 8f, 8f, itemCardBgPaint)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + cardH, 8f, 8f, cardBorderPaint)

                val weightStr = String.format(Locale.US, "%.3f", g.weightKg).trimEnd('0').trimEnd('.')
                canvas.drawText("📅 ${g.date} (${g.ageMonths} meses)", 46f, currentY + 14f, itemTitlePaint)

                val metrics = "Peso: $weightStr kg (P${g.weightPercentile})    •    Talla: ${g.heightCm} cm (P${g.heightPercentile})    •    IMC: ${g.bmi} kg/m² (P${g.bmiPercentile} • ${g.bmiStatus})"
                canvas.drawText(metrics, 46f, currentY + 26f, itemDetailPaint)

                if (!g.notes.isNullOrBlank()) {
                    canvas.drawText("Observación: ${g.notes}", 46f, currentY + 38f, labelPaint)
                }
                currentY += cardH + 6f
            }
        }
        currentY += 8f

        // --- 2. SALUD Y TRATAMIENTOS ---
        drawSectionHeader("💊 2. SALUD, TRATAMIENTOS Y FIEBRE")

        // Medicamentos
        if (treatments.isNotEmpty()) {
            treatments.forEach { t ->
                ensureSpace(36f)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 30f, 8f, 8f, itemCardBgPaint)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 30f, 8f, 8f, cardBorderPaint)
                canvas.drawText("💊 ${t.name} (${t.formula}) - Dosis: ${t.dosage} c/${t.frequencyHours}h", 46f, currentY + 13f, itemTitlePaint)
                canvas.drawText("Duración: ${t.durationDays} días (${t.startDate} al ${t.endDate}) • Estado: ${t.status}", 46f, currentY + 24f, itemDetailPaint)
                currentY += 36f
            }
        }

        // Fiebre
        if (temperatures.isNotEmpty()) {
            temperatures.forEach { temp ->
                ensureSpace(34f)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 28f, 8f, 8f, itemCardBgPaint)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 28f, 8f, 8f, cardBorderPaint)
                canvas.drawText("🌡️ ${temp.celsius}°C - [${temp.date} ${temp.time} hs]", 46f, currentY + 12f, itemTitlePaint)
                val med = if (!temp.medicationGiven.isNullOrBlank()) " • Antipirético: ${temp.medicationGiven}" else ""
                canvas.drawText("Síntomas: ${temp.symptoms}$med", 46f, currentY + 22f, itemDetailPaint)
                currentY += 34f
            }
        }

        // Turnos
        if (appointments.isNotEmpty()) {
            appointments.forEach { appt ->
                ensureSpace(34f)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 28f, 8f, 8f, itemCardBgPaint)
                canvas.drawRoundRect(36f, currentY, 559f, currentY + 28f, 8f, 8f, cardBorderPaint)
                canvas.drawText("🩺 ${appt.doctorName} (${appt.specialty}) - [${appt.date} ${appt.time} hs]", 46f, currentY + 12f, itemTitlePaint)
                canvas.drawText("Lugar: ${appt.clinic} • Motivo: ${appt.reason}", 46f, currentY + 22f, itemDetailPaint)
                currentY += 34f
            }
        }

        if (treatments.isEmpty() && temperatures.isEmpty() && appointments.isEmpty()) {
            ensureSpace(20f)
            canvas.drawText("Sin registros de salud o tratamientos en el período.", 46f, currentY + 10f, itemDetailPaint)
            currentY += 18f
        }
        currentY += 8f

        // --- 3. RUTINAS DIARIAS ---
        drawSectionHeader("🌟 3. RUTINAS DIARIAS Y DESARROLLO")

        // Sueño
        sleepRecords.forEach { s ->
            ensureSpace(26f)
            canvas.drawText("• 😴 [${s.date}] ${s.type.uppercase()}: ${s.startTime} - ${s.endTime} (${s.durationMinutes} min) - Calidad: ${s.quality}", 46f, currentY + 10f, itemDetailPaint)
            currentY += 16f
        }

        // Comidas (sin hidratación)
        mealRecords.forEach { m ->
            ensureSpace(26f)
            canvas.drawText("• 🍲 [${m.date} ${m.time}] ${m.mealType.uppercase()}: ${m.foods} - Apetito: ${m.appetite}", 46f, currentY + 10f, itemDetailPaint)
            currentY += 16f
        }

        // Actividades
        activityRecords.forEach { a ->
            ensureSpace(26f)
            canvas.drawText("• 🎨 [${a.date} ${a.time}] ${a.title} (${a.category}) - ${a.durationMinutes} min", 46f, currentY + 10f, itemDetailPaint)
            currentY += 16f
        }

        if (sleepRecords.isEmpty() && mealRecords.isEmpty() && activityRecords.isEmpty()) {
            ensureSpace(20f)
            canvas.drawText("Sin registros de rutinas en este período.", 46f, currentY + 10f, itemDetailPaint)
            currentY += 18f
        }

        // Footer en la última página
        drawFooter()
        pdfDocument.finishPage(currentPage)

        val pdfFile = File(context.cacheDir, "Reporte_Pediatrico_PequeCare.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
    }

    /**
     * Versión genérica basada en texto plano usando fuente ARIAL.
     */
    fun generatePdfFile(context: Context, reportContent: String): Uri {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val lines = reportContent.split("\n")

        val arialRegular = Typeface.create("Arial", Typeface.NORMAL)
        val arialBold = Typeface.create("Arial", Typeface.BOLD)

        val paint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            typeface = arialRegular
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(225, 29, 72)
            textSize = 12f
            typeface = arialBold
            isAntiAlias = true
        }

        var pageNum = 1
        var lineIndex = 0

        while (lineIndex < lines.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 45f

            canvas.drawText("PequeCare - Reporte Pediátrico Oficial (Página $pageNum)", 40f, y, headerPaint)
            y += 22f

            while (lineIndex < lines.size) {
                val line = lines[lineIndex]
                if (y > pageHeight - 50) {
                    break
                }
                canvas.drawText(line, 40f, y, paint)
                y += 14f
                lineIndex++
            }

            pdfDocument.finishPage(page)
            pageNum++
        }

        val pdfFile = File(context.cacheDir, "Reporte_Pediatrico_PequeCare.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
    }

    /**
     * Lanza un Intent nativo para compartir el reporte de texto en WhatsApp.
     */
    fun shareViaWhatsApp(context: Context, reportText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, reportText)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareGeneralText(context, reportText)
        }
    }

    /**
     * Lanza un Intent nativo para compartir el PDF directamente por WhatsApp.
     */
    fun sharePdfViaWhatsApp(context: Context, pdfUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareGeneralFile(context, pdfUri, "application/pdf", "Compartir Reporte PDF")
        }
    }

    /**
     * Lanza un Intent nativo para enviar el reporte por Correo Electrónico.
     */
    fun shareViaEmail(context: Context, subject: String, reportText: String, attachmentUri: Uri? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (attachmentUri != null) "application/pdf" else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, reportText)
            if (attachmentUri != null) {
                putExtra(Intent.EXTRA_STREAM, attachmentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(intent, "Enviar reporte por Email"))
    }

    /**
     * Selector general nativo de Android para compartir texto plano.
     */
    fun shareGeneralText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    }

    /**
     * Selector general nativo de Android para compartir un archivo (PDF / TXT).
     */
    fun shareGeneralFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
