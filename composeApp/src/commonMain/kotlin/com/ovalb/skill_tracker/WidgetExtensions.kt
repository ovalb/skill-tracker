package com.ovalb.skill_tracker

import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun WidgetState.Weight.addEntry(weightKg: Double, label: String = currentDateLabel()): WidgetState.Weight {
    val filtered = entries.filterNot { it.dateLabel == label }
    val newEntries = (filtered + WeightEntry(label, weightKg)).takeLast(20)
    return copy(entries = newEntries)
}

fun WidgetState.Weight.updateHeight(heightCm: Double?): WidgetState.Weight = copy(heightCm = heightCm)

fun WidgetState.Weight.latestWeight(): Double? = entries.lastOrNull()?.kilograms

fun formatWeight(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

fun currentDateLabel(): String {
    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val prefix = month.take(3)
    return "$prefix ${date.dayOfMonth.toString().padStart(2, '0')}"
}
