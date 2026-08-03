package com.example.allinone.workspace.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.allinone.workspace.data.*

enum class WorkspaceTab(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Goals("Goals", Icons.Default.Flag),
    Tasks("Tasks", Icons.Default.Checklist),
    Notes("Notes", Icons.Default.Description),
    Features("Features", Icons.Default.Extension),
    Bugs("Bugs", Icons.Default.BugReport),
    Ideas("Ideas", Icons.Default.Lightbulb),
    Resources("Resources", Icons.Default.Folder),
    ActivityLog("Activity", Icons.Default.History)
}

enum class WorkspaceAction(val title: String, val icon: ImageVector) {
    AddProject("New Project", Icons.Default.CreateNewFolder),
    EditProject("Edit Project", Icons.Default.Edit),
    ImportProject("Import Project", Icons.Default.UploadFile),
    
    AddTask("Task", Icons.Default.Add),
    ViewTask("Task Details", Icons.Default.Description),
    EditTask("Edit Task", Icons.Default.Edit),
    
    AddGoal("Goal", Icons.Default.Flag),
    ViewGoal("Goal Details", Icons.Default.Description),
    EditGoal("Edit Goal", Icons.Default.Edit),
    
    AddFeature("Feature", Icons.Default.Extension),
    ViewFeature("Feature Details", Icons.Default.Description),
    EditFeature("Edit Feature", Icons.Default.Edit),
    
    AddBug("Bug", Icons.Default.BugReport),
    ViewBug("Bug Details", Icons.Default.Description),
    EditBug("Edit Bug", Icons.Default.Edit),
    
    AddIdea("Idea", Icons.Default.Lightbulb),
    ViewIdea("Idea Details", Icons.Default.Description),
    EditIdea("Edit Idea", Icons.Default.Edit),
    
    AddNote("Note", Icons.Default.NoteAdd),
    ViewNote("Note Details", Icons.Default.Description),
    EditNote("Edit Note", Icons.Default.Edit),
    
    AddResource("Resource", Icons.Default.Link),
    ViewResource("Resource Details", Icons.Default.Description),
    EditResource("Edit Resource", Icons.Default.Edit)
}

data class WorkspaceUIState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProject: ProjectEntity? = null,
    val goals: List<GoalEntity> = emptyList(),
    val tasks: List<WorkspaceTaskEntity> = emptyList(),
    val features: List<FeatureEntity> = emptyList(),
    val bugs: List<BugEntity> = emptyList(),
    val ideas: List<IdeaEntity> = emptyList(),
    val notes: List<WorkspaceNoteEntity> = emptyList(),
    val resources: List<ResourceEntity> = emptyList(),
    val logs: List<ActivityLogEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class ProjectStats(
    val totalTasks: Int = 0,
    val taskBreakdown: Map<String, Int> = emptyMap(),
    val totalFeatures: Int = 0,
    val featureBreakdown: Map<String, Int> = emptyMap(),
    val totalBugs: Int = 0,
    val bugBreakdown: Map<String, Int> = emptyMap()
)
