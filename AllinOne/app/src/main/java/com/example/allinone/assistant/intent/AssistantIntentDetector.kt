package com.example.allinone.assistant.intent

import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.assistant.parser.AssistantEntityExtractor
import com.example.allinone.assistant.context.AssistantContextManager
import com.example.allinone.assistant.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantIntentDetector @Inject constructor(
    private val entityExtractor: AssistantEntityExtractor,
    private val contextManager: AssistantContextManager
) {

    fun detectIntent(command: String): CommandAction? {
        val cmd = command.lowercase().trim()

        // 1. Session Triggers (Multi-turn starts)
        if (cmd == "create a workout" || cmd == "start a workout" || cmd == "new workout") {
            contextManager.setSession(AssistantSession.WorkoutCreation())
            return CommandAction("CHAT_RESPONSE", "Creating a new workout, what will be the title?")
        }
        if (cmd == "create a task" || cmd == "new task" || cmd == "add task") {
            contextManager.setSession(AssistantSession.TaskCreation())
            return CommandAction("CHAT_RESPONSE", "Creating a new task, what will be the title?")
        }
        if (cmd == "create a note" || cmd == "new note" || cmd == "take a note") {
            contextManager.setSession(AssistantSession.NoteCreation())
            return CommandAction("CHAT_RESPONSE", "Creating a new note, what will be the title?")
        }
        if (cmd == "create a project" || cmd == "new project" || cmd == "start a project" || cmd == "new roadmap") {
            contextManager.setSession(AssistantSession.ProjectCreation())
            return CommandAction("CHAT_RESPONSE", "Creating a new project, what will be the title?")
        }

        // 2. Project Management Intents
        if (cmd.startsWith("add a feature to ") || cmd.startsWith("add feature to ")) {
            val name = cmd.replace("add a feature to ", "").replace("add feature to ", "").trim()
            val proj = entityExtractor.findProjectInCmd(name)
            return if (proj != null) {
                contextManager.lastMentionedProject = proj.title
                contextManager.setSession(AssistantSession.FeatureAddition(proj.title))
                CommandAction("CHAT_RESPONSE", "Adding a new feature to '${proj.title}'. What is the name of the feature?")
            } else CommandAction("CHAT_RESPONSE", "Project '$name' not found.")
        }

        if (cmd.startsWith("update status for ") || cmd.startsWith("change status for ")) {
            val name = cmd.replace("update status for ", "").replace("change status for ", "").trim()
            val proj = entityExtractor.findProjectInCmd(name)
            return if (proj != null) {
                contextManager.setSession(AssistantSession.ProjectPropertyUpdate(proj.title, "STATUS"))
                CommandAction("CHAT_RESPONSE", "Current status of '${proj.title}' is ${proj.status}. What should I change it to?")
            } else null
        }

        if (cmd.contains("project status") || cmd.contains("progress") || cmd.contains("deadline")) {
            val proj = entityExtractor.findProjectInCmd(cmd)
            proj?.let { 
                contextManager.lastMentionedProject = it.title
                return CommandAction("PROJECT_REPORT", it.title) 
            }
        }

        // 3. Mark/Complete Intents
        if (cmd.startsWith("mark habit") || cmd.startsWith("habit mark")) {
            val name = cmd.replace("mark habit", "").replace("habit mark", "").trim()
            val habit = entityExtractor.findHabitInCmd(name)
            return if (habit != null) {
                contextManager.lastMentionedHabit = habit
                contextManager.setSession(AssistantSession.HabitCompletion(CompletionStep.CONFIRM, habit))
                CommandAction("CHAT_RESPONSE", "Marking the habit '$habit' as completed?")
            } else {
                contextManager.setSession(AssistantSession.HabitCompletion(CompletionStep.NAME))
                CommandAction("CHAT_RESPONSE", "Which habit would you like to mark as completed?")
            }
        }

        // 4. Creation Intents (Single-turn or quick)
        return when {
            cmd.startsWith("add habit") -> CommandAction("ADD_HABIT", cmd.replace("add habit", "").trim())
            cmd.startsWith("add task") -> CommandAction("ADD_TASK", cmd.replace("add task", "").trim())
            cmd.startsWith("add workout") -> CommandAction("ADD_WORKOUT", cmd.replace("add workout", "").trim())
            cmd.startsWith("add note") -> CommandAction("ADD_NOTE", cmd.replace("add note", "").trim())
            
            cmd.contains("log income") -> CommandAction("LOG_INCOME", cmd.replace(Regex("[^0-9.]"), ""))
            cmd.contains("log expense") -> CommandAction("LOG_EXPENSE", cmd.replace(Regex("[^0-9.]"), ""))
            
            // 5. Navigation Intents
            cmd.contains("show finance") || cmd.contains("open finance") -> CommandAction("NAVIGATE", "FINANCE")
            cmd.contains("show habits") || cmd.contains("go to habits") -> CommandAction("NAVIGATE", "HABITS")
            cmd.contains("show settings") || cmd.contains("open settings") -> CommandAction("NAVIGATE", "SETTINGS")
            
            // 6. Help Intent
            cmd == "help" || cmd == "guide" || cmd == "what can you do" -> 
                CommandAction("CHAT_RESPONSE", "I can help you manage habits, tasks, workouts and projects. Try 'Add task Buy Milk' or 'Create a project'.")
            
            else -> null
        }
    }
}
