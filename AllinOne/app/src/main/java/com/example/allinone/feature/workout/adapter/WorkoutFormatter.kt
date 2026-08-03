package com.example.allinone.feature.workout.adapter

import com.example.allinone.core.utils.UIUtils

/**
 * WorkoutFormatter: Formats exercise targets, set counts, and timer durations.
 */
object WorkoutFormatter {

    fun formatTitle(title: String): String {
        return UIUtils.formatTitleCase(title)
    }

    fun formatTargetText(target: Int, mode: String, repsPerSet: Int): String {
        return when (mode) {
            "Sets" -> "$target sets × $repsPerSet reps"
            "Timer" -> "${target / 60}m ${target % 60}s"
            else -> "$target reps"
        }
    }

    fun formatProgressText(progress: Int, target: Int, mode: String): String {
        return when (mode) {
            "Sets" -> "$progress / $target sets"
            "Timer" -> "${progress / 60}m / ${target / 60}m"
            else -> "$progress / $target reps"
        }
    }
}
