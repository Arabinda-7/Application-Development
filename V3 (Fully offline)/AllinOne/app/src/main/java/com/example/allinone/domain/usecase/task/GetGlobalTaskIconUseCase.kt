package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class GetGlobalTaskIconUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Int = repository.getGlobalTaskIcon()
}
