package com.ovalb.skill_tracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun WeightWidgetCard(
    widget: TrackedWidget,
    state: WidgetState.Weight,
    onTrack: (TrackedWidget) -> Unit,
    onOpenDetails: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var draftValue by remember(state.entries) { mutableStateOf(state.latestWeight()?.let { formatWeight(it) } ?: "") }

    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = shape, clip = false),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        onClick = onOpenDetails,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Filled.MonitorWeight),
                    contentDescription = "Weight",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            val latest = state.latestWeight()
            val weightText = latest?.let { formatWeight(it) } ?: "--"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val interaction = remember { MutableInteractionSource() }
                Text(
                    text = weightText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) {
                        draftValue = latest?.let { formatWeight(it) } ?: ""
                        showDialog = true
                    },
                )
                Text(
                    text = "kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }
        }
    }

   if (showDialog) {
       WeightTrackDialog(
           currentValue = draftValue,
           onValueChange = { draftValue = it },
           onDismiss = { showDialog = false },
           onConfirm = {
                draftValue.toDoubleOrNull()?.let { value ->
                    draftValue = formatWeight(value)
                    val updatedState = state.addEntry(value)
                    onTrack(widget.copy(state = updatedState))
                }
                showDialog = false
            },
        )
    }
}

@Composable
fun WeightTrackDialog(
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
