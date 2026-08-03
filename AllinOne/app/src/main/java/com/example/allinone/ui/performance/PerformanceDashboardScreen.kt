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
    initialFilter: PerformanceFilterType? = null,
    viewModel: PerformanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(selectedDate, currentMonth, performanceData, trendData, habits, isWorkoutContext, initialFilter) {
        viewModel.initializeData(
            selectedDate = selectedDate,
            currentMonth = currentMonth,
            performanceData = performanceData,
            trendData = trendData,
            habits = habits,
            isWorkoutContext = isWorkoutContext,
            forcedFilter = initialFilter
        )
    }
    
    LaunchedEffect(currentMood, overrideColor) {
        viewModel.updateMood(currentMood, overrideColor)
    }

    val currentIsWorkoutContext = state.primaryFilter == PerformanceFilterType.WORKOUTS

    val animatedMoodColor by animateColorAsState(
        targetValue = state.moodColorTarget,
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
                    primaryFilter = state.primaryFilter,
                    animatedMoodColor = animatedMoodColor,
                    habits = habits,
                    selectedHabitName = state.selectedHabitName,
                    onFilterSelected = { viewModel.setPrimaryFilter(it) },
                    onHabitSelected = { 
                        viewModel.setSelectedHabit(it)
                        onHabitSelected(it)
                    },
                    currentMonth = state.currentMonth
                )
            }

            // Month History / Calendar (Moved up after title)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PerformanceCalendarSection(
                        currentMonth = state.currentMonth,
                        selectedDate = state.selectedDate,
                        themeColor = animatedMoodColor,
                        onDateSelected = onDateSelected,
                        onMonthChanged = { 
                            viewModel.updateCurrentMonth(it)
                            onMonthChanged(it)
                        }
                    )
                }
            }

            if (showPerformanceCard) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PerformanceSummaryContainer(
                            performanceData = state.performanceData,
                            animatedMoodColor = animatedMoodColor,
                            primaryFilter = state.primaryFilter,
                            isWorkoutContext = currentIsWorkoutContext,
                            onShowPicker = onShowPicker,
                            currentStreak = state.currentStreak,
                            longestStreak = state.longestStreak
                        )
                    }
                }
            }

            if (showTrendCard) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PerformanceCard(title = "TREND ANALYSIS") {
                            TrendChart(
                                data = state.trendData,
                                themeColor = animatedMoodColor,
                                filterType = state.primaryFilter
                            )
                        }
                    }
                }
            }

            // Consistency Heatmap
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PerformanceHeatmapSection(
                        heatmapData = state.heatmapData,
                        animatedMoodColor = animatedMoodColor
                    )
                }
            }

            // Advanced Analytics Sections
            if (state.primaryFilter == PerformanceFilterType.HABITS) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PerformanceHabitAnalyticsSection(
                            stabilityScore = state.stabilityScore,
                            resilienceScore = state.resilienceScore,
                            momentumData = state.monthlyMomentum,
                            milestone = state.milestoneProgress,
                            temporalDensity = state.temporalDensity,
                            correlations = state.correlations,
                            animatedMoodColor = animatedMoodColor
                        )
                    }
                }
            } else if (state.primaryFilter == PerformanceFilterType.WORKOUTS) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PerformanceWorkoutAnalyticsSection(
                            volumeData = state.volumeData,
                            diversityData = state.diversityData,
                            intensityData = state.intensityData,
                            muscleFocus = state.muscleFocusData,
                            muscleDistribution = state.muscleDistribution,
                            recoveryStatus = state.recoveryStatus,
                            acwrData = state.acwrData,
                            animatedMoodColor = animatedMoodColor
                        )
                    }
                }
            }
        }
    }
}
