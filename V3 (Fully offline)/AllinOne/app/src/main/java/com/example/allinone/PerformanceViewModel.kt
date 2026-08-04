package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.usecase.habit.GetHabitStatisticsUseCase
import com.example.allinone.domain.usecase.workout.GetWorkoutStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workouts: StateFlow<List<Workout>> = workoutRepository.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun updateCurrentMonth(calendar: Calendar) {
        _currentMonth.value = calendar
    }

    // Statistics methods that combine current data with use cases
    fun getHabitStreaks(habitName: String): Pair<Int, Int>? {
        val habit = habits.value.find { it.name == habitName } ?: return null
        return getHabitStatisticsUseCase.getStreaks(habit)
    }

    fun getWorkoutStreaks() = getWorkoutStatisticsUseCase.getStreaks(workouts.value)
    
    fun getHabitHeatmap(habitName: String?, calendar: Calendar): Map<Int, Int> {
        if (habitName == null) {
            // Overall habit heatmap (placeholder or composite logic)
            return emptyMap() 
        }
        val habit = habits.value.find { it.name == habitName } ?: return emptyMap()
        return getHabitStatisticsUseCase.getHeatmap(habit, calendar)
    }

    fun getWorkoutVolumeHeatmap(calendar: Calendar) = 
        // Need to implement volume weighted heatmap in use case or here
        emptyMap<Int, Int>()

    fun getHabitWeeklyCyclicalData(habitName: String?) = 
        getHabitStatisticsUseCase.getWeeklyCyclicalData(if (habitName != null) habits.value.filter { it.name == habitName } else habits.value)

    fun getHabitStabilityIndex(habitName: String?) = 
        getHabitStatisticsUseCase.getStabilityIndex(if (habitName != null) habits.value.filter { it.name == habitName } else habits.value)

    fun getHabitResilienceScore(habitName: String?) = 
        getHabitStatisticsUseCase.getResilienceScore(if (habitName != null) habits.value.filter { it.name == habitName } else habits.value)

    fun getHabitMonthlyMomentum(habitName: String?) = 
        getHabitStatisticsUseCase.getMonthlyMomentumHistory(if (habitName != null) habits.value.filter { it.name == habitName } else habits.value)

    fun getHabitMilestoneProgress(habitName: String?) = 
        getHabitStatisticsUseCase.getStreakMilestoneProgress(habits.value, habitName)

    fun getHabitTemporalDensity() = getHabitStatisticsUseCase.getTemporalDensityData(habits.value)
    
    fun getHabitCorrelations() = getHabitStatisticsUseCase.getHabitCorrelationMatrix(habits.value)

    fun getWorkoutMuscleDistribution() = getWorkoutStatisticsUseCase.getMuscleRecovery(workouts.value) // Placeholder distribution
    
    fun getWorkoutRecovery() = getWorkoutStatisticsUseCase.getMuscleRecovery(workouts.value)
    
    fun getWorkoutACWR() = getWorkoutStatisticsUseCase.getACWR(workouts.value)
    
    // ... other workout metrics
}
