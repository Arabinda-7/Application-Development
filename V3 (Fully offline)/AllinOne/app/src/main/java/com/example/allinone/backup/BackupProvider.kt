package com.example.allinone.backup

import kotlinx.serialization.json.JsonElement

/**
 * Interface for feature-specific backup and restore logic.
 * Implementing this allows a feature to be included in the backup system 
 * without modifying the core BackupManager/RestoreManager files.
 */
interface BackupProvider {
    /**
     * Unique key for this feature's data in the backup JSON (e.g., "tasks", "finance").
     */
    val featureKey: String

    /**
     * Fetches the data for this feature and converts it to a serializable JsonElement.
     */
    suspend fun exportData(): JsonElement

    /**
     * Takes the serialized data and restores it to the local database/preferences.
     */
    suspend fun importData(data: JsonElement)
}
