package com.example.allinone.domain.usecase.assistant

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoriesUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    operator fun invoke(): Flow<List<AssistantMemory>> = repository.getAllMemories()
}
