package com.example.allinone.assistant.usecase

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import javax.inject.Inject

class SearchMemoryUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    suspend operator fun invoke(query: String): List<AssistantMemory> {
        return repository.searchMemories(query)
    }
}
