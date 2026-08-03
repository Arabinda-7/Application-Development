package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun addXP(amount: Int): Boolean
    
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings)
    
    suspend fun addActivity(activity: String)

    fun getDayHistory(): Flow<Map<String, com.example.allinone.DayHistory>>
    suspend fun updateDayHistory(history: Map<String, com.example.allinone.DayHistory>)
}

@Serializable
data class UserProfile(
    val xp: Int = 0,
    val level: Int = 1,
    val name: String = "User",
    val bio: String = "",
    val avatarRes: Int = -1,
    val profileImageUri: String? = null,
    val recentActivities: List<String> = emptyList(),
    val dailyMoods: Map<String, String> = emptyMap(),
    val lastMoodTimestamp: Long = 0
)

@Serializable
data class UserSettings(
    val displaySize: String = "S",
    val homeDisplaySize: String = "S",
    val homeFocusSize: String = "M",
    val fontSize: String = "S",
    val isSystemAppearanceEnabled: Boolean = true,
    val appThemeMode: String = "DARK",
    val appAccentColor: Int = -1,
    val appFontFamily: String = "DEFAULT",
    val appBorderRadius: Int = 16,
    val appCardStyle: String = "GLASS",
    val appShowShadows: Boolean = true,
    val isDynamicColorEnabled: Boolean = false,
    val startupLoadingTime: Int = 2000,
    
    // Visibility Sections
    val showHabitSection: Boolean = true,
    val showWorkoutSection: Boolean = true,
    val showTaskSection: Boolean = true,
    val showNoteSection: Boolean = true,
    val showProjectSection: Boolean = true,
    val showFinanceSection: Boolean = true,
    val showPerformanceSection: Boolean = true,
    
    // Assistant
    val isAiAssistantEnabled: Boolean = false,
    val isAssistantVoiceEnabled: Boolean = false,
    val isAiVoiceChatEnabled: Boolean = true,
    val isAssistantAutoCleanupEnabled: Boolean = false,
    val assistantVoiceName: String? = null,
    val assistantPitch: Float = 1.0f,
    val assistantSpeechRate: Float = 1.0f,
    
    // Reminders
    val isMorningReminderEnabled: Boolean = false,
    val morningReminderTime: String = "08:00",
    val isNightReminderEnabled: Boolean = false,
    val nightReminderTime: String = "21:00",

    // Workspace & Notifications State
    val lastViewedNotificationDate: String = "",
    val lastSummaryNotificationDate: String = "",
    val hasNewTodayNotifications: Boolean = false,

    // Notifications Config
    val isTaskNotificationEnabled: Boolean = true,
    val isHabitNotificationEnabled: Boolean = true,
    val isWorkoutNotificationEnabled: Boolean = true,
    val isNoteNotificationEnabled: Boolean = true,
    val isProjectNotificationEnabled: Boolean = true,
    val isFinanceNotificationEnabled: Boolean = true,
    val isWorkspaceNotificationEnabled: Boolean = true,

    // Security
    val isOnboardingCompleted: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isBiometricLockEnabled: Boolean = false,
    val isScreenshotProtectionEnabled: Boolean = false,
    val appLockPin: String? = null,
    val appLockQuestion: String? = null,
    val appLockAnswer: String? = null,
    
    val userCustomColors: List<Int> = emptyList()
)
