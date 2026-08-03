package com.example.allinone.assistant.parser

import com.example.allinone.DataManager
import com.example.allinone.data.model.Note
import com.example.allinone.assistant.context.AssistantContextManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantEntityExtractor @Inject constructor(
    private val contextManager: AssistantContextManager
) {

    /**
     * Attempts to find a project (Note with isGlobalProject=true) mentioned in the command.
     */
    fun findProjectInCmd(cmd: String): Note? {
        val allProjects = synchronized(DataManager.projects) { DataManager.projects.toList() }
        
        // 1. Exact match
        allProjects.find { cmd.contains(it.title.lowercase()) }?.let { 
            return it 
        }
        
        // 2. Candidate extraction
        val nameCandidate = cmd.split("for", "of", "about", "project").lastOrNull()?.trim()
        if (!nameCandidate.isNullOrEmpty()) {
            allProjects.find { it.title.contains(nameCandidate, ignoreCase = true) }?.let { 
                return it 
            }
        }
        
        // 3. Pronoun fallback
        if ((cmd.contains(" it ") || cmd.endsWith(" it") || cmd.contains(" that ") || cmd.contains(" project")) && 
            contextManager.lastMentionedProject != null) {
            return allProjects.find { it.title == contextManager.lastMentionedProject }
        }
        
        return null
    }

    /**
     * Attempts to find a habit mentioned in the command.
     */
    fun findHabitInCmd(cmd: String): String? {
        val habitNames = synchronized(DataManager.habits) { DataManager.habits.map { it.name.lowercase() } }
        return habitNames.find { cmd.contains(it) } ?: contextManager.lastMentionedHabit
    }
}
