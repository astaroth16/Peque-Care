package com.pequecare.ui.health

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.ui.theme.RosePrimary

@Composable
fun HealthScreen(
    treatments: List<MedicationTreatmentEntity>,
    appointments: List<MedicalAppointmentEntity>,
    temperatures: List<TemperatureRecordEntity>,
    onAddMedicationClick: () -> Unit = {},
    onAddAppointmentClick: () -> Unit = {},
    onAddTemperatureClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideScreen = maxWidth > 600.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Control de Salud y Tratamientos",
                style = MaterialTheme.typography.headlineLarge
            )

            // Tratamientos Detallados
            HealthSectionCard(
                title = "💊 Medicamentos y Dosis Detalladas",
                onAddClick = onAddMedicationClick
            ) {
                if (treatments.isEmpty()) {
                    EmptyStateText("Sin tratamientos activos registrados. Toca '+' para añadir.")
                } else {
                    treatments.forEach { med ->
                        Text(
                            text = "• ${med.name} (${med.formula}) - Dosis: ${med.dosage} c/${med.frequencyHours}h. Estado: ${med.status}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Turnos Médicos
            HealthSectionCard(
                title = "🩺 Consultas y Turnos Médicos",
                onAddClick = onAddAppointmentClick
            ) {
                if (appointments.isEmpty()) {
                    EmptyStateText("No hay turnos ni consultas médicas agendadas. Toca '+' para añadir.")
                } else {
                    appointments.forEach { appt ->
                        Text(
                            text = "• ${appt.doctorName} (${appt.specialty}) - ${appt.date} a las ${appt.time} hs. LUGAR: ${appt.clinic}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Registro de Fiebre y Temperatura
            HealthSectionCard(
                title = "🌡️ Registro de Fiebre y Síntomas",
                onAddClick = onAddTemperatureClick
            ) {
                if (temperatures.isEmpty()) {
                    EmptyStateText("Sin episodios de fiebre o lecturas de temperatura registradas. Toca '+' para añadir.")
                } else {
                    temperatures.forEach { temp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${temp.date} ${temp.time} hs - ${temp.celsius}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Síntomas: ${temp.symptoms}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HealthSectionCard(
    title: String,
    onAddClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (onAddClick != null) {
                    IconButton(onClick = onAddClick) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir", tint = RosePrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyStateText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
