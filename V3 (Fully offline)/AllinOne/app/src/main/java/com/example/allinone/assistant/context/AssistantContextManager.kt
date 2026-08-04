package com.example.allinone.assistant.context

import com.example.allinone.assistant.model.AssistantSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantContextManager @Inject constructor() {

    private var _activeSession: AssistantSession? = null
    val activeSession: AssistantSession? get() = _activeSession

    // Continuous Context Tracking (recently mentioned entities)
    var lastMentionedProject: String? = null
    var lastMentionedHabit: String? = null
    var lastMentionedTask: String? = null
    var lastMentionedNote: String? = null

    fun setSession(session: AssistantSession?) {
        _activeSession = session
    }

    fun clearContext() {
        _activeSession = null
        lastMentionedProject = null
        lastMentionedHabit = null
        lastMentionedTask = null
        lastMentionedNote = null
    }

    /**
     * Resolves pronouns like "it", "that", "its" based on the current context.
     */
    fun resolveContext(command: String): String {
        var resolved = command.lowercase().trim()
        
        // Contextual Replacement for Projects
        lastMentionedProject?.let { name ->
            if (resolved.contains(" it ") || resolved.endsWith(" it") || 
                resolved.contains(" that project") || resolved.contains(" its ")) {
                resolved = resolved.replace(" its ", " $name ")
                    .replace(" it ", " $name ")
                    .replace(" that project", " $name ")
                if (resolved.endsWith(" it")) {
                    resolved = resolved.substringBeforeLast(" it") + " $name"
                }
            }
        }
        
        // Contextual Replacement for Habits
        lastMentionedHabit?.let { name ->
            if (resolved.contains(" it ") || resolved.endsWith(" it") || resolved.contains(" that habit")) {
                resolved = resolved.replace(" that habit", " $name ")
                    .replace(" it ", " $name ")
                if (resolved.endsWith(" it")) {
                    resolved = resolved.substringBeforeLast(" it") + " $name"
                }
            }
        }

        return resolved
    }

    fun updateContextFromCommand(command: String, foundProject: String? = null, foundHabit: String? = null) {
        foundProject?.let { lastMentionedProject = it }
        foundHabit?.let { lastMentionedHabit = it }
    }
}
