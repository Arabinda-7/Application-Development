package com.example.allinone.backup

object BackupValidator {
    const val CURRENT_VERSION = 1

    fun validate(data: BackupData): ValidationResult {
        if (data.version <= 0) return ValidationResult.Invalid("Invalid version")
        if (data.version > CURRENT_VERSION) return ValidationResult.Invalid("Backup version too high. Please update the app.")
        
        return ValidationResult.Valid
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }
}
