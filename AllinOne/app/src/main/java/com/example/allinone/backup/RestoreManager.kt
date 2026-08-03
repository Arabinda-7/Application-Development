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
            
            val data = serializer.deserialize(json)
            val validation = BackupValidator.validate(data)
            
            if (validation is BackupValidator.ValidationResult.Invalid) {
                return RestoreResult.Error(validation.message)
            }
            
            restoreData(data)
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Unknown error")
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
