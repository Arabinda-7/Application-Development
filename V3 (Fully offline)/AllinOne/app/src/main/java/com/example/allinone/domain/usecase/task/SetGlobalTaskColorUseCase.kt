package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetGlobalTaskColorUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(color: Int) = repository.setGlobalTaskColor(color)
}
