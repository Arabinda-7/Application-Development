package com.example.allinone.feature.habit.utils

import com.example.allinone.core.utils.UIUtils

/**
 * HabitFormatter: Pure formatting rules for habit titles, streak counters, and target displays.
 */
object HabitFormatter {

    fun formatTitle(title: String): String {
        return UIUtils.formatTitleCase(title)
    }

    fun formatStreak(streak: Int): String {
        return if (streak > 0) "🔥 $streak days" else "0 days"
    }
}
