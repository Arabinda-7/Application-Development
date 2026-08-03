package com.example.allinone.domain.usecase.workout

import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.repository.WorkoutSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutSettingsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<WorkoutSettings> = repository.getWorkoutSettings()
}
