package com.example.allinone.ui.performance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.allinone.DayHistory
import com.example.allinone.data.model.Habit
import com.example.allinone.ui.performance.components.*
import com.example.allinone.ui.performance.state.PerformanceFilterType
import com.example.allinone.ui.performance.viewmodel.PerformanceViewModel
import java.util.Calendar

/**
 * PerformanceDashboardScreen: High-level screen composable for momentum logs and analytics.
 * Delegates layout components to PerformanceHeader, PerformanceCard, and PerformanceSections.
 */
@Composable
fun PerformanceDashboardScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    title: String? = null,
    onDateSelected: (String) -> Unit,
    selectedDate: String,
    currentMonth: Calendar,
    onMonthChanged: (Calendar) -> Unit,
    onShowPicker: () -> Unit,
    performanceData: DayHistory,
    trendData: List<Pair<Int, Int>>,
    currentMood: String? = null,
    overrideColor: Color? = null,
    isWorkoutContext: Boolean = false,
    showPerformanceCard: Boolean = true,
    showTrendCard: Boolean = true,
    showBackgroundAura: Boolean = true,
    showFilterSelector: Boolean = true,
    habits: List<Habit> = emptyList(),
    selectedHabitName: String? = null,
    onHabitSelected: (String?) -> Unit = {},
    onSaveNote: (String, String) -> Unit = { _, _ -> },
    viewModel: PerformanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var primaryFilter by remember(isWorkoutContext) {
        mutableStateOf(if (isWorkoutContext) PerformanceFilterType.WORKOUTS else if (selectedHabitName != null) PerformanceFilterType.HABITS else PerformanceFilterType.OVERALL)
    }

    val currentIsWorkoutContext = primaryFilter == PerformanceFilterType.WORKOUTS

    val moodColorTarget = remember(currentMood, overrideColor, primaryFilter) {
        if (overrideColor != null && primaryFilter != PerformanceFilterType.OVERALL) return@remember overrideColor
        when (currentMood) {
            "🔥" -> Color(0xFFFFB800)
            "⚡" -> Color(0xFF2EC4B6)
            "🧘" -> Color(0xFF673AB7)
            "💼" -> Color(0xFF1A73E8)
            "😴" -> Color(0xFF9E9E9E)
            "🧠" -> Color(0xFF3F51B5)
            else -> {
                if (primaryFilter == PerformanceFilterType.WORKOUTS) Color(0xFFFFB800)
                else if (primaryFilter == PerformanceFilterType.HABITS) Color(0xFFFF7A59)
                else Color(0xFF1A73E8)
            }
        }
    }

    val animatedMoodColor by animateColorAsState(
        targetValue = moodColorTarget,
        animationSpec = tween(durationMillis = 500),
        label = "MoodColorAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (showBackgroundAura) Modifier.background(Color.Black) else Modifier)
    ) {
        if (showBackgroundAura) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedMoodColor.copy(alpha = 0.45f),
                                animatedMoodColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = androidx.compose.ui.geometry.Offset(x = 500f, y = 0f),
                            radius = 1200f
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                PerformanceHeader(
                    title = title,
                    onBack = onBack,
                    showFilterSelector = showFilterSelector,
                    primaryFilter = primaryFilter,
                    animatedMoodColor = animatedMoodColor,
                    habits = habits,
                    selectedHabitName = selectedHabitName,
                    onFilterSelected = { primaryFilter = it },
                    onHabitSelected = onHabitSelected
                )
            }

            if (showPerformanceCard) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PerformanceSummaryContainer(
                            performanceData = performanceData,
                            animatedMoodColor = animatedMoodColor,
                            primaryFilter = primaryFilter,
                            isWorkoutContext = currentIsWorkoutContext,
                            onShowPicker = onShowPicker
                        )
                    }
                }
            }

            if (showTrendCard) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PerformanceCard(title = "TREND ANALYSIS") {
                            TrendChart(
                                data = trendData,
                                themeColor = animatedMoodColor,
                                isWorkoutContext = currentIsWorkoutContext
                            )
                        }
                    }
                }
            }
        }
    }
}
