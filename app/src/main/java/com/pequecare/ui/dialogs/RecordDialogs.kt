package com.pequecare.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.ui.theme.RosePrimary
import java.time.LocalDate

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
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Fecha de Nacimiento (AAAA-MM-DD) *") },
                    placeholder = { Text("2024-05-20") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Género:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == "female",
                        onClick = { gender = "female" },
                        label = { Text("👧 Niña") }
                    )
                    FilterChip(
                        selected = gender == "male",
                        onClick = { gender = "male" },
                        label = { Text("👦 Niño") }
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
    var weightText by remember { mutableStateOf(initialRecord?.weightKg?.toString() ?: "") }
    var heightText by remember { mutableStateOf(initialRecord?.heightCm?.toString() ?: "") }
    var dateText by remember { mutableStateOf(initialRecord?.date ?: today) }
    var notesText by remember { mutableStateOf(initialRecord?.notes ?: "") }

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
                    text = "Ingresa peso, tamaño (estatura) y fecha de medición",
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
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("⚖️ Peso (kg) *") },
                    placeholder = { Text("Ej: 9.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("📏 Tamaño / Estatura (cm) *") },
                    placeholder = { Text("Ej: 76.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("📅 Fecha del registro (AAAA-MM-DD) *") },
                    placeholder = { Text("2026-10-28") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = dateText == today,
                        onClick = { dateText = today },
                        label = { Text("📅 Hoy") }
                    )
                    FilterChip(
                        selected = dateText == LocalDate.now().minusDays(1).toString(),
                        onClick = { dateText = LocalDate.now().minusDays(1).toString() },
                        label = { Text("Ayer") }
                    )
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("📝 Notas / Observaciones") },
                    placeholder = { Text("Ej: Control sano del mes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.replace(',', '.').toDoubleOrNull()
                    val h = heightText.replace(',', '.').toDoubleOrNull()
                    if (w != null && h != null && w > 0 && h > 0) {
                        onConfirm(w, h, dateText, notesText.ifBlank { null })
                    }
                },
                enabled = weightText.isNotBlank() && heightText.isNotBlank(),
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
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, formula: String, dosage: String, frequencyHours: Int, durationDays: Int, notes: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var formula by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequencyText by remember { mutableStateOf("8") }
    var durationText by remember { mutableStateOf("7") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "💊 Añadir Tratamiento / Medicamento", fontWeight = FontWeight.Bold)
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
                OutlinedTextField(
                    value = frequencyText,
                    onValueChange = { frequencyText = it },
                    label = { Text("Frecuencia (cada cuántas horas)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duración (en días)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    val freq = frequencyText.toIntOrNull() ?: 8
                    val dur = durationText.toIntOrNull() ?: 7
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        onConfirm(name, formula, dosage, freq, dur, notes.ifBlank { null })
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank(),
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
fun AddTemperatureDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (celsius: Double, symptoms: String, medicationGiven: String?) -> Unit
) {
    var celsiusText by remember { mutableStateOf("38.0") }
    var symptoms by remember { mutableStateOf("Fiebre") }
    var medicationGiven by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "🌡️ Registrar Fiebre / Temperatura", fontWeight = FontWeight.Bold)
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
fun AddAppointmentDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (doctor: String, specialty: String, clinic: String, reason: String, date: String, time: String) -> Unit
) {
    var doctor by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Pediatría") }
    var clinic by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Control sano") }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(3).toString()) }
    var time by remember { mutableStateOf("10:00") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "🩺 Agendar Turno Médico", fontWeight = FontWeight.Bold)
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
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hora (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (doctor.isNotBlank() && date.isNotBlank()) {
                        onConfirm(doctor, specialty, clinic, reason, date, time)
                    }
                },
                enabled = doctor.isNotBlank() && date.isNotBlank(),
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
fun AddSleepDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (startTime: String, endTime: String, type: String, quality: String) -> Unit
) {
    var startTime by remember { mutableStateOf("14:00") }
    var endTime by remember { mutableStateOf("15:30") }
    var type by remember { mutableStateOf("siesta") }
    var quality by remember { mutableStateOf("tranquilo") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "😴 Registrar Sueño / Siesta", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tipo de sueño:", style = MaterialTheme.typography.labelMedium)
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

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Hora Inicio (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Hora Fin (HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quality,
                    onValueChange = { quality = it },
                    label = { Text("Calidad (tranquilo, inquieto...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(startTime, endTime, type, quality) },
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
fun AddMealDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (mealType: String, foods: String, appetite: String) -> Unit
) {
    var mealType by remember { mutableStateOf("almuerzo") }
    var foods by remember { mutableStateOf("") }
    var appetite by remember { mutableStateOf("bueno") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "🍲 Registrar Comida", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tipo de comida:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("desayuno", "almuerzo", "merienda", "cena").forEach { item ->
                        FilterChip(
                            selected = mealType == item,
                            onClick = { mealType = item },
                            label = { Text(item.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                OutlinedTextField(
                    value = foods,
                    onValueChange = { foods = it },
                    label = { Text("Alimentos (ej: Puré de calabaza y pollo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = appetite,
                    onValueChange = { appetite = it },
                    label = { Text("Apetito (excelente, bueno, regular, poco)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
fun AddActivityDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (title: String, category: String, durationMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("juego al aire libre") }
    var durationText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "🎨 Registrar Actividad / Juego", fontWeight = FontWeight.Bold)
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
