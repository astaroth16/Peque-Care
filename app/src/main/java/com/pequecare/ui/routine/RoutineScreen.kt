package com.pequecare.ui.routine

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
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.ui.theme.RosePrimary

@Composable
fun RoutineScreen(
    sleepRecords: List<SleepEntity>,
    mealRecords: List<MealEntity>,
    activityRecords: List<ActivityEntity>,
    onAddSleepClick: () -> Unit = {},
    onAddMealClick: () -> Unit = {},
    onAddActivityClick: () -> Unit = {},
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
                text = "Rutinas Diarias",
                style = MaterialTheme.typography.headlineLarge
            )

            // Sueño y Descanso
            RoutineSectionCard(
                title = "😴 Sueño y Siestas",
                onAddClick = onAddSleepClick
            ) {
                if (sleepRecords.isEmpty()) {
                    Text("Sin registros de sueño para hoy. Toca '+' para añadir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    sleepRecords.forEach { sleep ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "• ${sleep.type.uppercase()}: ${sleep.startTime} - ${sleep.endTime} (${sleep.durationMinutes} min)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Calidad: ${sleep.quality}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Comidas e Hidratación
            RoutineSectionCard(
                title = "🍲 Alimentación y Comidas",
                onAddClick = onAddMealClick
            ) {
                if (mealRecords.isEmpty()) {
                    Text("Sin registros de alimentos hoy. Toca '+' para añadir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    mealRecords.forEach { meal ->
                        Text(text = "• ${meal.mealType.uppercase()} (${meal.time}): ${meal.foods} [Apetito: ${meal.appetite}]", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            // Actividades
            RoutineSectionCard(
                title = "🎨 Actividades y Juegos",
                onAddClick = onAddActivityClick
            ) {
                if (activityRecords.isEmpty()) {
                    Text("Sin actividades registradas. Toca '+' para añadir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    activityRecords.forEach { act ->
                        Text(text = "• ${act.title} (${act.category}) - Duración: ${act.durationMinutes} min", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RoutineSectionCard(
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
