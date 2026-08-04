package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetEditModeEnabledUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(enabled: Boolean) = repository.setEditModeEnabled(enabled)
}
