package com.example.allinone.assistant.usecase

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import javax.inject.Inject

class DeleteMemoryUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    suspend fun deleteById(id: String) {
        repository.deleteMemory(id)
    }

    suspend fun clearOld(days: Int) {
        repository.clearOldMemories(days)
    }
}
