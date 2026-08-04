package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class UserBackupProvider @Inject constructor(
    private val userRepository: UserRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "user_module"

    override suspend fun exportData(): JsonElement {
        return buildJsonObject {
            put("profile", json.encodeToJsonElement(userRepository.getUserProfile().first()))
            put("settings", json.encodeToJsonElement(userRepository.getUserSettings().first()))
            put("history", json.encodeToJsonElement(userRepository.getDayHistory().first()))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["profile"]?.let { userRepository.updateUserProfile(json.decodeFromJsonElement(it)) }
        root["settings"]?.let { userRepository.updateUserSettings(json.decodeFromJsonElement(it)) }
        root["history"]?.let { userRepository.updateDayHistory(json.decodeFromJsonElement(it)) }
    }
}
