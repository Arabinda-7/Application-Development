package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class GetGlobalTaskColorUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Int = repository.getGlobalTaskColor()
}
