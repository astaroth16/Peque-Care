package com.pequecare.ui.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.util.WhoGrowthStandards
import kotlin.math.max
import kotlin.math.min

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

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 24.dp else 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabecera informativa y botón de añadir
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Curvas de Crecimiento OMS",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Seguimiento de peso, estatura y percentiles oficiales",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onAddRecordClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "➕ Registrar Peso, Tamaño y Fecha",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Gráfica de curvatura de crecimiento OMS
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
                            text = "Historial de Mediciones (${growthRecords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Acciones en ⋮",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (growthRecords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📏 Sin controles de crecimiento aún",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Añade el primer registro con el peso en kilogramos, tamaño en centímetros y la fecha de la medición para calcular los percentiles y trazar la gráfica de crecimiento.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = onAddRecordClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Añadir Primer Registro", style = MaterialTheme.typography.labelMedium)
                                }
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
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }

            // FAB Flotante
            ExtendedFloatingActionButton(
                onClick = onAddRecordClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Control")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Nuevo Control", fontWeight = FontWeight.SemiBold)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Curva de Crecimiento OMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Selector de Métrica
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedMetric == ChartMetric.PESO,
                        onClick = { onMetricChanged(ChartMetric.PESO) },
                        label = { Text("⚖️ Peso") }
                    )
                    FilterChip(
                        selected = selectedMetric == ChartMetric.TALLA,
                        onClick = { onMetricChanged(ChartMetric.TALLA) },
                        label = { Text("📏 Talla") }
                    )
                }
            }

            // Gráfico Canvas
            GrowthCanvasChart(
                records = records,
                isMale = isMale,
                metric = selectedMetric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // Leyenda visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFE11D48), text = "👶 Registros del Niño")
                LegendItem(color = Color(0xFF10B981), text = "🟢 Mediana OMS (P50)")
                LegendItem(color = Color(0xFFFBBF24), text = "🟡 Rango OMS (P3-P97)")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Rango Y según métrica
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

    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.hashCode()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 60f
        val paddingRight = 40f
        val paddingTop = 30f
        val paddingBottom = 45f

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

        // 1. Líneas de cuadrícula horizontal
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

            // Texto eje Y
            val label = when (metric) {
                ChartMetric.PESO -> String.format("%.0fkg", yVal)
                ChartMetric.TALLA -> String.format("%.0fcm", yVal)
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                8f,
                yPos + 8f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 24f
                    isAntiAlias = true
                }
            )
        }

        // 2. Líneas de cuadrícula vertical (Meses)
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
                height - 12f,
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = 24f
                    isAntiAlias = true
                }
            )
        }

        // 3. Área y curvas OMS (P3, P50, P97)
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

        // Cerrar el corridor para rellenar entre P97 y P3
        for (m in maxAge downTo minAge) {
            val (p3, _, _) = when (metric) {
                ChartMetric.PESO -> WhoGrowthStandards.getWeightPercentiles(m, isMale)
                ChartMetric.TALLA -> WhoGrowthStandards.getHeightPercentiles(m, isMale)
            }
            corridorPath.lineTo(getX(m.toFloat()), getY(p3.toFloat()))
        }
        corridorPath.close()

        // Rellenar corredor P3-P97 OMS
        drawPath(
            path = corridorPath,
            color = Color(0xFFFDE68A).copy(alpha = 0.35f)
        )

        // Líneas límite OMS P3 y P97
        drawPath(
            path = p3Path,
            color = Color(0xFFF59E0B).copy(alpha = 0.6f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
        drawPath(
            path = p97Path,
            color = Color(0xFFF59E0B).copy(alpha = 0.6f),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )

        // Mediana OMS P50
        drawPath(
            path = p50Path,
            color = Color(0xFF10B981),
            style = Stroke(width = 3.5f)
        )

        // 4. Curva y Puntos Reales del Niño
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
                    ChartMetric.PESO -> "${record.weightKg}"
                    ChartMetric.TALLA -> "${record.heightCm}"
                })

                if (idx == 0) {
                    childPath.moveTo(pt.x, pt.y)
                } else {
                    childPath.lineTo(pt.x, pt.y)
                }
            }

            // Línea del niño
            drawPath(
                path = childPath,
                color = Color(0xFFE11D48),
                style = Stroke(width = 5f)
            )

            // Puntos y etiquetas
            points.forEachIndexed { i, pt ->
                // Sombra y punto
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFE11D48),
                    radius = 6.5f,
                    center = pt
                )

                // Etiqueta de valor
                drawContext.canvas.nativeCanvas.drawText(
                    values[i],
                    pt.x - 16f,
                    pt.y - 14f,
                    android.graphics.Paint().apply {
                        color = Color(0xFF9F1239).hashCode()
                        textSize = 22f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                )
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
                Column {
                    Text(
                        text = "📅 ${record.date}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Edad: ${record.ageMonths} meses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = record.weightStatus,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Menú contextual de 3 puntos
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

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "⚖️ Peso",
                    value = "${record.weightKg} kg",
                    percentile = "Percentil ${record.weightPercentile}"
                )
                MetricItem(
                    label = "📏 Tamaño / Talla",
                    value = "${record.heightCm} cm",
                    percentile = "Percentil ${record.heightPercentile}"
                )
                MetricItem(
                    label = "📊 IMC",
                    value = "${record.bmi}",
                    percentile = "Percentil ${record.bmiPercentile}"
                )
            }

            if (!record.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 Nota: ${record.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, percentile: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = percentile, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
