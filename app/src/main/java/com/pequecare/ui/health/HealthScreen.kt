package com.pequecare.ui.health

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.ui.theme.AmberAccent
import com.pequecare.ui.theme.AmberContainer
import com.pequecare.ui.theme.EmeraldContainer
import com.pequecare.ui.theme.EmeraldSuccess
import com.pequecare.ui.theme.RoseContainer
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
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Informativa Maternal
            item {
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
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Salud y Cuidado Médico",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tratamientos, turnos con el pediatra y control de temperatura",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // SECCIÓN 1: MEDICAMENTOS Y TRATAMIENTOS
            item {
                SectionHeader(
                    title = "💊 Tratamientos (${treatments.size})",
                    onAddClick = onAddMedicationClick
                )
            }

            if (treatments.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin tratamientos activos",
                        description = "Registra medicamentos con dosis, principio activo y alarmas de toma.",
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
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "🩺 Turnos Médicos (${appointments.size})",
                    onAddClick = onAddAppointmentClick
                )
            }

            if (appointments.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin consultas agendadas",
                        description = "Agenda controles de niño sano y visitas a especialistas con alerta 24h antes.",
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
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "🌡️ Fiebre y Temperatura (${temperatures.size})",
                    onAddClick = onAddTemperatureClick
                )
            }

            if (temperatures.isEmpty()) {
                item {
                    EmptySectionCard(
                        title = "Sin registros de temperatura",
                        description = "Lleva el control térmico en °C, síntomas observados y antitérmicos administrados.",
                        buttonText = "Registrar Temperatura",
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
                Spacer(modifier = Modifier.height(28.dp))
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        FilledTonalIconButton(
            onClick = onAddClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir")
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, style = MaterialTheme.typography.labelLarge)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (med.formula.isNotBlank()) med.formula else "Tratamiento pediátrico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isActive = med.status.uppercase() == "ACTIVO"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) EmeraldContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isActive) "Activo" else "Culminado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
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
                "Cada ${med.frequencyHours}h ${med.frequencyMinutes}m"
            } else {
                "Cada ${med.frequencyHours} horas"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBox(label = "Dosis", value = med.dosage, modifier = Modifier.weight(1f))
                InfoBox(label = "Frecuencia", value = freqDisplay, modifier = Modifier.weight(1.25f))
                InfoBox(label = "Duración", value = "${med.durationDays} días", modifier = Modifier.weight(1f))
            }

            if (!med.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${med.notes}",
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
    val isPending = appt.status.lowercase() == "pendiente" || appt.status.lowercase() == "programada"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🩺",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = appt.doctorName.ifBlank { "Médico / Especialista" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📅 ${appt.date}  •  ⏰ ${appt.time} hs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPending) EmeraldContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isPending) "⏳ Pendiente" else "✔️ Realizado",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPending) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = appt.specialty.ifBlank { "Pediatría" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoBox(
                    label = "Centro Médico",
                    value = appt.clinic.ifBlank { "Consultorio" },
                    modifier = Modifier.weight(1f)
                )
                InfoBox(
                    label = "Motivo",
                    value = appt.reason.ifBlank { "Control sano" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (!appt.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${appt.notes}",
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
        isFever -> RoseContainer
        isMildFever -> AmberContainer
        else -> EmeraldContainer
    }

    val badgeTextColor = when {
        isFever -> RosePrimary
        isMildFever -> AmberAccent
        else -> EmeraldSuccess
    }

    val statusText = when {
        isFever -> "Fiebre"
        isMildFever -> "Febrícula"
        else -> "Normal"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFever) RosePrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📅 ${temp.date}  •  ⏰ ${temp.time} hs",
                        style = MaterialTheme.typography.bodySmall,
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
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                InfoBox(
                    label = "Síntomas",
                    value = temp.symptoms.ifBlank { "Sin síntomas" },
                    modifier = Modifier.weight(1f)
                )
                InfoBox(
                    label = "Antitérmico",
                    value = temp.medicationGiven?.ifBlank { "Ninguno" } ?: "Ninguno",
                    modifier = Modifier.weight(1f)
                )
            }

            if (!temp.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${temp.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
