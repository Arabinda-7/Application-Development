package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AppWorkoutDao
import com.example.allinone.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class WorkoutBackupProvider @Inject constructor(
    private val workoutDao: AppWorkoutDao,
    private val workoutRepository: WorkoutRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "workouts_module"

    override suspend fun exportData(): JsonElement {
        val workouts = workoutDao.getAllWorkoutsSync()
        val settings = workoutRepository.getWorkoutSettings().first()
        
        return buildJsonObject {
            put("entities", json.encodeToJsonElement(workouts))
            put("settings", json.encodeToJsonElement(settings))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["entities"]?.let {
            val entities: List<com.example.allinone.data.database.WorkoutEntity> = json.decodeFromJsonElement(it)
            workoutDao.deleteAll()
            workoutDao.insertAllWorkouts(entities)
        }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.WorkoutSettings = json.decodeFromJsonElement(it)
            workoutRepository.updateSettings(settings)
        }
    }
}
