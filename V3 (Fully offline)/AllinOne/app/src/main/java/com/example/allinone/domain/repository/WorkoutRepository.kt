package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import com.example.allinone.data.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    suspend fun insertWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    suspend fun syncAll(workouts: List<Workout>)
    
    // Settings & Shared State
    fun getWorkoutSettings(): Flow<WorkoutSettings>
    suspend fun updateSettings(settings: WorkoutSettings)
}

@Serializable
data class WorkoutSettings(
    val muscleGroups: List<String> = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body"),
    val filterType: String = "TIME",
    val autoRestTimer: Boolean = false,
    val weightUnit: String = "Kg",
    val defaultMode: String = "Reps",
    val restDuration: Int = 60,
    val showCompleted: Boolean = true,
    val globalWorkoutColor: Int = -1,
    val workoutAddThemeColor: Int = -1,
    val globalWorkoutIcon: Int = -1
)
