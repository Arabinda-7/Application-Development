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

    fun getStreaks() = getWorkoutStatisticsUseCase.getStreaks(workouts.value)
    fun getTotalFinished() = getWorkoutStatisticsUseCase.getTotalFinished(workouts.value)
    fun getWorkoutsThisMonth() = getWorkoutStatisticsUseCase.getWorkoutsThisMonth(workouts.value)
    fun getGlobalCompletionRate() = getWorkoutStatisticsUseCase.getGlobalCompletionRate(workouts.value)
}
