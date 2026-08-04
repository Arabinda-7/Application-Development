package com.example.allinone.assistant.usecase

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import javax.inject.Inject

class SaveMemoryUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    suspend operator fun invoke(memory: AssistantMemory) {
        repository.saveMemory(memory)
    }
}
