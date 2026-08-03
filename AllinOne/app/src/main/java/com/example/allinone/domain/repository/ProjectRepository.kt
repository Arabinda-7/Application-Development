package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import com.example.allinone.data.model.Note
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Note>>
    suspend fun insertProject(project: Note)
    suspend fun updateProject(project: Note)
    suspend fun deleteProject(project: Note)
    
    // Settings & Shared State
    fun getProjectSettings(): Flow<ProjectSettings>
    suspend fun updateSettings(settings: ProjectSettings)
}

@Serializable
data class ProjectSettings(
    val autoArchive: Boolean = false,
    val synergySync: Boolean = false,
    val deadlineAlerts: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val customTags: List<String> = listOf("TASKS", "NOTES", "FEATURES", "BUGS", "RESOURCES"),
    val sortCompletedToBottom: Boolean = true,
    val activeExpanded: Boolean = true,
    val completedExpanded: Boolean = false,
    val ideaActiveExpanded: Boolean = true,
    val ideaCompletedExpanded: Boolean = false,
    val autoSaveIdeas: Boolean = true,
    val dualExistEnabled: Boolean = false,
    val ideasEnabled: Boolean = true,
    val roadmapsEnabled: Boolean = true,
    val projectTemplates: Map<String, List<String>> = mapOf(
        "App Feature" to listOf("UI Design", "Business Logic", "Integration", "Testing", "Deployment"),
        "Personal Goal" to listOf("Planning", "Execution", "Review"),
        "Bug Fix" to listOf("Reproduction", "Debugging", "Fix", "Verification")
    ),
    val globalProjectColor: Int = -1,
    val projectAddThemeColor: Int = -1,
    val globalProjectIcon: Int = -1
)
