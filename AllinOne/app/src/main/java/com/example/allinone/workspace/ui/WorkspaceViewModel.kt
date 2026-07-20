package com.example.allinone.workspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.domain.WorkspaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkspaceUIState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProject: ProjectEntity? = null,
    val goals: List<GoalEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val features: List<FeatureEntity> = emptyList(),
    val bugs: List<BugEntity> = emptyList(),
    val ideas: List<IdeaEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val resources: List<ResourceEntity> = emptyList(),
    val logs: List<ActivityLogEntity> = emptyList(),
    val isLoading: Boolean = false
)

class WorkspaceViewModel(private val repository: WorkspaceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUIState())
    val uiState: StateFlow<WorkspaceUIState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            repository.getAllProjects().collect { projects ->
                _uiState.update { it.copy(projects = projects) }
                if (projects.isNotEmpty() && _uiState.value.selectedProject == null) {
                    selectProject(projects.first().id)
                }
            }
        }
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            combine(
                repository.getProjectById(projectId),
                repository.getGoalsForProject(projectId),
                repository.getTasksForProject(projectId),
                repository.getFeaturesForProject(projectId),
                repository.getBugsForProject(projectId),
                repository.getIdeasForProject(projectId),
                repository.getNotesForProject(projectId),
                repository.getResourcesForProject(projectId),
                repository.getActivityLogs(projectId)
            ) { data ->
                WorkspaceUIState(
                    selectedProject = data[0] as ProjectEntity?,
                    goals = data[1] as List<GoalEntity>,
                    tasks = data[2] as List<TaskEntity>,
                    features = data[3] as List<FeatureEntity>,
                    bugs = data[4] as List<BugEntity>,
                    ideas = data[5] as List<IdeaEntity>,
                    notes = data[6] as List<NoteEntity>,
                    resources = data[7] as List<ResourceEntity>,
                    logs = data[8] as List<ActivityLogEntity>,
                    projects = _uiState.value.projects
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addProject(name: String, desc: String, color: Int = -1, icon: String = "") {
        viewModelScope.launch { repository.createProject(name, desc, color, icon) }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun addTask(
        title: String,
        projectId: String,
        description: String = "",
        priority: Int = 1,
        weight: Int = 1,
        dueDate: Long? = null,
        milestoneId: String? = null
    ) {
        viewModelScope.launch {
            repository.insertTask(TaskEntity(
                projectId = projectId,
                title = title,
                description = description,
                priority = priority,
                weight = weight,
                dueDate = dueDate,
                milestoneId = milestoneId
            ))
        }
    }

    fun addGoal(title: String, projectId: String, description: String = "") {
        viewModelScope.launch {
            repository.insertGoal(GoalEntity(projectId = projectId, title = title, description = description))
        }
    }

    fun addFeature(title: String, projectId: String, description: String = "", complexity: String = "Medium") {
        viewModelScope.launch {
            repository.insertFeature(FeatureEntity(projectId = projectId, title = title, description = description, complexity = complexity))
        }
    }

    fun addBug(
        title: String,
        projectId: String,
        description: String = "",
        severity: String = "Medium",
        steps: String = ""
    ) {
        viewModelScope.launch {
            repository.insertBug(BugEntity(
                projectId = projectId,
                title = title,
                description = description,
                severity = severity,
                stepsToReproduce = steps
            ))
        }
    }

    fun addIdea(
        title: String,
        projectId: String,
        description: String = "",
        impact: Int = 1,
        difficulty: Int = 1
    ) {
        viewModelScope.launch {
            repository.insertIdea(IdeaEntity(
                projectId = projectId,
                title = title,
                description = description,
                impact = impact,
                difficulty = difficulty
            ))
        }
    }

    fun addNote(title: String, content: String, projectId: String) {
        viewModelScope.launch {
            repository.insertNote(NoteEntity(projectId = projectId, title = title, content = content))
        }
    }

    fun importNote(note: com.example.allinone.Note) {
        viewModelScope.launch {
            repository.importFromNote(note)
            loadProjects()
        }
    }

    fun createSampleProject() {
        val sampleNote = com.example.allinone.Note(
            title = "Sample Workspace Project",
            content = "This is a sample project to showcase how the workspace works. It includes goals, tasks, and activity logs.",
            category = "Project"
        ).apply {
            subFeatures.add(com.example.allinone.ProjectFeature(name = "Review Workspace Features", weight = 2))
            subFeatures.add(com.example.allinone.ProjectFeature(name = "Add first task", isCompleted = true))
            ideaGoals.add(com.example.allinone.JournalEntry("Understand the layout"))
            journalEntries.add(com.example.allinone.JournalEntry("Successfully started exploring the new workspace!"))
        }
        importNote(sampleNote)
    }

    fun addResource(title: String, type: String, path: String, projectId: String) {
        viewModelScope.launch {
            repository.insertResource(ResourceEntity(projectId = projectId, title = title, type = type, pathOrUrl = path))
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun convertIdeaToTask(idea: IdeaEntity) {
        viewModelScope.launch {
            // Mark idea as converted
            repository.insertIdea(idea.copy(status = "Converted"))
            // Create task
            repository.insertTask(TaskEntity(
                projectId = idea.projectId,
                title = idea.title,
                description = idea.description
            ))
        }
    }
}
