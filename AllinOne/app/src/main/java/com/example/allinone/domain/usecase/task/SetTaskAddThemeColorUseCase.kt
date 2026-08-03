package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetTaskAddThemeColorUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(color: Int) = repository.setTaskAddThemeColor(color)
}
