package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import com.example.allinone.data.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun syncAll(habits: List<Habit>)
    
    // Settings & Shared State
    fun getHabitSettings(): Flow<HabitSettings>
    suspend fun updateSettings(settings: HabitSettings)
}

@Serializable
data class HabitSettings(
    val defaultTab: String = "TODAY",
    val vacationMode: Boolean = false,
    val sortOrder: String = "Time",
    val completionSound: Boolean = true,
    val completionHaptics: Boolean = true,
    val dayResetHour: Int = 0,
    val bulkMode: Boolean = false,
    val graceDaysAllowed: Int = 1,
    val showCompleted: Boolean = true,
    val globalHabitColor: Int = -1,
    val habitAddThemeColor: Int = -1,
    val globalHabitIcon: Int = -1
)
