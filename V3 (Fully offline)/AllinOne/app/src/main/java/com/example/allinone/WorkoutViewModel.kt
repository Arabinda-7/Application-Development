package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.repository.WorkoutSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

import com.example.allinone.domain.usecase.user.AddActivityUseCase
import com.example.allinone.domain.usecase.user.AddXPUseCase

import com.example.allinone.domain.usecase.workout.GetWorkoutStatisticsUseCase

data class WorkoutStats(
    val streaks: Pair<Int, Int>,
    val totalFinished: Int,
    val workoutsThisMonth: Int,
    val completionRate: Int
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val addXPUseCase: AddXPUseCase,
    private val addActivityUseCase: AddActivityUseCase,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase
) : ViewModel() {
    var selectedTimeFilter by mutableStateOf("All")
    var selectedDateString by mutableStateOf(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    var currentTab by mutableStateOf("TODAY")
    var currentlySelectedHistoryDate by mutableStateOf(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    var currentGridCalendar by mutableStateOf(Calendar.getInstance())
    var currentlyTimingWorkoutPosition by mutableStateOf(-1)

    val workouts: StateFlow<List<Workout>> = workoutRepository.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutSettings: StateFlow<WorkoutSettings> = workoutRepository.getWorkoutSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutSettings())

    val stats: StateFlow<WorkoutStats> = workouts
        .map { list ->
            WorkoutStats(
                streaks = getWorkoutStatisticsUseCase.getStreaks(list),
                totalFinished = getWorkoutStatisticsUseCase.getTotalFinished(list),
                workoutsThisMonth = getWorkoutStatisticsUseCase.getWorkoutsThisMonth(list),
                completionRate = getWorkoutStatisticsUseCase.getGlobalCompletionRate(list)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutStats(Pair(0, 0), 0, 0, 0))

    fun updateSettings(newSettings: WorkoutSettings) {
        viewModelScope.launch {
            workoutRepository.updateSettings(newSettings)
        }
    }

    fun insertWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.insertWorkout(workout)
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.updateWorkout(workout)
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workout)
        }
    }

    fun addXP(amount: Int) {
        viewModelScope.launch {
            addXPUseCase(amount)
        }
    }

    fun addActivity(activity: String) {
        viewModelScope.launch {
            addActivityUseCase(activity)
        }
    }

    fun completeWorkoutWithTimer(position: Int) {
        if (position == -1 || position >= workouts.value.size) return
        val workout = workouts.value[position]
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val updatedWorkout = workout.copy(
            isCompleted = true,
            progress = workout.target,
            dailyProgress = workout.dailyProgress.toMutableMap().apply { put(today, 100) },
            completedDates = workout.completedDates.toMutableList().apply { 
                if (!contains(today)) add(today) 
            }
        )
        
        if (!workout.completedDates.contains(today)) {
            addActivity("Finished Workout: ${workout.name}")
            addXP(25)
        }
        
        updateWorkout(updatedWorkout)
    }

    fun getStreaks() = getWorkoutStatisticsUseCase.getStreaks(workouts.value)
    fun getTotalFinished() = getWorkoutStatisticsUseCase.getTotalFinished(workouts.value)
    fun getWorkoutsThisMonth() = getWorkoutStatisticsUseCase.getWorkoutsThisMonth(workouts.value)
    fun getGlobalCompletionRate() = getWorkoutStatisticsUseCase.getGlobalCompletionRate(workouts.value)
}
