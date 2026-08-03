package com.example.allinone.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(data: BackupData): String {
        return json.encodeToString(data)
    }

    fun deserialize(jsonString: String): BackupData {
        return json.decodeFromString(jsonString)
    }
}
