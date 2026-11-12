package com.pequecare.ui.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.ui.theme.EmeraldSuccess
import com.pequecare.ui.theme.RosePrimary
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportExportComponent(
    profile: ChildProfileEntity?,
    allGrowthRecords: List<GrowthRecordEntity>,
    allSleepRecords: List<SleepEntity>,
    allMealRecords: List<MealEntity>,
    allActivityRecords: List<ActivityEntity>,
    allAppointments: List<MedicalAppointmentEntity>,
    allTemperatures: List<TemperatureRecordEntity>,
    allTreatments: List<MedicationTreatmentEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateRangePickerState = rememberDateRangePickerState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var selectedPreset by remember { mutableStateOf("30dias") }
    var showCalendarPicker by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    var manualStartDate by remember { mutableStateOf(today.minusMonths(1).toString()) }
    var manualEndDate by remember { mutableStateOf(today.toString()) }

    val startDateStr by remember {
        derivedStateOf {
            if (showCalendarPicker && dateRangePickerState.selectedStartDateMillis != null) {
                dateFormat.format(Date(dateRangePickerState.selectedStartDateMillis!!))
            } else {
                manualStartDate
            }
        }
    }

    val endDateStr by remember {
        derivedStateOf {
            if (showCalendarPicker && dateRangePickerState.selectedEndDateMillis != null) {
                dateFormat.format(Date(dateRangePickerState.selectedEndDateMillis!!))
            } else {
                manualEndDate
            }
        }
    }

    val filteredGrowth = remember(startDateStr, endDateStr, allGrowthRecords) {
        allGrowthRecords.filter { it.date >= startDateStr && it.date <= endDateStr }
    }
    val filteredSleep = remember(startDateStr, endDateStr, allSleepRecords) {
        allSleepRecords.filter { it.date >= startDateStr && it.date <= endDateStr }
    }
    val filteredMeals = remember(startDateStr, endDateStr, allMealRecords) {
        allMealRecords.filter { it.date >= startDateStr && it.date <= endDateStr }
    }
    val filteredActivities = remember(startDateStr, endDateStr, allActivityRecords) {
        allActivityRecords.filter { it.date >= startDateStr && it.date <= endDateStr }
    }
    val filteredAppointments = remember(startDateStr, endDateStr, allAppointments) {
        allAppointments.filter { it.date >= startDateStr && it.date <= endDateStr }
    }
    val filteredTemperatures = remember(startDateStr, endDateStr, allTemperatures) {
        allTemperatures.filter { it.date >= startDateStr && it.date <= endDateStr }
    }

    val reportContent by remember(
        profile, startDateStr, endDateStr,
        filteredGrowth, filteredSleep, filteredMeals,
        filteredActivities, filteredAppointments,
        filteredTemperatures, allTreatments
    ) {
        derivedStateOf {
            ReportExporter.buildReportText(
                profile = profile,
                startDate = startDateStr,
                endDate = endDateStr,
                growthRecords = filteredGrowth,
                sleepRecords = filteredSleep,
                mealRecords = filteredMeals,
                activityRecords = filteredActivities,
                appointments = filteredAppointments,
                temperatures = filteredTemperatures,
                treatments = allTreatments
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideScreen = this.maxWidth > 600.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera Maternal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Informes para el Pediatra",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Filtra por período y comparte por WhatsApp o PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Selector de Período y Calendario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📅 Período: $startDateStr al $endDateStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("7dias", "7 Días", today.minusDays(7)),
                            Triple("30dias", "1 Mes", today.minusMonths(1)),
                            Triple("3meses", "3 Meses", today.minusMonths(3)),
                            Triple("todo", "Todo", LocalDate.of(2020, 1, 1))
                        ).forEach { (key, label, start) ->
                            FilterChip(
                                selected = selectedPreset == key && !showCalendarPicker,
                                onClick = {
                                    selectedPreset = key
                                    showCalendarPicker = false
                                    manualStartDate = start.toString()
                                    manualEndDate = today.toString()
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showCalendarPicker = !showCalendarPicker },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showCalendarPicker) "Ocultar Calendario" else "Elegir Fechas en Calendario",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCalendarPicker) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        ) {
                            DateRangePicker(
                                state = dateRangePickerState,
                                modifier = Modifier.fillMaxSize(),
                                headline = null,
                                title = null
                            )
                        }
                    }
                }
            }

            // Opciones de Compartir e Imprimir (PDF y WhatsApp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📤 Exportar y Compartir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val generateCustomPdf = {
                        ReportExporter.generateStyledPdf(
                            context = context,
                            profile = profile,
                            startDate = startDateStr,
                            endDate = endDateStr,
                            growthRecords = filteredGrowth,
                            sleepRecords = filteredSleep,
                            mealRecords = filteredMeals,
                            activityRecords = filteredActivities,
                            appointments = filteredAppointments,
                            temperatures = filteredTemperatures,
                            treatments = allTreatments
                        )
                    }

                    Button(
                        onClick = {
                            val pdfUri = generateCustomPdf()
                            ReportExporter.sharePdfViaWhatsApp(context, pdfUri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Enviar PDF por WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            ReportExporter.shareViaWhatsApp(context, reportContent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.TextSnippet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Enviar Resumen Texto por WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val pdfUri = generateCustomPdf()
                            ReportExporter.shareViaEmail(
                                context = context,
                                subject = "Reporte Pediátrico - ${profile?.name ?: "PequeCare"}",
                                reportText = reportContent,
                                attachmentUri = pdfUri
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Enviar por Correo Electrónico (PDF)", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            val pdfUri = generateCustomPdf()
                            ReportExporter.shareGeneralFile(context, pdfUri, "application/pdf", "Compartir Reporte Pediátrico")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Más opciones de compartir...", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Vista Previa del Reporte (Estilo Ficha Médica Cálida)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📄 Vista Previa del Resumen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp)
                        ) {
                            Text(
                                text = reportContent,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
