package com.pequecare.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.pequecare.ui.theme.RosePrimary
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.pequecare.ui.components.PequeDatePickerField
import com.pequecare.ui.components.PequeFrequencyPicker
import com.pequecare.ui.components.PequeTimePickerField
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RegisterChildDialog(
    initialProfile: ChildProfileEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (
        name: String,
        lastName: String,
        birthDate: String,
        gender: String,
        bloodType: String,
        allergies: String,
        pediatrician: String,
        phone: String,
        clinic: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var lastName by remember { mutableStateOf(initialProfile?.lastName ?: "") }
    var birthDate by remember { mutableStateOf(initialProfile?.birthDate ?: "2024-01-15") }
    var gender by remember { mutableStateOf(initialProfile?.gender ?: "female") }
    var bloodType by remember { mutableStateOf(initialProfile?.bloodType ?: "O+") }
    var allergies by remember { mutableStateOf(initialProfile?.allergies ?: "") }
    var pediatrician by remember { mutableStateOf(initialProfile?.pediatricianName ?: "") }
    var phone by remember { mutableStateOf(initialProfile?.pediatricianPhone ?: "") }
    var clinic by remember { mutableStateOf(initialProfile?.clinicName ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialProfile != null) "Editar Perfil del Peque" else "Registrar al Niño/a",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellido") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PequeDatePickerField(
                    label = "Fecha de Nacimiento *",
                    selectedDate = birthDate,
                    onDateSelected = { birthDate = it }
                )

                Text("Género:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == "female",
                        onClick = { gender = "female" },
                        label = { Text("Niña") }
                    )
                    FilterChip(
                        selected = gender == "male",
                        onClick = { gender = "male" },
                        label = { Text("Niño") }
                    )
                }

                OutlinedTextField(
                    value = bloodType,
                    onValueChange = { bloodType = it },
                    label = { Text("Grupo Sanguíneo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Alergias conocidas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pediatrician,
                    onValueChange = { pediatrician = it },
                    label = { Text("Pediatra de cabecera") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Hospital / Clínica") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && birthDate.isNotBlank()) {
                        onConfirm(name, lastName, birthDate, gender, bloodType, allergies, pediatrician, phone, clinic)
                    }
                },
                enabled = name.isNotBlank() && birthDate.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddGrowthDialog(
    initialRecord: GrowthRecordEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (weightKg: Double, heightCm: Double, date: String, notes: String?) -> Unit
) {
    val today = LocalDate.now().toString()

    // Formateo inicial del peso con decimales para gramos
    var weightText by remember {
        mutableStateOf(
            initialRecord?.let {
                String.format(Locale.US, "%.3f", it.weightKg).trimEnd('0').trimEnd('.')
            } ?: ""
        )
    }

    // Carrusel de números para el tamaño / estatura (40.0 cm a 130.0 cm en pasos de 0.5 cm)
    val heightList = remember {
        val list = mutableListOf<Double>()
        for (i in 80..260) {
            list.add(i / 2.0)
        }
        list
    }

    val defaultHeight = initialRecord?.heightCm ?: 75.0
    var selectedHeight by remember { mutableDoubleStateOf(defaultHeight) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val index = heightList.indexOfFirst { kotlin.math.abs(it - defaultHeight) < 0.25 }
        if (index >= 0) {
            val scrollTarget = (index - 2).coerceAtLeast(0)
            listState.scrollToItem(scrollTarget)
        }
    }

    var dateText by remember { mutableStateOf(initialRecord?.date ?: today) }
    var notesText by remember { mutableStateOf(initialRecord?.notes ?: "") }

    // Cálculo dinámico de peso y gramos
    val currentWeight = weightText.replace(',', '.').toDoubleOrNull() ?: 0.0

    // Cálculo dinámico del IMC: Peso / (Talla en metros)^2
    val heightMeters = selectedHeight / 100.0
    val liveBmi = if (heightMeters > 0.0 && currentWeight > 0.0) {
        (currentWeight / (heightMeters * heightMeters) * 10).roundToInt() / 10.0
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = if (initialRecord != null) "✏️ Editar Control de Crecimiento" else "📏 Registrar Crecimiento OMS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ingresa peso con gramos, tamaño en carrusel e IMC calculado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECCIÓN 1: PESO CON DECIMALES PARA GRAMOS
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { input ->
                            // Permitir números, coma o punto
                            if (input.all { it.isDigit() || it == '.' || it == ',' }) {
                                weightText = input
                            }
                        },
                        label = { Text("Peso en kg *") },
                        placeholder = { Text("Ej: 9.450") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Desglose visual en Kilogramos y Gramos
                    if (currentWeight > 0.0) {
                        val wholeKg = currentWeight.toInt()
                        val grams = ((currentWeight - wholeKg) * 1000).roundToInt()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Equivale a: $wholeKg kg con $grams g (${(currentWeight * 1000).roundToInt()} g)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Botones de ajuste rápido de gramos (+50g, +100g, +250g, +500g, -100g)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "+50g" to 0.05,
                            "+100g" to 0.10,
                            "+250g" to 0.25,
                            "+500g" to 0.50,
                            "-100g" to -0.10
                        ).forEach { (label, delta) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val newWeight = (currentWeight + delta).coerceAtLeast(0.0)
                                        weightText = String.format(Locale.US, "%.3f", newWeight).trimEnd('0').trimEnd('.')
                                    }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // SECCIÓN 2: TAMAÑO / ESTATURA EN CARRUSEL DE NÚMEROS
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Talla (cm):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val newH = (selectedHeight - 0.5).coerceAtLeast(40.0)
                                    selectedHeight = (newH * 10).roundToInt() / 10.0
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos 0.5 cm")
                            }

                            Text(
                                text = "${String.format(Locale.US, "%.1f", selectedHeight)} cm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = {
                                    val newH = (selectedHeight + 0.5).coerceAtMost(130.0)
                                    selectedHeight = (newH * 10).roundToInt() / 10.0
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Más 0.5 cm")
                            }
                        }
                    }

                    // Carrusel horizontal deslizable
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(heightList) { hVal ->
                            val isSelected = kotlin.math.abs(hVal - selectedHeight) < 0.25
                            Box(
                                modifier = Modifier
                                    .clickable { selectedHeight = hVal }
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", hVal),
                                    fontSize = if (isSelected) 13.sp else 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // SECCIÓN 3: RESULTADO DEL IMC (Índice de Masa Corporal)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (liveBmi > 0.0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Resultado del IMC",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Fórmula OMS: Peso / (Talla en m)²",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                        }

                        if (liveBmi > 0.0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", liveBmi)} kg/m²",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "IMC Calculado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        } else {
                            Text(
                                text = "Ingresa peso",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // FECHA CON SELECTOR DE CALENDARIO
                PequeDatePickerField(
                    label = "Fecha del registro *",
                    selectedDate = dateText,
                    onDateSelected = { dateText = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = dateText == today,
                        onClick = { dateText = today },
                        label = { Text("Hoy") }
                    )
                    FilterChip(
                        selected = dateText == LocalDate.now().minusDays(1).toString(),
                        onClick = { dateText = LocalDate.now().minusDays(1).toString() },
                        label = { Text("Ayer") }
                    )
                }

                // NOTAS
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas / Observaciones (opcional)") },
                    placeholder = { Text("Ej: Control sano del mes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentWeight > 0 && selectedHeight > 0) {
                        onConfirm(currentWeight, selectedHeight, dateText, notesText.ifBlank { null })
                    }
                },
                enabled = currentWeight > 0.0 && selectedHeight > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (initialRecord != null) "Actualizar Control" else "Guardar Control")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddMedicationDialog(
    initialRecord: MedicationTreatmentEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (
        name: String,
        formula: String,
        dosage: String,
        startDate: String,
        startTime: String,
        frequencyHours: Int,
        frequencyMinutes: Int,
        durationDays: Int,
        status: String,
        notifyMedication: Boolean,
        notes: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialRecord?.name ?: "") }
    var formula by remember { mutableStateOf(initialRecord?.formula ?: "") }
    var dosage by remember { mutableStateOf(initialRecord?.dosage ?: "") }
    var startDate by remember { mutableStateOf(initialRecord?.startDate ?: LocalDate.now().toString()) }
    var startTime by remember { mutableStateOf(initialRecord?.startTime ?: "08:00") }
    var frequencyHours by remember { mutableStateOf(initialRecord?.frequencyHours ?: 8) }
    var frequencyMinutes by remember { mutableStateOf(initialRecord?.frequencyMinutes ?: 0) }
    var durationText by remember { mutableStateOf(initialRecord?.durationDays?.toString() ?: "7") }
    var status by remember {
        mutableStateOf(
            if (initialRecord?.status?.uppercase() == "CULMINADO" || initialRecord?.status?.uppercase() == "COMPLETADO") "CULMINADO" else "ACTIVO"
        )
    }
    var notifyMedication by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialRecord != null) "✏️ Editar Tratamiento / Medicamento" else "💊 Añadir Tratamiento / Medicamento",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre comercial (ej: Ibuprofeno)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text("Principio Activo (ej: Ibuprofeno 2%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosis prescrita (ej: 5 ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // SELECTOR DE FECHA Y HORA DE INICIO
                Text(
                    text = "Fecha y hora de inicio:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PequeDatePickerField(
                        label = "Fecha Inicio *",
                        selectedDate = startDate,
                        onDateSelected = { startDate = it },
                        modifier = Modifier.weight(1.3f)
                    )
                    PequeTimePickerField(
                        label = "Hora *",
                        selectedTime = startTime,
                        onTimeSelected = { startTime = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // SELECTOR DE FRECUENCIA DE 24 HORAS CON MINUTOS
                PequeFrequencyPicker(
                    hours = frequencyHours,
                    minutes = frequencyMinutes,
                    onFrequencyChange = { h, m ->
                        frequencyHours = h
                        frequencyMinutes = m
                    }
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duración del tratamiento (en días)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ESTADO: ACTIVO O CULMINADO
                Text(
                    text = "Estado del tratamiento:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "ACTIVO",
                        onClick = { status = "ACTIVO" },
                        label = { Text("Activo") }
                    )
                    FilterChip(
                        selected = status == "CULMINADO",
                        onClick = { status = "CULMINADO" },
                        label = { Text("Culminado") }
                    )
                }

                // SI ES ACTIVO -> OPCIÓN DE NOTIFICACIÓN MÓVIL
                if (status == "ACTIVO") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Recordatorio móvil",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Avisar cada ${frequencyHours}h ${if (frequencyMinutes > 0) "${frequencyMinutes}m " else ""}para suministrar dosis",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = notifyMedication,
                                onCheckedChange = { notifyMedication = it }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas o indicaciones médicas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationText.toIntOrNull() ?: 7
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        onConfirm(
                            name,
                            formula,
                            dosage,
                            startDate,
                            startTime,
                            frequencyHours,
                            frequencyMinutes,
                            dur,
                            status,
                            notifyMedication,
                            notes.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(if (initialRecord != null) "Actualizar Tratamiento" else "Guardar Tratamiento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddTemperatureDialog(
    initialRecord: TemperatureRecordEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (celsius: Double, symptoms: String, medicationGiven: String?) -> Unit
) {
    var celsiusText by remember { mutableStateOf(initialRecord?.celsius?.toString() ?: "38.0") }
    var symptoms by remember { mutableStateOf(initialRecord?.symptoms ?: "Fiebre") }
    var medicationGiven by remember { mutableStateOf(initialRecord?.medicationGiven ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialRecord != null) "✏️ Editar Fiebre / Temperatura" else "🌡️ Registrar Fiebre / Temperatura",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = celsiusText,
                    onValueChange = { celsiusText = it },
                    label = { Text("Temperatura en °C * (ej: 38.2)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Síntomas (ej: Apatía, mejillas rojas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = medicationGiven,
                    onValueChange = { medicationGiven = it },
                    label = { Text("Medicamento administrado (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = celsiusText.replace(',', '.').toDoubleOrNull()
                    if (c != null) {
                        onConfirm(c, symptoms, medicationGiven.ifBlank { null })
                    }
                },
                enabled = celsiusText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(if (initialRecord != null) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddAppointmentDialog(
    initialRecord: MedicalAppointmentEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (
        doctor: String,
        specialty: String,
        clinic: String,
        reason: String,
        date: String,
        time: String,
        status: String,
        addToCalendar: Boolean,
        notify24hBefore: Boolean
    ) -> Unit
) {
    var doctor by remember { mutableStateOf(initialRecord?.doctorName ?: "") }
    var specialty by remember { mutableStateOf(initialRecord?.specialty ?: "Pediatría") }
    var clinic by remember { mutableStateOf(initialRecord?.clinic ?: "") }
    var reason by remember { mutableStateOf(initialRecord?.reason ?: "Control sano") }
    var date by remember { mutableStateOf(initialRecord?.date ?: LocalDate.now().plusDays(3).toString()) }
    var time by remember { mutableStateOf(initialRecord?.time ?: "10:00") }
    var status by remember {
        mutableStateOf(
            if (initialRecord?.status?.lowercase() == "pasado" || initialRecord?.status?.lowercase() == "completada") "pasado" else "pendiente"
        )
    }
    var addToCalendar by remember { mutableStateOf(true) }
    var notify24hBefore by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialRecord != null) "✏️ Editar Turno Médico" else "🩺 Agendar Turno Médico",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Médico / Profesional *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Lugar / Centro Médico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo de consulta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // SELECTORES DE FECHA Y HORA CON CALENDARIO
                Text(
                    text = "Fecha y hora de la cita:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PequeDatePickerField(
                        label = "Fecha *",
                        selectedDate = date,
                        onDateSelected = { date = it },
                        modifier = Modifier.weight(1.3f)
                    )
                    PequeTimePickerField(
                        label = "Hora *",
                        selectedTime = time,
                        onTimeSelected = { time = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ESTADO: PENDIENTE O PASADO
                Text(
                    text = "Estado del turno:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "pendiente",
                        onClick = { status = "pendiente" },
                        label = { Text("Pendiente") }
                    )
                    FilterChip(
                        selected = status == "pasado",
                        onClick = { status = "pasado" },
                        label = { Text("Pasado") }
                    )
                }

                // SI EL TURNO ES PENDIENTE -> OFRECER CALENDARIO Y NOTIFICACIÓN 24H
                if (status == "pendiente") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Recordatorios de la cita",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // SWITCH 1: CALENDARIO DEL TELÉFONO
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Calendario del teléfono",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Crear evento en la app de calendario nativa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = addToCalendar,
                                    onCheckedChange = { addToCalendar = it }
                                )
                            }

                            // SWITCH 2: NOTIFICACIÓN 24 HORAS ANTES
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Notificación 24 horas antes",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Alerta automática un día antes de la cita",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = notify24hBefore,
                                    onCheckedChange = { notify24hBefore = it }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (doctor.isNotBlank() && date.isNotBlank()) {
                        onConfirm(
                            doctor,
                            specialty,
                            clinic,
                            reason,
                            date,
                            time,
                            status,
                            addToCalendar,
                            notify24hBefore
                        )
                    }
                },
                enabled = doctor.isNotBlank() && date.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(if (initialRecord != null) "Actualizar Turno" else "Guardar Turno")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddSleepDialog(
    initialRecord: SleepEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (startTime: String, endTime: String, type: String, quality: String, date: String, notes: String?) -> Unit
) {
    val nowTimeStr = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }
    var selectedTab by remember { mutableStateOf(if (initialRecord != null) 1 else 0) } // 0: Tiempo Real, 1: Manual

    var date by remember { mutableStateOf(initialRecord?.date ?: LocalDate.now().toString()) }
    var startTime by remember { mutableStateOf(initialRecord?.startTime ?: nowTimeStr) }
    var endTime by remember { mutableStateOf(initialRecord?.endTime ?: nowTimeStr) }
    var type by remember { mutableStateOf(initialRecord?.type ?: "siesta") }
    var quality by remember { mutableStateOf(initialRecord?.quality ?: "tranquilo") }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    // Estado del temporizador / cronómetro en tiempo real
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var realStartTime by remember { mutableStateOf(nowTimeStr) }

    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            val baseTime = System.currentTimeMillis() - (elapsedSeconds * 1000L)
            while (isRunning && !isPaused) {
                elapsedSeconds = ((System.currentTimeMillis() - baseTime) / 1000L).coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    val timerHours = elapsedSeconds / 3600
    val timerMinutes = (elapsedSeconds % 3600) / 60
    val timerSecs = elapsedSeconds % 60
    val formattedTimer = String.format(Locale.US, "%02d:%02d:%02d", timerHours, timerMinutes, timerSecs)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = if (initialRecord != null) "✏️ Editar Registro de Sueño" else "😴 Registro de Sueño y Siestas",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Monitorea en tiempo real o ingresa horarios manualmente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PESTAÑAS DE MODO: TIEMPO REAL vs MANUAL
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Tiempo Real")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Manual") }
                    )
                }

                // TIPO DE SUEÑO (Común para ambos modos)
                Text("Tipo de sueño:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "siesta",
                        onClick = { type = "siesta" },
                        label = { Text("Siesta") }
                    )
                    FilterChip(
                        selected = type == "noche",
                        onClick = { type = "noche" },
                        label = { Text("Noche") }
                    )
                }

                if (selectedTab == 0) {
                    // --- MODO TIEMPO REAL CON CONTADOR ---
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when {
                                    !isRunning -> "Listo para medir el sueño"
                                    isPaused -> "Temporizador en pausa"
                                    else -> "Bebé durmiendo... (Inicio: $realStartTime hs)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            // RELOJ DIGITAL GIGANTE
                            Text(
                                text = formattedTimer,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )

                            // BOTONES DE CONTROL DEL CRONÓMETRO
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isRunning) {
                                    Button(
                                        onClick = {
                                            isRunning = true
                                            isPaused = false
                                            elapsedSeconds = 0L
                                            realStartTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Iniciar Sueño")
                                    }
                                } else {
                                    if (!isPaused) {
                                        Button(
                                            onClick = { isPaused = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pausar")
                                        }
                                    } else {
                                        Button(
                                            onClick = { isPaused = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reanudar")
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val currentNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                                            onConfirm(
                                                realStartTime,
                                                currentNow,
                                                type,
                                                quality,
                                                LocalDate.now().toString(),
                                                notes.ifBlank { null }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Despertó - Guardar")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- MODO MANUAL ---
                    PequeDatePickerField(
                        label = "Fecha del registro *",
                        selectedDate = date,
                        onDateSelected = { date = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PequeTimePickerField(
                            label = "Hora Inicio *",
                            selectedTime = startTime,
                            onTimeSelected = { startTime = it },
                            modifier = Modifier.weight(1f)
                        )
                        PequeTimePickerField(
                            label = "Hora Fin *",
                            selectedTime = endTime,
                            onTimeSelected = { endTime = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // CALIDAD DEL SUEÑO
                Text("Calidad del descanso:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("tranquilo", "inquieto", "despertares frecuentes", "pesadillas").forEach { q ->
                        FilterChip(
                            selected = quality == q,
                            onClick = { quality = q },
                            label = { Text(q.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas u observaciones (opcional)") },
                    placeholder = { Text("Ej: Tomó teta antes de dormirse") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            if (selectedTab == 1 || !isRunning) {
                Button(
                    onClick = {
                        val finalStart = if (selectedTab == 0 && isRunning) realStartTime else startTime
                        val finalEnd = if (selectedTab == 0 && isRunning) LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) else endTime
                        onConfirm(finalStart, finalEnd, type, quality, date, notes.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text(if (initialRecord != null) "Actualizar" else "Guardar Registro")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun AddMealDialog(
    initialRecord: MealEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (mealType: String, foods: String, appetite: String) -> Unit
) {
    var mealType by remember { mutableStateOf(initialRecord?.mealType ?: "almuerzo") }
    var foods by remember { mutableStateOf(initialRecord?.foods ?: "") }
    var appetite by remember { mutableStateOf(initialRecord?.appetite ?: "bueno") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialRecord != null) "✏️ Editar Registro de Comida" else "🍲 Registrar Comida",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tipo de comida:", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("desayuno" to "Desayuno", "almuerzo" to "Almuerzo", "merienda" to "Merienda").forEach { (type, label) ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("cena" to "Cena", "colacion" to "Colación").forEach { (type, label) ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = foods,
                    onValueChange = { foods = it },
                    label = { Text("Alimentos (ej: Puré de calabaza y pollo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Apetito:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("excelente" to "Excelente", "bueno" to "Bueno", "regular" to "Regular", "poco" to "Poco").forEach { (app, label) ->
                        FilterChip(
                            selected = appetite.lowercase() == app,
                            onClick = { appetite = app },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (foods.isNotBlank()) {
                        onConfirm(mealType, foods, appetite)
                    }
                },
                enabled = foods.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(if (initialRecord != null) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddActivityDialog(
    initialRecord: ActivityEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (title: String, category: String, durationMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialRecord?.title ?: "") }
    var category by remember { mutableStateOf(initialRecord?.category ?: "juego al aire libre") }
    var durationText by remember { mutableStateOf(initialRecord?.durationMinutes?.toString() ?: "30") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialRecord != null) "✏️ Editar Actividad / Juego" else "🎨 Registrar Actividad / Juego",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la actividad *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: aire libre, lectura, motricidad)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duración en minutos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationText.toIntOrNull() ?: 30
                    if (title.isNotBlank()) {
                        onConfirm(title, category, dur)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text(if (initialRecord != null) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
