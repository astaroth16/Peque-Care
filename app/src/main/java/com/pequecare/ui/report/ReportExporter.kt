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

object ReportExporter {

    /**
     * Agrupa y formatea todos los datos pediátricos en un texto plano estructurado.
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
        sb.appendLine("Grupo Sanguíneo: ${profile?.bloodType ?: "N/D"}")
        sb.appendLine("Alergias: ${profile?.allergies?.ifEmpty { "Ninguna declarada" } ?: "Ninguna"}")
        sb.appendLine("Pediatra: ${profile?.pediatricianName ?: "N/D"}")
        sb.appendLine("Período del Reporte: $startDate a $endDate")
        sb.appendLine("==================================================")
        sb.appendLine()

        // 1. CRECIMIENTO Y METRICAS OMS
        sb.appendLine("--- 1. MÉTRICAS DE CRECIMIENTO (OMS) ---")
        if (growthRecords.isEmpty()) {
            sb.appendLine("No hay registros de crecimiento en el rango seleccionado.")
        } else {
            growthRecords.forEach { g ->
                sb.appendLine("• [${g.date}] Peso: ${g.weightKg} kg (Percentil ${g.weightPercentile}) | Talla: ${g.heightCm} cm (P${g.heightPercentile}) | IMC: ${g.bmi} (P${g.bmiPercentile})")
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
        sb.appendLine("[Alimentación e Hidratación]:")
        if (mealRecords.isEmpty()) {
            sb.appendLine("Sin registros de alimentación.")
        } else {
            mealRecords.forEach { m ->
                val water = if (m.waterGlasses > 0) " (Agua: ${m.waterGlasses} vasitos)" else ""
                sb.appendLine("• [${m.date} ${m.time}] ${m.mealType.uppercase()}: ${m.foods}$water - Apetito: ${m.appetite}")
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
     * Genera un archivo PDF con soporte multi-página utilizando la API nativa PdfDocument de Android.
     */
    fun generatePdfFile(context: Context, reportContent: String): Uri {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val lines = reportContent.split("\n")

        val paint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(225, 29, 72) // RosePrimary
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var pageNum = 1
        var lineIndex = 0

        while (lineIndex < lines.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 45f

            // Encabezado de página
            canvas.drawText("PequeCare - Reporte Pediátrico Oficial (Página $pageNum)", 40f, y, headerPaint)
            y += 20f

            while (lineIndex < lines.size) {
                val line = lines[lineIndex]
                if (y > pageHeight - 50) {
                    break // Siguiente página
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
     * Selector general nativo de Android para compartir texto.
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
