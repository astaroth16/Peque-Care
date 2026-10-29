package com.pequecare.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.ui.theme.AmberAccent
import com.pequecare.ui.theme.EmeraldSuccess
import com.pequecare.ui.theme.IndigoPrimary
import com.pequecare.ui.theme.RosePrimary

@Composable
fun DashboardScreen(
    profile: ChildProfileEntity?,
    latestGrowth: GrowthRecordEntity?,
    activeMedications: List<MedicationTreatmentEntity>,
    todayWaterGlasses: Int,
    onIncrementWater: () -> Unit,
    onAddRecordClick: (type: String) -> Unit,
    onEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth > 600.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del Niño/a y Saludo
            ChildHeaderCard(profile = profile, onEditProfile = onEditProfile)

            // Resumen de Crecimiento OMS
            GrowthSummaryCard(
                latestGrowth = latestGrowth,
                onAddGrowth = { onAddRecordClick("growth") }
            )

            // Layout Adaptativo (Filas en pantallas anchas, Columnas en teléfonos estrechos)
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WaterTrackerCard(
                        waterGlasses = todayWaterGlasses,
                        onIncrementWater = onIncrementWater,
                        modifier = Modifier.weight(1f)
                    )
                    ActiveMedicationsCard(
                        medications = activeMedications,
                        onAddMedication = { onAddRecordClick("medication") },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                WaterTrackerCard(
                    waterGlasses = todayWaterGlasses,
                    onIncrementWater = onIncrementWater,
                    modifier = Modifier.fillMaxWidth()
                )
                ActiveMedicationsCard(
                    medications = activeMedications,
                    onAddMedication = { onAddRecordClick("medication") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Accesos Rápidos Responsivos
            QuickActionsGrid(onAddRecordClick = onAddRecordClick)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChildHeaderCard(
    profile: ChildProfileEntity?,
    onEditProfile: () -> Unit
) {
    Card(
        onClick = onEditProfile,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(RosePrimary, AmberAccent)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (profile?.gender == "male") "👦" else "👧",
                            fontSize = 28.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!profile?.name.isNull_or_blank()) "¡Hola, ${profile?.name}!" else "¡Bienvenido a PequeCare!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (!profile?.birthDate.isNull_or_blank()) "Fecha Nac.: ${profile?.birthDate} • Toca para editar" else "👉 Toca aquí para registrar al niño/a",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (profile == null) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()

@Composable
private fun GrowthSummaryCard(
    latestGrowth: GrowthRecordEntity?,
    onAddGrowth: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = RosePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crecimiento OMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAddGrowth,
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Registrar", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (latestGrowth != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GrowthMetricColumn("Peso", "${latestGrowth.weightKg} kg", "P${latestGrowth.weightPercentile}")
                    GrowthMetricColumn("Talla", "${latestGrowth.heightCm} cm", "P${latestGrowth.heightPercentile}")
                    GrowthMetricColumn("IMC", "${latestGrowth.bmi}", "P${latestGrowth.bmiPercentile}")
                }
            } else {
                Text(
                    text = "Aún no hay registros de crecimiento. Añade el primer peso y estatura según las curvas OMS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GrowthMetricColumn(title: String, value: String, percentile: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldSuccess.copy(alpha = 0.15f)
        ) {
            Text(
                text = percentile,
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldSuccess,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun WaterTrackerCard(
    waterGlasses: Int,
    onIncrementWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = IndigoPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = IndigoPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Agua de Hoy", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$waterGlasses vasitos (${waterGlasses * 200} ml)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onIncrementWater,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir vaso")
            }
        }
    }
}

@Composable
private fun ActiveMedicationsCard(
    medications: List<MedicationTreatmentEntity>,
    onAddMedication: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(text = "Tratamientos Activos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddMedication) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Tratamiento", tint = RosePrimary)
                }
            }

            if (medications.isNotEmpty()) {
                medications.forEach { med ->
                    Text(
                        text = "💊 ${med.name} - ${med.dosage} c/${med.frequencyHours}h",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "Sin tratamientos o medicamentos activos en este momento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(onAddRecordClick: (type: String) -> Unit) {
    Column {
        Text(text = "Registro Rápido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton("😴 Sueño", Modifier.weight(1f)) { onAddRecordClick("sleep") }
            QuickActionButton("🍲 Comida", Modifier.weight(1f)) { onAddRecordClick("meal") }
            QuickActionButton("🌡️ Fiebre", Modifier.weight(1f)) { onAddRecordClick("temperature") }
            QuickActionButton("🩺 Turno", Modifier.weight(1f)) { onAddRecordClick("appointment") }
        }
    }
}

@Composable
private fun QuickActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
