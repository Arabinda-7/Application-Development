package com.example.allinone.data.mapper

import com.example.allinone.data.local.entity.TaskEntity
import com.example.allinone.domain.model.Task
import com.example.allinone.domain.model.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskMapper @Inject constructor(
    private val gson: Gson
) {

    fun toDomain(entity: TaskEntity): Task {
        val subtasksType = object : TypeToken<List<Subtask>>() {}.type
        val subtasks: List<Subtask> = try {
            gson.fromJson(entity.subtasksJson, subtasksType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Task(
            id = entity.id,
            title = entity.title,
            isCompleted = entity.isCompleted,
            priority = entity.priority,
            category = entity.category,
            section = entity.section,
            subtasks = subtasks,
            iconRes = entity.iconRes,
            color = entity.color,
            dueDate = entity.dueDate,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            title = domain.title,
            isCompleted = domain.isCompleted,
            priority = domain.priority,
            category = domain.category,
            section = domain.section,
            subtasksJson = gson.toJson(domain.subtasks),
            iconRes = domain.iconRes,
            color = domain.color,
            dueDate = domain.dueDate,
            createdAt = domain.createdAt
        )
    }

    fun toDomainList(entities: List<TaskEntity>): List<Task> = entities.map { toDomain(it) }
    fun toEntityList(domains: List<Task>): List<TaskEntity> = domains.map { toEntity(it) }
}
