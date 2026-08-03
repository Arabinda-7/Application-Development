package com.example.allinone

import android.content.Context
import com.example.allinone.assistant.context.AssistantContextManager
import com.example.allinone.assistant.executor.AssistantSessionProcessor
import com.example.allinone.assistant.intent.AssistantIntentDetector
import com.example.allinone.assistant.model.*
import com.example.allinone.assistant.response.AssistantResponseProvider
import com.example.allinone.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AssistantBrain (Orchestrator): The central entry point for the AI assistant.
 * 
 * Responsibilities:
 * - Coordinates between Intent Detector, Session Processor, and Context Manager.
 * - Handles the primary flow of command parsing and action resolution.
 * - Manages lifecycle initialization (Asset loading).
 */
@Singleton
class AssistantBrain @Inject constructor(
    private val contextManager: AssistantContextManager,
    private val intentDetector: AssistantIntentDetector,
    private val sessionProcessor: AssistantSessionProcessor,
    private val responseProvider: AssistantResponseProvider
) {

    /**
     * Initializes the assistant by loading necessary assets (responses, models).
     */
    fun initialize(context: Context) {
        responseProvider.initialize(context)
    }

    /**
     * Main entry point for processing a user's natural language command.
     */
    fun parseCommand(command: String): CommandAction? {
        val rawCmd = command.lowercase().trim()
        
        // 1. Check for global "cancel" intent
        if (rawCmd == "cancel") {
            contextManager.clearContext()
            return CommandAction("CHAT_RESPONSE", "Operation cancelled.")
        }
        
        // 2. Resolve pronouns based on recent context
        val resolvedCmd = contextManager.resolveContext(rawCmd)

        // 3. Delegate to Active Session Processor if a session is in progress
        contextManager.activeSession?.let { session ->
            return sessionProcessor.processSession(session, resolvedCmd)
        }

        // 4. Detect Intent for new commands
        val detectedAction = intentDetector.detectIntent(resolvedCmd)
        if (detectedAction != null) {
            return detectedAction
        }

        // 5. Fallback to general Chat Responses from knowledge base
        val chatResponse = responseProvider.getChatResponse(resolvedCmd)
        return if (chatResponse != null) {
            CommandAction("CHAT_RESPONSE", chatResponse)
        } else {
            // Ultimate fallback
            null
        }
    }

    fun clearContext() {
        contextManager.clearContext()
    }

    companion object {
        /**
         * Legacy Bridge: These methods are placeholders for components that don't use DI yet.
         * They should be phased out as Activity-level injection is completed.
         */
        
        fun initialize(context: Context) {
            // Logic handled by injected instance in DataManager or Activity
        }

        fun clearContext() {
            // Context clearance handled by instance
        }

        fun parseCommand(command: String, history: List<ChatMessage> = emptyList()): CommandAction? {
            val contextManager = com.example.allinone.assistant.context.AssistantContextManager()
            val entityExtractor = com.example.allinone.assistant.parser.AssistantEntityExtractor(contextManager)
            val intentDetector = com.example.allinone.assistant.intent.AssistantIntentDetector(entityExtractor, contextManager)
            return intentDetector.detectIntent(command)
        }
        
        fun generateInsights(context: Context): List<AssistantBrain.Insight> {
            // Moved to GetAssistantInsightsUseCase
            return emptyList()
        }
    }

    // Moved to GetAssistantInsightsUseCase, kept type for binary compatibility
    data class Insight(val title: String, val description: String, val type: String, val importance: Int)
}
