package com.example.allinone.domain.usecase.agenda

import com.example.allinone.domain.model.AgendaItem
import com.example.allinone.domain.repository.TaskRepository
import com.example.allinone.domain.repository.ProjectRepository
import com.example.allinone.data.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.*
import javax.inject.Inject

class GetTodayAgendaUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val database: AppDatabase
) {
    operator fun invoke(): Flow<Map<String, List<AgendaItem>>> {
        val dao = database.workspaceDao()
        
        return combine(
            taskRepository.getTasks(),
            projectRepository.getAllProjects(),
            dao.getAllProjects(),
            dao.getAllGoals(),
            dao.getAllTasks(),
            dao.getAllFeatures(),
            dao.getAllBugs()
        ) { args ->
            val globalTasks = args[0] as List<com.example.allinone.data.model.Task>
            val globalProjects = args[1] as List<com.example.allinone.data.model.Note>
            val wsProjects = args[2] as List<com.example.allinone.workspace.data.ProjectEntity>
            val wsGoals = args[3] as List<com.example.allinone.workspace.data.GoalEntity>
            val wsTasks = args[4] as List<com.example.allinone.workspace.data.WorkspaceTaskEntity>
            val wsFeatures = args[5] as List<com.example.allinone.workspace.data.FeatureEntity>
            val wsBugs = args[6] as List<com.example.allinone.workspace.data.BugEntity>

            val list = mutableListOf<AgendaItem>()
            val now = Calendar.getInstance()
            val todayStart = now.clone() as Calendar
            todayStart.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayEnd = todayStart.timeInMillis + 86400000
            val startMs = todayStart.timeInMillis

            // 1. Global Tasks
            globalTasks.forEach { task ->
                val isDueTodayOrOverdue = task.reminderTime?.let { it < todayEnd } ?: (task.timestamp in startMs until todayEnd)
                if (!task.isCompleted && isDueTodayOrOverdue) {
                    list.add(AgendaItem(
                        id = task.name,
                        title = task.name,
                        time = task.reminderTime ?: 0L,
                        category = "TASKS",
                        priority = when(task.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                        navigationTarget = "TASK_ACTIVITY",
                        color = if (task.color != -1) task.color else taskRepository.getGlobalTaskColor()
                    ))
                }
            }

            // 2. Global Projects
            globalProjects.forEach { project ->
                val projColor = if (project.color != -1) project.color else -1
                
                project.deadline?.let { deadline ->
                    if (project.status != "Completed" && deadline < todayEnd) {
                        list.add(AgendaItem(
                            id = project.timestamp.toString(),
                            title = project.title,
                            time = deadline,
                            category = "PROJECTS",
                            priority = when(project.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                            navigationTarget = "PROJECT_ACTIVITY",
                            color = projColor
                        ))
                    }
                }
                
                project.subFeatures.forEach { f ->
                    f.dueDate?.let { dueDate ->
                        if (!f.isCompleted && dueDate < todayEnd) {
                            list.add(AgendaItem(
                                id = f.id ?: UUID.randomUUID().toString(),
                                parentId = project.timestamp.toString(),
                                title = f.name ?: "Untitled Milestone",
                                time = dueDate,
                                category = "MILESTONES",
                                priority = when(f.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                                navigationTarget = "PROJECT_ACTIVITY",
                                color = projColor
                            ))
                        }
                    }
                }
            }

            // 3. Workspace Items
            wsProjects.filter { it.deadline != null && it.deadline!! < todayEnd && it.status != "Archived" && it.status != "Completed" }.forEach {
                list.add(AgendaItem(id = it.id, title = it.name, time = it.deadline!!, category = "WORKSPACES", navigationTarget = "WORKSPACE", color = if (it.color != -1) it.color else -1))
            }
            
            wsGoals.filter { it.deadline != null && it.deadline!! < todayEnd && it.status != "Completed" }.forEach {
                list.add(AgendaItem(id = it.id, title = it.title, time = it.deadline!!, category = "WORKSPACES", details = "Goal", navigationTarget = "WORKSPACE", color = if (it.color != -1) it.color else -1))
            }
            
            wsTasks.filter { it.dueDate != null && it.dueDate!! < todayEnd && it.status != "Done" }.forEach {
                list.add(AgendaItem(id = it.id, title = it.title, time = it.dueDate!!, category = "WORKSPACES", details = "Task", navigationTarget = "WORKSPACE", color = taskRepository.getGlobalTaskColor()))
            }
            
            wsFeatures.filter { it.deadline != null && it.deadline!! < todayEnd && it.status != "Shipped" }.forEach {
                list.add(AgendaItem(id = it.id, title = it.title, time = it.deadline!!, category = "WORKSPACES", details = "Feature: ${it.status}", navigationTarget = "WORKSPACE", color = -1))
            }
            
            wsBugs.filter { it.deadline != null && it.deadline!! < todayEnd && it.status != "Fixed" && it.status != "Verified" }.forEach {
                list.add(AgendaItem(id = it.id, title = it.title, time = it.deadline!!, category = "WORKSPACES", details = "Bug: ${it.severity} priority", navigationTarget = "WORKSPACE", color = -1))
            }

            if (list.isEmpty()) emptyMap<String, List<AgendaItem>>()
            else {
                list.groupBy { it.category }
                    .mapValues { (_, items) -> items.sortedBy { it.time } }
                    .toList()
                    .sortedBy { (_, items) -> items.firstOrNull()?.time ?: 0L }
                    .toMap()
            }
        }
    }
}
