package com.example.allinone.domain.usecase.task

import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}
