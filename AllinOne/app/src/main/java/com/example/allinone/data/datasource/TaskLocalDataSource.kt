package com.example.allinone.data.datasource

import android.content.Context
import com.example.allinone.data.database.AppTaskDao
import com.example.allinone.data.database.GlobalTaskEntity
import com.example.allinone.domain.repository.TaskSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskLocalDataSource @Inject constructor(
    private val taskDao: AppTaskDao,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val sharedPrefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<TaskSettings> = _settings.asStateFlow()

    // Database access
    fun observeTasks(): Flow<List<GlobalTaskEntity>> = taskDao.getAllTasks()
    suspend fun insertTask(entity: GlobalTaskEntity) = taskDao.insertTask(entity)
    suspend fun insertAllTasks(entities: List<GlobalTaskEntity>) = taskDao.insertAllTasks(entities)
    suspend fun deleteTask(entity: GlobalTaskEntity) = taskDao.deleteTask(entity)
    suspend fun clearCompleted() = taskDao.clearCompletedTasks()
    
    suspend fun syncAll(entities: List<GlobalTaskEntity>) {
        taskDao.deleteOthers(entities.map { it.timestamp })
        taskDao.insertAllTasks(entities)
    }

    // SharedPreferences access
    fun getBoolean(key: String, default: Boolean) = sharedPrefs.getBoolean(key, default)
    fun setBoolean(key: String, value: Boolean) {
        sharedPrefs.edit().putBoolean(key, value).apply()
        _settings.value = loadSettings()
    }
    
    fun getString(key: String, default: String) = sharedPrefs.getString(key, default) ?: default
    fun setString(key: String, value: String) {
        sharedPrefs.edit().putString(key, value).apply()
        _settings.value = loadSettings()
    }

    fun getStringList(key: String, default: List<String>): List<String> {
        val json = sharedPrefs.getString(key, null) ?: return default
        return try {
            gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            default
        }
    }
    
    fun setStringList(key: String, value: List<String>) {
        sharedPrefs.edit().putString(key, gson.toJson(value)).apply()
        _settings.value = loadSettings()
    }

    fun getInt(key: String, default: Int) = sharedPrefs.getInt(key, default)
    fun setInt(key: String, value: Int) {
        sharedPrefs.edit().putInt(key, value).apply()
        _settings.value = loadSettings()
    }

    private fun loadSettings(): TaskSettings {
        return TaskSettings(
            showCompleted = sharedPrefs.getBoolean("task_show_completed", true),
            showHidden = sharedPrefs.getBoolean("task_show_hidden", false),
            sortOrder = sharedPrefs.getString("task_sort_order", "Priority") ?: "Priority",
            customCategories = getStringList("task_custom_categories", listOf("General", "Personal", "Work", "Shopping")),
            autoArchive = sharedPrefs.getBoolean("task_auto_archive", false),
            globalTaskColor = sharedPrefs.getInt("global_task_color", -1),
            taskAddThemeColor = sharedPrefs.getInt("task_add_theme_color", -1),
            globalTaskIcon = sharedPrefs.getInt("global_task_icon", -1),
            editModeEnabled = sharedPrefs.getBoolean("task_edit_mode_enabled", false),
            defaultSection = sharedPrefs.getString("task_default_section", "Tasks") ?: "Tasks",
            visibleSections = getStringList("task_visible_sections", listOf("Tasks"))
        )
    }
}
