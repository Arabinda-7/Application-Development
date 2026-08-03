package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AppTaskDao
import com.example.allinone.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class TaskBackupProvider @Inject constructor(
    private val taskDao: AppTaskDao,
    private val taskRepository: TaskRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "tasks_module"

    override suspend fun exportData(): JsonElement {
        val tasks = taskDao.getAllTasksSync()
        val settings = taskRepository.getTaskSettings().first()
        
        return buildJsonObject {
            put("entities", json.encodeToJsonElement(tasks))
            put("settings", json.encodeToJsonElement(settings))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["entities"]?.let {
            val entities: List<com.example.allinone.data.database.GlobalTaskEntity> = json.decodeFromJsonElement(it)
            taskDao.deleteAll()
            taskDao.insertAllTasks(entities)
        }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.TaskSettings = json.decodeFromJsonElement(it)
            taskRepository.updateSettings(settings)
        }
    }
}
