package com.example.allinone.domain.model

data class Habit(
    val id: String,
    val title: String,
    val targetDaysPerWeek: Int = 7,
    val completedDates: List<String> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val section: String = "Anytime",
    val category: String = "General",
    val iconRes: String = "",
    val color: Int = -1,
    val createdAt: Long = System.currentTimeMillis()
)
