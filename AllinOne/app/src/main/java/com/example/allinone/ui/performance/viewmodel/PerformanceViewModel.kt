package com.example.allinone.ui.performance.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.allinone.DataManager
import com.example.allinone.DayHistory
import com.example.allinone.data.model.Habit
import com.example.allinone.ui.performance.state.PerformanceFilterType
import com.example.allinone.ui.performance.state.PerformanceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * PerformanceViewModel: Manages state and calculations for the Performance Dashboard.
 */
@HiltViewModel
class PerformanceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceState())
    val uiState: StateFlow<PerformanceState> = _uiState.asStateFlow()

    fun setPrimaryFilter(filter: PerformanceFilterType) {
        _uiState.update { it.copy(primaryFilter = filter) }
        recalculateState()
    }

    fun setSelectedHabit(habitName: String?) {
        _uiState.update { it.copy(selectedHabitName = habitName) }
        recalculateState()
    }

    fun updateSelectedDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        recalculateState()
    }

    fun updateCurrentMonth(month: Calendar) {
        _uiState.update { it.copy(currentMonth = month) }
        recalculateState()
    }

    fun updateMood(mood: String?, overrideColor: Color? = null) {
        val targetColor = calculateMoodColor(mood, overrideColor, _uiState.value.primaryFilter)
        _uiState.update { it.copy(currentMood = mood, moodColorTarget = targetColor) }
    }

    fun initializeData(
        selectedDate: String,
        currentMonth: Calendar,
        performanceData: DayHistory,
        trendData: List<Pair<Int, Int>>,
        habits: List<Habit>,
        isWorkoutContext: Boolean
    ) {
        val initialFilter = if (isWorkoutContext) PerformanceFilterType.WORKOUTS else PerformanceFilterType.OVERALL
        _uiState.update {
            it.copy(
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                performanceData = performanceData,
                trendData = trendData,
                habits = habits,
                isWorkoutContext = isWorkoutContext,
                primaryFilter = initialFilter
            )
        }
        recalculateState()
    }

    private fun recalculateState() {
        val state = _uiState.value
        val currentSelectedHabit = if (state.primaryFilter == PerformanceFilterType.HABITS) state.selectedHabitName else null

        val filteredPerformance = when (state.primaryFilter) {
            PerformanceFilterType.OVERALL -> state.performanceData
            PerformanceFilterType.HABITS -> {
                if (currentSelectedHabit == null) {
                    val raw = DataManager.calculateDayHistory(state.selectedDate)
                    DayHistory(raw.habitsCompleted, raw.totalHabits, 0, 0, null)
                } else {
                    val habit = DataManager.habits.find { it.name == currentSelectedHabit }
                    val isCompleted = habit?.completedDates?.contains(state.selectedDate) == true
                    DayHistory(if (isCompleted) 1 else 0, 1, 0, 0, null)
                }
            }
            PerformanceFilterType.WORKOUTS -> {
                val raw = DataManager.calculateDayHistory(state.selectedDate)
                DayHistory(0, 0, raw.workoutsCompleted, raw.totalWorkouts, raw.workoutDetails)
            }
        }

        val filteredTrend = when (state.primaryFilter) {
            PerformanceFilterType.OVERALL -> state.trendData
            PerformanceFilterType.HABITS -> {
                if (currentSelectedHabit == null) {
                    DataManager.getLastSevenDaysDetailedProgress().mapIndexed { idx, pair -> Pair(idx, pair.second) }
                } else {
                    val habit = DataManager.habits.find { it.name == currentSelectedHabit }
                    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    (0..6).map { i ->
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -i)
                        val date = sdf.format(cal.time)
                        val progress = if (habit?.completedDates?.contains(date) == true) 100 else 0
                        Pair(i, progress)
                    }.reversed()
                }
            }
            PerformanceFilterType.WORKOUTS -> {
                state.trendData
            }
        }

        _uiState.update {
            it.copy(
                performanceData = filteredPerformance,
                trendData = filteredTrend
            )
        }
    }

    private fun calculateMoodColor(mood: String?, overrideColor: Color?, filter: PerformanceFilterType): Color {
        if (overrideColor != null && filter != PerformanceFilterType.OVERALL) return overrideColor
        return when (mood) {
            "🔥" -> Color(0xFFFFB800)
            "⚡" -> Color(0xFF2EC4B6)
            "🧘" -> Color(0xFF673AB7)
            "💼" -> Color(0xFF1A73E8)
            "😴" -> Color(0xFF9E9E9E)
            "🧠" -> Color(0xFF3F51B5)
            else -> {
                if (filter == PerformanceFilterType.WORKOUTS) Color(0xFFFFB800)
                else if (filter == PerformanceFilterType.HABITS) Color(0xFFFF7A59)
                else Color(0xFF1A73E8)
            }
        }
    }
}
