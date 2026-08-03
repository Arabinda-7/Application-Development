package com.example.allinone.feature.workout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.R
import com.example.allinone.data.model.Workout
import com.example.allinone.feature.workout.data.WorkoutRepository
import com.example.allinone.feature.workout.domain.SaveWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AddWorkoutViewModel: Retains screen form state, user selections, and validation events for AddWorkoutActivity.
 */
@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val saveWorkoutUseCase: SaveWorkoutUseCase
) : ViewModel() {

    private val _selectedMode = MutableStateFlow("Reps")
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    private val _selectedFrequency = MutableStateFlow("Anytime")
    val selectedFrequency: StateFlow<String> = _selectedFrequency.asStateFlow()

    private val _selectedColor = MutableStateFlow(-1)
    val selectedColor: StateFlow<Int> = _selectedColor.asStateFlow()

    private val _selectedIcon = MutableStateFlow(R.drawable.icons8_exercise_100)
    val selectedIcon: StateFlow<Int> = _selectedIcon.asStateFlow()

    private val _repeatDays = MutableStateFlow(listOf(0, 1, 2, 3, 4, 5, 6))
    val repeatDays: StateFlow<List<Int>> = _repeatDays.asStateFlow()

    private val _muscleGroups = MutableStateFlow(listOf("General"))
    val muscleGroups: StateFlow<List<String>> = _muscleGroups.asStateFlow()

    private val _existingWorkout = MutableStateFlow<Workout?>(null)
    val existingWorkout: StateFlow<Workout?> = _existingWorkout.asStateFlow()

    var workoutId: Long = -1L
        private set

    fun initialize(id: Long) {
        workoutId = id
        if (workoutId != -1L) {
            val workout = repository.getWorkoutById(workoutId)
            _existingWorkout.value = workout
            workout?.let {
                _selectedMode.value = it.trackingMode
                _selectedFrequency.value = it.frequency
                _selectedColor.value = it.color
                _selectedIcon.value = it.iconResId
                _repeatDays.value = it.repeatDays.toList()
                _muscleGroups.value = it.muscleGroups.toList()
            }
        }
    }

    fun setMode(mode: String) { _selectedMode.value = mode }
    fun setFrequency(freq: String) { _selectedFrequency.value = freq }
    fun setColor(color: Int) { _selectedColor.value = color }
    fun setIcon(iconResId: Int) { _selectedIcon.value = iconResId }
    fun setRepeatDays(days: List<Int>) { _repeatDays.value = days }
    fun setMuscleGroups(groups: List<String>) { _muscleGroups.value = groups }

    fun saveWorkout(
        name: String,
        targetInput: String,
        targetSetsInput: String,
        repsPerSetInput: String,
        targetTimerInput: String,
        onSuccess: (Workout) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = saveWorkoutUseCase(
                existingId = workoutId,
                name = name,
                mode = _selectedMode.value,
                frequency = _selectedFrequency.value,
                color = _selectedColor.value,
                iconResId = _selectedIcon.value,
                targetInput = targetInput,
                targetSetsInput = targetSetsInput,
                repsPerSetInput = repsPerSetInput,
                targetTimerInput = targetTimerInput,
                repeatDays = _repeatDays.value,
                muscleGroups = _muscleGroups.value
            )
            result.onSuccess(onSuccess).onFailure { onError(it.message ?: "Failed to save workout") }
        }
    }
}
