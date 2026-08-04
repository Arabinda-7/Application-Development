package com.example.allinone.feature.workout.domain

import com.example.allinone.data.model.Workout
import com.example.allinone.feature.workout.data.WorkoutRepository
import javax.inject.Inject

/**
 * SaveWorkoutUseCase: Business logic use case for validating and saving workouts.
 */
class SaveWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val validator: WorkoutValidator
) {
    suspend operator fun invoke(
        existingId: Long,
        name: String,
        mode: String,
        frequency: String,
        color: Int,
        iconResId: Int,
        targetInput: String,
        targetSetsInput: String,
        repsPerSetInput: String,
        targetTimerInput: String,
        repeatDays: List<Int>,
        muscleGroups: List<String>
    ): Result<Workout> {
        val validation = validator.validate(
            name, mode, targetInput, targetSetsInput, repsPerSetInput, targetTimerInput, repeatDays
        )

        if (validation is WorkoutValidator.ValidationResult.Error) {
            return Result.failure(IllegalArgumentException(validation.message))
        }

        val targetValue = when (mode) {
            "Sets" -> targetSetsInput.toIntOrNull() ?: 10
            "Timer" -> targetTimerInput.toIntOrNull() ?: 60
            else -> targetInput.toIntOrNull() ?: 50
        }

        val repsPerSetValue = if (mode == "Sets") repsPerSetInput.toIntOrNull() ?: 10 else 0
        val timestamp = if (existingId != -1L) existingId else System.currentTimeMillis()

        val existingWorkout = if (existingId != -1L) repository.getWorkoutById(existingId) else null
        val isCompleted = existingWorkout?.isCompleted ?: false

        val workout = Workout(
            name = name.trim(),
            isCompleted = isCompleted,
            trackingMode = mode,
            frequency = frequency,
            color = color,
            iconResId = iconResId,
            target = targetValue,
            repsPerSet = repsPerSetValue,
            repeatDays = repeatDays.toMutableList(),
            muscleGroups = muscleGroups.toMutableList(),
            timestamp = timestamp
        )

        repository.saveWorkout(workout)
        return Result.success(workout)
    }
}
