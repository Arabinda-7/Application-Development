package com.example.allinone.backup

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards BackupProvider>,
    private val serializer: BackupSerializer
) {
    /**
     * Dynamically creates a backup by collecting data from all registered providers.
     */
    suspend fun createBackup(): BackupData {
        val featureDataMap = mutableMapOf<String, JsonElement>()
        
        providers.forEach { provider ->
            featureDataMap[provider.featureKey] = provider.exportData()
        }
        
        return BackupData(
            version = BackupValidator.CURRENT_VERSION,
            timestamp = System.currentTimeMillis(),
            featureData = featureDataMap
        )
    }

    suspend fun exportBackup(password: CharArray? = null): String {
        val data = createBackup()
        val json = serializer.serialize(data)
        return if (password != null) {
            EncryptionUtils.encrypt(json, password)
        } else {
            json
        }
    }
}
