package com.example.allinone.domain.usecase.task

import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        if (task.name.isBlank()) task.name = "Untitled Task"
        repository.addTask(task)
    }
}
