package com.ovalb.skill_tracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<AppScreen>(AppScreen.SkillSelection) }

        when (val current = screen) {
            AppScreen.SkillSelection -> SkillSelectionScreen(
                onStart = { selectedSkill ->
                    screen = AppScreen.NextStep(selectedSkill)
                }
            )

            is AppScreen.NextStep -> PlaceholderScreen()
        }
    }
}

@Composable
private fun SkillSelectionScreen(
    onStart: (SkillCategory) -> Unit,
) {
    val allSkills = remember {
        listOf(
            SkillCategory(
                id = "fitness",
                name = "Functional Fitness",
                description = "Strength, mobility, and endurance routines",
                icon = SkillGlyph.Fitness,
            ),
            SkillCategory(
                id = "music",
                name = "Rhythm Guitar",
                description = "Chord progressions, strumming, and timing",
                icon = SkillGlyph.Guitar,
            ),
            SkillCategory(
                id = "language",
                name = "Conversational Spanish",
                description = "Everyday phrases and listening practice",
                icon = SkillGlyph.Language,
            ),
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedSkill by remember { mutableStateOf<SkillCategory?>(null) }

    val filteredSkills = remember(query, allSkills) {
        if (query.isBlank()) allSkills
        else allSkills.filter { skill ->
            skill.name.contains(query, ignoreCase = true) ||
                skill.description.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
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
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(filteredSkills, key = { it.id }) { skill ->
                SkillCard(
                    skill = skill,
                    isSelected = skill == selectedSkill,
                    onSelect = { selectedSkill = skill },
                )
            }
        }

        AnimatedVisibility(visible = selectedSkill != null) {
            Button(
                onClick = { selectedSkill?.let(onStart) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start")
            }
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

@Composable
private fun PlaceholderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private sealed interface AppScreen {
    data object SkillSelection : AppScreen
    data class NextStep(val selection: SkillCategory) : AppScreen
}

private data class SkillCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: SkillGlyph,
)

private enum class SkillGlyph {
    Fitness,
    Guitar,
    Language,
}
