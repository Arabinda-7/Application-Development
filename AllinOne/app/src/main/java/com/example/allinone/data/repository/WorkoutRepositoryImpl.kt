package com.example.allinone.data.repository

import com.example.allinone.data.datasource.WorkoutLocalDataSource
import com.example.allinone.data.mapper.WorkoutMapper
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.repository.WorkoutSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val localDataSource: WorkoutLocalDataSource
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return localDataSource.getAllWorkouts().map { entities ->
            entities.map { WorkoutMapper.toDomain(it) }
        }
    }

    override suspend fun insertWorkout(workout: Workout) {
        localDataSource.insertWorkout(WorkoutMapper.toEntity(workout))
    }

    override suspend fun updateWorkout(workout: Workout) {
        localDataSource.insertWorkout(WorkoutMapper.toEntity(workout))
    }

    override suspend fun deleteWorkout(workout: Workout) {
        localDataSource.deleteWorkout(WorkoutMapper.toEntity(workout))
    }

    override suspend fun syncAll(workouts: List<Workout>) {
        val entities = workouts.map { WorkoutMapper.toEntity(it) }
        localDataSource.insertAllWorkouts(entities)
        localDataSource.deleteOthers(entities.map { it.timestamp })
    }

    override fun getWorkoutSettings(): Flow<WorkoutSettings> = localDataSource.settings

    override suspend fun updateSettings(settings: WorkoutSettings) {
        localDataSource.updateSettings(settings)
    }
}
