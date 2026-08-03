package com.example.allinone.domain.usecase.assistant

import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.assistant.model.AssistantMemory
import javax.inject.Inject

class GetMemoryByKeyUseCase @Inject constructor(
    private val repository: AssistantMemoryRepository
) {
    suspend operator fun invoke(key: String): AssistantMemory? = repository.getMemoryByKey(key)
}
