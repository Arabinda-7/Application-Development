package com.example.allinone.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A decoupled, modular backup container.
 * This class DOES NOT need to change when app features are added or modified.
 */
@Serializable
data class BackupData(
    val version: Int,
    val timestamp: Long,
    /**
     * Map of feature keys (e.g., "tasks", "finance") to their serialized JSON data.
     * Each BackupProvider manages its own entry in this map.
     */
    val featureData: Map<String, JsonElement> = emptyMap()
)
