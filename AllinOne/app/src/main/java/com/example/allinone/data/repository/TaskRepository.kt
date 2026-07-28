package com.example.allinone.data.repository

import com.example.allinone.Subtask
import com.example.allinone.Task
import com.example.allinone.data.database.AppTaskDao
import com.example.allinone.data.database.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val dao: AppTaskDao) {

    fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertTask(task: Task) {
        dao.insertTask(task.toEntity())
    }

    suspend fun insertAllTasks(tasks: List<Task>) {
        dao.insertAllTasks(tasks.map { it.toEntity() })
    }

    suspend fun deleteTask(task: Task) {
        dao.deleteTask(task.toEntity())
    }

    suspend fun clearCompletedTasks() {
        dao.clearCompletedTasks()
    }

    private fun TaskEntity.toDomain() = Task(
        name = name,
        isCompleted = isCompleted,
        color = color,
        timestamp = timestamp,
        priority = priority,
        reminderTime = reminderTime,
        category = category,
        section = section,
        isHidden = isHidden,
        subtasks = subtasks.toMutableList(),
        completedTimestamp = completedTimestamp
    )

    private fun Task.toEntity() = TaskEntity(
        timestamp = timestamp,
        name = name,
        isCompleted = isCompleted,
        color = color,
        priority = priority,
        reminderTime = reminderTime,
        category = category,
        section = section,
        isHidden = isHidden,
        subtasks = subtasks,
        completedTimestamp = completedTimestamp
    )
}
