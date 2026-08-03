package com.example.allinone.data.preferences

import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationPreferences: Manages domain notification toggles and scheduled reminder times.
 */
@Singleton
class NotificationPreferences @Inject constructor() {
    var isTaskNotificationEnabled: Boolean = true
    var isHabitNotificationEnabled: Boolean = true
    var isWorkoutNotificationEnabled: Boolean = true
    var isNoteNotificationEnabled: Boolean = true
    var isProjectNotificationEnabled: Boolean = true
    var isWorkspaceNotificationEnabled: Boolean = true
    var isFinanceNotificationEnabled: Boolean = true

    var isNightReminderEnabled: Boolean = true
    var nightReminderTime: String = "21:00"
    var isMorningReminderEnabled: Boolean = true
    var morningReminderTime: String = "08:00"
}
