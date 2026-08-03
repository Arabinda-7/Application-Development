package com.example.allinone.domain.usecase.assistant

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import javax.inject.Inject

class ClearOldMemoriesUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    suspend operator fun invoke(days: Int) = repository.clearOldMemories(days)
}
