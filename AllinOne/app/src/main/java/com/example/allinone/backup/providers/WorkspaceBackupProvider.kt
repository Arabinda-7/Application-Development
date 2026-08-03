package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.workspace.data.WorkspaceDao
import com.example.allinone.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class WorkspaceBackupProvider @Inject constructor(
    private val workspaceDao: WorkspaceDao,
    private val projectRepository: ProjectRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "workspace_module"

    override suspend fun exportData(): JsonElement {
        return buildJsonObject {
            put("projects", json.encodeToJsonElement(workspaceDao.getAllProjectsSync()))
            put("goals", json.encodeToJsonElement(workspaceDao.getAllGoalsSync()))
            put("tasks", json.encodeToJsonElement(workspaceDao.getAllTasksSync()))
            put("features", json.encodeToJsonElement(workspaceDao.getAllFeaturesSync()))
            put("bugs", json.encodeToJsonElement(workspaceDao.getAllBugsSync()))
            put("ideas", json.encodeToJsonElement(workspaceDao.getAllIdeasSync()))
            put("notes", json.encodeToJsonElement(workspaceDao.getAllNotesSync()))
            put("resources", json.encodeToJsonElement(workspaceDao.getAllResourcesSync()))
            put("logs", json.encodeToJsonElement(workspaceDao.getAllActivityLogsSync()))
            put("refs", json.encodeToJsonElement(workspaceDao.getAllNoteCrossReferencesSync()))
            put("settings", json.encodeToJsonElement(projectRepository.getProjectSettings().first()))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["projects"]?.let { workspaceDao.deleteAllProjects(); workspaceDao.insertAllProjects(json.decodeFromJsonElement(it)) }
        root["goals"]?.let { workspaceDao.deleteAllGoals(); workspaceDao.insertAllGoals(json.decodeFromJsonElement(it)) }
        root["tasks"]?.let { workspaceDao.deleteAllTasks(); workspaceDao.insertAllTasks(json.decodeFromJsonElement(it)) }
        root["features"]?.let { workspaceDao.deleteAllFeatures(); workspaceDao.insertAllFeatures(json.decodeFromJsonElement(it)) }
        root["bugs"]?.let { workspaceDao.deleteAllBugs(); workspaceDao.insertAllBugs(json.decodeFromJsonElement(it)) }
        root["ideas"]?.let { workspaceDao.deleteAllIdeas(); workspaceDao.insertAllIdeas(json.decodeFromJsonElement(it)) }
        root["notes"]?.let { workspaceDao.deleteAllNotes(); workspaceDao.insertAllNotes(json.decodeFromJsonElement(it)) }
        root["resources"]?.let { workspaceDao.deleteAllResources(); workspaceDao.insertAllResources(json.decodeFromJsonElement(it)) }
        root["logs"]?.let { workspaceDao.deleteAllActivityLogs(); workspaceDao.insertAllActivityLogs(json.decodeFromJsonElement(it)) }
        root["refs"]?.let { workspaceDao.deleteAllNoteCrossReferences(); workspaceDao.insertAllNoteCrossReferences(json.decodeFromJsonElement(it)) }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.ProjectSettings = json.decodeFromJsonElement(it)
            projectRepository.updateSettings(settings)
        }
    }
}
