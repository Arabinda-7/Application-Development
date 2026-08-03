package com.example.allinone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "global_tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 1,
    val category: String = "General",
    val section: String = "Anytime",
    val subtasksJson: String = "[]",
    val iconRes: String = "",
    val color: Int = -1,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
