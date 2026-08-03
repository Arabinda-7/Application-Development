package com.example.allinone.data.repository

import com.example.allinone.data.datasource.TaskLocalDataSource
import com.example.allinone.data.database.GlobalTaskEntity
import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import com.example.allinone.domain.repository.TaskSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: TaskLocalDataSource
) : TaskRepository {

    private fun GlobalTaskEntity.toModel(): Task = Task(
        name = name,
        isCompleted = isCompleted,
        color = color,
        priority = priority,
        reminderTime = reminderTime,
        category = category,
        section = section,
        isHidden = isHidden,
        subtasks = subtasks.toMutableList(),
        completedTimestamp = completedTimestamp,
        timestamp = timestamp
    )

    private fun Task.toEntity(): GlobalTaskEntity = GlobalTaskEntity(
        timestamp = timestamp,
        name = name,
        isCompleted = isCompleted,
        color = color,
        priority = priority,
        reminderTime = reminderTime,
        category = category,
        section = section,
        isHidden = isHidden,
        subtasks = subtasks,
        completedTimestamp = completedTimestamp
    )

    override fun getTasks(): Flow<List<Task>> = localDataSource.observeTasks().map { entities ->
        entities.map { it.toModel() }
    }

    override suspend fun addTask(task: Task) = localDataSource.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) = localDataSource.insertTask(task.toEntity())

    override suspend fun deleteTask(task: Task) = localDataSource.deleteTask(task.toEntity())

    override suspend fun clearCompletedTasks() = localDataSource.clearCompleted()

    override suspend fun syncTasks(tasks: List<Task>) = localDataSource.syncAll(tasks.map { it.toEntity() })

    // Settings
    override fun getTaskSettings(): Flow<TaskSettings> = localDataSource.settings

    override fun getShowCompleted() = localDataSource.getBoolean("task_show_completed", true)
    override fun setShowCompleted(show: Boolean) = localDataSource.setBoolean("task_show_completed", show)
    
    override fun getShowHidden() = localDataSource.getBoolean("task_show_hidden", false)
    override fun setShowHidden(show: Boolean) = localDataSource.setBoolean("task_show_hidden", show)
    
    override fun getSortOrder() = localDataSource.getString("task_sort_order", "Priority")
    override fun setSortOrder(order: String) = localDataSource.setString("task_sort_order", order)
    
    override fun getCustomCategories() = localDataSource.getStringList("task_custom_categories", listOf("General", "Personal", "Work", "Shopping"))
    override fun setCustomCategories(categories: List<String>) = localDataSource.setStringList("task_custom_categories", categories)
    
    override fun getAutoArchive() = localDataSource.getBoolean("task_auto_archive", false)
    override fun setAutoArchive(enabled: Boolean) = localDataSource.setBoolean("task_auto_archive", enabled)
    
    override fun getGlobalTaskColor() = localDataSource.getInt("task_global_color", -1)
    override fun setGlobalTaskColor(color: Int) = localDataSource.setInt("task_global_color", color)
    
    override fun getTaskAddThemeColor() = localDataSource.getInt("task_add_theme_color", -1)
    override fun setTaskAddThemeColor(color: Int) = localDataSource.setInt("task_add_theme_color", color)
    
    override fun getGlobalTaskIcon() = localDataSource.getInt("task_global_icon", -1)
    override fun setGlobalTaskIcon(iconResId: Int) = localDataSource.setInt("task_global_icon", iconResId)
    
    override fun getEditModeEnabled() = localDataSource.getBoolean("task_edit_mode_enabled", false)
    override fun setEditModeEnabled(enabled: Boolean) = localDataSource.setBoolean("task_edit_mode_enabled", enabled)
    
    override fun getDefaultSection() = localDataSource.getString("task_default_section", "Tasks")
    override fun setDefaultSection(section: String) = localDataSource.setString("task_default_section", section)
    
    override fun getVisibleSections() = localDataSource.getStringList("task_visible_sections", listOf("Tasks"))
    override fun setVisibleSections(sections: List<String>) = localDataSource.setStringList("task_visible_sections", sections)
}
