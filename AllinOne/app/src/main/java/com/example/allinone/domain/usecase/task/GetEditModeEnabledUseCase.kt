package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class GetEditModeEnabledUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Boolean = repository.getEditModeEnabled()
}
