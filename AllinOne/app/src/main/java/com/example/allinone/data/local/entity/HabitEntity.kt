package com.example.allinone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDaysPerWeek: Int = 7,
    val completedDatesJson: String = "[]",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val section: String = "Anytime",
    val category: String = "General",
    val iconRes: String = "",
    val color: Int = -1,
    val createdAt: Long = System.currentTimeMillis()
)
