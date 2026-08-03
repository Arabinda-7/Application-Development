package com.example.allinone.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "ai_chat_sessions")
data class AiChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String = "chat",
    val timestamp: Long = System.currentTimeMillis()
)
