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
        assertEquals("Drink water", action?.payload)
    }

    @Test
    fun parseCommand_addTask_extractsCorrectPayload() {
        val command = "Add task Buy Groceries"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("Buy groceries", action?.payload)
    }

    @Test
    fun parseCommand_addWorkout_extractsCorrectPayload() {
        val command = "Add workout Running"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_WORKOUT", action?.type)
        assertEquals("Running", action?.payload)
    }

    @Test
    fun parseCommand_addNote_extractsCorrectPayload() {
        val command = "Add note Project Idea"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("Project idea", action?.payload)
    }

    @Test
    fun parseCommand_logExpense_extractsAmount() {
        val command = "log 500 expense"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("LOG_EXPENSE", action?.type)
        assertEquals("500", action?.payload)
    }

    @Test
    fun parseCommand_setBudget_extractsAmount() {
        val command = "set budget 10000"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("SET_BUDGET", action?.type)
        assertEquals("10000", action?.payload)
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
        val command = "please start workout now"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("START_WORKOUT", action?.type)
    }

    @Test
    fun parseCommand_remindMeTo_extractsCorrectPayload() {
        val command = "Remind me to buy milk"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_TASK", action?.type)
        assertEquals("Buy milk", action?.payload)
    }

    @Test
    fun parseCommand_takeANote_extractsCorrectPayload() {
        val command = "Take a quick note Meeting summary"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("ADD_NOTE", action?.type)
        assertEquals("Meeting summary", action?.payload)
    }

    @Test
    fun parseCommand_projectStatus_recognizesIntent() {
        val command = "how is my project Website"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("PROJECT_REPORT", action?.type)
        assertEquals("website", action?.payload)
    }

    @Test
    fun parseCommand_fallback_returnsChatResponse() {
        // Since we can't easily mock assets in local JUnit without Robolectric, 
        // we check the fallback logic in getChatResponse
        val command = "thanks a lot"
        val action = AssistantBrain.parseCommand(command)
        
        assertNotNull(action)
        assertEquals("CHAT_RESPONSE", action?.type)
        assertTrue(action?.payload?.contains("welcome", ignoreCase = true) == true)
    }
}
