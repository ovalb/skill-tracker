package com.ovalb.skill_tracker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var pickerOpen by remember { mutableStateOf(false) }
        val trackedWidgets = remember { mutableStateListOf<TrackedWidget>() }
        var widgetIdCounter by remember { mutableStateOf(0) }

        LaunchedEffect(pickerOpen) {
            if (pickerOpen) {
                sheetState.show()
            }
        }

        val handleAddWidget: (WidgetTemplate) -> Unit = { template ->
            val widget = TrackedWidget(
                id = "instance-${template.id}-${widgetIdCounter++}",
                template = template,
                state = when (template.kind) {
                    WidgetKind.Weight -> WidgetState.Weight(
                        heightCm = null,
                        entries = emptyList(),
                    )

                    WidgetKind.Numeric -> WidgetState.Simple(value = "")
                    WidgetKind.Text -> WidgetState.Simple(value = "")
                }
            )
            trackedWidgets += widget
            scope.launch {
                sheetState.hide()
                pickerOpen = false
            }
        }

        val handleUpdateWidget: (TrackedWidget) -> Unit = { updated ->
            val index = trackedWidgets.indexOfFirst { it.id == updated.id }
            if (index >= 0) {
                trackedWidgets[index] = updated
            }
        }

        var detailWidgetId by remember { mutableStateOf<String?>(null) }

        val openDetail: (TrackedWidget) -> Unit = { detailWidgetId = it.id }

        Box(modifier = Modifier.fillMaxSize()) {
            val contentModifier = if (pickerOpen) {
                Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .alpha(0.55f)
            } else {
                Modifier.fillMaxSize()
            }

            Box(modifier = contentModifier) {
                HomeScreen(
                    widgets = trackedWidgets,
                    onAddWidget = { pickerOpen = true },
                    onUpdateWidget = handleUpdateWidget,
                    onOpenDetails = openDetail,
                )
            }

            if (pickerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                )

                ModalBottomSheet(
                    onDismissRequest = {
                        scope.launch {
                            sheetState.hide()
                            pickerOpen = false
                        }
                    },
                    sheetState = sheetState,
                    scrimColor = Color.Transparent,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    WidgetPickerSheet(
                        templates = WidgetCatalog.templates,
                        onSelect = handleAddWidget,
                    )
                }
            }

            detailWidgetId?.let { id ->
                val widget = trackedWidgets.firstOrNull { it.id == id }
                if (widget == null) {
                    detailWidgetId = null
                } else {
                    if (widget.template.kind is WidgetKind.Weight) {
                        val weightState = widget.state as? WidgetState.Weight
                        if (weightState == null) {
                            detailWidgetId = null
                        } else {
                            WeightDetailsScreen(
                                widget = widget,
                                state = weightState,
                                onUpdate = handleUpdateWidget,
                                onClose = { detailWidgetId = null },
                            )
                        }
                    } else {
                        detailWidgetId = null
                    }
                }
            }
        }
    }
}

enum class WidgetTag(val displayName: String) {
    Fitness("Fitness"),
    Music("Music"),
    Language("Language"),
    Lifestyle("Lifestyle"),
}

sealed interface WidgetKind {
    data object Weight : WidgetKind
    data object Text : WidgetKind
    data object Numeric : WidgetKind
}

data class WidgetTemplate(
    val id: String,
    val name: String,
    val description: String,
    val tag: WidgetTag,
    val kind: WidgetKind,
)

data class TrackedWidget(
    val id: String,
    val template: WidgetTemplate,
    val state: WidgetState,
)

sealed interface WidgetState {
    data class Weight(
        val heightCm: Double?,
        val entries: List<WeightEntry>,
    ) : WidgetState

    data class Simple(val value: String) : WidgetState
}

data class WeightEntry(
    val dateLabel: String,
    val kilograms: Double,
)

object WidgetCatalog {
    val templates: List<WidgetTemplate> = listOf(
        WidgetTemplate(
            id = "fitness_weight",
            name = "Weight tracker",
            description = "Log your weight and track trends over time",
            tag = WidgetTag.Fitness,
            kind = WidgetKind.Weight,
        ),
        WidgetTemplate(
            id = "fitness_sets",
            name = "Workout sets",
            description = "Track completed sets or reps for your main lift",
            tag = WidgetTag.Fitness,
            kind = WidgetKind.Numeric,
        ),
        WidgetTemplate(
            id = "music_minutes",
            name = "Practice minutes",
            description = "Target how long you want to rehearse today",
            tag = WidgetTag.Music,
            kind = WidgetKind.Numeric,
        ),
        WidgetTemplate(
            id = "language_phrase",
            name = "Phrase of the day",
            description = "Write down a new phrase you want to memorise",
            tag = WidgetTag.Language,
            kind = WidgetKind.Text,
        ),
    )
}
