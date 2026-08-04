package com.example.allinone.feature.idea.domain

import javax.inject.Inject

/**
 * IdeaValidator: Pure domain validator for Idea form inputs.
 */
class IdeaValidator @Inject constructor() {

    fun validateTitle(title: String): ValidationResult {
        return if (title.trim().isBlank()) {
            ValidationResult.Error("Title cannot be empty")
        } else {
            ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
