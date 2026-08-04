package com.example.allinone.feature.workout.domain

import javax.inject.Inject

/**
 * WorkoutValidator: Pure domain validator for workout configuration inputs.
 */
class WorkoutValidator @Inject constructor() {

    fun validate(
        name: String,
        mode: String,
        targetInput: String,
        targetSetsInput: String,
        repsPerSetInput: String,
        targetTimerInput: String,
        repeatDays: List<Int>
    ): ValidationResult {
        if (name.trim().isBlank()) {
            return ValidationResult.Error("Workout name is required")
        }

        if (repeatDays.isEmpty()) {
            return ValidationResult.Error("Select at least one day")
        }

        val targetValid = when (mode) {
            "Sets" -> (targetSetsInput.toIntOrNull() ?: 0) > 0 && (repsPerSetInput.toIntOrNull() ?: 0) > 0
            "Timer" -> (targetTimerInput.toIntOrNull() ?: 0) > 0
            else -> (targetInput.toIntOrNull() ?: 0) > 0
        }

        if (!targetValid) {
            return ValidationResult.Error("Valid goal target is required")
        }

        return ValidationResult.Success
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
