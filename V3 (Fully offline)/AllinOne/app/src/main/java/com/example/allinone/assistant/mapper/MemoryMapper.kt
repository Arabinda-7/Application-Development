package com.example.allinone.assistant.mapper

import com.example.allinone.assistant.model.AssistantMemory
import com.example.allinone.assistant.model.MemoryType
import com.example.allinone.data.database.AssistantMemoryEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MemoryMapper {
    private val gson = Gson()

    fun toEntity(domain: AssistantMemory): AssistantMemoryEntity {
        return AssistantMemoryEntity(
            id = domain.id,
            key = domain.key,
            content = domain.content,
            type = domain.type.name,
            timestamp = domain.timestamp,
            importance = domain.importance,
            metadata = gson.toJson(domain.metadata),
            embedding = domain.embedding?.let { gson.toJson(it) }
        )
    }

    fun toDomain(entity: AssistantMemoryEntity): AssistantMemory {
        val metadataType = object : TypeToken<Map<String, String>>() {}.type
        val embeddingType = object : TypeToken<FloatArray>() {}.type

        return AssistantMemory(
            id = entity.id,
            key = entity.key,
            content = entity.content,
            type = MemoryType.valueOf(entity.type),
            timestamp = entity.timestamp,
            importance = entity.importance,
            metadata = entity.metadata?.let { gson.fromJson(it, metadataType) } ?: emptyMap(),
            embedding = entity.embedding?.let { gson.fromJson(it, embeddingType) }
        )
    }
}
