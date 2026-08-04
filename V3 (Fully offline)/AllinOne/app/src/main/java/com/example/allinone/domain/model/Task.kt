package com.example.allinone.domain.model

data class Subtask(
    val title: String,
    val isCompleted: Boolean = false
)

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 1,
    val category: String = "General",
    val section: String = "Anytime",
    val subtasks: List<Subtask> = emptyList(),
    val iconRes: String = "",
    val color: Int = -1,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
