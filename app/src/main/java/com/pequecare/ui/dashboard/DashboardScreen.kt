package com.pequecare.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.ui.theme.AmberAccent
import com.pequecare.ui.theme.AmberContainer
import com.pequecare.ui.theme.EmeraldContainer
import com.pequecare.ui.theme.EmeraldSuccess
import com.pequecare.ui.theme.IndigoContainer
import com.pequecare.ui.theme.IndigoPrimary
import com.pequecare.ui.theme.PastelLavender
import com.pequecare.ui.theme.PastelLavenderContainer
import com.pequecare.ui.theme.RoseContainer
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
    onCloneChildClick: (ChildProfileEntity) -> Unit = {},
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
                .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Selector de Peques o Tarjeta de Bienvenida Maternal
            if (allProfiles.isNotEmpty()) {
                ChildrenSelectorSection(
                    profiles = allProfiles,
                    activeProfileId = profile?.id,
                    onSelectChild = onSelectChild,
                    onAddChildClick = onAddChildClick,
                    onEditChildClick = onEditChildClick,
                    onCloneChildClick = onCloneChildClick,
                    onDeleteChildClick = onDeleteChildClick
                )
            } else {
                Card(
                    onClick = onAddChildClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👶", fontSize = 28.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Bienvenida a PequeCare!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toca aquí para crear la ficha de tu bebé y comenzar el seguimiento.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // 2. Registro Rápido del Día (Accesos directos maternales minimalistas)
            QuickActionsSection(onAddRecordClick = onAddRecordClick)

            // 3. Resumen de Crecimiento y Percentiles
            GrowthSummaryCard(
                latestGrowth = latestGrowth,
                onAddGrowth = { onAddRecordClick("growth") }
            )

            // 4. Tratamientos Activos
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
private fun QuickActionsSection(
    onAddRecordClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "✨ Registro Rápido del Día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Anota con un toque las actividades y cuidados de tu bebé",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    emoji = "😴",
                    label = "Sueño",
                    subtitle = "Cronómetro",
                    bgColor = PastelLavenderContainer,
                    accentColor = PastelLavender,
                    onClick = { onAddRecordClick("sleep") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    emoji = "🍲",
                    label = "Comida",
                    subtitle = "Lactancia/Menú",
                    bgColor = AmberContainer,
                    accentColor = AmberAccent,
                    onClick = { onAddRecordClick("meal") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    emoji = "📏",
                    label = "Crecimiento",
                    subtitle = "Peso y Talla",
                    bgColor = EmeraldContainer,
                    accentColor = EmeraldSuccess,
                    onClick = { onAddRecordClick("growth") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    emoji = "🌡️",
                    label = "Temperatura",
                    subtitle = "Control fiebre",
                    bgColor = RoseContainer,
                    accentColor = RosePrimary,
                    onClick = { onAddRecordClick("temperature") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    emoji = "💊",
                    label = "Remedio",
                    subtitle = "Dosis y alarma",
                    bgColor = IndigoContainer,
                    accentColor = IndigoPrimary,
                    onClick = { onAddRecordClick("medication") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    emoji = "🩺",
                    label = "Pediatra",
                    subtitle = "Agendar turno",
                    bgColor = EmeraldContainer,
                    accentColor = EmeraldSuccess,
                    onClick = { onAddRecordClick("appointment") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    emoji: String,
    label: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = bgColor.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2124),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B5B60),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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
    onCloneChildClick: (ChildProfileEntity) -> Unit,
    onDeleteChildClick: (ChildProfileEntity) -> Unit
) {
    var childToDelete by remember { mutableStateOf<ChildProfileEntity?>(null) }

    if (childToDelete != null) {
        val child = childToDelete!!
        val isOnlyOne = profiles.size <= 1
        AlertDialog(
            onDismissRequest = { childToDelete = null },
            title = {
                Text(
                    text = if (isOnlyOne) "⚠️ ¿Eliminar único perfil?" else "🗑️ ¿Eliminar a ${child.name}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isOnlyOne)
                        "Estás a punto de eliminar a ${child.name}. Al ser el único peque registrado, se vaciará toda la base de datos de la app para no dejar datos residuales."
                    else
                        "Se eliminará el perfil de ${child.name} y todos sus controles de crecimiento, medicamentos, turnos y rutinas."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = child
                        childToDelete = null
                        onDeleteChildClick(toDel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isOnlyOne) "Eliminar y vaciar app" else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { childToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir Peque", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(profiles, key = { it.id }) { child ->
                val isSelected = child.id == activeProfileId
                var menuExpanded by remember { mutableStateOf(false) }

                Card(
                    onClick = {
                        if (isSelected) onEditChildClick(child)
                        else onSelectChild(child.id)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (child.gender == "male") "👦" else "👧",
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = child.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "ACTIVO",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = buildString {
                                        append(formatChildAge(child.birthDate))
                                        if (child.bloodType.isNotBlank()) {
                                            append(" • ")
                                            append(child.bloodType)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opciones de ${child.name}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("✏️ Editar ficha") },
                                        onClick = {
                                            menuExpanded = false
                                            onEditChildClick(child)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👥 Clonar perfil (Gemelos)") },
                                        onClick = {
                                            menuExpanded = false
                                            onCloneChildClick(child)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🗑️ Eliminar perfil", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            menuExpanded = false
                                            childToDelete = child
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

@Composable
private fun GrowthSummaryCard(
    latestGrowth: GrowthRecordEntity?,
    onAddGrowth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RoseContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Último Control de Crecimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (latestGrowth != null) {
                            Text(
                                text = "Medición del ${latestGrowth.date} (${latestGrowth.ageMonths} meses)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = onAddGrowth,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Crecimiento"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (latestGrowth != null) {
                val formattedWeight = String.format(java.util.Locale.US, "%.3f", latestGrowth.weightKg).trimEnd('0').trimEnd('.')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GrowthMetricColumn(
                        title = "Peso",
                        value = "$formattedWeight kg",
                        percentile = "P${latestGrowth.weightPercentile}",
                        modifier = Modifier.weight(1f)
                    )
                    GrowthMetricColumn(
                        title = "Talla",
                        value = "${latestGrowth.heightCm} cm",
                        percentile = "P${latestGrowth.heightPercentile}",
                        modifier = Modifier.weight(1f)
                    )
                    GrowthMetricColumn(
                        title = "IMC",
                        value = "${latestGrowth.bmi}",
                        percentile = "P${latestGrowth.bmiPercentile}",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aún no hay registros de crecimiento. Toca el botón (+) para registrar el primer peso y estatura de tu bebé.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthMetricColumn(
    title: String,
    value: String,
    percentile: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldContainer
            ) {
                Text(
                    text = percentile,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = IndigoContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tratamientos Activos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (medications.isEmpty()) "Sin medicación en curso" else "${medications.size} en seguimiento",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onAddMedication,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Tratamiento"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (medications.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    medications.forEach { med ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "💊 ${med.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Dosis: ${med.dosage} • Cada ${med.frequencyHours}h",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldContainer
                                ) {
                                    Text(
                                        text = "Activo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay medicamentos o tratamientos activos en este momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
