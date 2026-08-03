package com.example.allinone.assistant.usecase

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import com.example.allinone.assistant.model.MemoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RetrieveMemoryUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    fun getAll(): Flow<List<AssistantMemory>> = repository.getAllMemories()

    fun getByType(type: MemoryType): Flow<List<AssistantMemory>> = repository.getMemoriesByType(type)

    suspend fun getByKey(key: String): AssistantMemory? = repository.getMemoryByKey(key)
}
