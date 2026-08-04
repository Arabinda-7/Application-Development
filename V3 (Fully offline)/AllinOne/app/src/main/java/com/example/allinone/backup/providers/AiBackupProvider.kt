package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AiChatDao
import com.example.allinone.data.database.AssistantMemoryDao
import kotlinx.serialization.json.*
import javax.inject.Inject

class AiBackupProvider @Inject constructor(
    private val aiChatDao: AiChatDao,
    private val assistantMemoryDao: AssistantMemoryDao,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "ai_module"

    override suspend fun exportData(): JsonElement {
        return buildJsonObject {
            put("messages", json.encodeToJsonElement(aiChatDao.getAllMessagesSync()))
            put("sessions", json.encodeToJsonElement(aiChatDao.getAllSessionsSync()))
            put("memories", json.encodeToJsonElement(assistantMemoryDao.getAllMemoriesSync()))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["sessions"]?.let { aiChatDao.clearEverything(); aiChatDao.insertAllSessions(json.decodeFromJsonElement(it)) }
        root["messages"]?.let { aiChatDao.insertAllMessages(json.decodeFromJsonElement(it)) }
        root["memories"]?.let { assistantMemoryDao.deleteAll(); assistantMemoryDao.insertAllMemories(json.decodeFromJsonElement(it)) }
    }
}
