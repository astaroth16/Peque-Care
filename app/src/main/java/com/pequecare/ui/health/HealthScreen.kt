package com.pequecare.ui.health

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.ui.theme.EmeraldSuccess
import com.pequecare.ui.theme.RosePrimary

@Composable
fun HealthScreen(
    treatments: List<MedicationTreatmentEntity>,
    appointments: List<MedicalAppointmentEntity>,
    temperatures: List<TemperatureRecordEntity>,
    onAddMedicationClick: () -> Unit = {},
    onEditMedicationClick: (MedicationTreatmentEntity) -> Unit = {},
    onDuplicateMedicationClick: (MedicationTreatmentEntity) -> Unit = {},
    onDeleteMedicationClick: (MedicationTreatmentEntity) -> Unit = {},
    onAddAppointmentClick: () -> Unit = {},
    onEditAppointmentClick: (MedicalAppointmentEntity) -> Unit = {},
    onDuplicateAppointmentClick: (MedicalAppointmentEntity) -> Unit = {},
    onDeleteAppointmentClick: (MedicalAppointmentEntity) -> Unit = {},
    onAddTemperatureClick: () -> Unit = {},
    onEditTemperatureClick: (TemperatureRecordEntity) -> Unit = {},
    onDuplicateTemperatureClick: (TemperatureRecordEntity) -> Unit = {},
    onDeleteTemperatureClick: (TemperatureRecordEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideScreen = this.maxWidth > 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Informativa
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Control de Salud Pediátrico",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Medicamentos, consultas al pediatra y control de fiebre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN 1: MEDICAMENTOS Y TRATAMIENTOS
            item {
                SectionHeader(
                    title = "💊 Tratamientos y Medicamentos (${treatments.size})",
                    onAddClick = onAddMedicationClick
                )
            }

            if (treatments.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin tratamientos activos",
                        description = "Registra medicamentos con dosis, principio activo y frecuencia horaria.",
                        buttonText = "Añadir Tratamiento",
                        onAddClick = onAddMedicationClick
                    )
                }
            } else {
                items(treatments, key = { it.id }) { med ->
                    MedicationCard(
                        med = med,
                        onEdit = { onEditMedicationClick(med) },
                        onDuplicate = { onDuplicateMedicationClick(med) },
                        onDelete = { onDeleteMedicationClick(med) }
                    )
                }
            }

            // SECCIÓN 2: CONSULTAS Y TURNOS MÉDICOS
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(
                    title = "🩺 Consultas y Turnos Médicos (${appointments.size})",
                    onAddClick = onAddAppointmentClick
                )
            }

            if (appointments.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin consultas agendadas",
                        description = "Agenda turnos médicos, controles sanos y visitas a especialistas.",
                        buttonText = "Agendar Consulta",
                        onAddClick = onAddAppointmentClick
                    )
                }
            } else {
                items(appointments, key = { it.id }) { appt ->
                    AppointmentCard(
                        appt = appt,
                        onEdit = { onEditAppointmentClick(appt) },
                        onDuplicate = { onDuplicateAppointmentClick(appt) },
                        onDelete = { onDeleteAppointmentClick(appt) }
                    )
                }
            }

            // SECCIÓN 3: FIEBRE Y TEMPERATURA
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(
                    title = "🌡️ Registro de Fiebre y Temperatura (${temperatures.size})",
                    onAddClick = onAddTemperatureClick
                )
            }

            if (temperatures.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin registros de temperatura",
                        description = "Lleva control térmico en °C, síntomas asociados y medicamentos dados.",
                        buttonText = "Registrar Fiebre",
                        onAddClick = onAddTemperatureClick
                    )
                }
            } else {
                items(temperatures, key = { it.id }) { temp ->
                    TemperatureCard(
                        temp = temp,
                        onEdit = { onEditTemperatureClick(temp) },
                        onDuplicate = { onDuplicateTemperatureClick(temp) },
                        onDelete = { onDeleteTemperatureClick(temp) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onAddClick) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir", tint = RosePrimary)
        }
    }
}

@Composable
private fun EmptySectionCard(
    title: String,
    description: String,
    buttonText: String,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(buttonText, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun MedicationCard(
    med: MedicationTreatmentEntity,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💊 ${med.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (med.formula.isNotBlank()) "Principio Activo: ${med.formula}" else "Tratamiento pediátrico",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isActive = med.status.uppercase() == "ACTIVO"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isActive) "🟢 ACTIVO" else "🏁 CULMINADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✏️ Editar") },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📋 Duplicar") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Eliminar", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val freqDisplay = if (med.frequencyMinutes > 0) {
                "c/${med.frequencyHours}h ${med.frequencyMinutes}m"
            } else {
                "c/${med.frequencyHours} horas"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoColumn(label = "Dosis", value = med.dosage, modifier = Modifier.weight(1f))
                InfoColumn(label = "Frecuencia", value = freqDisplay, modifier = Modifier.weight(1.3f))
                InfoColumn(label = "Duración", value = "${med.durationDays} días", modifier = Modifier.weight(1f))
            }

            if (!med.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: ${med.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appt: MedicalAppointmentEntity,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🩺 ${appt.doctorName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📅 ${appt.date} • ⏰ ${appt.time} hs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPending = appt.status.lowercase() == "pendiente" || appt.status.lowercase() == "programada"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPending) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isPending) "⏳ PENDIENTE" else "✔️ PASADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPending) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = appt.specialty.ifBlank { "Pediatría" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✏️ Editar") },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📋 Duplicar") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Eliminar", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoColumn(label = "Lugar / Clínica", value = appt.clinic.ifBlank { "Consultorio" }, modifier = Modifier.weight(1f))
                InfoColumn(label = "Motivo", value = appt.reason.ifBlank { "Control" }, modifier = Modifier.weight(1f))
            }

            if (!appt.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: ${appt.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TemperatureCard(
    temp: TemperatureRecordEntity,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isFever = temp.celsius >= 38.0
    val isMildFever = temp.celsius >= 37.5 && temp.celsius < 38.0

    val badgeColor = when {
        isFever -> RosePrimary.copy(alpha = 0.2f)
        isMildFever -> Color(0xFFFBBF24).copy(alpha = 0.2f)
        else -> EmeraldSuccess.copy(alpha = 0.2f)
    }

    val badgeTextColor = when {
        isFever -> RosePrimary
        isMildFever -> Color(0xFFB45309)
        else -> EmeraldSuccess
    }

    val statusText = when {
        isFever -> "Fiebre (Alta)"
        isMildFever -> "Febrícula"
        else -> "Temperatura Normal"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🌡️ ${temp.celsius} °C",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isFever) RosePrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📅 ${temp.date} • ⏰ ${temp.time} hs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✏️ Editar") },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📋 Duplicar") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Eliminar", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoColumn(label = "Síntomas", value = temp.symptoms.ifBlank { "Sin síntomas" }, modifier = Modifier.weight(1f))
                InfoColumn(label = "Antipirético", value = temp.medicationGiven?.ifBlank { "Ninguno" } ?: "Ninguno", modifier = Modifier.weight(1f))
            }

            if (!temp.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: ${temp.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
