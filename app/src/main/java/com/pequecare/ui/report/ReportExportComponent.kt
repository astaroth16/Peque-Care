package com.pequecare.ui.report

import android.content.Context
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.pequecare.ui.theme.IndigoPrimary
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

    // Rango de fechas seleccionado (calculado según preset o calendario)
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

    // Filtrar los datos por el rango de fechas
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

    // Generar el texto formateado
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
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(32.dp)
                )
                Column {
                    Text(
                        text = "Generar Informe Pediátrico",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Filtra por período y comparte en PDF o WhatsApp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Selector de Período y Calendario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📅 Período del Informe: $startDateStr  ➔  $endDateStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Presets rápidos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPreset == "7dias" && !showCalendarPicker,
                            onClick = {
                                selectedPreset = "7dias"
                                showCalendarPicker = false
                                manualStartDate = today.minusDays(7).toString()
                                manualEndDate = today.toString()
                            },
                            label = { Text("7 días") }
                        )
                        FilterChip(
                            selected = selectedPreset == "30dias" && !showCalendarPicker,
                            onClick = {
                                selectedPreset = "30dias"
                                showCalendarPicker = false
                                manualStartDate = today.minusMonths(1).toString()
                                manualEndDate = today.toString()
                            },
                            label = { Text("1 Mes") }
                        )
                        FilterChip(
                            selected = selectedPreset == "3meses" && !showCalendarPicker,
                            onClick = {
                                selectedPreset = "3meses"
                                showCalendarPicker = false
                                manualStartDate = today.minusMonths(3).toString()
                                manualEndDate = today.toString()
                            },
                            label = { Text("3 Meses") }
                        )
                        FilterChip(
                            selected = selectedPreset == "todo" && !showCalendarPicker,
                            onClick = {
                                selectedPreset = "todo"
                                showCalendarPicker = false
                                manualStartDate = "2020-01-01"
                                manualEndDate = today.toString()
                            },
                            label = { Text("Todo") }
                        )
                    }

                    // Botón para desplegar selector de calendario
                    OutlinedButton(
                        onClick = { showCalendarPicker = !showCalendarPicker },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (showCalendarPicker) "Ocultar Calendario" else "Elegir Rango en Calendario",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCalendarPicker) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🚀 Enviar y Exportar Informe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Botón WhatsApp PDF
                    Button(
                        onClick = {
                            val pdfUri = ReportExporter.generatePdfFile(context, reportContent)
                            ReportExporter.sharePdfViaWhatsApp(context, pdfUri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📲 Enviar PDF por WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Botón WhatsApp Texto
                    Button(
                        onClick = {
                            ReportExporter.shareViaWhatsApp(context, reportContent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess.copy(alpha = 0.85f))
                    ) {
                        Icon(imageVector = Icons.Default.TextSnippet, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "💬 Enviar Resumen Texto por WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Botón Email con PDF
                    Button(
                        onClick = {
                            val pdfUri = ReportExporter.generatePdfFile(context, reportContent)
                            ReportExporter.shareViaEmail(
                                context = context,
                                subject = "Reporte Pediátrico - ${profile?.name ?: "PequeCare"}",
                                reportText = reportContent,
                                attachmentUri = pdfUri
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "✉️ Enviar por Correo Electrónico (con PDF)", fontWeight = FontWeight.Bold)
                    }

                    // Botón Selector General
                    OutlinedButton(
                        onClick = {
                            val pdfUri = ReportExporter.generatePdfFile(context, reportContent)
                            ReportExporter.shareGeneralFile(context, pdfUri, "application/pdf", "Compartir Reporte Pediátrico")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📤 Más opciones de compartir...", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Vista Previa del Reporte
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📄 Vista Previa del Informe Estructurado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = reportContent,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
