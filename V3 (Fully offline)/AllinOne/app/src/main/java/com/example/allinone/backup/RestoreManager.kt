package com.example.allinone.backup

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreManager @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards BackupProvider>,
    private val serializer: BackupSerializer
) {
    suspend fun importBackup(backupString: String, password: CharArray? = null): RestoreResult {
        return try {
            val json = if (password != null) {
                EncryptionUtils.decrypt(backupString, password)
            } else {
                backupString
            }
            
            // Try modern format first
            try {
                val data = serializer.deserialize(json)
                val validation = BackupValidator.validate(data)
                
                if (validation is BackupValidator.ValidationResult.Invalid) {
                    return RestoreResult.Error(validation.message)
                }
                
                restoreData(data)
                return RestoreResult.Success
            } catch (e: Exception) {
                // Fallback to legacy format check
                if (json.contains("\"tasks_data\"") || json.contains("\"habits_data\"")) {
                    return restoreLegacyData(json)
                }
                throw e
            }
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun restoreLegacyData(json: String): RestoreResult {
        return try {
            val context = com.example.allinone.AllInOneApplication.instance
            val prefs = com.example.allinone.security.SecurityManager.getEncryptedPrefs(context)
            val legacyMap: Map<String, String> = com.google.gson.Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
            
            prefs.edit().apply {
                legacyMap.forEach { (key, value) ->
                    putString(key, value)
                }
                // Force migration on next start
                putBoolean("data_migrated_to_sql", false)
            }.apply()
            
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Error("Legacy import failed: ${e.message}")
        }
    }

    private suspend fun restoreData(data: BackupData) {
        // Iterate through all available providers and check if the backup has data for them
        providers.forEach { provider ->
            data.featureData[provider.featureKey]?.let { featureJson ->
                provider.importData(featureJson)
            }
        }
    }

    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }
}
