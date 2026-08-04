package com.example.allinone.domain.usecase.task

import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        val updatedTask = task.copy(
            isCompleted = !task.isCompleted,
            completedTimestamp = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        repository.updateTask(updatedTask)
    }
}
