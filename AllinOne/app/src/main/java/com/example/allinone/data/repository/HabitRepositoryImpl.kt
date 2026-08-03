package com.example.allinone.data.repository

import com.example.allinone.data.datasource.HabitLocalDataSource
import com.example.allinone.data.database.HabitEntity
import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.repository.HabitSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val localDataSource: HabitLocalDataSource
) : HabitRepository {

    private fun HabitEntity.toModel(): Habit = Habit(
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

    private fun Habit.toEntity(): HabitEntity = HabitEntity(
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

    override fun getAllHabits(): Flow<List<Habit>> {
        return localDataSource.getAllHabits().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun insertHabit(habit: Habit) {
        localDataSource.insertHabit(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        localDataSource.insertHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) {
        localDataSource.deleteHabit(habit.toEntity())
    }

    override suspend fun syncAll(habits: List<Habit>) {
        val entities = habits.map { it.toEntity() }
        localDataSource.insertAllHabits(entities)
        localDataSource.deleteOthers(entities.map { it.timestamp })
    }

    override fun getHabitSettings(): Flow<HabitSettings> = localDataSource.settings

    override suspend fun updateSettings(settings: HabitSettings) {
        localDataSource.updateSettings(settings)
    }
}
