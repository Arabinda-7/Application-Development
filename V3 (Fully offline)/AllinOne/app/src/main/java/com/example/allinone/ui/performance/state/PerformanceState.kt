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
    val moodColorTarget: Color = Color(0xFF1A73E8),
    
    // Advanced Analytics
    val volumeData: List<Float> = emptyList(),
    val diversityData: Map<String, Int> = emptyMap(),
    val intensityData: List<Int> = emptyList(),
    val muscleFocusData: Map<Int, List<String>> = emptyMap(),
    val muscleDistribution: Map<String, Int> = emptyMap(),
    val recoveryStatus: Map<String, Float> = emptyMap(),
    val acwrData: Pair<List<Float>, List<Float>> = Pair(emptyList(), emptyList()),
    val stabilityScore: Float = 0f,
    val resilienceScore: Float = 0f,
    val monthlyMomentum: List<Pair<String, Int>> = emptyList(),
    val milestoneProgress: Triple<Int, Int, Float> = Triple(0, 0, 0f),
    val temporalDensity: Map<Int, Map<String, Int>> = emptyMap(),
    val correlations: List<Triple<String, String, Double>> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)
