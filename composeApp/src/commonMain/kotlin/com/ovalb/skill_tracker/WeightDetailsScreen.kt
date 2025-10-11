package com.ovalb.skill_tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeightDetailsScreen(
    widget: TrackedWidget,
    state: WidgetState.Weight,
    onUpdate: (TrackedWidget) -> Unit,
    onClose: () -> Unit,
) {
    PlatformBackHandler(onBack = onClose)
    var heightField by remember { mutableStateOf("") }
    var weightField by remember { mutableStateOf("") }

    LaunchedEffect(widget.id) {
        heightField = state.heightCm?.let { formatWeight(it) } ?: ""
        weightField = state.entries.lastOrNull()?.kilograms?.let { formatWeight(it) } ?: ""
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Weight details",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = heightField,
                    onValueChange = {
                        heightField = it
                        persistChanges(heightText = it, weightText = weightField, state = state, widget = widget, onUpdate = onUpdate)
                    },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = weightField,
                    onValueChange = {
                        weightField = it
                        persistChanges(heightText = heightField, weightText = it, state = state, widget = widget, onUpdate = onUpdate)
                    },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            SummaryRow(state)

            WeightHistoryChart(entries = state.entries.takeLast(5))

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.weight(1f, fill = true))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onClose) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.size(12.dp))
                TextButton(onClick = onClose) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(state: WidgetState.Weight) {
    val latest = state.entries.lastOrNull()?.kilograms
    val height = state.heightCm
    val bmi = if (latest != null && height != null && height > 0.0) {
        val heightMeters = height / 100.0
        latest / (heightMeters * heightMeters)
    } else null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SummaryCell(title = "Current BMI", value = bmi?.let { formatWeight(it) } ?: "--")
        SummaryCell(title = "Height", value = height?.let { "${formatWeight(it)} cm" } ?: "--")
        SummaryCell(title = "Weight", value = latest?.let { "${formatWeight(it)} kg" } ?: "--")
    }
}

@Composable
private fun RowScope.SummaryCell(title: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun WeightHistoryChart(entries: List<WeightEntry>) {
    val padded = if (entries.isEmpty()) mockEntries else entries
    val values = padded.map { it.kilograms }
    val minValue = min(values.minOrNull() ?: 0.0, values.maxOrNull() ?: 0.0) - 1
    val maxValue = max(values.maxOrNull() ?: 0.0, values.minOrNull() ?: 0.0) + 1
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(
            text = "Last 5 entries",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val width = size.width
            val height = size.height
            val paddingH = 24.dp.toPx()
            val paddingV = 24.dp.toPx()
            val usableWidth = width - paddingH * 2
            val usableHeight = height - paddingV * 2

            // axes
            drawLine(
                color = outlineColor.copy(alpha = 0.4f),
                start = Offset(paddingH, height - paddingV),
                end = Offset(width - paddingH, height - paddingV),
                strokeWidth = 2.dp.toPx(),
            )
            drawLine(
                color = outlineColor.copy(alpha = 0.4f),
                start = Offset(paddingH, paddingV),
                end = Offset(paddingH, height - paddingV),
                strokeWidth = 2.dp.toPx(),
            )

            val stepX = if (padded.size <= 1) 0f else usableWidth / (padded.size - 1)
            val range = (maxValue - minValue).coerceAtLeast(1.0)

            val points = padded.mapIndexed { index, entry ->
                val normalized = (entry.kilograms - minValue) / range
                val x = paddingH + stepX * index
                val y = height - paddingV - (normalized * usableHeight).toFloat()
                entry to Offset(x, y)
            }

            for (i in 0 until points.lastIndex) {
                drawLine(
                    color = outlineColor,
                    start = points[i].second,
                    end = points[i + 1].second,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            points.forEach { (_, offset) ->
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = offset,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            padded.forEach { entry ->
                Text(
                    text = entry.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun persistChanges(
    heightText: String,
    weightText: String,
    state: WidgetState.Weight,
    widget: TrackedWidget,
    onUpdate: (TrackedWidget) -> Unit,
) {
    val targetHeight = heightText.toDoubleOrNull()
    val heightUpdated = state.updateHeight(targetHeight)
    val weightValue = weightText.toDoubleOrNull()
    val updatedState = if (weightValue != null) {
        heightUpdated.addEntry(weightValue)
    } else {
        heightUpdated
    }

    if (updatedState != state) {
        onUpdate(widget.copy(state = updatedState))
    }
}

private val mockEntries = listOf(
    WeightEntry("Sep 01", 72.0),
    WeightEntry("Sep 12", 71.4),
    WeightEntry("Sep 24", 71.0),
    WeightEntry("Oct 05", 70.6),
    WeightEntry("Oct 20", 70.1),
)
