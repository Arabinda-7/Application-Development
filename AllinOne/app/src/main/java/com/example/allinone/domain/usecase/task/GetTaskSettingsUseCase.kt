package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import com.example.allinone.domain.repository.TaskSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskSettingsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<TaskSettings> = repository.getTaskSettings()
}
