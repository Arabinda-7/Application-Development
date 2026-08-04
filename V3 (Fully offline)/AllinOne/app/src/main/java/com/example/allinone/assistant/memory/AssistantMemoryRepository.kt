package com.example.allinone.assistant.memory

import com.example.allinone.assistant.model.AssistantMemory
import com.example.allinone.assistant.model.MemoryType
import kotlinx.coroutines.flow.Flow

interface AssistantMemoryRepository {
    fun getAllMemories(): Flow<List<AssistantMemory>>
    fun getMemoriesByType(type: MemoryType): Flow<List<AssistantMemory>>
    suspend fun getMemoryByKey(key: String): AssistantMemory?
    suspend fun saveMemory(memory: AssistantMemory)
    suspend fun searchMemories(query: String): List<AssistantMemory>
    suspend fun deleteMemory(id: String)
    suspend fun clearOldMemories(days: Int)
}
