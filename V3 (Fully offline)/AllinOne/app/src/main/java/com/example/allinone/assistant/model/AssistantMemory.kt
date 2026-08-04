package com.example.allinone.assistant.model

import java.util.UUID

data class AssistantMemory(
    val id: String = UUID.randomUUID().toString(),
    val key: String? = null,
    val content: String,
    val type: MemoryType,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 5,
    val metadata: Map<String, String> = emptyMap(),
    val embedding: FloatArray? = null
)

enum class MemoryType {
    PREFERENCE,
    CONTEXT,
    PATTERN,
    FACT,
    USER_INFO
}
