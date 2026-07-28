package com.example.allinone.data.repository

import com.example.allinone.Habit
import com.example.allinone.data.database.AppHabitDao
import com.example.allinone.data.database.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val dao: AppHabitDao) {

    fun getAllHabits(): Flow<List<Habit>> {
        return dao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertHabit(habit: Habit) {
        dao.insertHabit(habit.toEntity())
    }

    suspend fun insertAllHabits(habits: List<Habit>) {
        dao.insertAllHabits(habits.map { it.toEntity() })
    }

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit.toEntity())
    }

    private fun HabitEntity.toDomain() = Habit(
        name = name,
        isCompleted = isCompleted,
        frequency = frequency,
        trackingMode = trackingMode,
        target = target,
        progress = progress,
        isDayOff = isDayOff,
        color = color,
        iconResId = iconResId,
        repeatType = repeatType,
        repeatDays = repeatDays,
        repeatCount = repeatCount,
        timestamp = timestamp,
        completedDates = completedDates.toMutableList(),
        dailyProgress = dailyProgress.toMutableMap(),
        reminderTime = reminderTime
    )

    private fun Habit.toEntity() = HabitEntity(
        timestamp = timestamp,
        name = name,
        isCompleted = isCompleted,
        frequency = frequency,
        trackingMode = trackingMode,
        target = target,
        progress = progress,
        isDayOff = isDayOff,
        color = color,
        iconResId = iconResId,
        repeatType = repeatType,
        repeatDays = repeatDays,
        repeatCount = repeatCount,
        completedDates = completedDates,
        dailyProgress = dailyProgress,
        reminderTime = reminderTime
    )
}
