package com.example.allinone.data.datasource

import android.content.Context
import com.example.allinone.DayHistory
import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserSettings
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    
    private val _profile = MutableStateFlow(loadProfile())
    val profile: Flow<UserProfile> = _profile.asStateFlow()
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<UserSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: Flow<Map<String, DayHistory>> = _history.asStateFlow()

    fun updateProfile(newProfile: UserProfile) {
        prefs.edit().apply {
            putInt("user_xp_data", newProfile.xp)
            putInt("user_level_data", newProfile.level)
            putString("user_profile_name", newProfile.name)
            putString("user_profile_bio", newProfile.bio)
            putString("user_profile_avatar", getResourceName(newProfile.avatarRes))
            putString("user_profile_image_uri", newProfile.profileImageUri)
            putString("recent_activities_data", gson.toJson(newProfile.recentActivities))
            putString("daily_moods_data", gson.toJson(newProfile.dailyMoods))
            putLong("last_mood_timestamp", newProfile.lastMoodTimestamp)
            apply()
        }
        _profile.value = newProfile
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit().apply {
            putString("app_display_size", newSettings.displaySize)
            putString("home_display_size", newSettings.homeDisplaySize)
            putString("home_focus_size", newSettings.homeFocusSize)
            putString("app_font_size", newSettings.fontSize)
            putBoolean("is_system_appearance_enabled", newSettings.isSystemAppearanceEnabled)
            putString("app_theme_mode", newSettings.appThemeMode)
            putInt("app_accent_color", newSettings.appAccentColor)
            putString("app_font_family", newSettings.appFontFamily)
            putInt("app_border_radius", newSettings.appBorderRadius)
            putString("app_card_style", newSettings.appCardStyle)
            putBoolean("app_show_shadows", newSettings.appShowShadows)
            putBoolean("is_dynamic_color_enabled", newSettings.isDynamicColorEnabled)
            putInt("startup_loading_time", newSettings.startupLoadingTime)
            
            putBoolean("show_habit_section", newSettings.showHabitSection)
            putBoolean("show_workout_section", newSettings.showWorkoutSection)
            putBoolean("show_task_section", newSettings.showTaskSection)
            putBoolean("show_note_section", newSettings.showNoteSection)
            putBoolean("show_project_section", newSettings.showProjectSection)
            putBoolean("show_finance_section", newSettings.showFinanceSection)
            putBoolean("show_performance_section", newSettings.showPerformanceSection)

            putBoolean("is_ai_assistant_enabled", newSettings.isAiAssistantEnabled)
            putBoolean("assistant_voice_enabled", newSettings.isAssistantVoiceEnabled)
            putBoolean("ai_voice_chat_enabled", newSettings.isAiVoiceChatEnabled)
            putBoolean("assistant_auto_cleanup_enabled", newSettings.isAssistantAutoCleanupEnabled)
            putString("assistant_voice_name", newSettings.assistantVoiceName)
            putFloat("assistant_voice_pitch", newSettings.assistantPitch)
            putFloat("assistant_voice_rate", newSettings.assistantSpeechRate)
            
            putBoolean("morning_reminder_enabled", newSettings.isMorningReminderEnabled)
            putString("morning_reminder_time", newSettings.morningReminderTime)
            putBoolean("night_reminder_enabled", newSettings.isNightReminderEnabled)
            putString("night_reminder_time", newSettings.nightReminderTime)

            putString("last_viewed_notification_date", newSettings.lastViewedNotificationDate)
            putString("last_summary_notification_date", newSettings.lastSummaryNotificationDate)
            putBoolean("has_new_today_notifications", newSettings.hasNewTodayNotifications)

            putBoolean("notif_tasks_enabled", newSettings.isTaskNotificationEnabled)
            putBoolean("notif_habits_enabled", newSettings.isHabitNotificationEnabled)
            putBoolean("notif_workouts_enabled", newSettings.isWorkoutNotificationEnabled)
            putBoolean("notif_notes_enabled", newSettings.isNoteNotificationEnabled)
            putBoolean("notif_projects_enabled", newSettings.isProjectNotificationEnabled)
            putBoolean("notif_finance_enabled", newSettings.isFinanceNotificationEnabled)
            putBoolean("notif_workspace_enabled", newSettings.isWorkspaceNotificationEnabled)

            putBoolean("onboarding_completed", newSettings.isOnboardingCompleted)
            putBoolean("app_lock_enabled", newSettings.isAppLockEnabled)
            putBoolean("biometric_lock_enabled", newSettings.isBiometricLockEnabled)
            putBoolean("screenshot_protection_enabled", newSettings.isScreenshotProtectionEnabled)
            putString("app_lock_pin", newSettings.appLockPin)
            putString("app_lock_question", newSettings.appLockQuestion)
            putString("app_lock_answer", newSettings.appLockAnswer)
            
            putString("user_custom_colors_data", gson.toJson(newSettings.userCustomColors))
            apply()
        }
        _settings.value = newSettings
    }

    fun updateHistory(newHistory: Map<String, DayHistory>) {
        prefs.edit().apply {
            putString("history_data", gson.toJson(newHistory))
            apply()
        }
        _history.value = newHistory
    }

    private fun loadProfile(): UserProfile {
        val activitiesJson = prefs.getString("recent_activities_data", "[]")
        val activities: List<String> = try {
            gson.fromJson(activitiesJson, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { emptyList() }

        val moodsJson = prefs.getString("daily_moods_data", "{}")
        val moods: Map<String, String> = try {
            gson.fromJson(moodsJson, object : TypeToken<Map<String, String>>() {}.type)
        } catch (e: Exception) { emptyMap() }

        val avatarName = prefs.getString("user_profile_avatar", "boy_avatar_profile") ?: "boy_avatar_profile"
        val avatarRes = getResourceId(avatarName)

        return UserProfile(
            xp = prefs.getInt("user_xp_data", 0),
            level = prefs.getInt("user_level_data", 1),
            name = prefs.getString("user_profile_name", "User") ?: "User",
            bio = prefs.getString("user_profile_bio", "") ?: "",
            avatarRes = avatarRes,
            profileImageUri = prefs.getString("user_profile_image_uri", null),
            recentActivities = activities,
            dailyMoods = moods,
            lastMoodTimestamp = prefs.getLong("last_mood_timestamp", 0L)
        )
    }

    private fun loadSettings(): UserSettings {
        val colorsJson = prefs.getString("user_custom_colors_data", "[]")
        val colors: List<Int> = try {
            gson.fromJson(colorsJson, object : TypeToken<List<Int>>() {}.type)
        } catch (e: Exception) { emptyList() }

        return UserSettings(
            displaySize = prefs.getString("app_display_size", "S") ?: "S",
            homeDisplaySize = prefs.getString("home_display_size", "S") ?: "S",
            homeFocusSize = prefs.getString("home_focus_size", "M") ?: "M",
            fontSize = prefs.getString("app_font_size", "S") ?: "S",
            isSystemAppearanceEnabled = prefs.getBoolean("is_system_appearance_enabled", true),
            appThemeMode = prefs.getString("app_theme_mode", "DARK") ?: "DARK",
            appAccentColor = prefs.getInt("app_accent_color", -1),
            appFontFamily = prefs.getString("app_font_family", "DEFAULT") ?: "DEFAULT",
            appBorderRadius = prefs.getInt("app_border_radius", 16),
            appCardStyle = prefs.getString("app_card_style", "GLASS") ?: "GLASS",
            appShowShadows = prefs.getBoolean("app_show_shadows", true),
            isDynamicColorEnabled = prefs.getBoolean("is_dynamic_color_enabled", false),
            startupLoadingTime = prefs.getInt("startup_loading_time", 2000),
            
            showHabitSection = prefs.getBoolean("show_habit_section", true),
            showWorkoutSection = prefs.getBoolean("show_workout_section", true),
            showTaskSection = prefs.getBoolean("show_task_section", true),
            showNoteSection = prefs.getBoolean("show_note_section", true),
            showProjectSection = prefs.getBoolean("show_project_section", true),
            showFinanceSection = prefs.getBoolean("show_finance_section", true),
            showPerformanceSection = prefs.getBoolean("show_performance_section", true),

            isAiAssistantEnabled = prefs.getBoolean("is_ai_assistant_enabled", false),
            isAssistantVoiceEnabled = prefs.getBoolean("assistant_voice_enabled", false),
            isAiVoiceChatEnabled = prefs.getBoolean("ai_voice_chat_enabled", true),
            isAssistantAutoCleanupEnabled = prefs.getBoolean("assistant_auto_cleanup_enabled", false),
            assistantVoiceName = prefs.getString("assistant_voice_name", null),
            assistantPitch = prefs.getFloat("assistant_voice_pitch", 1.0f),
            assistantSpeechRate = prefs.getFloat("assistant_voice_rate", 1.0f),

            isMorningReminderEnabled = prefs.getBoolean("morning_reminder_enabled", false),
            morningReminderTime = prefs.getString("morning_reminder_time", "08:00") ?: "08:00",
            isNightReminderEnabled = prefs.getBoolean("night_reminder_enabled", false),
            nightReminderTime = prefs.getString("night_reminder_time", "21:00") ?: "21:00",
            
            lastViewedNotificationDate = prefs.getString("last_viewed_notification_date", "") ?: "",
            lastSummaryNotificationDate = prefs.getString("last_summary_notification_date", "") ?: "",
            hasNewTodayNotifications = prefs.getBoolean("has_new_today_notifications", false),
            
            isTaskNotificationEnabled = prefs.getBoolean("notif_tasks_enabled", true),
            isHabitNotificationEnabled = prefs.getBoolean("notif_habits_enabled", true),
            isWorkoutNotificationEnabled = prefs.getBoolean("notif_workouts_enabled", true),
            isNoteNotificationEnabled = prefs.getBoolean("notif_notes_enabled", true),
            isProjectNotificationEnabled = prefs.getBoolean("notif_projects_enabled", true),
            isFinanceNotificationEnabled = prefs.getBoolean("notif_finance_enabled", true),
            isWorkspaceNotificationEnabled = prefs.getBoolean("notif_workspace_enabled", true),

            isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            isAppLockEnabled = prefs.getBoolean("app_lock_enabled", false),
            isBiometricLockEnabled = prefs.getBoolean("biometric_lock_enabled", false),
            isScreenshotProtectionEnabled = prefs.getBoolean("screenshot_protection_enabled", false),
            appLockPin = prefs.getString("app_lock_pin", null),
            appLockQuestion = prefs.getString("app_lock_question", null),
            appLockAnswer = prefs.getString("app_lock_answer", null),
            
            userCustomColors = colors
        )
    }

    private fun loadHistory(): Map<String, DayHistory> {
        val json = prefs.getString("history_data", "{}")
        return try {
            gson.fromJson(json, object : TypeToken<Map<String, DayHistory>>() {}.type)
        } catch (e: Exception) { emptyMap() }
    }

    private fun getResourceId(name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName).let {
            if (it != 0) it else context.resources.getIdentifier("ic_launcher_foreground", "drawable", context.packageName)
        }
    }

    private fun getResourceName(id: Int): String {
        return try { context.resources.getResourceEntryName(id) } catch (e: Exception) { "ic_launcher_foreground" }
    }
}
