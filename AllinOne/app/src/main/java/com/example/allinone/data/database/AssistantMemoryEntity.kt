package com.example.allinone.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "assistant_memories")
data class AssistantMemoryEntity(
    @PrimaryKey
    val id: String,
    val key: String?, // For quick lookup of specific info like "user_home_address"
    val content: String,
    val type: String, // "PREFERENCE", "CONTEXT", "PATTERN", "FACT"
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 5, // 0 to 10
    val metadata: String? = null, // JSON string for additional context
    val embedding: String? = null // For future: stored as serialized float array or blob
)
