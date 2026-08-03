package com.example.allinone.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.allinone.data.database.AppNoteDao
import com.example.allinone.data.database.GlobalNoteEntity
import com.example.allinone.domain.repository.ProjectSettings
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteDao: AppNoteDao,
    private val gson: Gson
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<ProjectSettings> = _settings.asStateFlow()

    fun getAllProjects(): Flow<List<GlobalNoteEntity>> {
        return noteDao.getAllNotes().map { notes ->
            notes.filter { it.isGlobalProject }
        }
    }

    suspend fun insertProject(project: GlobalNoteEntity) = noteDao.insertNote(project)
    
    suspend fun deleteProject(project: GlobalNoteEntity) = noteDao.deleteNote(project)

    fun updateSettings(newSettings: ProjectSettings) {
        prefs.edit().apply {
            putBoolean("project_auto_archive", newSettings.autoArchive)
            putBoolean("project_synergy_sync", newSettings.synergySync)
            putBoolean("project_deadline_alerts", newSettings.deadlineAlerts)
            putBoolean("project_analytics_enabled", newSettings.analyticsEnabled)
            putString("project_custom_tags_data", gson.toJson(newSettings.customTags))
            putBoolean("project_sort_completed_bottom", newSettings.sortCompletedToBottom)
            putBoolean("project_active_expanded", newSettings.activeExpanded)
            putBoolean("project_completed_expanded", newSettings.completedExpanded)
            putBoolean("idea_active_expanded", newSettings.ideaActiveExpanded)
            putBoolean("idea_completed_expanded", newSettings.ideaCompletedExpanded)
            putBoolean("project_autosave_ideas", newSettings.autoSaveIdeas)
            putBoolean("project_dual_exist_enabled", newSettings.dualExistEnabled)
            putBoolean("project_ideas_enabled", newSettings.ideasEnabled)
            putBoolean("project_roadmaps_enabled", newSettings.roadmapsEnabled)
            putString("project_templates", gson.toJson(newSettings.projectTemplates))
            putInt("global_project_color", newSettings.globalProjectColor)
            putInt("project_add_theme_color", newSettings.projectAddThemeColor)
            putInt("global_project_icon", newSettings.globalProjectIcon)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): ProjectSettings {
        val tagsJson = prefs.getString("project_custom_tags_data", "[\"TASKS\", \"NOTES\", \"FEATURES\", \"BUGS\", \"RESOURCES\"]")
        val tags: List<String> = try {
            gson.fromJson(tagsJson, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { listOf("TASKS", "NOTES", "FEATURES", "BUGS", "RESOURCES") }

        val templatesJson = prefs.getString("project_templates", "{}")
        val templates: Map<String, List<String>> = try {
            gson.fromJson(templatesJson, object : TypeToken<Map<String, List<String>>>() {}.type)
        } catch (e: Exception) { emptyMap() }

        return ProjectSettings(
            autoArchive = prefs.getBoolean("project_auto_archive", false),
            synergySync = prefs.getBoolean("project_synergy_sync", false),
            deadlineAlerts = prefs.getBoolean("project_deadline_alerts", true),
            analyticsEnabled = prefs.getBoolean("project_analytics_enabled", false),
            customTags = if (tags.isEmpty()) listOf("TASKS", "NOTES", "FEATURES", "BUGS", "RESOURCES") else tags,
            sortCompletedToBottom = prefs.getBoolean("project_sort_completed_bottom", true),
            activeExpanded = prefs.getBoolean("project_active_expanded", true),
            completedExpanded = prefs.getBoolean("project_completed_expanded", false),
            ideaActiveExpanded = prefs.getBoolean("idea_active_expanded", true),
            ideaCompletedExpanded = prefs.getBoolean("idea_completed_expanded", false),
            autoSaveIdeas = prefs.getBoolean("project_autosave_ideas", true),
            dualExistEnabled = prefs.getBoolean("project_dual_exist_enabled", false),
            ideasEnabled = prefs.getBoolean("project_ideas_enabled", true),
            roadmapsEnabled = prefs.getBoolean("project_roadmaps_enabled", true),
            projectTemplates = if (templates.isEmpty()) mapOf(
                "App Feature" to listOf("UI Design", "Business Logic", "Integration", "Testing", "Deployment"),
                "Personal Goal" to listOf("Planning", "Execution", "Review"),
                "Bug Fix" to listOf("Reproduction", "Debugging", "Fix", "Verification")
            ) else templates,
            globalProjectColor = prefs.getInt("global_project_color", -1),
            projectAddThemeColor = prefs.getInt("project_add_theme_color", -1),
            globalProjectIcon = prefs.getInt("global_project_icon", -1)
        )
    }
}
