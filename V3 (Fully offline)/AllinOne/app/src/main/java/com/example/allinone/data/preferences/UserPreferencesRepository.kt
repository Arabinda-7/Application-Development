package com.example.allinone.data.preferences

import com.example.allinone.DataManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserPreferencesRepository: Encapsulates application preferences, theme configurations,
 * and section visibility settings, decoupling them from the legacy DataManager God object.
 */
@Singleton
class UserPreferencesRepository @Inject constructor() {

    var appThemeMode: String
        get() = DataManager.appThemeMode
        set(value) { DataManager.appThemeMode = value }

    var appAccentColor: Int
        get() = DataManager.appAccentColor
        set(value) { DataManager.appAccentColor = value }

    var isAssistantVoiceEnabled: Boolean
        get() = DataManager.isAssistantVoiceEnabled
        set(value) { DataManager.isAssistantVoiceEnabled = value }

    var isScreenshotProtectionEnabled: Boolean
        get() = DataManager.isScreenshotProtectionEnabled
        set(value) { DataManager.isScreenshotProtectionEnabled = value }

    var showHabitSection: Boolean
        get() = DataManager.showHabitSection
        set(value) { DataManager.showHabitSection = value }

    var showWorkoutSection: Boolean
        get() = DataManager.showWorkoutSection
        set(value) { DataManager.showWorkoutSection = value }

    var showTaskSection: Boolean
        get() = DataManager.showTaskSection
        set(value) { DataManager.showTaskSection = value }

    var showNoteSection: Boolean
        get() = DataManager.showNoteSection
        set(value) { DataManager.showNoteSection = value }

    var showProjectSection: Boolean
        get() = DataManager.showProjectSection
        set(value) { DataManager.showProjectSection = value }

    var showFinanceSection: Boolean
        get() = DataManager.showFinanceSection
        set(value) { DataManager.showFinanceSection = value }
}
