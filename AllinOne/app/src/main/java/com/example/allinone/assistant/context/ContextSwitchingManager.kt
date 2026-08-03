package com.example.allinone.assistant.context

import javax.inject.Inject
import javax.inject.Singleton

enum class AssistantDomain {
    TASK,
    HABIT,
    FINANCE,
    WORKOUT,
    NOTE,
    PROJECT,
    GENERAL
}

data class ConversationSessionContext(
    val activeDomain: AssistantDomain = AssistantDomain.GENERAL,
    val previousDomain: AssistantDomain? = null,
    val activeParameters: Map<String, Any> = emptyMap()
)

@Singleton
class ContextSwitchingManager @Inject constructor() {

    private var currentContext = ConversationSessionContext()

    fun switchDomain(newDomain: AssistantDomain): ConversationSessionContext {
        currentContext = currentContext.copy(
            previousDomain = currentContext.activeDomain,
            activeDomain = newDomain
        )
        return currentContext
    }

    fun getCurrentContext(): ConversationSessionContext = currentContext
}
