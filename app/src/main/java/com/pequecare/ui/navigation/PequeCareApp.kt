package com.pequecare.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.ui.PequeCareViewModel
import com.pequecare.ui.dashboard.DashboardScreen
import com.pequecare.ui.dialogs.AddActivityDialog
import com.pequecare.ui.dialogs.AddAppointmentDialog
import com.pequecare.ui.dialogs.AddGrowthDialog
import com.pequecare.ui.dialogs.AddMealDialog
import com.pequecare.ui.dialogs.AddMedicationDialog
import com.pequecare.ui.dialogs.AddSleepDialog
import com.pequecare.ui.dialogs.AddTemperatureDialog
import com.pequecare.ui.dialogs.RegisterChildDialog
import com.pequecare.ui.growth.GrowthScreen
import com.pequecare.ui.health.HealthScreen
import com.pequecare.ui.report.ReportExportComponent
import com.pequecare.ui.routine.RoutineScreen
import com.pequecare.ui.settings.SettingsScreen
import com.pequecare.ui.theme.PequeCareTheme
import com.pequecare.util.CalendarHelper
import com.pequecare.util.NotificationHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

enum class NavigationTab(val title: String, val icon: ImageVector) {
    RESUMEN("Resumen", Icons.Default.Home),
    CRECIMIENTO("Crecimiento", Icons.Default.Straighten),
    SALUD("Salud", Icons.Default.MedicalServices),
    RUTINA("Rutina", Icons.Default.DateRange),
    INFORMES("Informes", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PequeCareApp(
    viewModel: PequeCareViewModel
) {
    val context = LocalContext.current
    val profile by viewModel.childProfile.collectAsState()
    val growthRecords by viewModel.growthRecords.collectAsState()
    val latestGrowth by viewModel.latestGrowth.collectAsState()
    val activeMedications by viewModel.activeMedications.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val temperatures by viewModel.temperatures.collectAsState()
    val sleepRecords by viewModel.sleepRecords.collectAsState()
    val mealRecords by viewModel.mealRecords.collectAsState()
    val activityRecords by viewModel.activityRecords.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var selectedTab by remember { mutableStateOf(NavigationTab.RESUMEN) }
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var editingGrowthRecord by remember { mutableStateOf<GrowthRecordEntity?>(null) }
    var editingMedication by remember { mutableStateOf<MedicationTreatmentEntity?>(null) }
    var editingAppointment by remember { mutableStateOf<MedicalAppointmentEntity?>(null) }
    var editingTemperature by remember { mutableStateOf<TemperatureRecordEntity?>(null) }
    var editingSleep by remember { mutableStateOf<SleepEntity?>(null) }
    var editingMeal by remember { mutableStateOf<MealEntity?>(null) }
    var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    fun checkProfileAndOpen(dialogType: String) {
        if (profile == null) {
            Toast.makeText(context, "Primero debes registrar al niño/a", Toast.LENGTH_SHORT).show()
            activeDialog = "child_profile"
        } else {
            activeDialog = dialogType
        }
    }

    PequeCareTheme(darkTheme = isDarkTheme) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🌸 PequeCare",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (profile != null) {
                                Text(
                                    text = "${profile?.name ?: ""} (${profile?.birthDate ?: ""})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Menú de Ajustes
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes y Modo Oscuro",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            label = { Text(text = tab.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    NavigationTab.RESUMEN -> DashboardScreen(
                        profile = profile,
                        latestGrowth = latestGrowth,
                        activeMedications = activeMedications,
                        onAddRecordClick = { type -> checkProfileAndOpen(type) },
                        onEditProfile = { activeDialog = "child_profile" }
                    )
                    NavigationTab.CRECIMIENTO -> GrowthScreen(
                        growthRecords = growthRecords,
                        childGender = profile?.gender ?: "female",
                        onAddRecordClick = {
                            editingGrowthRecord = null
                            checkProfileAndOpen("growth")
                        },
                        onEditRecordClick = { record ->
                            editingGrowthRecord = record
                            activeDialog = "growth"
                        },
                        onDuplicateRecordClick = { record ->
                            viewModel.duplicateGrowthRecord(record)
                            Toast.makeText(context, "Control duplicado para hoy", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteRecordClick = { record ->
                            viewModel.deleteGrowthRecord(record)
                            Toast.makeText(context, "Control eliminado", Toast.LENGTH_SHORT).show()
                        }
                    )
                    NavigationTab.SALUD -> HealthScreen(
                        treatments = activeMedications,
                        appointments = appointments,
                        temperatures = temperatures,
                        onAddMedicationClick = {
                            editingMedication = null
                            checkProfileAndOpen("medication")
                        },
                        onEditMedicationClick = { med ->
                            editingMedication = med
                            activeDialog = "medication"
                        },
                        onDuplicateMedicationClick = { med ->
                            viewModel.duplicateMedication(med)
                            Toast.makeText(context, "Tratamiento duplicado", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteMedicationClick = { med ->
                            viewModel.deleteMedication(med)
                            Toast.makeText(context, "Tratamiento eliminado", Toast.LENGTH_SHORT).show()
                        },
                        onAddAppointmentClick = {
                            editingAppointment = null
                            checkProfileAndOpen("appointment")
                        },
                        onEditAppointmentClick = { appt ->
                            editingAppointment = appt
                            activeDialog = "appointment"
                        },
                        onDuplicateAppointmentClick = { appt ->
                            viewModel.duplicateAppointment(appt)
                            Toast.makeText(context, "Turno duplicado", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteAppointmentClick = { appt ->
                            viewModel.deleteAppointment(appt)
                            Toast.makeText(context, "Turno eliminado", Toast.LENGTH_SHORT).show()
                        },
                        onAddTemperatureClick = {
                            editingTemperature = null
                            checkProfileAndOpen("temperature")
                        },
                        onEditTemperatureClick = { temp ->
                            editingTemperature = temp
                            activeDialog = "temperature"
                        },
                        onDuplicateTemperatureClick = { temp ->
                            viewModel.duplicateTemperature(temp)
                            Toast.makeText(context, "Temperatura duplicada", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteTemperatureClick = { temp ->
                            viewModel.deleteTemperature(temp)
                            Toast.makeText(context, "Temperatura eliminada", Toast.LENGTH_SHORT).show()
                        }
                    )
                    NavigationTab.RUTINA -> RoutineScreen(
                        sleepRecords = sleepRecords,
                        mealRecords = mealRecords,
                        activityRecords = activityRecords,
                        onAddSleepClick = {
                            editingSleep = null
                            checkProfileAndOpen("sleep")
                        },
                        onEditSleepClick = { sleep ->
                            editingSleep = sleep
                            activeDialog = "sleep"
                        },
                        onDuplicateSleepClick = { sleep ->
                            viewModel.duplicateSleep(sleep)
                            Toast.makeText(context, "Registro de sueño duplicado", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteSleepClick = { sleep ->
                            viewModel.deleteSleep(sleep)
                            Toast.makeText(context, "Registro de sueño eliminado", Toast.LENGTH_SHORT).show()
                        },
                        onAddMealClick = {
                            editingMeal = null
                            checkProfileAndOpen("meal")
                        },
                        onEditMealClick = { meal ->
                            editingMeal = meal
                            activeDialog = "meal"
                        },
                        onDuplicateMealClick = { meal ->
                            viewModel.duplicateMeal(meal)
                            Toast.makeText(context, "Comida duplicada", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteMealClick = { meal ->
                            viewModel.deleteMeal(meal)
                            Toast.makeText(context, "Comida eliminada", Toast.LENGTH_SHORT).show()
                        },
                        onAddActivityClick = {
                            editingActivity = null
                            checkProfileAndOpen("activity")
                        },
                        onEditActivityClick = { act ->
                            editingActivity = act
                            activeDialog = "activity"
                        },
                        onDuplicateActivityClick = { act ->
                            viewModel.duplicateActivity(act)
                            Toast.makeText(context, "Actividad duplicada", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteActivityClick = { act ->
                            viewModel.deleteActivity(act)
                            Toast.makeText(context, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                        }
                    )
                    NavigationTab.INFORMES -> ReportExportComponent(
                        profile = profile,
                        allGrowthRecords = growthRecords,
                        allSleepRecords = sleepRecords,
                        allMealRecords = mealRecords,
                        allActivityRecords = activityRecords,
                        allAppointments = appointments,
                        allTemperatures = temperatures,
                        allTreatments = activeMedications
                    )
                }
            }
        }

        // BottomSheet Modal de Ajustes
        if (showSettingsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState
            ) {
                SettingsScreen(
                    profile = profile,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                    onEditProfileClick = {
                        showSettingsSheet = false
                        activeDialog = "child_profile"
                    },
                    onOpenReportsClick = {
                        showSettingsSheet = false
                        selectedTab = NavigationTab.INFORMES
                    }
                )
            }
        }

        // Diálogos modales interactivos
        when (activeDialog) {
            "child_profile" -> {
                RegisterChildDialog(
                    initialProfile = profile,
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { name, lastName, birthDate, gender, bloodType, allergies, pediatrician, phone, clinic ->
                        viewModel.saveChildProfile(
                            name = name,
                            lastName = lastName,
                            birthDate = birthDate,
                            gender = gender,
                            bloodType = bloodType,
                            allergies = allergies,
                            pediatricianName = pediatrician,
                            pediatricianPhone = phone,
                            clinicName = clinic
                        )
                        activeDialog = null
                        Toast.makeText(context, "Perfil de $name guardado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "growth" -> {
                AddGrowthDialog(
                    initialRecord = editingGrowthRecord,
                    onDismissRequest = {
                        editingGrowthRecord = null
                        activeDialog = null
                    },
                    onConfirm = { weightKg, heightCm, date, notes ->
                        if (editingGrowthRecord != null) {
                            viewModel.updateGrowthRecord(
                                record = editingGrowthRecord!!,
                                weightKg = weightKg,
                                heightCm = heightCm,
                                date = date,
                                notes = notes
                            )
                            Toast.makeText(context, "Control de crecimiento actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addGrowthRecord(
                                weightKg = weightKg,
                                heightCm = heightCm,
                                date = date,
                                notes = notes
                            )
                            Toast.makeText(context, "Control OMS registrado", Toast.LENGTH_SHORT).show()
                        }
                        editingGrowthRecord = null
                        activeDialog = null
                    }
                )
            }
            "medication" -> {
                AddMedicationDialog(
                    initialRecord = editingMedication,
                    onDismissRequest = {
                        editingMedication = null
                        activeDialog = null
                    },
                    onConfirm = { name, formula, dosage, startDate, startTime, freqH, freqM, dur, status, notifyMed, notes ->
                        if (editingMedication != null) {
                            viewModel.updateMedication(
                                med = editingMedication!!,
                                name = name,
                                formula = formula,
                                dosage = dosage,
                                startDate = startDate,
                                startTime = startTime,
                                frequencyHours = freqH,
                                frequencyMinutes = freqM,
                                durationDays = dur,
                                status = status,
                                notes = notes
                            )
                            if (status == "ACTIVO" && notifyMed) {
                                val d = try { LocalDate.parse(startDate) } catch (_: Exception) { LocalDate.now() }
                                val t = try { LocalTime.parse(startTime) } catch (_: Exception) { LocalTime.of(8, 0) }
                                val startMillis = LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val intervalMillis = ((freqH * 60L) + freqM) * 60L * 1000L
                                NotificationHelper.scheduleMedicationNotification(
                                    context = context,
                                    medicationId = editingMedication!!.id,
                                    medName = name,
                                    dosage = dosage,
                                    startEpochMillis = startMillis,
                                    intervalMillis = intervalMillis
                                )
                            } else {
                                NotificationHelper.cancelReminder(context, editingMedication!!.id)
                            }
                            Toast.makeText(context, "Tratamiento actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            val newId = viewModel.addMedication(
                                name = name,
                                formula = formula,
                                dosage = dosage,
                                startDate = startDate,
                                startTime = startTime,
                                frequencyHours = freqH,
                                frequencyMinutes = freqM,
                                durationDays = dur,
                                status = status,
                                notes = notes
                            )
                            if (status == "ACTIVO" && notifyMed) {
                                val d = try { LocalDate.parse(startDate) } catch (_: Exception) { LocalDate.now() }
                                val t = try { LocalTime.parse(startTime) } catch (_: Exception) { LocalTime.of(8, 0) }
                                val startMillis = LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val intervalMillis = ((freqH * 60L) + freqM) * 60L * 1000L
                                NotificationHelper.scheduleMedicationNotification(
                                    context = context,
                                    medicationId = newId,
                                    medName = name,
                                    dosage = dosage,
                                    startEpochMillis = startMillis,
                                    intervalMillis = intervalMillis
                                )
                            }
                            Toast.makeText(context, "Tratamiento registrado con recordatorio", Toast.LENGTH_SHORT).show()
                        }
                        editingMedication = null
                        activeDialog = null
                    }
                )
            }
            "temperature" -> {
                AddTemperatureDialog(
                    initialRecord = editingTemperature,
                    onDismissRequest = {
                        editingTemperature = null
                        activeDialog = null
                    },
                    onConfirm = { celsius, symptoms, med ->
                        if (editingTemperature != null) {
                            viewModel.updateTemperature(
                                record = editingTemperature!!,
                                celsius = celsius,
                                symptoms = symptoms,
                                medicationGiven = med
                            )
                            Toast.makeText(context, "Temperatura actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addTemperature(celsius, symptoms, med)
                            Toast.makeText(context, "Temperatura registrada", Toast.LENGTH_SHORT).show()
                        }
                        editingTemperature = null
                        activeDialog = null
                    }
                )
            }
            "appointment" -> {
                AddAppointmentDialog(
                    initialRecord = editingAppointment,
                    onDismissRequest = {
                        editingAppointment = null
                        activeDialog = null
                    },
                    onConfirm = { doc, spec, clinic, reason, date, time, status, addToCalendar, notify24h ->
                        if (editingAppointment != null) {
                            viewModel.updateAppointment(
                                appointment = editingAppointment!!,
                                doctorName = doc,
                                specialty = spec,
                                clinic = clinic,
                                reason = reason,
                                date = date,
                                time = time,
                                status = status
                            )
                            if (status == "pendiente") {
                                if (addToCalendar) {
                                    CalendarHelper.addEventToCalendar(
                                        context = context,
                                        title = "Turno Médico PequeCare: $doc ($spec)",
                                        description = "Consulta en $clinic. Motivo: $reason",
                                        location = clinic,
                                        dateStr = date,
                                        timeStr = time
                                    )
                                }
                                if (notify24h) {
                                    NotificationHelper.scheduleAppointment24hNotification(
                                        context = context,
                                        appointmentId = editingAppointment!!.id,
                                        doctorName = doc,
                                        specialty = spec,
                                        clinic = clinic,
                                        dateStr = date,
                                        timeStr = time
                                    )
                                }
                            }
                            Toast.makeText(context, "Turno médico actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            val newId = viewModel.addAppointment(
                                doctorName = doc,
                                specialty = spec,
                                clinic = clinic,
                                reason = reason,
                                date = date,
                                time = time,
                                status = status
                            )
                            if (status == "pendiente") {
                                if (addToCalendar) {
                                    CalendarHelper.addEventToCalendar(
                                        context = context,
                                        title = "Turno Médico PequeCare: $doc ($spec)",
                                        description = "Consulta en $clinic. Motivo: $reason",
                                        location = clinic,
                                        dateStr = date,
                                        timeStr = time
                                    )
                                }
                                if (notify24h) {
                                    NotificationHelper.scheduleAppointment24hNotification(
                                        context = context,
                                        appointmentId = newId,
                                        doctorName = doc,
                                        specialty = spec,
                                        clinic = clinic,
                                        dateStr = date,
                                        timeStr = time
                                    )
                                }
                            }
                            Toast.makeText(context, "Turno médico agendado", Toast.LENGTH_SHORT).show()
                        }
                        editingAppointment = null
                        activeDialog = null
                    }
                )
            }
            "sleep" -> {
                AddSleepDialog(
                    initialRecord = editingSleep,
                    onDismissRequest = {
                        editingSleep = null
                        activeDialog = null
                    },
                    onConfirm = { start, end, type, quality, date, notes ->
                        if (editingSleep != null) {
                            viewModel.updateSleep(
                                record = editingSleep!!,
                                startTime = start,
                                endTime = end,
                                type = type,
                                quality = quality,
                                date = date,
                                notes = notes
                            )
                            Toast.makeText(context, "Registro de sueño actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addSleep(
                                startTime = start,
                                endTime = end,
                                type = type,
                                quality = quality,
                                date = date,
                                notes = notes
                            )
                            Toast.makeText(context, "Registro de sueño guardado", Toast.LENGTH_SHORT).show()
                        }
                        editingSleep = null
                        activeDialog = null
                    }
                )
            }
            "meal" -> {
                AddMealDialog(
                    initialRecord = editingMeal,
                    onDismissRequest = {
                        editingMeal = null
                        activeDialog = null
                    },
                    onConfirm = { mealType, foods, appetite ->
                        if (editingMeal != null) {
                            viewModel.updateMeal(
                                record = editingMeal!!,
                                mealType = mealType,
                                foods = foods,
                                appetite = appetite
                            )
                            Toast.makeText(context, "Comida actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMeal(mealType, foods, appetite)
                            Toast.makeText(context, "Comida registrada", Toast.LENGTH_SHORT).show()
                        }
                        editingMeal = null
                        activeDialog = null
                    }
                )
            }
            "activity" -> {
                AddActivityDialog(
                    initialRecord = editingActivity,
                    onDismissRequest = {
                        editingActivity = null
                        activeDialog = null
                    },
                    onConfirm = { title, cat, dur ->
                        if (editingActivity != null) {
                            viewModel.updateActivity(
                                record = editingActivity!!,
                                title = title,
                                category = cat,
                                durationMinutes = dur
                            )
                            Toast.makeText(context, "Actividad actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addActivity(title, cat, dur)
                            Toast.makeText(context, "Actividad registrada", Toast.LENGTH_SHORT).show()
                        }
                        editingActivity = null
                        activeDialog = null
                    }
                )
            }
        }
    }
}
