package com.example.allinone.feature.workout.data

import com.example.allinone.DataManager
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository as BaseWorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkoutRepository: Data access layer for workouts and training plans.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val baseRepository: BaseWorkoutRepository
) {
    fun getWorkoutById(id: Long): Workout? {
        return DataManager.workouts.find { it.timestamp == id }
    }

    suspend fun saveWorkout(workout: Workout) {
        baseRepository.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        baseRepository.deleteWorkout(workout)
    }
}
