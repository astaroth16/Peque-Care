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
import com.pequecare.data.local.entity.GrowthRecordEntity
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
    val todayWaterGlasses by viewModel.todayWaterGlasses.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var selectedTab by remember { mutableStateOf(NavigationTab.RESUMEN) }
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var editingGrowthRecord by remember { mutableStateOf<GrowthRecordEntity?>(null) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    fun checkProfileAndOpen(dialogType: String) {
        if (profile == null) {
            Toast.makeText(context, "Primero debes registrar al niño/a", Toast.LENGTH_SHORT).show()
            activeDialog = "child_profile"
        } else {
            activeDialog = dialogType
        }
    }

    fun checkProfileAndRun(action: () -> Unit) {
        if (profile == null) {
            Toast.makeText(context, "Primero debes registrar al niño/a", Toast.LENGTH_SHORT).show()
            activeDialog = "child_profile"
        } else {
            action()
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
                        todayWaterGlasses = todayWaterGlasses,
                        onIncrementWater = { checkProfileAndRun { viewModel.incrementWater() } },
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
                        onAddMedicationClick = { checkProfileAndOpen("medication") },
                        onAddAppointmentClick = { checkProfileAndOpen("appointment") },
                        onAddTemperatureClick = { checkProfileAndOpen("temperature") }
                    )
                    NavigationTab.RUTINA -> RoutineScreen(
                        sleepRecords = sleepRecords,
                        mealRecords = mealRecords,
                        activityRecords = activityRecords,
                        onAddSleepClick = { checkProfileAndOpen("sleep") },
                        onAddMealClick = { checkProfileAndOpen("meal") },
                        onAddActivityClick = { checkProfileAndOpen("activity") }
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
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { name, formula, dosage, freq, dur, notes ->
                        viewModel.addMedication(name, formula, dosage, freq, dur, notes)
                        activeDialog = null
                        Toast.makeText(context, "Tratamiento registrado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "temperature" -> {
                AddTemperatureDialog(
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { celsius, symptoms, med ->
                        viewModel.addTemperature(celsius, symptoms, med)
                        activeDialog = null
                        Toast.makeText(context, "Temperatura registrada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "appointment" -> {
                AddAppointmentDialog(
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { doc, spec, clinic, reason, date, time ->
                        viewModel.addAppointment(doc, spec, clinic, reason, date, time)
                        activeDialog = null
                        Toast.makeText(context, "Turno médico agendado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "sleep" -> {
                AddSleepDialog(
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { start, end, type, quality ->
                        viewModel.addSleep(start, end, type, quality)
                        activeDialog = null
                        Toast.makeText(context, "Registro de sueño guardado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "meal" -> {
                AddMealDialog(
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { mealType, foods, appetite ->
                        viewModel.addMeal(mealType, foods, appetite)
                        activeDialog = null
                        Toast.makeText(context, "Comida registrada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "activity" -> {
                AddActivityDialog(
                    onDismissRequest = { activeDialog = null },
                    onConfirm = { title, cat, dur ->
                        viewModel.addActivity(title, cat, dur)
                        activeDialog = null
                        Toast.makeText(context, "Actividad registrada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
