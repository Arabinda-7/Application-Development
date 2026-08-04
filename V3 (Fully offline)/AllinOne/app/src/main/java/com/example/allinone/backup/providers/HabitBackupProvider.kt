package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AppHabitDao
import com.example.allinone.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class HabitBackupProvider @Inject constructor(
    private val habitDao: AppHabitDao,
    private val habitRepository: HabitRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "habits_module"

    override suspend fun exportData(): JsonElement {
        val habits = habitDao.getAllHabitsSync()
        val settings = habitRepository.getHabitSettings().first()
        
        return buildJsonObject {
            put("entities", json.encodeToJsonElement(habits))
            put("settings", json.encodeToJsonElement(settings))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["entities"]?.let {
            val entities: List<com.example.allinone.data.database.HabitEntity> = json.decodeFromJsonElement(it)
            habitDao.deleteAll()
            habitDao.insertAllHabits(entities)
        }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.HabitSettings = json.decodeFromJsonElement(it)
            habitRepository.updateSettings(settings)
        }
    }
}
