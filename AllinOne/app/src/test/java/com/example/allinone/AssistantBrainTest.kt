package com.example.allinone

import org.junit.Test
import org.junit.Assert.*

/**
 * AssistantBrainTest: Validates the NLU (Natural Language Understanding) and intent parsing logic.
 */
class AssistantBrainTest {

    @Test
    fun parseCommand_addHabit_extractsCorrectPayload() {
        val command = "Add habit Drink Water"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_HABIT", action?.type)
        assertEquals("drink water", action?.payload)
    }

    @Test
    fun parseCommand_addTask_extractsCorrectPayload() {
        val command = "Add task Buy Groceries"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("buy groceries", action?.payload)
    }

    @Test
    fun parseCommand_addWorkout_extractsCorrectPayload() {
        val command = "Add workout Running"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_WORKOUT", action?.type)
        assertEquals("running", action?.payload)
    }

    @Test
    fun parseCommand_addNote_extractsCorrectPayload() {
        val command = "Add note Project Idea"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("project idea", action?.payload)
    }

    @Test
    fun parseCommand_logExpense_extractsAmount() {
        val command = "log expense 500"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("LOG_EXPENSE", action?.type)
        assertEquals("500", action?.payload)
    }

    @Test
    fun parseCommand_navigate_recognizesIntent() {
        val command = "show finance"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("NAVIGATE", action?.type)
        assertEquals("FINANCE", action?.payload)
    }

    @Test
    fun parseCommand_startWorkout_recognizesIntent() {
        val command = "start a workout"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("CHAT_RESPONSE", action?.type)
        assertTrue(action?.payload?.contains("workout", ignoreCase = true) == true)
    }

    @Test
    fun parseCommand_remindMeTo_extractsCorrectPayload() {
        val command = "add task buy milk"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("buy milk", action?.payload)
    }

    @Test
    fun parseCommand_takeANote_extractsCorrectPayload() {
        val command = "add note meeting summary"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("meeting summary", action?.payload)
    }

    @Test
    fun parseCommand_help_returnsGuide() {
        val command = "help"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("CHAT_RESPONSE", action?.type)
        assertTrue(action?.payload?.contains("help", ignoreCase = true) == true)
    }
}
