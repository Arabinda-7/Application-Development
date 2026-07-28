package com.example.allinone.workspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.domain.WorkspaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkspaceViewModel(private val repository: WorkspaceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUIState())
    val uiState: StateFlow<WorkspaceUIState> = _uiState.asStateFlow()

    private var selectionJob: kotlinx.coroutines.Job? = null

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            repository.getAllProjects().collect { projects ->
                if (projects.isEmpty()) {
                    _uiState.update { it.copy(projects = projects, isLoading = false) }
                } else {
                    _uiState.update { it.copy(projects = projects) }
                    if (_uiState.value.selectedProject == null) {
                        selectProject(projects.first().id)
                    }
                }
            }
        }
    }

    fun selectProject(projectId: String) {
        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val projectFlow = repository.getProjectById(projectId)
            val goalsFlow = repository.getGoalsForProject(projectId)
            val tasksFlow = repository.getTasksForProject(projectId)
            val featuresFlow = repository.getFeaturesForProject(projectId)
            val bugsFlow = repository.getBugsForProject(projectId)
            val ideasFlow = repository.getIdeasForProject(projectId)
            val notesFlow = repository.getNotesForProject(projectId)
            val resourcesFlow = repository.getResourcesForProject(projectId)
            val logsFlow = repository.getActivityLogs(projectId)
            val allProjectsFlow = repository.getAllProjects()

            combine(
                projectFlow, goalsFlow, tasksFlow, featuresFlow, bugsFlow, 
                ideasFlow, notesFlow, resourcesFlow, logsFlow, allProjectsFlow
            ) { array ->
                try {
                    WorkspaceUIState(
                        selectedProject = array[0] as? ProjectEntity,
                        goals = array[1] as List<GoalEntity>,
                        tasks = array[2] as List<TaskEntity>,
                        features = array[3] as List<FeatureEntity>,
                        bugs = array[4] as List<BugEntity>,
                        ideas = array[5] as List<IdeaEntity>,
                        notes = array[6] as List<NoteEntity>,
                        resources = array[7] as List<ResourceEntity>,
                        logs = array[8] as List<ActivityLogEntity>,
                        projects = array[9] as List<ProjectEntity>
                    )
                } catch (e: Exception) {
                    android.util.Log.e("WorkspaceViewModel", "Mapping error in combine", e)
                    _uiState.value 
                }
            }.catch { e ->
                android.util.Log.e("WorkspaceViewModel", "Flow error in combine", e)
            }.collect { newState ->
                _uiState.value = newState.copy(isLoading = false)
            }
        }
    }

    fun addProject(name: String, desc: String, color: Int = -1, icon: String = "") {
        viewModelScope.launch { repository.createProject(name, desc, color, icon) }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch { repository.updateProject(project) }
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
        status: String = "Todo",
        progress: Int = 0,
        weight: Int = 1,
        dueDate: Long? = null,
        milestoneId: String? = null
    ) {
        com.example.allinone.DataManager.checkAndSetNewTodayNotification(dueDate)
        viewModelScope.launch {
            repository.insertTask(TaskEntity(
                projectId = projectId,
                title = title,
                description = description,
                priority = priority,
                status = status,
                progress = progress,
                weight = weight,
                dueDate = dueDate,
                milestoneId = milestoneId
            ))
        }
    }

    fun insertTask(task: TaskEntity) {
        com.example.allinone.DataManager.checkAndSetNewTodayNotification(task.dueDate)
        viewModelScope.launch { repository.insertTask(task) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun addGoal(title: String, projectId: String, description: String = "", color: Int = -1, priority: Int = 1, deadline: Long? = null) {
        com.example.allinone.DataManager.checkAndSetNewTodayNotification(deadline)
        viewModelScope.launch {
            repository.insertGoal(GoalEntity(
                projectId = projectId, 
                title = title, 
                description = description,
                color = color,
                priority = priority,
                deadline = deadline
            ))
        }
    }

    fun updateGoal(goal: GoalEntity) {
        com.example.allinone.DataManager.checkAndSetNewTodayNotification(goal.deadline)
        viewModelScope.launch { repository.updateGoal(goal) }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    fun addFeature(
        title: String,
        projectId: String,
        description: String = "",
        complexity: String = "Medium",
        effort: String = "M",
        requirements: String = "",
        version: String = "",
        status: String = "Backlog"
    ) {
        viewModelScope.launch {
            repository.insertFeature(FeatureEntity(
                projectId = projectId,
                title = title,
                description = description,
                complexity = complexity,
                effortSize = effort,
                requirements = requirements,
                targetVersion = version,
                status = status
            ))
        }
    }

    fun updateFeature(feature: FeatureEntity) {
        viewModelScope.launch { repository.updateFeature(feature) }
    }

    fun deleteFeature(feature: FeatureEntity) {
        viewModelScope.launch { repository.deleteFeature(feature) }
    }

    fun graduateIdea(idea: IdeaEntity) {
        viewModelScope.launch { repository.convertIdeaToFeature(idea) }
    }

    fun quickTasks(feature: FeatureEntity) {
        viewModelScope.launch { repository.generateFeatureTasks(feature) }
    }

    fun addBug(
        title: String,
        projectId: String,
        description: String = "",
        severity: String = "Medium",
        priority: Int = 1,
        environment: String = "Production",
        version: String = "",
        steps: String = ""
    ) {
        viewModelScope.launch {
            repository.insertBug(BugEntity(
                projectId = projectId,
                title = title,
                description = description,
                severity = severity,
                priority = priority,
                environment = environment,
                version = version,
                stepsToReproduce = steps
            ))
        }
    }

    fun updateBug(bug: BugEntity) {
        viewModelScope.launch { repository.updateBug(bug) }
    }

    fun deleteBug(bug: BugEntity) {
        viewModelScope.launch { repository.deleteBug(bug) }
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

    fun updateIdea(idea: IdeaEntity) {
        viewModelScope.launch { repository.updateIdea(idea) }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch { repository.deleteIdea(idea) }
    }

    fun addNote(title: String, content: String, projectId: String) {
        viewModelScope.launch {
            repository.insertNote(NoteEntity(projectId = projectId, title = title, content = content))
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun importNote(note: com.example.allinone.Note, isTransfer: Boolean = false) {
        viewModelScope.launch {
            repository.importFromNote(note)
            if (isTransfer) {
                com.example.allinone.DataManager.projects.remove(note)
            }
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

    fun updateResource(resource: ResourceEntity) {
        viewModelScope.launch { repository.updateResource(resource) }
    }

    fun deleteResource(resource: ResourceEntity) {
        viewModelScope.launch { repository.deleteResource(resource) }
    }

    fun updateTask(task: TaskEntity) {
        com.example.allinone.DataManager.checkAndSetNewTodayNotification(task.dueDate)
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun getProjectStats(projectId: String): Flow<ProjectStats> {
        return combine(
            repository.getTasksForProject(projectId),
            repository.getFeaturesForProject(projectId),
            repository.getBugsForProject(projectId)
        ) { tasks, features, bugs ->
            ProjectStats(
                totalTasks = tasks.size,
                taskBreakdown = tasks.groupingBy { it.status }.eachCount(),
                totalFeatures = features.size,
                featureBreakdown = features.groupingBy { it.status }.eachCount(),
                totalBugs = bugs.size,
                bugBreakdown = bugs.groupingBy { it.status }.eachCount()
            )
        }
    }

    fun convertIdeaToTask(idea: IdeaEntity) {
        viewModelScope.launch {
            repository.insertIdea(idea.copy(status = "Converted"))
            repository.insertTask(TaskEntity(
                projectId = idea.projectId,
                title = idea.title,
                description = idea.description
            ))
        }
    }

    fun canCompleteProject(projectId: String): Pair<Boolean, String> {
        val state = _uiState.value
        if (state.selectedProject?.id != projectId) return true to ""

        val unfinishedTasks = state.tasks.count { it.status != "Done" }
        val unfinishedFeatures = state.features.count { it.status != "Shipped" }
        val unfinishedBugs = state.bugs.count { it.status != "Fixed" && it.status != "Verified" }
        val unfinishedGoals = state.goals.count { it.status != "Completed" }

        val totalUnfinished = unfinishedTasks + unfinishedFeatures + unfinishedBugs + unfinishedGoals
        if (totalUnfinished == 0) return true to ""

        val message = buildString {
            append("Cannot complete project. Pending items: ")
            if (unfinishedTasks > 0) append("$unfinishedTasks Tasks, ")
            if (unfinishedFeatures > 0) append("$unfinishedFeatures Features, ")
            if (unfinishedBugs > 0) append("$unfinishedBugs Bugs, ")
            if (unfinishedGoals > 0) append("$unfinishedGoals Goals, ")
        }.removeSuffix(", ")

        return false to message
    }
}
