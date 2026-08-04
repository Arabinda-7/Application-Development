package com.example.allinone.workspace.domain

import com.example.allinone.DataManager
import com.example.allinone.data.model.*
import com.example.allinone.workspace.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class WorkspaceRepository(private val dao: WorkspaceDao) {

    // Projects
    fun getAllProjects(): Flow<List<ProjectEntity>> = dao.getAllProjects()
    fun getProjectById(projectId: String): Flow<ProjectEntity?> = dao.getProjectById(projectId)

    suspend fun createProject(name: String, description: String = "", color: Int = -1, icon: String = "") {
        val project = ProjectEntity(name = name, description = description, color = color, iconRes = icon)
        dao.insertProject(project)
        logActivity(project.id, "PROJECT", project.id, "CREATE", "Project '$name' created")
        notifyChange()
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
        logActivity(project.id, "PROJECT", project.id, "UPDATE", "Project settings updated")
        notifyChange()
    }

    suspend fun deleteProject(project: ProjectEntity) {
        dao.deleteProject(project)
        notifyChange()
        // No logging needed for deleted project usually, but can be done if project exists in log still
    }

    // Tasks & Automations
    suspend fun updateTaskStatus(taskId: String, newStatus: String, progress: Int) {
        // Implementation will include automations
        val tasks = dao.getTasksForProject("").firstOrNull() // This is a simplification, we need the task first
        // Better:
    }

    // Since we need to handle automations, let's refine the update methods
    suspend fun updateTask(task: WorkspaceTaskEntity) {
        val updatedTask = task.copy(updatedAt = System.currentTimeMillis())
        dao.insertTask(updatedTask)
        
        logActivity(task.projectId, "TASK", task.id, "UPDATE", "Task '${task.title}' updated to ${task.status}")

        // 1. Task-to-Milestone Automation
        task.milestoneId?.let { milestoneId ->
            checkAndUpdateMilestone(task.projectId, milestoneId)
        }

        // 2. Recalculate Dashboard Rollup
        updateProjectRollup(task.projectId)
    }

    private suspend fun checkAndUpdateMilestone(projectId: String, milestoneId: String) {
        val siblingTasks = dao.getTasksByMilestone(milestoneId)
        val allDone = siblingTasks.all { it.status == "Done" || it.progress == 100 }
        
        if (allDone && siblingTasks.isNotEmpty()) {
            // Find if it's a Goal or Feature
            // For simplicity in this demo, let's assume it's a Goal
            val goals = dao.getGoalsForProject(projectId).firstOrNull()
            val goal = goals?.find { it.id == milestoneId }
            if (goal != null && goal.status != "Completed") {
                dao.updateGoal(goal.copy(status = "Completed", updatedAt = System.currentTimeMillis()))
                logActivity(projectId, "GOAL", milestoneId, "UPDATE", "Goal '${goal.title}' automatically completed")
                
                // 3. Milestone-to-Project Automation
                checkAndUpdateProject(projectId)
            }
        }
    }

    private suspend fun checkAndUpdateProject(projectId: String) {
        val goals = dao.getGoalsForProject(projectId).firstOrNull() ?: emptyList()
        val features = dao.getFeaturesForProject(projectId).firstOrNull() ?: emptyList()
        val tasks = dao.getTasksForProject(projectId).firstOrNull() ?: emptyList()
        val bugs = dao.getBugsForProject(projectId).firstOrNull() ?: emptyList()
        
        val allGoalsDone = goals.all { it.status == "Completed" }
        val allFeaturesDone = features.all { it.status == "Shipped" }
        val allTasksDone = tasks.all { it.status == "Done" }
        val allBugsFixed = bugs.all { it.status == "Fixed" || it.status == "Verified" }
        
        if (allGoalsDone && allFeaturesDone && allTasksDone && allBugsFixed && 
            (goals.isNotEmpty() || features.isNotEmpty() || tasks.isNotEmpty() || bugs.isNotEmpty())) {
            val project = dao.getProjectById(projectId).firstOrNull()
            if (project != null && project.status != "Completed") {
                dao.updateProject(project.copy(status = "Completed", updatedAt = System.currentTimeMillis()))
                logActivity(projectId, "PROJECT", projectId, "UPDATE", "Project '${project.name}' automatically completed")
            }
        }
    }

    private suspend fun updateProjectRollup(projectId: String) {
        val project = dao.getProjectById(projectId).firstOrNull() ?: return
        val tasks = dao.getTasksForProject(projectId).firstOrNull() ?: emptyList()
        
        if (tasks.isEmpty()) return

        val totalProgress = tasks.sumOf { it.progress } / tasks.size
        val weightedProgress = if (tasks.sumOf { it.weight } > 0) {
            tasks.sumOf { it.progress * it.weight } / tasks.sumOf { it.weight }
        } else 0

        // Health check: Overdue detection
        val now = System.currentTimeMillis()
        val overdueTasks = tasks.count { it.dueDate != null && it.dueDate < now && it.status != "Done" }
        val health = when {
            overdueTasks > 3 -> "Delayed"
            overdueTasks > 0 -> "At Risk"
            else -> "Healthy"
        }

        dao.updateProject(project.copy(
            progress = totalProgress,
            weightedProgress = weightedProgress,
            health = health,
            updatedAt = System.currentTimeMillis()
        ))
    }

    private fun notifyChange() {
        DataManager.notifyDataChanged()
    }

    private suspend fun logActivity(projectId: String, type: String, id: String, action: String, desc: String) {
        dao.insertActivityLog(ActivityLogEntity(
            projectId = projectId,
            entityType = type,
            entityId = id,
            action = action,
            description = desc
        ))
    }

    // CRUD Wrappers
    suspend fun insertGoal(goal: GoalEntity) {
        dao.insertGoal(goal)
        logActivity(goal.projectId, "GOAL", goal.id, "CREATE", "Goal '${goal.title}' created")
        notifyChange()
    }

    suspend fun updateGoal(goal: GoalEntity) {
        dao.updateGoal(goal)
        logActivity(goal.projectId, "GOAL", goal.id, "UPDATE", "Goal '${goal.title}' updated")
        notifyChange()
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        dao.deleteGoal(goal)
        logActivity(goal.projectId, "GOAL", goal.id, "DELETE", "Goal '${goal.title}' removed")
        notifyChange()
    }

    suspend fun insertTask(task: WorkspaceTaskEntity) = updateTask(task)

    suspend fun deleteTask(task: WorkspaceTaskEntity) {
        dao.deleteTask(task)
        logActivity(task.projectId, "TASK", task.id, "DELETE", "Task '${task.title}' removed")
        // Trigger progress update
        checkAndUpdateMilestone(task.projectId, task.milestoneId ?: "")
        checkAndUpdateProject(task.projectId)
        notifyChange()
    }

    suspend fun insertFeature(feature: FeatureEntity) {
        dao.insertFeature(feature)
        logActivity(feature.projectId, "FEATURE", feature.id, "CREATE", "Feature '${feature.title}' planned")
        notifyChange()
    }

    suspend fun updateFeature(feature: FeatureEntity) {
        dao.updateFeature(feature.copy(updatedAt = System.currentTimeMillis()))
        logActivity(feature.projectId, "FEATURE", feature.id, "UPDATE", "Feature '${feature.title}' updated")
        notifyChange()
    }

    suspend fun deleteFeature(feature: FeatureEntity) {
        dao.deleteFeature(feature)
        notifyChange()
    }

    suspend fun convertIdeaToFeature(idea: IdeaEntity) {
        val feature = FeatureEntity(
            projectId = idea.projectId,
            title = idea.title,
            description = idea.description,
            status = "Backlog"
        )
        dao.insertFeature(feature)
        dao.deleteIdea(idea)
        logActivity(idea.projectId, "FEATURE", feature.id, "CONVERT", "Idea graduated to Feature: '${idea.title}'")
        notifyChange()
    }

    suspend fun generateFeatureTasks(feature: FeatureEntity) {
        val tasks = listOf(
            WorkspaceTaskEntity(projectId = feature.projectId, milestoneId = feature.id, title = "UI Design - ${feature.title}", description = "Design the user interface and interactions"),
            WorkspaceTaskEntity(projectId = feature.projectId, milestoneId = feature.id, title = "Implementation - ${feature.title}", description = "Core logic and data integration"),
            WorkspaceTaskEntity(projectId = feature.projectId, milestoneId = feature.id, title = "Testing & QA - ${feature.title}", description = "Unit tests and user acceptance testing")
        )
        tasks.forEach { dao.insertTask(it) }
        logActivity(feature.projectId, "FEATURE", feature.id, "AUTOMATION", "Generated boilerplate tasks for '${feature.title}'")
        notifyChange()
    }

    suspend fun insertBug(bug: BugEntity) {
        dao.insertBug(bug)
        logActivity(bug.projectId, "BUG", bug.id, "CREATE", "Bug '${bug.title}' reported")
        notifyChange()
    }

    suspend fun updateBug(bug: BugEntity) {
        val oldBug = dao.getBugsForProject(bug.projectId).firstOrNull()?.find { it.id == bug.id }
        val updatedBug = bug.copy(updatedAt = System.currentTimeMillis())
        dao.updateBug(updatedBug)
        
        if (oldBug != null && oldBug.status != bug.status) {
            logActivity(bug.projectId, "BUG", bug.id, "STATUS", "Bug '${bug.title}' status: ${bug.status}")
            
            // Automation: Auto-Task creation on Confirmation
            if (bug.status == "Confirmed" && bug.linkedTaskId == null) {
                val taskId = UUID.randomUUID().toString()
                val task = WorkspaceTaskEntity(
                    id = taskId,
                    projectId = bug.projectId,
                    title = "FIX BUG: ${bug.title}",
                    description = "Resolution for Bug ID: ${bug.id}\n\nSteps: ${bug.stepsToReproduce}",
                    priority = bug.priority
                )
                dao.insertTask(task)
                // Link them
                dao.updateBug(updatedBug.copy(linkedTaskId = taskId))
                logActivity(bug.projectId, "BUG", bug.id, "AUTOMATION", "Auto-generated fix task")
            }
        }
        notifyChange()
    }

    suspend fun deleteBug(bug: BugEntity) {
        dao.deleteBug(bug)
        logActivity(bug.projectId, "BUG", bug.id, "DELETE", "Bug '${bug.title}' removed")
        notifyChange()
    }

    suspend fun insertIdea(idea: IdeaEntity) {
        dao.insertIdea(idea)
        logActivity(idea.projectId, "IDEA", idea.id, "CREATE", "New Idea: '${idea.title}'")
        notifyChange()
    }

    suspend fun updateIdea(idea: IdeaEntity) {
        dao.updateIdea(idea.copy(updatedAt = System.currentTimeMillis()))
        notifyChange()
    }

    suspend fun deleteIdea(idea: IdeaEntity) {
        dao.deleteIdea(idea)
        notifyChange()
    }

    suspend fun insertNote(note: WorkspaceNoteEntity) {
        dao.insertNote(note)
        logActivity(note.projectId, "NOTE", note.id, "CREATE", "Note '${note.title}' added")
        notifyChange()
    }

    suspend fun updateNote(note: WorkspaceNoteEntity) {
        dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        notifyChange()
    }

    suspend fun deleteNote(note: WorkspaceNoteEntity) {
        dao.deleteNote(note)
        logActivity(note.projectId, "NOTE", note.id, "DELETE", "Note '${note.title}' removed")
        notifyChange()
    }

    suspend fun insertResource(resource: ResourceEntity) {
        dao.insertResource(resource)
        logActivity(resource.projectId, "RESOURCE", resource.id, "CREATE", "Resource '${resource.title}' linked")
        notifyChange()
    }

    suspend fun updateResource(resource: ResourceEntity) {
        dao.updateResource(resource.copy(updatedAt = System.currentTimeMillis()))
        notifyChange()
    }

    suspend fun deleteResource(resource: ResourceEntity) {
        dao.deleteResource(resource)
        notifyChange()
    }

    fun getTasksForProject(projectId: String) = dao.getTasksForProject(projectId)
    fun getGoalsForProject(projectId: String) = dao.getGoalsForProject(projectId)
    fun getFeaturesForProject(projectId: String) = dao.getFeaturesForProject(projectId)
    fun getBugsForProject(projectId: String) = dao.getBugsForProject(projectId)
    fun getIdeasForProject(projectId: String) = dao.getIdeasForProject(projectId)
    fun getNotesForProject(projectId: String) = dao.getNotesForProject(projectId)
    fun getResourcesForProject(projectId: String) = dao.getResourcesForProject(projectId)
    fun getActivityLogs(projectId: String) = dao.getActivityLogs(projectId)

    suspend fun getDeadlinesForToday(start: Long, end: Long): List<Any> {
        val projects = dao.getProjectsDueBefore(end)
        val goals = dao.getGoalsDueBefore(end)
        val tasks = dao.getTasksDueBefore(end)
        val features = dao.getFeaturesDueBefore(end)
        val bugs = dao.getBugsDueBefore(end)
        return projects + goals + tasks + features + bugs
    }

    suspend fun importFromNote(note: Note) {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = note.title,
            description = note.content,
            color = note.color,
            status = if (note.status == "Completed") "Completed" else "Active",
            progress = note.progress,
            deadline = note.deadline,
            createdAt = note.timestamp
        )
        dao.insertProject(project)

        // Map sub-features to tasks
        note.subFeatures.forEach { feature ->
            dao.insertTask(WorkspaceTaskEntity(
                projectId = projectId,
                title = feature.name,
                description = feature.details,
                progress = if (feature.isCompleted) 100 else 0,
                status = if (feature.isCompleted) "Done" else "Todo",
                weight = feature.weight,
                priority = feature.priority,
                dueDate = feature.dueDate
            ))
        }

        // Map idea goals to goals
        note.ideaGoals.forEach { goal ->
            dao.insertGoal(GoalEntity(
                projectId = projectId,
                title = goal.text,
                status = "Pending",
                createdAt = goal.timestamp
            ))
        }

        // Map journal entries to notes
        note.journalEntries.forEach { journal ->
            dao.insertNote(WorkspaceNoteEntity(
                projectId = projectId,
                title = "Journal Entry",
                content = journal.text,
                createdAt = journal.timestamp
            ))
        }

        // Map history to activity logs
        note.changeHistory.forEach { history ->
            dao.insertActivityLog(ActivityLogEntity(
                projectId = projectId,
                entityType = "PROJECT",
                entityId = projectId,
                action = history.action,
                description = history.description,
                timestamp = history.timestamp
            ))
        }

        logActivity(projectId, "PROJECT", projectId, "IMPORT", "Project '${note.title}' imported from AllInOne")
        notifyChange()
    }
}
