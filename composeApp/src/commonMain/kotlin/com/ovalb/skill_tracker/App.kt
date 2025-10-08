package com.ovalb.skill_tracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
        val selectedSkills = remember { mutableStateListOf<TrackedSkill>() }

        when (val current = screen) {
            AppScreen.Home -> HomeScreen(
                selectedSkills = selectedSkills,
                onAddNew = { screen = AppScreen.SkillSelection },
                onOpenSkill = { skill ->
                    screen = AppScreen.SkillDetail(skill.category.id)
                },
            )

            AppScreen.SkillSelection -> SkillSelectionScreen(
                onStart = { trackedSkill ->
                    val existingIndex = selectedSkills.indexOfFirst { it.category.id == trackedSkill.category.id }
                    if (existingIndex >= 0) {
                        selectedSkills[existingIndex] = trackedSkill
                    } else {
                        selectedSkills += trackedSkill
                    }
                    screen = AppScreen.Home
                },
                onCancel = { screen = AppScreen.Home },
            )

            is AppScreen.SkillDetail -> {
                val skill = selectedSkills.firstOrNull { it.category.id == current.skillId }
                if (skill == null) {
                    screen = AppScreen.Home
                } else {
                    SkillDetailScreen(
                        skill = skill,
                        onUpdate = { updated ->
                            val index = selectedSkills.indexOfFirst { it.category.id == updated.category.id }
                            if (index >= 0) {
                                selectedSkills[index] = updated
                            }
                        },
                        onBack = { screen = AppScreen.Home },
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillSelectionScreen(
    onStart: (TrackedSkill) -> Unit,
    onCancel: () -> Unit,
) {
    val allSkills = remember {
        listOf(
            SkillCategory(
                id = "fitness",
                name = "Functional Fitness",
                description = "Strength, mobility, and endurance routines",
                icon = SkillGlyph.Fitness,
                widgets = listOf(
                    SkillWidgetTemplate(
                        id = "fitness_weight",
                        name = "Weight tracker",
                        description = "Log your weight and watch it trend over time",
                        inputLabel = "Today’s weight",
                        placeholder = "72.5",
                        kind = SkillWidgetKind.Weight,
                    ),
                    SkillWidgetTemplate(
                        id = "fitness_sets",
                        name = "Workout sets",
                        description = "Track completed sets or reps for your main lift",
                        inputLabel = "Sets completed",
                        placeholder = "3 x 10",
                        kind = SkillWidgetKind.Numeric,
                    ),
                ),
            ),
            SkillCategory(
                id = "music",
                name = "Rhythm Guitar",
                description = "Chord progressions, strumming, and timing",
                icon = SkillGlyph.Guitar,
                widgets = listOf(
                    SkillWidgetTemplate(
                        id = "guitar_minutes",
                        name = "Practice minutes",
                        description = "Target how long you want to rehearse today",
                        inputLabel = "Minutes practiced",
                        placeholder = "25",
                        kind = SkillWidgetKind.Numeric,
                    ),
                    SkillWidgetTemplate(
                        id = "guitar_progression",
                        name = "Chord focus",
                        description = "Capture the chord progression you are refining",
                        inputLabel = "Chord progression",
                        placeholder = "Am - F - C - G",
                        kind = SkillWidgetKind.Text,
                    ),
                ),
            ),
            SkillCategory(
                id = "language",
                name = "Conversational Spanish",
                description = "Everyday phrases and listening practice",
                icon = SkillGlyph.Language,
                widgets = listOf(
                    SkillWidgetTemplate(
                        id = "spanish_phrase",
                        name = "Phrase of the day",
                        description = "Write down a new phrase you want to memorise",
                        inputLabel = "New phrase",
                        placeholder = "¿Cómo estás?",
                        kind = SkillWidgetKind.Text,
                    ),
                    SkillWidgetTemplate(
                        id = "spanish_listening",
                        name = "Listening minutes",
                        description = "Log how much time you listen to native content",
                        inputLabel = "Minutes listened",
                        placeholder = "15",
                        kind = SkillWidgetKind.Numeric,
                    ),
                ),
            ),
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedSkill by remember { mutableStateOf<SkillCategory?>(null) }
    var selectedWidgets by remember(selectedSkill) { mutableStateOf(emptySet<String>()) }
    val widgetInputs = remember(selectedSkill) {
        mutableStateMapOf<String, String>().apply {
            selectedSkill?.widgets?.forEach { template ->
                put(template.id, "")
            }
        }
    }

    val filteredSkills = remember(query, allSkills) {
        if (query.isBlank()) allSkills
        else allSkills.filter { skill ->
            skill.name.contains(query, ignoreCase = true) ||
                skill.description.contains(query, ignoreCase = true)
        }
    }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Add a new focus",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Choose your focus",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Narrow down what you want to work on today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search skills") },
                    singleLine = true,
                )
            }
        }

        items(filteredSkills, key = { it.id }) { skill ->
            SkillCard(
                skill = skill,
                isSelected = skill == selectedSkill,
                onSelect = {
                    selectedSkill = skill
                },
            )
        }

        val activeSkill = selectedSkill
        if (activeSkill != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Pick widgets for ${activeSkill.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WidgetSelectionList(
                        widgets = activeSkill.widgets,
                        selectedWidgets = selectedWidgets,
                        onToggle = { template ->
                            selectedWidgets = if (template.id in selectedWidgets) {
                                selectedWidgets - template.id
                            } else {
                                selectedWidgets + template.id
                            }
                        },
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                AnimatedVisibility(visible = selectedSkill != null && selectedWidgets.isNotEmpty()) {
                    Column {
                        Button(
                            onClick = {
                                val skill = selectedSkill ?: return@Button
                                val chosen = skill.widgets.filter { it.id in selectedWidgets }
                                val instances = chosen.map { template ->
                                    SkillWidgetInstance(
                                        template = template,
                                        value = widgetInputs[template.id].orEmpty(),
                                    )
                                }
                                onStart(TrackedSkill(category = skill, widgets = instances))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Add skill")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                FilledTonalButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    selectedSkills: List<TrackedSkill>,
    onAddNew: () -> Unit,
    onOpenSkill: (TrackedSkill) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Skill tracker",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Stay consistent by focusing on a few areas at a time",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedSkills.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "No skills yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap \"Add new skill\" to pick your first focus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(selectedSkills, key = { it.category.id }) { skill ->
                    SelectedSkillCard(
                        skill = skill,
                        onClick = { onOpenSkill(skill) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddNew,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add new skill")
        }
    }
}

@Composable
private fun SkillDetailScreen(
    skill: TrackedSkill,
    onUpdate: (TrackedSkill) -> Unit,
    onBack: () -> Unit,
) {
    PlatformBackHandler(onBack = onBack)
    val scrollState = rememberScrollState()
    val widgetValues = remember(skill) {
        mutableStateMapOf<String, String>().apply {
            skill.widgets.forEach { put(it.template.id, it.value) }
        }
    }
    val applyUpdate: (SkillWidgetTemplate, String) -> Unit = { template, newValue ->
        widgetValues[template.id] = newValue
        val updatedSkill = skill.copy(
            widgets = skill.widgets.map { instance ->
                if (instance.template.id == template.id) {
                    instance.copy(value = newValue)
                } else {
                    instance
                }
            }
        )
        onUpdate(updatedSkill)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = skill.category.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = skill.category.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 2.dp,
        ) {
            WidgetDetailGrid(
                widgets = skill.widgets,
                valueProvider = { id -> widgetValues[id].orEmpty() },
                onValueChange = applyUpdate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun SkillCard(
    skill: SkillCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val targetContainerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val backgroundColor by animateColorAsState(
        targetValue = targetContainerColor,
        label = "skillCardBackground",
    )

    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = backgroundColor,
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkillIcon(
                glyph = skill.icon,
                isSelected = isSelected,
            )
            Column {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.takeIf { isSelected }
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = skill.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.takeIf { isSelected }
                        ?.copy(alpha = 0.8f)
                        ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SelectedSkillCard(
    skill: TrackedSkill,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkillIcon(
                glyph = skill.category.icon,
                isSelected = false,
            )
            Column {
                Text(
                    text = skill.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = skill.category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (skill.widgets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    skill.widgets.forEach { widget ->
                        Text(
                            text = buildString {
                                append(widget.template.name)
                                val value = widget.value.trim()
                                if (value.isNotEmpty()) {
                                    append(" — ")
                                    append(
                                        if (widget.template.kind == SkillWidgetKind.Weight) {
                                            "${value} kg"
                                        } else {
                                            value
                                        }
                                    )
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetSelectionList(
    widgets: List<SkillWidgetTemplate>,
    selectedWidgets: Set<String>,
    onToggle: (SkillWidgetTemplate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        widgets.forEach { widget ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .clickable { onToggle(widget) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Checkbox(
                        checked = widget.id in selectedWidgets,
                        onCheckedChange = { onToggle(widget) },
                    )
                    Text(
                        text = widget.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun WidgetDetailGrid(
    widgets: List<SkillWidgetInstance>,
    valueProvider: (String) -> String,
    onValueChange: (SkillWidgetTemplate, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 2,
    ) {
        widgets.forEach { widget ->
            val value = valueProvider(widget.template.id)
            val modifier = if (widget.template.kind == SkillWidgetKind.Weight) {
                Modifier.width(150.dp)
            } else {
                Modifier.fillMaxWidth()
            }
            when (widget.template.kind) {
                SkillWidgetKind.Weight -> WeightWidgetDetailCard(
                    template = widget.template,
                    value = value,
                    onValueChange = { updated -> onValueChange(widget.template, updated) },
                    modifier = modifier,
                )

                else -> GenericWidgetDetailCard(
                    template = widget.template,
                    value = value,
                    onValueChange = { updated -> onValueChange(widget.template, updated) },
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun WeightWidgetDetailCard(
    template: SkillWidgetTemplate,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditor by remember { mutableStateOf(false) }
    var draft by remember(value, showEditor) { mutableStateOf(value) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScaleIcon(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = value.takeIf { it.isNotBlank() } ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            draft = value
                            showEditor = true
                        },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PenIcon(
                            modifier = Modifier.size(14.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                )
            }
        }
    }

    if (showEditor) {
        WeightEditDialog(
            currentValue = value,
            onDismiss = { showEditor = false },
            onConfirm = { updated ->
                onValueChange(updated)
                draft = updated
                showEditor = false
            },
        )
    }
}

@Composable
private fun WeightEditDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember(currentValue) { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update weight") },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Weight (kg)") },
                placeholder = { Text("72.5") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft.trim()) }) {
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
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val baseHeight = height * 0.55f
        val baseTop = height - baseHeight
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, baseTop),
            size = Size(width, baseHeight),
            cornerRadius = CornerRadius(width * 0.2f, width * 0.2f),
        )
        val needleHeight = height * 0.45f
        val needleWidth = width * 0.18f
        drawRoundRect(
            color = color,
            topLeft = Offset((width - needleWidth) / 2f, baseTop - needleHeight * 0.6f),
            size = Size(needleWidth, needleHeight),
            cornerRadius = CornerRadius(needleWidth / 2f, needleWidth / 2f),
        )
        drawCircle(
            color = Color.White,
            radius = needleWidth * 0.45f,
            center = Offset(width / 2f, baseTop),
        )
    }
}

@Composable
private fun PenIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val pathLength = size.minDimension * 0.8f
        val start = Offset(size.width * 0.2f, size.height * 0.7f)
        val end = Offset(start.x + pathLength * 0.7f, start.y - pathLength * 0.7f)
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(end.x - strokeWidth, end.y - strokeWidth * 1.5f),
            size = Size(strokeWidth * 1.8f, strokeWidth * 1.8f),
            cornerRadius = CornerRadius(strokeWidth * 0.4f, strokeWidth * 0.4f),
        )
    }
}

@Composable
private fun GenericWidgetDetailCard(
    template: SkillWidgetTemplate,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(template.inputLabel) },
                placeholder = { Text(template.placeholder) },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun SkillIcon(
    glyph: SkillGlyph,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val surfaceColor = MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier.size(48.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Canvas(modifier = Modifier.padding(12.dp)) {
            when (glyph) {
                SkillGlyph.Fitness -> drawFitnessIcon(accentColor)
                SkillGlyph.Guitar -> drawGuitarIcon(accentColor, surfaceColor)
                SkillGlyph.Language -> drawLanguageIcon(accentColor, surfaceColor)
            }
        }
    }
}

private fun DrawScope.drawFitnessIcon(color: Color) {
    val barHeight = size.height * 0.3f
    val barTop = (size.height - barHeight) / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width * 0.2f, barTop),
        size = Size(size.width * 0.6f, barHeight),
        cornerRadius = CornerRadius(x = barHeight / 2, y = barHeight / 2),
    )
    val endRadius = barHeight / 1.6f
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width * 0.05f, barTop - endRadius / 2),
        size = Size(endRadius, barHeight + endRadius),
        cornerRadius = CornerRadius(endRadius / 2, endRadius / 2),
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - size.width * 0.05f - endRadius, barTop - endRadius / 2),
        size = Size(endRadius, barHeight + endRadius),
        cornerRadius = CornerRadius(endRadius / 2, endRadius / 2),
    )
}

private fun DrawScope.drawGuitarIcon(color: Color, innerColor: Color) {
    val bodyWidth = size.width * 0.55f
    val bodyHeight = size.height * 0.55f
    val bodyTop = size.height - bodyHeight
    drawRoundRect(
        color = color,
        topLeft = Offset((size.width - bodyWidth) / 2f, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyWidth / 2, bodyWidth / 2),
    )
    val neckWidth = size.width * 0.18f
    val neckHeight = size.height * 0.55f
    drawRoundRect(
        color = color.copy(alpha = 0.8f),
        topLeft = Offset((size.width - neckWidth) / 2f, bodyTop - neckHeight * 0.75f),
        size = Size(neckWidth, neckHeight),
        cornerRadius = CornerRadius(neckWidth / 2, neckWidth / 2),
    )
    drawCircle(
        color = innerColor,
        radius = bodyWidth * 0.2f,
        center = Offset(size.width / 2f, bodyTop + bodyHeight * 0.45f),
    )
}

private fun DrawScope.drawLanguageIcon(color: Color, innerColor: Color) {
    val bubbleWidth = size.width * 0.8f
    val bubbleHeight = size.height * 0.65f
    val bubbleTopLeft = Offset((size.width - bubbleWidth) / 2f, size.height * 0.1f)
    drawRoundRect(
        color = color,
        topLeft = bubbleTopLeft,
        size = Size(bubbleWidth, bubbleHeight),
        cornerRadius = CornerRadius(bubbleHeight * 0.3f, bubbleHeight * 0.3f),
    )
    val tailWidth = bubbleWidth * 0.25f
    val tailHeight = bubbleHeight * 0.3f
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width * 0.45f, bubbleTopLeft.y + bubbleHeight - tailHeight * 0.1f),
        size = Size(tailWidth, tailHeight),
        cornerRadius = CornerRadius(tailHeight * 0.3f, tailHeight * 0.3f),
    )
    drawCircle(
        color = innerColor,
        radius = bubbleHeight * 0.08f,
        center = Offset(size.width * 0.45f, bubbleTopLeft.y + bubbleHeight * 0.4f),
    )
    drawCircle(
        color = innerColor,
        radius = bubbleHeight * 0.08f,
        center = Offset(size.width * 0.55f, bubbleTopLeft.y + bubbleHeight * 0.4f),
    )
    drawRoundRect(
        color = innerColor,
        topLeft = Offset(size.width * 0.45f, bubbleTopLeft.y + bubbleHeight * 0.6f),
        size = Size(bubbleWidth * 0.3f, bubbleHeight * 0.12f),
        cornerRadius = CornerRadius(bubbleHeight * 0.06f, bubbleHeight * 0.06f),
    )
}

private sealed interface AppScreen {
    data object Home : AppScreen
    data object SkillSelection : AppScreen
    data class SkillDetail(val skillId: String) : AppScreen
}

private data class SkillCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: SkillGlyph,
    val widgets: List<SkillWidgetTemplate>,
)

private data class SkillWidgetTemplate(
    val id: String,
    val name: String,
    val description: String,
    val inputLabel: String,
    val placeholder: String,
    val kind: SkillWidgetKind,
)

private data class SkillWidgetInstance(
    val template: SkillWidgetTemplate,
    val value: String,
)

private data class TrackedSkill(
    val category: SkillCategory,
    val widgets: List<SkillWidgetInstance>,
)

private enum class SkillGlyph {
    Fitness,
    Guitar,
    Language,
}

private enum class SkillWidgetKind {
    Weight,
    Numeric,
    Text,
}
