package com.example.allinone.assistant.executor

import com.example.allinone.assistant.model.*
import com.example.allinone.assistant.context.AssistantContextManager
import com.example.allinone.data.model.ProjectFeature
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

@Singleton
class AssistantSessionProcessor @Inject constructor(
    private val contextManager: AssistantContextManager
) {

    fun processSession(session: AssistantSession, cmd: String): CommandAction {
        return when (session) {
            is AssistantSession.HabitCreation -> handleHabitCreation(session, cmd)
            is AssistantSession.HabitCompletion -> handleHabitCompletion(session, cmd)
            is AssistantSession.WorkoutCreation -> handleWorkoutCreation(session, cmd)
            is AssistantSession.TaskCreation -> handleTaskCreation(session, cmd)
            is AssistantSession.NoteCreation -> handleNoteCreation(session, cmd)
            is AssistantSession.ProjectCreation -> handleProjectCreation(session, cmd)
            is AssistantSession.FeatureAddition -> handleFeatureAddition(session, cmd)
            is AssistantSession.ProjectPropertyUpdate -> handleProjectUpdate(session, cmd)
            else -> {
                contextManager.setSession(null)
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Session type not fully implemented. Resetting context.")
            }
        }
    }

    private fun handleHabitCreation(session: AssistantSession.HabitCreation, cmd: String): CommandAction {
        return when (session.step) {
            HabitStep.TITLE -> {
                session.tempName = cmd.capitalize()
                session.step = HabitStep.CONFIRM_DEFAULT
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Should I use default settings for '${session.tempName}'? (Yes/No)")
            }
            HabitStep.CONFIRM_DEFAULT -> {
                if (cmd.contains("yes")) {
                    contextManager.setSession(null)
                    CommandAction("ADD_HABIT", session.tempName, "Habit '${session.tempName}' created!")
                } else {
                    session.step = HabitStep.TARGET
                    CommandAction("CHAT_RESPONSE", dynamicResponse = "What is the daily goal target (e.g., 10)?")
                }
            }
            HabitStep.TARGET -> {
                session.tempTarget = cmd.filter { it.isDigit() }.toIntOrNull() ?: 1
                session.step = HabitStep.FREQUENCY
                CommandAction("CHAT_RESPONSE", dynamicResponse = "When do you want to do this? (Morning, Afternoon, Evening, Anytime)")
            }
            HabitStep.FREQUENCY -> {
                activeSessionFinished()
                CommandAction("ADD_HABIT", "${session.tempName}|${session.tempTarget}|$cmd", "Habit created successfully!")
            }
        }
    }

    private fun handleHabitCompletion(session: AssistantSession.HabitCompletion, cmd: String): CommandAction {
        return when (session.step) {
            CompletionStep.NAME -> {
                session.tempName = cmd
                session.step = CompletionStep.CONFIRM
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Marking '${session.tempName}' as completed?")
            }
            CompletionStep.CONFIRM -> {
                activeSessionFinished()
                if (cmd.contains("yes")) CommandAction("LOG_HABIT", session.tempName, "Marked as done!")
                else CommandAction("CHAT_RESPONSE", dynamicResponse = "Action cancelled.")
            }
        }
    }

    private fun handleWorkoutCreation(session: AssistantSession.WorkoutCreation, cmd: String): CommandAction {
        return when (session.step) {
            WorkoutStep.TITLE -> {
                session.tempName = cmd.capitalize()
                session.step = WorkoutStep.MODE
                CommandAction("CHAT_RESPONSE", dynamicResponse = "What will be the goal mode? (Reps, Sets, or Timer)")
            }
            WorkoutStep.MODE -> {
                session.tempMode = if (cmd.contains("set")) "Sets" else if (cmd.contains("time")) "Timer" else "Reps"
                session.step = WorkoutStep.TARGET
                CommandAction("CHAT_RESPONSE", dynamicResponse = "What is the target value?")
            }
            WorkoutStep.TARGET -> {
                session.tempTarget = cmd.filter { it.isDigit() }.toIntOrNull() ?: 10
                session.step = WorkoutStep.CONFIRM_DEFAULT
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Use default frequency? (Yes/No)")
            }
            WorkoutStep.CONFIRM_DEFAULT -> {
                if (cmd.contains("yes")) {
                    activeSessionFinished()
                    CommandAction("ADD_WORKOUT", "${session.tempName}|${session.tempMode}|${session.tempTarget}", "Workout added!")
                } else {
                    session.step = WorkoutStep.FREQUENCY
                    CommandAction("CHAT_RESPONSE", dynamicResponse = "What is the frequency?")
                }
            }
            WorkoutStep.FREQUENCY -> {
                activeSessionFinished()
                CommandAction("ADD_WORKOUT", "${session.tempName}|${session.tempMode}|${session.tempTarget}|0|$cmd")
            }
        }
    }

    private fun handleTaskCreation(session: AssistantSession.TaskCreation, cmd: String): CommandAction {
        return when (session.step) {
            TaskStep.NAME -> {
                session.tempName = cmd
                session.step = TaskStep.SUBTASK_PROMPT
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Any subtasks for this task? (Yes/No)")
            }
            TaskStep.SUBTASK_PROMPT -> {
                if (cmd.contains("yes")) {
                    session.step = TaskStep.SUBTASK_NAME
                    CommandAction("CHAT_RESPONSE", dynamicResponse = "Enter subtask name:")
                } else {
                    activeSessionFinished()
                    CommandAction("ADD_TASK", session.tempName, "Task saved!")
                }
            }
            TaskStep.SUBTASK_NAME -> {
                session.tempSubtasks.add(cmd)
                session.step = TaskStep.SUBTASK_PROMPT
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Added subtask. Any more?")
            }
            else -> CommandAction("CHAT_RESPONSE", dynamicResponse = "Processing task...")
        }
    }

    private fun handleNoteCreation(session: AssistantSession.NoteCreation, cmd: String): CommandAction {
        return when (session.step) {
            NoteStep.TITLE -> {
                session.tempTitle = cmd
                session.step = NoteStep.CONTENT
                CommandAction("CHAT_RESPONSE", dynamicResponse = "What is the content of the note?")
            }
            NoteStep.CONTENT -> {
                activeSessionFinished()
                CommandAction("ADD_NOTE", "${session.tempTitle}|$cmd", "Note saved!")
            }
        }
    }

    private fun handleProjectCreation(session: AssistantSession.ProjectCreation, cmd: String): CommandAction {
        return when (session.step) {
            ProjectStep.TITLE -> {
                session.tempTitle = cmd
                session.step = ProjectStep.DESC
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Project description?")
            }
            ProjectStep.DESC -> {
                session.tempDesc = cmd
                session.step = ProjectStep.CONFIRM
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Create project '${session.tempTitle}'? (Yes/No)")
            }
            ProjectStep.CONFIRM -> {
                activeSessionFinished()
                if (cmd.contains("yes")) CommandAction("CREATE_PROJECT", "${session.tempTitle}|${session.tempDesc}", "Project created!")
                else CommandAction("CHAT_RESPONSE", dynamicResponse = "Cancelled.")
            }
            else -> CommandAction("CHAT_RESPONSE", dynamicResponse = "Processing project...")
        }
    }

    private fun handleFeatureAddition(session: AssistantSession.FeatureAddition, cmd: String): CommandAction {
        return when (session.step) {
            FeatureStep.NAME -> {
                session.tempName = cmd
                session.step = FeatureStep.CONFIRM
                CommandAction("CHAT_RESPONSE", dynamicResponse = "Add feature '${session.tempName}' to '${session.projectTitle}'? (Yes/No)")
            }
            FeatureStep.CONFIRM -> {
                activeSessionFinished()
                if (cmd.contains("yes")) CommandAction("ADD_PROJECT_FEATURE", "${session.projectTitle}|${session.tempName}", "Feature added!")
                else CommandAction("CHAT_RESPONSE", dynamicResponse = "Cancelled.")
            }
            else -> CommandAction("CHAT_RESPONSE", dynamicResponse = "Processing feature...")
        }
    }

    private fun handleProjectUpdate(session: AssistantSession.ProjectPropertyUpdate, cmd: String): CommandAction {
        activeSessionFinished()
        return CommandAction("UPDATE_PROJECT_PROPERTY", "${session.projectTitle}|${session.property}|$cmd", "Updated ${session.property} to $cmd.")
    }

    private fun activeSessionFinished() {
        contextManager.setSession(null)
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
