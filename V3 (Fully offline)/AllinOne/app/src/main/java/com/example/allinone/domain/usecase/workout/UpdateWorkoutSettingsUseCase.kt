package com.example.allinone.domain.usecase.workout

import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.repository.WorkoutSettings
import javax.inject.Inject

class UpdateWorkoutSettingsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(settings: WorkoutSettings) = repository.updateSettings(settings)
}
