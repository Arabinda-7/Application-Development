package com.example.allinone.data.datasource

import android.content.Context
import com.example.allinone.data.database.AppWorkoutDao
import com.example.allinone.data.database.WorkoutEntity
import com.example.allinone.domain.repository.WorkoutSettings
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class WorkoutLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutDao: AppWorkoutDao,
    private val gson: Gson
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<WorkoutSettings> = _settings.asStateFlow()

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insertWorkout(workout: WorkoutEntity) = workoutDao.insertWorkout(workout)
    
    suspend fun insertAllWorkouts(workouts: List<WorkoutEntity>) = workoutDao.insertAllWorkouts(workouts)

    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)
    
    suspend fun deleteOthers(ids: List<Long>) = workoutDao.deleteOthers(ids)

    fun updateSettings(newSettings: WorkoutSettings) {
        prefs.edit().apply {
            putString("workout_muscle_groups", gson.toJson(newSettings.muscleGroups))
            putString("workout_filter_type", newSettings.filterType)
            putBoolean("workout_auto_rest_timer", newSettings.autoRestTimer)
            putString("workout_weight_unit", newSettings.weightUnit)
            putString("workout_default_mode", newSettings.defaultMode)
            putInt("workout_rest_duration", newSettings.restDuration)
            putBoolean("show_workout_section", newSettings.showCompleted)
            putInt("global_workout_color", newSettings.globalWorkoutColor)
            putInt("workout_add_theme_color", newSettings.workoutAddThemeColor)
            putInt("global_workout_icon", newSettings.globalWorkoutIcon)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): WorkoutSettings {
        val muscleGroupsJson = prefs.getString("workout_muscle_groups", "[\"Chest\", \"Back\", \"Legs\", \"Shoulders\", \"Arms\", \"Cardio\", \"Full Body\"]")
        val muscleGroups: List<String> = try {
            gson.fromJson(muscleGroupsJson, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body") }

        return WorkoutSettings(
            muscleGroups = if (muscleGroups.isEmpty()) listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body") else muscleGroups,
            filterType = prefs.getString("workout_filter_type", "TIME") ?: "TIME",
            autoRestTimer = prefs.getBoolean("workout_auto_rest_timer", false),
            weightUnit = prefs.getString("workout_weight_unit", "Kg") ?: "Kg",
            defaultMode = prefs.getString("workout_default_mode", "Reps") ?: "Reps",
            restDuration = prefs.getInt("workout_rest_duration", 60),
            showCompleted = prefs.getBoolean("show_workout_section", true),
            globalWorkoutColor = prefs.getInt("global_workout_color", -1),
            workoutAddThemeColor = prefs.getInt("workout_add_theme_color", -1),
            globalWorkoutIcon = prefs.getInt("global_workout_icon", -1)
        )
    }
}
