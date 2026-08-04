package com.example.allinone.domain.repository

import com.example.allinone.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class TaskSettings(
    val showCompleted: Boolean = true,
    val showHidden: Boolean = false,
    val sortOrder: String = "Priority",
    val customCategories: List<String> = listOf("General", "Personal", "Work", "Shopping"),
    val autoArchive: Boolean = false,
    val globalTaskColor: Int = -1,
    val taskAddThemeColor: Int = -1,
    val globalTaskIcon: Int = -1,
    val editModeEnabled: Boolean = false,
    val defaultSection: String = "Tasks",
    val visibleSections: List<String> = listOf("Tasks")
)

interface TaskRepository {
    // Data Operations
    fun getTasks(): Flow<List<Task>>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun clearCompletedTasks()
    suspend fun syncTasks(tasks: List<Task>)

    // Task Settings & Preferences
    fun getTaskSettings(): Flow<TaskSettings>
    suspend fun updateSettings(settings: TaskSettings)
    fun getShowCompleted(): Boolean
    fun setShowCompleted(show: Boolean)
    fun getShowHidden(): Boolean
    fun setShowHidden(show: Boolean)
    fun getSortOrder(): String
    fun setSortOrder(order: String)
    fun getCustomCategories(): List<String>
    fun setCustomCategories(categories: List<String>)
    fun getAutoArchive(): Boolean
    fun setAutoArchive(enabled: Boolean)
    
    // Theming & Icons
    fun getGlobalTaskColor(): Int
    fun setGlobalTaskColor(color: Int)
    fun getTaskAddThemeColor(): Int
    fun setTaskAddThemeColor(color: Int)
    fun getGlobalTaskIcon(): Int
    fun setGlobalTaskIcon(iconResId: Int)
    
    fun getEditModeEnabled(): Boolean
    fun setEditModeEnabled(enabled: Boolean)
    fun getDefaultSection(): String
    fun setDefaultSection(section: String)
    fun getVisibleSections(): List<String>
    fun setVisibleSections(sections: List<String>)
}
