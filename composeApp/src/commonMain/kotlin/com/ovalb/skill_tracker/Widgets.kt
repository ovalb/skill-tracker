package com.ovalb.skill_tracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeightWidgetCard(
    widget: TrackedWidget,
    onTrack: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var draft by remember(widget.value) { mutableStateOf(widget.value) }

    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = shape, clip = false),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScaleIcon(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = widget.template.tag.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = widget.value.takeIf { it.isNotBlank() } ?: "--",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    draft = widget.value
                    showDialog = true
                }) {
                    Text("Track")
                }
            }
        }
    }

    if (showDialog) {
        WeightTrackDialog(
            currentValue = draft,
            onValueChange = { draft = it },
            onDismiss = { showDialog = false },
            onConfirm = {
                onTrack(draft.trim())
                showDialog = false
            },
        )
    }
}

@Composable
private fun WeightTrackDialog(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track weight") },
        text = {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text("Weight (kg)") },
                placeholder = { Text("72.5") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ScaleIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.08f
        val height = size.height
        val width = size.width
        val rectHeight = height * 0.65f
        val top = (height - rectHeight) / 2f

        drawRoundRect(
            color = color,
            topLeft = Offset(0f, top),
            size = Size(width, rectHeight),
            cornerRadius = CornerRadius(width * 0.25f, width * 0.25f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(width * 0.25f, top + rectHeight * 0.15f),
            size = Size(width * 0.5f, rectHeight * 0.6f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(width / 2f, top + rectHeight * 0.2f),
            end = Offset(width / 2f, top + rectHeight * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
