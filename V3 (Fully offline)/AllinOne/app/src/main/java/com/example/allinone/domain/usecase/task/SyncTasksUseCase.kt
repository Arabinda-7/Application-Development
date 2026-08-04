package com.example.allinone.domain.usecase.task

import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SyncTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(tasks: List<Task>) = repository.syncTasks(tasks)
}
