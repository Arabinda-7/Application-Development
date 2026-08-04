package com.example.allinone.domain.usecase.assistant

import com.example.allinone.data.repository.AiChatRepository
import com.example.allinone.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * AutoCleanupAssistantHistoryUseCase: Deletes chat and voice sessions older than the configured threshold.
 */
class AutoCleanupAssistantHistoryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: AiChatRepository
) {
    suspend operator fun invoke() {
        val settings = userRepository.getUserSettings().first()
        if (settings.isAssistantAutoCleanupEnabled && settings.assistantCleanupDays > 0) {
            chatRepository.cleanupOldHistory(settings.assistantCleanupDays)
        }
    }
}
