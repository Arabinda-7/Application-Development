package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskAddThemeColorUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Int = repository.getTaskAddThemeColor()
}
