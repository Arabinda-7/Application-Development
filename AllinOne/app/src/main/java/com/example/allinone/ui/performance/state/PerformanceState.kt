package com.example.allinone.ui.performance.state

import androidx.compose.ui.graphics.Color
import com.example.allinone.DayHistory
import com.example.allinone.data.model.Habit
import java.util.Calendar

enum class PerformanceFilterType {
    OVERALL, HABITS, WORKOUTS
}

/**
 * PerformanceState: Immutable state holder for the Performance Dashboard screen.
 */
data class PerformanceState(
    val primaryFilter: PerformanceFilterType = PerformanceFilterType.OVERALL,
    val selectedDate: String = "",
    val currentMonth: Calendar = Calendar.getInstance(),
    val currentMood: String? = null,
    val selectedHabitName: String? = null,
    val isWorkoutContext: Boolean = false,
    val showPerformanceCard: Boolean = true,
    val showTrendCard: Boolean = true,
    val showBackgroundAura: Boolean = true,
    val showFilterSelector: Boolean = true,
    val performanceData: DayHistory = DayHistory(0, 0, 0, 0),
    val trendData: List<Pair<Int, Int>> = emptyList(),
    val heatmapData: List<Int> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val moodColorTarget: Color = Color(0xFF1A73E8)
)
