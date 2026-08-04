package com.example.allinone.domain.usecase.assistant

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import com.example.allinone.assistant.model.MemoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoriesByTypeUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    operator fun invoke(type: MemoryType): Flow<List<AssistantMemory>> = repository.getMemoriesByType(type)
}
