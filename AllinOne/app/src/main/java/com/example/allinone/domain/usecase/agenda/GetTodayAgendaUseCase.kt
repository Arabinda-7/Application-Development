package com.example.allinone.domain.usecase.agenda

import com.example.allinone.domain.model.AgendaItem
import com.example.allinone.domain.repository.TaskRepository
import com.example.allinone.domain.repository.ProjectRepository
import com.example.allinone.data.database.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

class GetTodayAgendaUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val database: AppDatabase
) {
    suspend operator fun invoke(): Map<String, List<AgendaItem>> {
        val list = mutableListOf<AgendaItem>()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86400000

        // 1. Global Tasks
        val tasksList = taskRepository.getTasks().first()
        tasksList.forEach { task ->
            val name = task.name
            task.reminderTime?.let { time ->
                if (!task.isCompleted && time in todayStart until todayEnd) {
                    list.add(AgendaItem(
                        id = name,
                        title = name,
                        time = time,
                        category = "TASKS",
                        priority = when(task.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                        navigationTarget = "TASK_ACTIVITY",
                        color = if (task.color != -1) task.color else taskRepository.getGlobalTaskColor()
                    ))
                }
            }
        }

        // 2. Global Projects
        val projects = projectRepository.getAllProjects().first()
        
        projects.forEach { project ->
            val projColor = if (project.color != -1) project.color else -1
            val title = project.title
            
            project.deadline?.let { deadline ->
                if (project.status != "Completed" && deadline in todayStart until todayEnd) {
                    list.add(AgendaItem(
                        id = project.timestamp.toString(),
                        title = title,
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
                    if (!f.isCompleted && dueDate in todayStart until todayEnd) {
                        list.add(AgendaItem(
                            id = f.id ?: UUID.randomUUID().toString(),
                            parentId = project.timestamp.toString(),
                            title = f.name ?: "Untitled Milestone",
                            time = dueDate,
                            category = "SUBFEATURES",
                            priority = when(f.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                            color = projColor
                        ))
                    }
                }
            }
        }

        // 3. Workspaces (Room)
        try {
            val dao = database.workspaceDao()
            
            dao.getProjectsDueBetween(todayStart, todayEnd).forEach {
                it.deadline?.let { deadline ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.name, 
                        time = deadline, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = if (it.color != -1) it.color else -1
                    ))
                }
            }
            
            dao.getGoalsDueBetween(todayStart, todayEnd).forEach {
                it.deadline?.let { deadline ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.title, 
                        time = deadline, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = if (it.color != -1) it.color else -1
                    ))
                }
            }
            
            dao.getTasksDueBetween(todayStart, todayEnd).forEach {
                it.dueDate?.let { dueDate ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.title, 
                        time = dueDate, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = taskRepository.getGlobalTaskColor()
                    ))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        if (list.isEmpty()) return emptyMap()
        
        return list.groupBy { it.category }
            .mapValues { (_, items) -> items.sortedBy { it.time } }
            .toList()
            .sortedBy { (_, items) -> items.firstOrNull()?.time ?: 0L }
            .toMap()
    }
}
