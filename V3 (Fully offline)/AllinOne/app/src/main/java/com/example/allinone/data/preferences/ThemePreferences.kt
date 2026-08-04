package com.example.allinone.data.preferences

import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThemePreferences: Manages application theme mode, accent colors, border radiuses,
 * and individual module header/card theme colors.
 */
@Singleton
class ThemePreferences @Inject constructor() {
    var appAccentColor: Int = -1
    var appBorderRadius: Int = 16
    var appThemeMode: String = "DARK"

    var globalHabitColor: Int = -1
    var globalWorkoutColor: Int = -1
    var globalTaskColor: Int = -1
    var globalNoteColor: Int = -1
    var globalFinanceColor: Int = -1
    var globalProjectColor: Int = -1

    var taskAddThemeColor: Int = -1
    var habitAddThemeColor: Int = -1
    var workoutAddThemeColor: Int = -1
    var noteAddThemeColor: Int = -1
    var projectAddThemeColor: Int = -1
    var financeAddThemeColor: Int = -1
}
