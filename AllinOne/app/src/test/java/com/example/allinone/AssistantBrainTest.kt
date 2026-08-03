package com.example.allinone

import com.example.allinone.assistant.context.AssistantContextManager
import com.example.allinone.assistant.executor.AssistantSessionProcessor
import com.example.allinone.assistant.intent.AssistantIntentDetector
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.assistant.parser.AssistantEntityExtractor
import com.example.allinone.assistant.response.AssistantResponseProvider
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * AssistantBrainTest: Validates the NLU (Natural Language Understanding) and intent parsing logic.
 */
class AssistantBrainTest {

    private lateinit var assistantBrain: AssistantBrain
    private val contextManager = mockk<AssistantContextManager>(relaxed = true)
    private val entityExtractor = AssistantEntityExtractor(contextManager)
    private val intentDetector = AssistantIntentDetector(entityExtractor, contextManager)
    private val sessionProcessor = mockk<AssistantSessionProcessor>(relaxed = true)
    private val responseProvider = mockk<AssistantResponseProvider>(relaxed = true)

    @Before
    fun setUp() {
        assistantBrain = AssistantBrain(
            contextManager,
            intentDetector,
            sessionProcessor,
            responseProvider
        )
        // Default: just return the same command after resolution
        every { contextManager.resolveContext(any()) } answers { it.invocation.args[0] as String }
    }

    @Test
    fun parseCommand_addHabit_extractsCorrectPayload() {
        val command = "Add habit Drink Water"
        every { intentDetector.detectIntent(any()) } returns CommandAction("ADD_HABIT", "drink water")
        
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_HABIT", action?.type)
        assertEquals("drink water", action?.payload)
    }

    @Test
    fun parseCommand_addTask_extractsCorrectPayload() {
        val command = "Add task Buy Groceries"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("buy groceries", action?.payload)
    }

    @Test
    fun parseCommand_addWorkout_extractsCorrectPayload() {
        val command = "Add workout Running"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_WORKOUT", action?.type)
        assertEquals("running", action?.payload)
    }

    @Test
    fun parseCommand_addNote_extractsCorrectPayload() {
        val command = "Add note Project Idea"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("project idea", action?.payload)
    }

    @Test
    fun parseCommand_logExpense_extractsAmount() {
        val command = "log expense 500"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("LOG_EXPENSE", action?.type)
        assertEquals("500", action?.payload)
    }

    @Test
    fun parseCommand_navigate_recognizesIntent() {
        val command = "show finance"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("NAVIGATE", action?.type)
        assertEquals("FINANCE", action?.payload)
    }

    @Test
    fun parseCommand_startWorkout_recognizesIntent() {
        val command = "start a workout"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("CHAT_RESPONSE", action?.type)
        val response = action?.dynamicResponse ?: action?.payload
        assertTrue(response?.contains("workout", ignoreCase = true) == true)
    }

    @Test
    fun parseCommand_remindMeTo_extractsCorrectPayload() {
        val command = "add task buy milk"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("buy milk", action?.payload)
    }

    @Test
    fun parseCommand_takeANote_extractsCorrectPayload() {
        val command = "add note meeting summary"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("meeting summary", action?.payload)
    }

    @Test
    fun parseCommand_help_returnsGuide() {
        val command = "help"
        val action = assistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("CHAT_RESPONSE", action?.type)
        val response = action?.dynamicResponse ?: action?.payload
        assertTrue(response?.contains("help", ignoreCase = true) == true)
    }
}
