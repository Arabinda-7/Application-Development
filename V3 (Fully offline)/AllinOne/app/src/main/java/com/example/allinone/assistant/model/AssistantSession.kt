package com.example.allinone.assistant.model

import com.example.allinone.data.model.ProjectFeature

sealed class AssistantSession {
    data class HabitCreation(
        var step: HabitStep = HabitStep.TITLE,
        var tempName: String = "",
        var tempTarget: Int = 1,
        var tempFrequency: String = "Anytime"
    ) : AssistantSession()

    data class HabitCompletion(
        var step: CompletionStep = CompletionStep.NAME,
        var tempName: String = ""
    ) : AssistantSession()

    data class WorkoutCreation(
        var step: WorkoutStep = WorkoutStep.TITLE,
        var tempName: String = "",
        var tempMode: String = "Reps",
        var tempTarget: Int = 0,
        var tempRepsPerSet: Int = 0,
        var tempFrequency: String = "Anytime"
    ) : AssistantSession()

    data class WorkoutCompletion(
        var step: CompletionStep = CompletionStep.NAME,
        var tempName: String = ""
    ) : AssistantSession()

    data class TaskCreation(
        var step: TaskStep = TaskStep.NAME,
        var tempName: String = "",
        val tempSubtasks: MutableList<String> = mutableListOf(),
        var tempReminderDate: String? = null,
        var tempReminderTime: String? = null
    ) : AssistantSession()

    data class NoteCreation(
        var step: NoteStep = NoteStep.TITLE,
        var tempTitle: String = "",
        var tempContent: String = ""
    ) : AssistantSession()

    data class ProjectCreation(
        var step: ProjectStep = ProjectStep.TITLE,
        var tempTitle: String = "",
        var tempDesc: String = "",
        var tempStatus: String = "Not Started",
        var tempPriority: Int = 1,
        var tempDeadline: Long? = null,
        val tempGoals: MutableList<String> = mutableListOf(),
        val tempFeatures: MutableList<ProjectFeature> = mutableListOf(),
        var currentFeatureName: String = ""
    ) : AssistantSession()

    data class FeatureAddition(
        var projectTitle: String,
        var step: FeatureStep = FeatureStep.NAME,
        var tempName: String = "",
        var tempTag: String = "",
        var tempDeadline: Long? = null
    ) : AssistantSession()

    data class ProjectPropertyUpdate(
        var projectTitle: String,
        var property: String, // "STATUS", "PRIORITY", "DEADLINE"
        var step: UpdateStep = UpdateStep.VALUE
    ) : AssistantSession()

    data class FeatureCompletion(
        var projectTitle: String,
        var step: CompletionPromptStep = CompletionPromptStep.NAME,
        var featureName: String = ""
    ) : AssistantSession()

    data class NoteToProjectConversion(
        var noteTitle: String,
        var step: ConversionStep = ConversionStep.CONFIRM
    ) : AssistantSession()
}

enum class HabitStep { TITLE, CONFIRM_DEFAULT, TARGET, FREQUENCY }
enum class CompletionStep { NAME, CONFIRM }
enum class WorkoutStep { TITLE, MODE, TARGET, CONFIRM_DEFAULT, FREQUENCY }
enum class TaskStep { NAME, SUBTASK_PROMPT, SUBTASK_NAME, REMINDER_PROMPT, REMINDER_DATE, REMINDER_TIME }
enum class NoteStep { TITLE, CONTENT }
enum class ProjectStep { TITLE, DESC, STATUS, PRIORITY, DEADLINE, GOAL_PROMPT, GOAL_NAME, FEATURE_PROMPT, FEATURE_NAME, FEATURE_TAG, FEATURE_DEADLINE, CONFIRM }
enum class FeatureStep { NAME, TAG, DEADLINE, CONFIRM }
enum class UpdateStep { VALUE, CONFIRM }
enum class CompletionPromptStep { NAME, NOTE, CONFIRM }
enum class ConversionStep { CONFIRM }
