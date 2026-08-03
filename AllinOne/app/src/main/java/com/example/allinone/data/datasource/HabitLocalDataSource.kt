package com.example.allinone.data.datasource

import android.content.Context
import com.example.allinone.data.database.AppHabitDao
import com.example.allinone.data.database.HabitEntity
import com.example.allinone.domain.repository.HabitSettings
import com.example.allinone.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: AppHabitDao
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<HabitSettings> = _settings.asStateFlow()

    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: HabitEntity) = habitDao.insertHabit(habit)
    
    suspend fun insertAllHabits(habits: List<HabitEntity>) = habitDao.insertAllHabits(habits)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)
    
    suspend fun deleteOthers(ids: List<Long>) = habitDao.deleteOthers(ids)

    fun updateSettings(newSettings: HabitSettings) {
        prefs.edit().apply {
            putString("habit_default_tab", newSettings.defaultTab)
            putBoolean("habit_vacation_mode", newSettings.vacationMode)
            putString("habit_sort_order", newSettings.sortOrder)
            putBoolean("habit_completion_sound", newSettings.completionSound)
            putBoolean("habit_completion_haptics", newSettings.completionHaptics)
            putInt("habit_day_reset_hour", newSettings.dayResetHour)
            putBoolean("habit_bulk_mode", newSettings.bulkMode)
            putInt("habit_grace_days_allowed", newSettings.graceDaysAllowed)
            putBoolean("show_habit_section", newSettings.showCompleted)
            putInt("global_habit_color", newSettings.globalHabitColor)
            putInt("habit_add_theme_color", newSettings.habitAddThemeColor)
            putInt("global_habit_icon", newSettings.globalHabitIcon)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): HabitSettings {
        return HabitSettings(
            defaultTab = prefs.getString("habit_default_tab", "TODAY") ?: "TODAY",
            vacationMode = prefs.getBoolean("habit_vacation_mode", false),
            sortOrder = prefs.getString("habit_sort_order", "Time") ?: "Time",
            completionSound = prefs.getBoolean("habit_completion_sound", true),
            completionHaptics = prefs.getBoolean("habit_completion_haptics", true),
            dayResetHour = prefs.getInt("habit_day_reset_hour", 0),
            bulkMode = prefs.getBoolean("habit_bulk_mode", false),
            graceDaysAllowed = prefs.getInt("habit_grace_days_allowed", 1),
            showCompleted = prefs.getBoolean("show_habit_section", true),
            globalHabitColor = prefs.getInt("global_habit_color", -1),
            habitAddThemeColor = prefs.getInt("habit_add_theme_color", -1),
            globalHabitIcon = prefs.getInt("global_habit_icon", -1)
        )
    }
}
