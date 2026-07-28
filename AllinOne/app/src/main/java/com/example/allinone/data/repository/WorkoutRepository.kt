package com.example.allinone.data.repository

import com.example.allinone.Workout
import com.example.allinone.data.database.AppWorkoutDao
import com.example.allinone.data.database.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(private val dao: AppWorkoutDao) {

    fun getAllWorkouts(): Flow<List<Workout>> {
        return dao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertWorkout(workout: Workout) {
        dao.insertWorkout(workout.toEntity())
    }

    suspend fun insertAllWorkouts(workouts: List<Workout>) {
        dao.insertAllWorkouts(workouts.map { it.toEntity() })
    }

    suspend fun deleteWorkout(workout: Workout) {
        dao.deleteWorkout(workout.toEntity())
    }

    private fun WorkoutEntity.toDomain() = Workout(
        name = name,
        isCompleted = isCompleted,
        trackingMode = trackingMode,
        target = target,
        repsPerSet = repsPerSet,
        progress = progress,
        frequency = frequency,
        isDayOff = isDayOff,
        color = color,
        iconResId = iconResId,
        muscleGroups = muscleGroups,
        repeatType = repeatType,
        repeatDays = repeatDays,
        repeatCount = repeatCount,
        timestamp = timestamp,
        completedDates = completedDates.toMutableList(),
        dailyProgress = dailyProgress.toMutableMap()
    )

    private fun Workout.toEntity() = WorkoutEntity(
        timestamp = timestamp,
        name = name,
        isCompleted = isCompleted,
        trackingMode = trackingMode,
        target = target,
        repsPerSet = repsPerSet,
        progress = progress,
        frequency = frequency,
        isDayOff = isDayOff,
        color = color,
        iconResId = iconResId,
        muscleGroups = muscleGroups,
        repeatType = repeatType,
        repeatDays = repeatDays,
        repeatCount = repeatCount,
        completedDates = completedDates,
        dailyProgress = dailyProgress
    )
}
