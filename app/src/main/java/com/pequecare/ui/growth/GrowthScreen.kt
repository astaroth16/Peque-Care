package com.pequecare.ui.growth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.util.WhoGrowthStandards
import kotlin.math.max

enum class ChartMetric {
    PESO,
    TALLA
}

@Composable
fun GrowthScreen(
    growthRecords: List<GrowthRecordEntity>,
    childGender: String = "female",
    onAddRecordClick: () -> Unit,
    onEditRecordClick: (GrowthRecordEntity) -> Unit = {},
    onDuplicateRecordClick: (GrowthRecordEntity) -> Unit = {},
    onDeleteRecordClick: (GrowthRecordEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.PESO) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera informativa maternal y botón principal de registro
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
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
                                        imageVector = Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Desarrollo y Crecimiento",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Estándares oficiales SAP / Ministerio de Salud",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = onAddRecordClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Registrar Peso y Estatura",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Gráfica de curvatura de crecimiento
            item {
                GrowthCurveCard(
                    records = growthRecords,
                    childGender = childGender,
                    selectedMetric = selectedMetric,
                    onMetricChanged = { selectedMetric = it }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de Controles (${growthRecords.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Opciones en ⋮",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (growthRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "📏 Sin controles registrados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Añade el primer control con el peso en kilogramos y la talla en centímetros para visualizar la curva y los percentiles de tu bebé.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(growthRecords, key = { it.id }) { record ->
                    GrowthRecordCard(
                        record = record,
                        onEdit = { onEditRecordClick(record) },
                        onDuplicate = { onDuplicateRecordClick(record) },
                        onDelete = { onDeleteRecordClick(record) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GrowthCurveCard(
    records: List<GrowthRecordEntity>,
    childGender: String,
    selectedMetric: ChartMetric,
    onMetricChanged: (ChartMetric) -> Unit
) {
    val isMale = childGender.lowercase() == "male" || childGender.lowercase() == "niño" || childGender.lowercase() == "m"

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Curva Percentilar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMetric == ChartMetric.PESO,
                    onClick = { onMetricChanged(ChartMetric.PESO) },
                    label = {
                        Text(
                            text = "Peso (kg)",
                            fontWeight = if (selectedMetric == ChartMetric.PESO) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMetric == ChartMetric.TALLA,
                    onClick = { onMetricChanged(ChartMetric.TALLA) },
                    label = {
                        Text(
                            text = "Estatura (cm)",
                            fontWeight = if (selectedMetric == ChartMetric.TALLA) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            GrowthCanvasChart(
                records = records,
                isMale = isMale,
                metric = selectedMetric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFD86C7E), text = "Tu peque")
                LegendItem(color = Color(0xFF429668), text = "Mediana (P50)")
                LegendItem(color = Color(0xFFE09153), text = "Normal (P3-P97)")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun GrowthCanvasChart(
    records: List<GrowthRecordEntity>,
    isMale: Boolean,
    metric: ChartMetric,
    modifier: Modifier = Modifier
) {
    val sortedRecords = remember(records) {
        records.sortedBy { it.ageMonths }
    }

    val maxRecordedAge = sortedRecords.maxOfOrNull { it.ageMonths } ?: 12
    val maxAge = max(maxRecordedAge + 3, 24).coerceAtMost(60)
    val minAge = 0

    val (minY, maxY) = when (metric) {
        ChartMetric.PESO -> {
            val maxWeight = sortedRecords.maxOfOrNull { it.weightKg } ?: 12.0
            val upper = max(maxWeight + 3.0, 18.0)
            2.0 to upper
        }
        ChartMetric.TALLA -> {
            val maxHeight = sortedRecords.maxOfOrNull { it.heightCm } ?: 75.0
            val upper = max(maxHeight + 8.0, 95.0)
            45.0 to upper
        }
    }

    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.hashCode()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 68f
        val paddingRight = 32f
        val paddingTop = 28f
        val paddingBottom = 42f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        fun getX(age: Float): Float {
            val ratio = (age - minAge) / (maxAge - minAge).toFloat()
            return paddingLeft + ratio * graphWidth
        }

        fun getY(value: Float): Float {
            val ratio = (value - minY.toFloat()) / (maxY - minY).toFloat()
            return paddingTop + (1f - ratio) * graphHeight
        }

        val ySteps = 4
        for (i in 0..ySteps) {
            val yVal = minY + (maxY - minY) * (i.toDouble() / ySteps)
            val yPos = getY(yVal.toFloat())
            drawLine(
                color = gridLineColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(width - paddingRight, yPos),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            val label = when (metric) {
                ChartMetric.PESO -> String.format("%.0fkg", yVal)
                ChartMetric.TALLA -> String.format("%.0fcm", yVal)
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                6f,
                yPos + 7f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 22f
                    isAntiAlias = true
                }
            )
        }

        val ageStep = if (maxAge > 36) 12 else 6
        for (m in minAge..maxAge step ageStep) {
            val xPos = getX(m.toFloat())
            drawLine(
                color = gridLineColor,
                start = Offset(xPos, paddingTop),
                end = Offset(xPos, height - paddingBottom),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${m}m",
                xPos - 14f,
                height - 10f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 22f
                    isAntiAlias = true
                }
            )
        }

        val p3Path = Path()
        val p50Path = Path()
        val p97Path = Path()
        val corridorPath = Path()

        var firstPoint = true

        for (m in minAge..maxAge) {
            val (p3, p50, p97) = when (metric) {
                ChartMetric.PESO -> WhoGrowthStandards.getWeightPercentiles(m, isMale)
                ChartMetric.TALLA -> WhoGrowthStandards.getHeightPercentiles(m, isMale)
            }

            val x = getX(m.toFloat())
            val y3 = getY(p3.toFloat())
            val y50 = getY(p50.toFloat())
            val y97 = getY(p97.toFloat())

            if (firstPoint) {
                p3Path.moveTo(x, y3)
                p50Path.moveTo(x, y50)
                p97Path.moveTo(x, y97)
                corridorPath.moveTo(x, y97)
                firstPoint = false
            } else {
                p3Path.lineTo(x, y3)
                p50Path.lineTo(x, y50)
                p97Path.lineTo(x, y97)
                corridorPath.lineTo(x, y97)
            }
        }

        for (m in maxAge downTo minAge) {
            val (p3, _, _) = when (metric) {
                ChartMetric.PESO -> WhoGrowthStandards.getWeightPercentiles(m, isMale)
                ChartMetric.TALLA -> WhoGrowthStandards.getHeightPercentiles(m, isMale)
            }
            corridorPath.lineTo(getX(m.toFloat()), getY(p3.toFloat()))
        }
        corridorPath.close()

        drawPath(
            path = corridorPath,
            color = Color(0xFFFDE68A).copy(alpha = 0.28f)
        )

        drawPath(
            path = p3Path,
            color = Color(0xFFE09153).copy(alpha = 0.65f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
        drawPath(
            path = p97Path,
            color = Color(0xFFE09153).copy(alpha = 0.65f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )

        drawPath(
            path = p50Path,
            color = Color(0xFF429668),
            style = Stroke(width = 3.2f)
        )

        if (sortedRecords.isNotEmpty()) {
            val childPath = Path()
            val points = mutableListOf<Offset>()
            val values = mutableListOf<String>()

            sortedRecords.forEachIndexed { idx, record ->
                val valToDraw = when (metric) {
                    ChartMetric.PESO -> record.weightKg.toFloat()
                    ChartMetric.TALLA -> record.heightCm.toFloat()
                }
                val pt = Offset(getX(record.ageMonths.toFloat()), getY(valToDraw))
                points.add(pt)
                values.add(when (metric) {
                    ChartMetric.PESO -> String.format(java.util.Locale.US, "%.2f", record.weightKg).trimEnd('0').trimEnd('.')
                    ChartMetric.TALLA -> "${record.heightCm}"
                })

                if (idx == 0) {
                    childPath.moveTo(pt.x, pt.y)
                } else {
                    childPath.lineTo(pt.x, pt.y)
                }
            }

            drawPath(
                path = childPath,
                color = Color(0xFFD86C7E),
                style = Stroke(width = 4.5f)
            )

            points.forEachIndexed { i, pt ->
                drawCircle(
                    color = Color.White,
                    radius = 8.5f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFD86C7E),
                    radius = 6f,
                    center = pt
                )

                // Mostrar etiqueta en el primer y último punto (o puntos espaciados) para evitar solapamientos
                if (i == 0 || i == points.lastIndex || (points[i].x - points[i - 1].x) > 55f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        values[i],
                        pt.x - 16f,
                        pt.y - 14f,
                        android.graphics.Paint().apply {
                            color = Color(0xFF8C2D3E).hashCode()
                            textSize = 21f
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthRecordCard(
    record: GrowthRecordEntity,
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
                        text = "📅 ${record.date}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Edad al control: ${record.ageMonths} meses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = record.weightStatus,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones del registro",
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val formattedWeight = String.format(java.util.Locale.US, "%.3f", record.weightKg).trimEnd('0').trimEnd('.')
                MetricItem(
                    label = "Peso",
                    value = "$formattedWeight kg",
                    percentile = "P${record.weightPercentile}",
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "Talla",
                    value = "${record.heightCm} cm",
                    percentile = "P${record.heightPercentile}",
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "IMC",
                    value = "${record.bmi}",
                    percentile = "P${record.bmiPercentile} • ${record.bmiStatus}",
                    modifier = Modifier.weight(1.25f)
                )
            }

            if (!record.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📝 ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, percentile: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = percentile,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
