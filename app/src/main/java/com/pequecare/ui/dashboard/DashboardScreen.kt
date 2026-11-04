package com.pequecare.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.pequecare.ui.theme.RosePrimary

fun formatChildAge(birthDateStr: String): String {
    return try {
        val birth = java.time.LocalDate.parse(birthDateStr)
        val now = java.time.LocalDate.now()
        val period = java.time.Period.between(birth, now)
        when {
            period.years > 0 -> "${period.years}a ${period.months}m"
            period.months > 0 -> "${period.months} meses"
            else -> "${period.days} días"
        }
    } catch (e: Exception) {
        birthDateStr
    }
}

@Composable
fun DashboardScreen(
    profile: ChildProfileEntity?,
    allProfiles: List<ChildProfileEntity> = emptyList(),
    latestGrowth: GrowthRecordEntity?,
    activeMedications: List<MedicationTreatmentEntity>,
    onAddRecordClick: (type: String) -> Unit,
    onSelectChild: (String) -> Unit = {},
    onAddChildClick: () -> Unit = {},
    onEditChildClick: (ChildProfileEntity) -> Unit = {},
    onDeleteChildClick: (ChildProfileEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
            // Sección de Microfichas de Niños (si hay niños registrados)
            if (allProfiles.isNotEmpty()) {
                ChildrenSelectorSection(
                    profiles = allProfiles,
                    activeProfileId = profile?.id,
                    onSelectChild = onSelectChild,
                    onAddChildClick = onAddChildClick,
                    onEditChildClick = onEditChildClick,
                    onDeleteChildClick = onDeleteChildClick
                )
            } else {
                Card(
                    onClick = onAddChildClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("👶", fontSize = 32.sp)
                        Column {
                            Text("¡Bienvenido a PequeCare!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Toca aquí para registrar a tu primer peque", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Resumen de Crecimiento
            GrowthSummaryCard(
                latestGrowth = latestGrowth,
                onAddGrowth = { onAddRecordClick("growth") }
            )

            // Tratamientos Activos
            ActiveMedicationsCard(
                medications = activeMedications,
                onAddMedication = { onAddRecordClick("medication") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChildrenSelectorSection(
    profiles: List<ChildProfileEntity>,
    activeProfileId: String?,
    onSelectChild: (String) -> Unit,
    onAddChildClick: () -> Unit,
    onEditChildClick: (ChildProfileEntity) -> Unit,
    onDeleteChildClick: (ChildProfileEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👶 Mis Peques (${profiles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(onClick = onAddChildClick) {
                Text("➕ Añadir Peque", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(profiles, key = { it.id }) { child ->
                val isSelected = child.id == activeProfileId
                var menuExpanded by remember { mutableStateOf(false) }

                Card(
                    onClick = {
                        if (isSelected) onEditChildClick(child)
                        else onSelectChild(child.id)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (child.gender == "male") "👦" else "👧",
                                        fontSize = 22.sp
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = child.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "ACTIVO",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = formatChildAge(child.birthDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opciones de ${child.name}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("✏️ Editar Perfil") },
                                        onClick = {
                                            menuExpanded = false
                                            onEditChildClick(child)
                                        }
                                    )
                                    if (profiles.size > 1) {
                                        DropdownMenuItem(
                                            text = { Text("🗑️ Eliminar", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                menuExpanded = false
                                                onDeleteChildClick(child)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                        text = "Crecimiento y Métricas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onAddGrowth) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Crecimiento",
                        tint = RosePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (latestGrowth != null) {
                val formattedWeight = String.format(java.util.Locale.US, "%.3f", latestGrowth.weightKg).trimEnd('0').trimEnd('.')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GrowthMetricColumn("Peso", "$formattedWeight kg", "P${latestGrowth.weightPercentile}")
                    GrowthMetricColumn("Talla", "${latestGrowth.heightCm} cm", "P${latestGrowth.heightPercentile}")
                    GrowthMetricColumn("IMC", "${latestGrowth.bmi} kg/m²", "P${latestGrowth.bmiPercentile}")
                }
            } else {
                Text(
                    text = "Aún no hay registros de crecimiento. Añade el primer peso y estatura del peque.",
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
