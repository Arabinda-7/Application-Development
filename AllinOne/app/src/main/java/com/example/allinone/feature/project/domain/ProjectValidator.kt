package com.example.allinone.feature.project.domain

import javax.inject.Inject

/**
 * ProjectValidator: Pure domain validator for Project form fields.
 */
class ProjectValidator @Inject constructor() {

    fun validateTitle(title: String): ValidationResult {
        return if (title.trim().isBlank()) {
            ValidationResult.Error("Project title cannot be empty")
        } else {
            ValidationResult.Success
        }
    }

    fun validateProgress(progress: Int): ValidationResult {
        return if (progress in 0..100) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Progress must be between 0 and 100")
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
