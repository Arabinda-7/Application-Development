package com.example.allinone

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object DataManager {
    var habits = mutableListOf<Habit>()
    var workouts = mutableListOf<Workout>()
    var tasks = mutableListOf<Task>()
    var notes = mutableListOf<Note>()
    var transactions = mutableListOf<Transaction>()
    var ledgerEntries = mutableListOf<LedgerEntry>()
    var personalLedgers = mutableListOf<PersonalLedger>()
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
    var financeSavingsGoalName: String = "Monthly Savings"
    var history = mutableMapOf<String, DayHistory>()
    var monthlyBudgets = mutableMapOf<String, Double>()
    var monthlySavingsGoals = mutableMapOf<String, Double>()
    
    // To-Do List Settings
    var taskShowCompleted: Boolean = true
    var taskShowHidden: Boolean = false
    var taskSortOrder: String = "Priority" // Options: Priority, Newest, Alphabetical
    var taskCustomCategories = mutableListOf("General", "Personal", "Work", "Shopping")
    var taskAutoArchive: Boolean = false
    var taskDefaultSection: String = "Tasks"
    var taskVisibleSections = mutableListOf("Tasks")
    var taskEditModeEnabled: Boolean = false

    // Finance Settings
    var financeCustomCategories = mutableListOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other")
    var financeCategoryIcons = mutableMapOf<String, Int>()
    var financeCategoryColors = mutableMapOf<String, Int>()
    var financeCurrency: String = "₹"
    var financeGraphStartMonth: Int = 0 // 0 = January, 1 = February, etc.
    var financeGraphColor: Int = -1 // Default spending color if -1
    var financeGraphSavingsColor: Int = -1 // Default savings color if -1
    var isFinanceLedgerEnabled: Boolean = true

    // Habit Settings
    var habitDefaultTab: String = "TODAY"
    var habitVacationMode: Boolean = false
    var habitSortOrder: String = "Time" // Time, Streak
    var habitCompletionSound: Boolean = true
    var habitCompletionHaptics: Boolean = true
    var habitDayResetHour: Int = 0
    var habitBulkMode: Boolean = false
    var habitGraceDaysAllowed: Int = 1
    var habitShowCompleted: Boolean = true

    // Workout Settings
    var workoutMuscleGroups = mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body")
    var workoutAutoRestTimer: Boolean = false
    var workoutWeightUnit: String = "Kg"
    var workoutDefaultMode: String = "Reps"
    var workoutRestDuration: Int = 60
    var workoutShowCompleted: Boolean = true

    var noteAutoCleanupDays: Int = 0
    var noteDefaultCategory: String = "Notes"
    var noteShowHidden: Boolean = false
    var noteVisibleSections = mutableListOf("Notes")
    
    // Project Advanced Settings
    var projectAutoArchive: Boolean = false
    var projectSynergySync: Boolean = false
    var projectDeadlineAlerts: Boolean = true
    var projectAnalyticsEnabled: Boolean = false
    var projectCustomTags = mutableListOf("UI", "LOGIC", "BUG")
    var projectSortCompletedToBottom: Boolean = true
    var projectActiveExpanded: Boolean = true
    var projectCompletedExpanded: Boolean = false
    var ideaActiveExpanded: Boolean = true
    var ideaCompletedExpanded: Boolean = false
    var projectAutoSaveIdeas: Boolean = true
    var projectDualExistEnabled: Boolean = false
    var projectIdeasEnabled: Boolean = true
    var projectRoadmapsEnabled: Boolean = true
    var isAppLockEnabled: Boolean = false
    var isOledThemeEnabled: Boolean = false
    var isOnboardingCompleted: Boolean = false
    var appLockPin: String? = null
    var lastViewedNotificationDate: String = ""

    var userXP: Int = 0
    var userLevel: Int = 1

    var userName: String = "Arabi"
    var userBio: String = "Professional Tier"
    var userAvatarRes: Int = R.drawable.boy_avatar_profile
    var userProfileImageUri: String? = null

    var recentActivities = mutableListOf<String>()
    var dailyMoods = mutableMapOf<String, String>() // DateString -> Emoji
    var lastMoodTimestamp: Long = 0L
    var displaySize: String = "S" // Options: XS, S, L
    var homeDisplaySize: String = "S" // Options: XS, S, L
    var homeFocusSize: String = "M" // Options: S, M, L
    var fontSize: String = "S" // Options: XS, S, L
    var isSystemAppearanceEnabled: Boolean = true

    // Advanced Look & Feel
    var appThemeMode: String = "DARK" // LIGHT, DARK, OLED
    var appAccentColor: Int = -1 // Default if -1
    var appFontFamily: String = "DEFAULT" // DEFAULT, SERIF, MONO
    var appBorderRadius: Int = 16 // 0 to 32 dp
    var appCardStyle: String = "GLASS" // GLASS, ELEVATED, FLAT
    var appShowShadows: Boolean = true

    // Home Page Section Visibility
    var showHabitSection: Boolean = true
    var showWorkoutSection: Boolean = true
    var showTaskSection: Boolean = true
    var showNoteSection: Boolean = true
    var showProjectSection: Boolean = true
    var showFinanceSection: Boolean = true

    // User Custom Colors
    var userCustomColors = mutableListOf<Int>()

    // Global Appearance Settings
    var globalHabitColor: Int = -1
    var globalWorkoutColor: Int = -1
    var globalTaskColor: Int = -1
    var globalProjectColor: Int = -1
    var globalNoteColor: Int = -1
    var globalFinanceColor: Int = -1

    var habitAddThemeColor: Int = -1
    var workoutAddThemeColor: Int = -1
    var taskAddThemeColor: Int = -1
    var noteAddThemeColor: Int = -1
    var projectAddThemeColor: Int = -1
    var financeAddThemeColor: Int = -1

    var globalHabitIcon: Int = R.drawable.ic_habit_tracker
    var globalWorkoutIcon: Int = R.drawable.ic_workout_routine
    var globalTaskIcon: Int = R.drawable.ic_task
    var globalProjectIcon: Int = R.drawable.ic_project
    var globalNoteIcon: Int = R.drawable.ic_notes
    var globalFinanceIcon: Int = R.drawable.ic_finance

    var projectTemplates: MutableMap<String, List<String>> = mutableMapOf(
        "App Feature" to listOf("UI Design", "Business Logic", "Integration", "Testing", "Deployment"),
        "Personal Goal" to listOf("Planning", "Execution", "Review"),
        "Bug Fix" to listOf("Reproduction", "Debugging", "Fix", "Verification")
    )

    var noteTemplates: MutableMap<String, String> = mutableMapOf(
        "Daily" to "1. Today I'm grateful for: \n2. Top goal for today: \n3. How I feel: ",
        "Questions" to "Question: \n\nContext: \n\nGoal: ",
        "Stories" to "Theme: \nCharacters: \n\nPlot: "
    )

    private const val PREFS_NAME = "all_in_one_prefs"
    private const val KEY_HABITS = "habits_data"
    private const val KEY_WORKOUTS = "workouts_data"
    private const val KEY_TASKS = "tasks_data"
    private const val KEY_NOTES = "notes_data"
    private const val KEY_TRANSACTIONS = "transactions_data"
    private const val KEY_LEDGER = "ledger_data"
    private const val KEY_PERSONAL_LEDGER = "personal_ledger_data"
    private const val KEY_BUDGET = "monthly_budget"
    private const val KEY_SAVINGS_GOAL = "monthly_savings_goal"
    private const val KEY_SAVINGS_GOAL_NAME = "savings_goal_name"
    private const val KEY_MONTHLY_BUDGETS = "monthly_budgets_data"
    private const val KEY_MONTHLY_SAVINGS_GOALS = "monthly_savings_goals_data"
    private const val KEY_HISTORY = "history_data"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"
    private const val KEY_LAST_MONTH_RESET = "last_month_reset"
    private const val KEY_TASK_SHOW_COMPLETED = "task_show_completed"
    private const val KEY_TASK_SHOW_HIDDEN = "task_show_hidden"
    private const val KEY_TASK_SORT_ORDER = "task_sort_order"
    private const val KEY_TASK_CUSTOM_CATEGORIES = "task_custom_categories"
    private const val KEY_TASK_AUTO_ARCHIVE = "task_auto_archive"
    private const val KEY_TASK_EDIT_MODE = "task_edit_mode_enabled"
    private const val KEY_TASK_DEFAULT_SECTION = "task_default_section"
    private const val KEY_TASK_VISIBLE_SECTIONS = "task_visible_sections"
    private const val KEY_FINANCE_CUSTOM_CATEGORIES = "finance_custom_categories"
    private const val KEY_FINANCE_CATEGORY_ICONS = "finance_category_icons"
    private const val KEY_FINANCE_CATEGORY_COLORS = "finance_category_colors"
    private const val KEY_FINANCE_CURRENCY = "finance_currency"
    private const val KEY_FINANCE_GRAPH_START_MONTH = "finance_graph_start_month"
    private const val KEY_FINANCE_GRAPH_COLOR = "finance_graph_color"
    private const val KEY_FINANCE_GRAPH_SAVINGS_COLOR = "finance_graph_savings_color"
    private const val KEY_FINANCE_LEDGER_ENABLED = "finance_ledger_enabled"
    private const val KEY_NOTE_AUTO_CLEANUP = "note_auto_cleanup"
    private const val KEY_NOTE_SHOW_HIDDEN = "note_show_hidden"
    private const val KEY_NOTE_VISIBLE_SECTIONS = "note_visible_sections"
    private const val KEY_NOTE_DEFAULT_CAT = "note_default_cat"
    private const val KEY_NOTE_TEMPLATES = "note_templates"
    private const val KEY_PROJ_ARCHIVE = "project_auto_archive"
    private const val KEY_PROJ_SYNC = "project_synergy_sync"
    private const val KEY_PROJ_ALERTS = "project_deadline_alerts"
    private const val KEY_PROJ_SORT_BOTTOM = "project_sort_completed_bottom"
    private const val KEY_PROJ_COMPLETED_EXP = "project_completed_expanded"
    private const val KEY_PROJ_AUTOSAVE_IDEAS = "project_autosave_ideas"
    private const val KEY_IDEA_ACTIVE_EXP = "idea_active_expanded"
    private const val KEY_IDEA_COMPLETED_EXP = "idea_completed_expanded"
    private const val KEY_PROJ_ANALYTICS = "project_analytics_enabled"
    private const val KEY_APP_LOCK = "app_lock_enabled"
    private const val KEY_APP_LOCK_PIN = "app_lock_pin"
    private const val KEY_OLED_THEME = "oled_theme_enabled"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_RECENT_ACT = "recent_activities_data"
    private const val KEY_DAILY_MOODS = "daily_moods_data"
    private const val KEY_USER_NAME = "user_profile_name"
    private const val KEY_USER_BIO = "user_profile_bio"
    private const val KEY_USER_AVATAR = "user_profile_avatar"
    private const val KEY_USER_IMAGE_URI = "user_profile_image_uri"
    private const val KEY_CUSTOM_COLORS = "user_custom_colors_data"
    private const val KEY_PROJ_TAGS = "project_custom_tags_data"
    private const val KEY_PROJ_DUAL_EXIST = "project_dual_exist_enabled"
    private const val KEY_PROJ_IDEAS_ENABLED = "project_ideas_enabled"
    private const val KEY_LAST_MOOD_TIMESTAMP = "last_mood_timestamp"
    private const val KEY_DISPLAY_SIZE = "app_display_size"
    private const val KEY_HOME_DISPLAY_SIZE = "home_display_size"
    private const val KEY_FONT_SIZE = "app_font_size"
    private const val KEY_SYSTEM_APPEARANCE = "is_system_appearance_enabled"
    private const val KEY_USER_XP = "user_xp_data"
    private const val KEY_USER_LEVEL = "user_level_data"

    private const val KEY_SHOW_HABITS = "show_habit_section"
    private const val KEY_SHOW_WORKOUTS = "show_workout_section"
    private const val KEY_SHOW_TASKS = "show_task_section"
    private const val KEY_SHOW_NOTES = "show_note_section"
    private const val KEY_SHOW_PROJECTS = "show_project_section"
    private const val KEY_SHOW_FINANCE = "show_finance_section"

    private const val KEY_GLOBAL_HABIT_COLOR = "global_habit_color"
    private const val KEY_GLOBAL_WORKOUT_COLOR = "global_workout_color"
    private const val KEY_GLOBAL_TASK_COLOR = "global_task_color"
    private const val KEY_GLOBAL_PROJECT_COLOR = "global_project_color"
    private const val KEY_GLOBAL_NOTE_COLOR = "global_note_color"
    private const val KEY_GLOBAL_FINANCE_COLOR = "global_finance_color"

    private const val KEY_HABIT_ADD_COLOR = "habit_add_theme_color"
    private const val KEY_WORKOUT_ADD_COLOR = "workout_add_theme_color"
    private const val KEY_TASK_ADD_COLOR = "task_add_theme_color"
    private const val KEY_NOTE_ADD_COLOR = "note_add_theme_color"
    private const val KEY_PROJECT_ADD_COLOR = "project_add_theme_color"
    private const val KEY_FINANCE_ADD_COLOR = "finance_add_theme_color"

    private const val KEY_GLOBAL_HABIT_ICON = "global_habit_icon"
    private const val KEY_GLOBAL_WORKOUT_ICON = "global_workout_icon"
    private const val KEY_GLOBAL_TASK_ICON = "global_task_icon"
    private const val KEY_GLOBAL_PROJECT_ICON = "global_project_icon"
    private const val KEY_GLOBAL_NOTE_ICON = "global_note_icon"
    private const val KEY_GLOBAL_FINANCE_ICON = "global_finance_icon"

    private const val KEY_PROJ_TEMPLATES = "project_templates_data"
    private const val KEY_PROJECT_AUTO_SYNC = "project_auto_task_sync"
    private const val KEY_PROJECT_AUTO_ARCHIVE = "project_auto_archive"
    private const val KEY_HABIT_DEFAULT_TAB = "habit_default_tab"
    private const val KEY_HABIT_VACATION_MODE = "habit_vacation_mode"
    private const val KEY_HABIT_SORT_ORDER = "habit_sort_order"
    private const val KEY_HABIT_SOUND = "habit_sound"
    private const val KEY_HABIT_HAPTICS = "habit_haptics"
    private const val KEY_HABIT_RESET_HOUR = "habit_reset_hour"
    private const val KEY_HABIT_BULK_MODE = "habit_bulk_mode"
    private const val KEY_HABIT_GRACE_DAYS = "habit_grace_days"
    private const val KEY_HABIT_SHOW_COMPLETED = "habit_show_completed"
    private const val KEY_WORKOUT_MUSCLE_GROUPS = "workout_muscle_groups"
    private const val KEY_WORKOUT_AUTO_REST = "workout_auto_rest"
    private const val KEY_WORKOUT_UNIT = "workout_unit"
    private const val KEY_WORKOUT_DEFAULT_MODE = "workout_default_mode"
    private const val KEY_WORKOUT_REST_DURATION = "workout_rest_duration"
    private const val KEY_WORKOUT_SHOW_COMPLETED = "workout_show_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveData(context: Context?) {
        if (context == null) return
        val prefs = getPrefs(context)
        val gson = Gson()
        
        prefs.edit().apply {
            putString(KEY_HABITS, gson.toJson(habits))
            putString(KEY_WORKOUTS, gson.toJson(workouts))
            putString(KEY_TASKS, gson.toJson(tasks))
            putString(KEY_NOTES, gson.toJson(notes))
            putString(KEY_TRANSACTIONS, gson.toJson(transactions))
            putString(KEY_LEDGER, gson.toJson(ledgerEntries))
            putString(KEY_PERSONAL_LEDGER, gson.toJson(personalLedgers))
            putString(KEY_SAVINGS_GOAL_NAME, financeSavingsGoalName)
            putFloat(KEY_BUDGET, monthlyBudget.toFloat())
            putFloat(KEY_SAVINGS_GOAL, monthlySavingsGoal.toFloat())
            putString(KEY_MONTHLY_BUDGETS, gson.toJson(monthlyBudgets))
            putString(KEY_MONTHLY_SAVINGS_GOALS, gson.toJson(monthlySavingsGoals))
            putString(KEY_HISTORY, gson.toJson(history))
            putBoolean(KEY_TASK_SHOW_COMPLETED, taskShowCompleted)
            putBoolean(KEY_TASK_SHOW_HIDDEN, taskShowHidden)
            putString(KEY_TASK_SORT_ORDER, taskSortOrder)
            putString(KEY_TASK_CUSTOM_CATEGORIES, gson.toJson(taskCustomCategories))
            putBoolean(KEY_TASK_AUTO_ARCHIVE, taskAutoArchive)
            putBoolean(KEY_TASK_EDIT_MODE, taskEditModeEnabled)
            putString(KEY_TASK_DEFAULT_SECTION, taskDefaultSection)
            putString(KEY_TASK_VISIBLE_SECTIONS, gson.toJson(taskVisibleSections))
            putString(KEY_FINANCE_CUSTOM_CATEGORIES, gson.toJson(financeCustomCategories))
            putString(KEY_FINANCE_CATEGORY_ICONS, gson.toJson(financeCategoryIcons))
            putString(KEY_FINANCE_CATEGORY_COLORS, gson.toJson(financeCategoryColors))
            putString(KEY_FINANCE_CURRENCY, financeCurrency)
            putInt(KEY_FINANCE_GRAPH_START_MONTH, financeGraphStartMonth)
            putInt(KEY_FINANCE_GRAPH_COLOR, financeGraphColor)
            putInt(KEY_FINANCE_GRAPH_SAVINGS_COLOR, financeGraphSavingsColor)
            putBoolean(KEY_FINANCE_LEDGER_ENABLED, isFinanceLedgerEnabled)
            putInt(KEY_NOTE_AUTO_CLEANUP, noteAutoCleanupDays)
            putBoolean(KEY_NOTE_SHOW_HIDDEN, noteShowHidden)
            putString(KEY_NOTE_VISIBLE_SECTIONS, gson.toJson(noteVisibleSections))
            putString(KEY_NOTE_DEFAULT_CAT, noteDefaultCategory)
            putString(KEY_NOTE_TEMPLATES, gson.toJson(noteTemplates))
            putBoolean(KEY_PROJ_ARCHIVE, projectAutoArchive)
            putBoolean(KEY_PROJ_SYNC, projectSynergySync)
            putBoolean(KEY_PROJ_ALERTS, projectDeadlineAlerts)
            putBoolean(KEY_PROJ_SORT_BOTTOM, projectSortCompletedToBottom)
            putBoolean("project_active_expanded", projectActiveExpanded)
            putBoolean(KEY_PROJ_COMPLETED_EXP, projectCompletedExpanded)
            putBoolean(KEY_IDEA_ACTIVE_EXP, ideaActiveExpanded)
            putBoolean(KEY_IDEA_COMPLETED_EXP, ideaCompletedExpanded)
            putBoolean(KEY_PROJ_AUTOSAVE_IDEAS, projectAutoSaveIdeas)
            putBoolean(KEY_PROJ_ANALYTICS, projectAnalyticsEnabled)
            putBoolean(KEY_PROJ_DUAL_EXIST, projectDualExistEnabled)
            putBoolean(KEY_PROJ_IDEAS_ENABLED, projectIdeasEnabled)
            putBoolean(KEY_APP_LOCK, isAppLockEnabled)
            putString(KEY_APP_LOCK_PIN, appLockPin)
            putBoolean(KEY_OLED_THEME, isOledThemeEnabled)
            putBoolean(KEY_ONBOARDING_COMPLETED, isOnboardingCompleted)
            putString(KEY_RECENT_ACT, gson.toJson(recentActivities))
            putString(KEY_DAILY_MOODS, gson.toJson(dailyMoods))
            putLong(KEY_LAST_MOOD_TIMESTAMP, lastMoodTimestamp)
            putString(KEY_DISPLAY_SIZE, displaySize)
            putString("home_focus_size", homeFocusSize)
            putString(KEY_HOME_DISPLAY_SIZE, homeDisplaySize)
            putString(KEY_FONT_SIZE, fontSize)
            putBoolean(KEY_SYSTEM_APPEARANCE, isSystemAppearanceEnabled)
            putString("app_theme_mode", appThemeMode)
            putInt("app_accent_color", appAccentColor)
            putString("app_font_family", appFontFamily)
            putInt("app_border_radius", appBorderRadius)
            putString("app_card_style", appCardStyle)
            putBoolean("app_show_shadows", appShowShadows)
            putInt(KEY_USER_XP, userXP)
            putInt(KEY_USER_LEVEL, userLevel)

            putBoolean(KEY_SHOW_HABITS, showHabitSection)
            putBoolean(KEY_SHOW_WORKOUTS, showWorkoutSection)
            putBoolean(KEY_SHOW_TASKS, showTaskSection)
            putBoolean(KEY_SHOW_NOTES, showNoteSection)
            putBoolean(KEY_SHOW_PROJECTS, showProjectSection)
            putBoolean(KEY_SHOW_FINANCE, showFinanceSection)

            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_BIO, userBio)
            putString(KEY_USER_AVATAR, getResourceName(context, userAvatarRes))
            putString(KEY_USER_IMAGE_URI, userProfileImageUri)
            putString(KEY_CUSTOM_COLORS, gson.toJson(userCustomColors))
            putString(KEY_PROJ_TAGS, gson.toJson(projectCustomTags))

            putInt(KEY_GLOBAL_HABIT_COLOR, globalHabitColor)
            putInt(KEY_GLOBAL_WORKOUT_COLOR, globalWorkoutColor)
            putInt(KEY_GLOBAL_TASK_COLOR, globalTaskColor)
            putInt(KEY_GLOBAL_PROJECT_COLOR, globalProjectColor)
            putInt(KEY_GLOBAL_NOTE_COLOR, globalNoteColor)
            putInt(KEY_GLOBAL_FINANCE_COLOR, globalFinanceColor)

            putInt(KEY_HABIT_ADD_COLOR, habitAddThemeColor)
            putInt(KEY_WORKOUT_ADD_COLOR, workoutAddThemeColor)
            putInt(KEY_TASK_ADD_COLOR, taskAddThemeColor)
            putInt(KEY_NOTE_ADD_COLOR, noteAddThemeColor)
            putInt(KEY_PROJECT_ADD_COLOR, projectAddThemeColor)
            putInt(KEY_FINANCE_ADD_COLOR, financeAddThemeColor)

            putString(KEY_GLOBAL_HABIT_ICON, getResourceName(context, globalHabitIcon))
            putString(KEY_GLOBAL_WORKOUT_ICON, getResourceName(context, globalWorkoutIcon))
            putString(KEY_GLOBAL_TASK_ICON, getResourceName(context, globalTaskIcon))
            putString(KEY_GLOBAL_PROJECT_ICON, getResourceName(context, globalProjectIcon))
            putString(KEY_GLOBAL_NOTE_ICON, getResourceName(context, globalNoteIcon))
            putString(KEY_GLOBAL_FINANCE_ICON, getResourceName(context, globalFinanceIcon))

            putString(KEY_PROJ_TEMPLATES, gson.toJson(projectTemplates))
            putString(KEY_HABIT_DEFAULT_TAB, habitDefaultTab)
            putBoolean(KEY_HABIT_VACATION_MODE, habitVacationMode)
            putString(KEY_HABIT_SORT_ORDER, habitSortOrder)
            putBoolean(KEY_HABIT_SOUND, habitCompletionSound)
            putBoolean(KEY_HABIT_HAPTICS, habitCompletionHaptics)
            putInt(KEY_HABIT_RESET_HOUR, habitDayResetHour)
            putBoolean(KEY_HABIT_BULK_MODE, habitBulkMode)
            putInt(KEY_HABIT_GRACE_DAYS, habitGraceDaysAllowed)
            putBoolean(KEY_HABIT_SHOW_COMPLETED, habitShowCompleted)
            putString(KEY_WORKOUT_MUSCLE_GROUPS, gson.toJson(workoutMuscleGroups))
            putBoolean(KEY_WORKOUT_AUTO_REST, workoutAutoRestTimer)
            putString(KEY_WORKOUT_UNIT, workoutWeightUnit)
            putString(KEY_WORKOUT_DEFAULT_MODE, workoutDefaultMode)
            putInt(KEY_WORKOUT_REST_DURATION, workoutRestDuration)
            putBoolean(KEY_WORKOUT_SHOW_COMPLETED, workoutShowCompleted)
            apply()
        }
    }

    fun loadData(context: Context) {
        val prefs = getPrefs(context)
        val gson = Gson()

        try {
            prefs.getString(KEY_HABITS, null)?.let {
                val type = object : TypeToken<MutableList<Habit>>() {}.type
                habits = gson.fromJson(it, type) ?: mutableListOf()
                habits.forEach { habit ->
                    habit.isExpanded = false
                    if (habit.completedDates == null) habit.completedDates = mutableListOf()
                    if (habit.repeatDays == null) habit.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
                    if (habit.repeatType == null) habit.repeatType = "SPECIFIC_DAYS"
                }
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load habits", e) }

        try {
            prefs.getString(KEY_WORKOUTS, null)?.let {
                val type = object : TypeToken<MutableList<Workout>>() {}.type
                workouts = gson.fromJson(it, type) ?: mutableListOf()
                workouts.forEach { workout ->
                    workout.isExpanded = false
                    if (workout.completedDates == null) workout.completedDates = mutableListOf()
                    if (workout.repeatDays == null) workout.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
                    if (workout.muscleGroups == null) workout.muscleGroups = listOf("General")
                    if (workout.repeatType == null) workout.repeatType = "SPECIFIC_DAYS"
                    if (workout.frequency == null) workout.frequency = "Anytime"
                }
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load workouts", e) }

        try {
            prefs.getString(KEY_TASKS, null)?.let {
                val type = object : TypeToken<MutableList<Task>>() {}.type
                tasks = gson.fromJson(it, type) ?: mutableListOf()
                tasks = tasks.map { oldTask ->
                    if (oldTask.subtasks == null || oldTask.category == null) {
                        oldTask.copy(
                            subtasks = oldTask.subtasks ?: mutableListOf(),
                            category = oldTask.category ?: "General",
                            section = oldTask.section ?: "Tasks"
                        )
                    } else oldTask
                }.toMutableList()
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load tasks", e) }

        try {
            prefs.getString(KEY_NOTES, null)?.let {
                val type = object : TypeToken<MutableList<Note>>() {}.type
                notes = gson.fromJson(it, type) ?: mutableListOf()
                // Sanitize for new fields
                notes.forEach { note ->
                    if (note.status == null) note.status = "Not Started"
                    if (note.category == null) note.category = "Notes"
                    
                    sanitizeProjectNote(note)
                }
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load notes", e) }

        try {
            prefs.getString(KEY_TRANSACTIONS, null)?.let {
                val type = object : TypeToken<MutableList<Transaction>>() {}.type
                transactions = gson.fromJson(it, type) ?: mutableListOf()
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load transactions", e) }

        try {
            prefs.getString(KEY_LEDGER, null)?.let {
                val type = object : TypeToken<MutableList<LedgerEntry>>() {}.type
                ledgerEntries = gson.fromJson(it, type) ?: mutableListOf()
                ledgerEntries.forEach { entry ->
                    if (entry.paymentHistory == null) entry.paymentHistory = mutableListOf()
                }
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load ledgerEntries", e) }

        try {
            prefs.getString(KEY_PERSONAL_LEDGER, null)?.let {
                val type = object : TypeToken<MutableList<PersonalLedger>>() {}.type
                personalLedgers = gson.fromJson(it, type) ?: mutableListOf()
                personalLedgers.forEach { ledger ->
                    ledger.entries.forEach { entry ->
                        if (entry.paymentHistory == null) entry.paymentHistory = mutableListOf()
                    }
                }
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load personalLedgers", e) }

        monthlyBudget = prefs.getFloat(KEY_BUDGET, 0.0f).toDouble()
        monthlySavingsGoal = prefs.getFloat(KEY_SAVINGS_GOAL, 0.0f).toDouble()

        try {
            prefs.getString(KEY_MONTHLY_BUDGETS, null)?.let {
                val type = object : TypeToken<MutableMap<String, Double>>() {}.type
                monthlyBudgets = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load monthlyBudgets", e) }

        try {
            prefs.getString(KEY_MONTHLY_SAVINGS_GOALS, null)?.let {
                val type = object : TypeToken<MutableMap<String, Double>>() {}.type
                monthlySavingsGoals = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load monthlySavingsGoals", e) }

        try {
            prefs.getString(KEY_HISTORY, null)?.let {
                val type = object : TypeToken<MutableMap<String, DayHistory>>() {}.type
                history = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) { android.util.Log.e("DataManager", "Failed to load history", e) }

        taskShowCompleted = prefs.getBoolean(KEY_TASK_SHOW_COMPLETED, true)
        taskShowHidden = prefs.getBoolean(KEY_TASK_SHOW_HIDDEN, false)
        taskSortOrder = prefs.getString(KEY_TASK_SORT_ORDER, "Priority") ?: "Priority"
        try {
            prefs.getString(KEY_TASK_CUSTOM_CATEGORIES, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                taskCustomCategories = gson.fromJson(it, type) ?: mutableListOf("General", "Personal", "Work", "Shopping")
            }
        } catch (e: Exception) {}
        taskAutoArchive = prefs.getBoolean(KEY_TASK_AUTO_ARCHIVE, false)
        taskEditModeEnabled = prefs.getBoolean(KEY_TASK_EDIT_MODE, false)
        taskDefaultSection = prefs.getString(KEY_TASK_DEFAULT_SECTION, "Tasks") ?: "Tasks"
        try {
            prefs.getString(KEY_TASK_VISIBLE_SECTIONS, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                taskVisibleSections = gson.fromJson(it, type) ?: mutableListOf("Tasks", "List")
            }
        } catch (e: Exception) {}

        try {
            prefs.getString(KEY_FINANCE_CUSTOM_CATEGORIES, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                financeCustomCategories = gson.fromJson(it, type) ?: mutableListOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other")
            }
        } catch (e: Exception) {}
        try {
            prefs.getString(KEY_FINANCE_CATEGORY_ICONS, null)?.let {
                val type = object : TypeToken<MutableMap<String, Int>>() {}.type
                financeCategoryIcons = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) {}
        try {
            prefs.getString(KEY_FINANCE_CATEGORY_COLORS, null)?.let {
                val type = object : TypeToken<MutableMap<String, Int>>() {}.type
                financeCategoryColors = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) {}
        financeCurrency = prefs.getString(KEY_FINANCE_CURRENCY, "₹") ?: "₹"
        financeGraphStartMonth = prefs.getInt(KEY_FINANCE_GRAPH_START_MONTH, 0)
        financeGraphColor = prefs.getInt(KEY_FINANCE_GRAPH_COLOR, -1)
        financeGraphSavingsColor = prefs.getInt(KEY_FINANCE_GRAPH_SAVINGS_COLOR, -1)
        financeSavingsGoalName = prefs.getString(KEY_SAVINGS_GOAL_NAME, "Monthly Savings") ?: "Monthly Savings"
        isFinanceLedgerEnabled = prefs.getBoolean(KEY_FINANCE_LEDGER_ENABLED, true)
        noteAutoCleanupDays = prefs.getInt(KEY_NOTE_AUTO_CLEANUP, 0)
        noteShowHidden = prefs.getBoolean(KEY_NOTE_SHOW_HIDDEN, false)
        try {
            prefs.getString(KEY_NOTE_VISIBLE_SECTIONS, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                noteVisibleSections = gson.fromJson(it, type) ?: mutableListOf("Notes", "Questions", "Daily", "Stories")
            }
        } catch (e: Exception) {}
        noteDefaultCategory = prefs.getString(KEY_NOTE_DEFAULT_CAT, "Notes") ?: "Notes"
        try {
            prefs.getString(KEY_NOTE_TEMPLATES, null)?.let {
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                noteTemplates = gson.fromJson(it, type) ?: noteTemplates
            }
        } catch (e: Exception) {}
        projectAutoArchive = prefs.getBoolean(KEY_PROJ_ARCHIVE, false)
        projectSynergySync = prefs.getBoolean(KEY_PROJ_SYNC, false)
        projectDeadlineAlerts = prefs.getBoolean(KEY_PROJ_ALERTS, true)
        projectSortCompletedToBottom = prefs.getBoolean(KEY_PROJ_SORT_BOTTOM, true)
        projectActiveExpanded = prefs.getBoolean("project_active_expanded", true)
        projectCompletedExpanded = prefs.getBoolean(KEY_PROJ_COMPLETED_EXP, false)
        ideaActiveExpanded = prefs.getBoolean(KEY_IDEA_ACTIVE_EXP, true)
        ideaCompletedExpanded = prefs.getBoolean(KEY_IDEA_COMPLETED_EXP, false)
        projectAutoSaveIdeas = prefs.getBoolean(KEY_PROJ_AUTOSAVE_IDEAS, true)
        projectAnalyticsEnabled = prefs.getBoolean(KEY_PROJ_ANALYTICS, false)
        projectDualExistEnabled = prefs.getBoolean(KEY_PROJ_DUAL_EXIST, false)
        projectIdeasEnabled = prefs.getBoolean(KEY_PROJ_IDEAS_ENABLED, true)
        isAppLockEnabled = prefs.getBoolean(KEY_APP_LOCK, false)
        appLockPin = prefs.getString(KEY_APP_LOCK_PIN, null)
        isOledThemeEnabled = prefs.getBoolean(KEY_OLED_THEME, false)
        isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        userName = prefs.getString(KEY_USER_NAME, "Arabi") ?: "Arabi"
        userBio = prefs.getString(KEY_USER_BIO, "Professional Tier") ?: "Professional Tier"
        userAvatarRes = getSavedDrawable(prefs, context, KEY_USER_AVATAR, R.drawable.boy_avatar_profile)
        userProfileImageUri = prefs.getString(KEY_USER_IMAGE_URI, null)

        try {
            prefs.getString(KEY_RECENT_ACT, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                recentActivities = gson.fromJson(it, type) ?: mutableListOf()
            }
        } catch (e: Exception) {}

        try {
            prefs.getString(KEY_DAILY_MOODS, null)?.let {
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                dailyMoods = gson.fromJson(it, type) ?: mutableMapOf()
            }
        } catch (e: Exception) {}

        lastMoodTimestamp = prefs.getLong(KEY_LAST_MOOD_TIMESTAMP, 0L)
        displaySize = prefs.getString(KEY_DISPLAY_SIZE, "S") ?: "S"
        homeFocusSize = prefs.getString("home_focus_size", "M") ?: "M"
        homeDisplaySize = prefs.getString(KEY_HOME_DISPLAY_SIZE, "S") ?: "S"
        fontSize = prefs.getString(KEY_FONT_SIZE, "S") ?: "S"
        isSystemAppearanceEnabled = prefs.getBoolean(KEY_SYSTEM_APPEARANCE, true)
        appThemeMode = prefs.getString("app_theme_mode", "DARK") ?: "DARK"
        appAccentColor = prefs.getInt("app_accent_color", -1)
        appFontFamily = prefs.getString("app_font_family", "DEFAULT") ?: "DEFAULT"
        appBorderRadius = prefs.getInt("app_border_radius", 16)
        appCardStyle = prefs.getString("app_card_style", "GLASS") ?: "GLASS"
        appShowShadows = prefs.getBoolean("app_show_shadows", true)
        userXP = prefs.getInt(KEY_USER_XP, 0)
        userLevel = prefs.getInt(KEY_USER_LEVEL, 1)

        showHabitSection = prefs.getBoolean(KEY_SHOW_HABITS, true)
        showWorkoutSection = prefs.getBoolean(KEY_SHOW_WORKOUTS, true)
        showTaskSection = prefs.getBoolean(KEY_SHOW_TASKS, true)
        showNoteSection = prefs.getBoolean(KEY_SHOW_NOTES, true)
        showProjectSection = prefs.getBoolean(KEY_SHOW_PROJECTS, true)
        showFinanceSection = prefs.getBoolean(KEY_SHOW_FINANCE, true)

        try {
            prefs.getString(KEY_CUSTOM_COLORS, null)?.let {
                val type = object : TypeToken<MutableList<Int>>() {}.type
                userCustomColors = gson.fromJson(it, type) ?: mutableListOf()
            }
        } catch (e: Exception) {}

        try {
            prefs.getString(KEY_PROJ_TAGS, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                projectCustomTags = gson.fromJson(it, type) ?: mutableListOf("UI", "LOGIC", "BUG")
            }
        } catch (e: Exception) {}

        globalHabitColor = prefs.getInt(KEY_GLOBAL_HABIT_COLOR, -1)
        globalWorkoutColor = prefs.getInt(KEY_GLOBAL_WORKOUT_COLOR, -1)
        globalTaskColor = prefs.getInt(KEY_GLOBAL_TASK_COLOR, -1)
        globalProjectColor = prefs.getInt(KEY_GLOBAL_PROJECT_COLOR, -1)
        globalNoteColor = prefs.getInt(KEY_GLOBAL_NOTE_COLOR, -1)
        globalFinanceColor = prefs.getInt(KEY_GLOBAL_FINANCE_COLOR, -1)

        habitAddThemeColor = prefs.getInt(KEY_HABIT_ADD_COLOR, -1)
        workoutAddThemeColor = prefs.getInt(KEY_WORKOUT_ADD_COLOR, -1)
        taskAddThemeColor = prefs.getInt(KEY_TASK_ADD_COLOR, -1)
        noteAddThemeColor = prefs.getInt(KEY_NOTE_ADD_COLOR, -1)
        projectAddThemeColor = prefs.getInt(KEY_PROJECT_ADD_COLOR, -1)
        financeAddThemeColor = prefs.getInt(KEY_FINANCE_ADD_COLOR, -1)

        globalHabitIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_HABIT_ICON, R.drawable.ic_habit_tracker)
        globalWorkoutIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_WORKOUT_ICON, R.drawable.ic_workout_routine)
        globalTaskIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_TASK_ICON, R.drawable.ic_task)
        globalProjectIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_PROJECT_ICON, R.drawable.ic_project)
        globalNoteIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_NOTE_ICON, R.drawable.ic_notes)
        globalFinanceIcon = getSavedDrawable(prefs, context, KEY_GLOBAL_FINANCE_ICON, R.drawable.ic_finance)

        try {
            prefs.getString(KEY_PROJ_TEMPLATES, null)?.let {
                val type = object : TypeToken<MutableMap<String, List<String>>>() {}.type
                projectTemplates = gson.fromJson(it, type) ?: projectTemplates
            }
        } catch (e: Exception) {}

        habitDefaultTab = prefs.getString(KEY_HABIT_DEFAULT_TAB, "TODAY") ?: "TODAY"
        habitVacationMode = prefs.getBoolean(KEY_HABIT_VACATION_MODE, false)
        habitSortOrder = prefs.getString(KEY_HABIT_SORT_ORDER, "Time") ?: "Time"
        habitCompletionSound = prefs.getBoolean(KEY_HABIT_SOUND, true)
        habitCompletionHaptics = prefs.getBoolean(KEY_HABIT_HAPTICS, true)
        habitDayResetHour = prefs.getInt(KEY_HABIT_RESET_HOUR, 0)
        habitBulkMode = prefs.getBoolean(KEY_HABIT_BULK_MODE, false)
        habitGraceDaysAllowed = prefs.getInt(KEY_HABIT_GRACE_DAYS, 1)

        try {
            prefs.getString(KEY_WORKOUT_MUSCLE_GROUPS, null)?.let {
                val type = object : TypeToken<MutableList<String>>() {}.type
                workoutMuscleGroups = gson.fromJson(it, type) ?: workoutMuscleGroups
            }
        } catch (e: Exception) {}
        workoutAutoRestTimer = prefs.getBoolean(KEY_WORKOUT_AUTO_REST, false)
        workoutWeightUnit = prefs.getString(KEY_WORKOUT_UNIT, "Kg") ?: "Kg"
        workoutDefaultMode = prefs.getString(KEY_WORKOUT_DEFAULT_MODE, "Reps") ?: "Reps"
        workoutRestDuration = prefs.getInt(KEY_WORKOUT_REST_DURATION, 60)

        if (taskAutoArchive) {
            autoArchiveTasks()
        }

        checkAndResetDailyProgress(context)
        checkAndResetMonthlyFinance(context)
    }

    private fun checkAndResetDailyProgress(context: Context) {
        val prefs = getPrefs(context)
        val today = getTrackingDateString()
        val lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""

        if (lastResetDate.isNotEmpty() && today != lastResetDate) {
            // Fix: Calculate scheduled count for the PREVIOUS date (lastResetDate)
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val prevCal = Calendar.getInstance()
            try {
                sdf.parse(lastResetDate)?.let { prevCal.time = it }
            } catch (e: Exception) {}
            
            val dayOfWeek = (prevCal.get(Calendar.DAY_OF_WEEK) - 1) // 0=Sun
            
            // Calculate the end of that previous day in milliseconds
            val prevDayEnd = prevCal.apply { 
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val scheduledHabits = habits.filter { 
                (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                it.timestamp <= prevDayEnd
            }
            val scheduledWorkouts = workouts.filter { 
                (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                it.timestamp <= prevDayEnd
            }

            val prevHabitsCompleted = habits.count { h -> 
                h.completedDates.contains(lastResetDate)
            }
            val prevWorkoutsCompleted = workouts.count { w -> 
                w.completedDates.contains(lastResetDate)
            }
            
            history[lastResetDate] = DayHistory(
                prevHabitsCompleted,
                scheduledHabits.size,
                prevWorkoutsCompleted,
                scheduledWorkouts.size
            )

            habits.forEach { 
                it.isCompleted = false 
                it.isExpanded = false
            }
            workouts.forEach {
                it.isCompleted = false
                it.progress = 0
                it.isExpanded = false
            }
            saveData(context)
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply()
        } else if (lastResetDate.isEmpty()) {
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply()
        }
    }

    private fun checkAndResetMonthlyFinance(context: Context) {
        val prefs = getPrefs(context)
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val currentMonth = sdf.format(Date())
        val lastResetMonth = prefs.getString(KEY_LAST_MONTH_RESET, "") ?: ""

        if (lastResetMonth.isNotEmpty() && currentMonth != lastResetMonth) {
            monthlyBudgets[lastResetMonth] = monthlyBudget
            monthlySavingsGoals[lastResetMonth] = monthlySavingsGoal
            
            // Fix: Don't reset to zero, carry over previous budget/goal for consistency
            // monthlyBudget = 0.0
            // monthlySavingsGoal = 0.0
            saveData(context)
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonth).apply()
        } else if (lastResetMonth.isEmpty()) {
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonth).apply()
        }
        
        if (!monthlyBudgets.containsKey(currentMonth)) {
            monthlyBudgets[currentMonth] = monthlyBudget
        }
        if (!monthlySavingsGoals.containsKey(currentMonth)) {
            monthlySavingsGoals[currentMonth] = monthlySavingsGoal
        }
    }

    fun exportData(): String {
        val allData = AllAppData(
            habits, workouts, tasks, notes, history, transactions,
            ledgerEntries, personalLedgers,
            monthlyBudget, monthlySavingsGoal,
            monthlyBudgets, monthlySavingsGoals, dailyMoods
        )
        return Gson().toJson(allData)
    }

    fun importData(context: Context, json: String): Boolean {
        return try {
            val allData = Gson().fromJson(json, AllAppData::class.java) ?: return false
            
            // Resilient Restoration with Null Safety
            habits = allData.habits?.toMutableList() ?: mutableListOf()
            workouts = allData.workouts?.toMutableList() ?: mutableListOf()
            tasks = allData.tasks?.toMutableList() ?: mutableListOf()
            notes = allData.notes?.toMutableList() ?: mutableListOf()
            transactions = allData.transactions?.toMutableList() ?: mutableListOf()
            history = allData.history?.toMutableMap() ?: mutableMapOf()
            
            ledgerEntries = allData.ledgerEntries?.toMutableList() ?: mutableListOf()
            personalLedgers = allData.personalLedgers?.toMutableList() ?: mutableListOf()
            monthlyBudgets = allData.monthlyBudgets?.toMutableMap() ?: mutableMapOf()
            monthlySavingsGoals = allData.monthlySavingsGoals?.toMutableMap() ?: mutableMapOf()
            dailyMoods = allData.dailyMoods?.toMutableMap() ?: mutableMapOf()
            
            monthlyBudget = allData.monthlyBudget
            monthlySavingsGoal = allData.monthlySavingsGoal
            
            // Sanitization for nested fields (important for older backup versions)
            habits.forEach { 
                it.isExpanded = false
                if (it.completedDates == null) it.completedDates = mutableListOf() 
            }
            
            workouts.forEach { workout ->
                workout.isExpanded = false
                if (workout.completedDates == null) workout.completedDates = mutableListOf()
                if (workout.muscleGroups == null) {
                    workout.muscleGroups = listOf("General")
                }
            }

            tasks = tasks.map { task ->
                task.copy(
                    subtasks = task.subtasks ?: mutableListOf(),
                    category = task.category ?: "General",
                    section = task.section ?: "Tasks"
                )
            }.toMutableList()

            notes.forEach { note ->
                sanitizeProjectNote(note)
            }

            saveData(context)
            true
        } catch (e: Exception) {
            android.util.Log.e("DataManager", "Import Failed: ${e.message}", e)
            false
        }
    }

    fun getHabitProgress(): Int {
        val todayIndex = (getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysHabits = habits.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        if (todaysHabits.isEmpty()) return 0
        return (todaysHabits.count { it.isCompleted } * 100) / todaysHabits.size
    }

    fun getWorkoutProgress(): Int {
        val todayIndex = (getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysWorkouts = workouts.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        if (todaysWorkouts.isEmpty()) return 0
        return (todaysWorkouts.count { it.isCompleted } * 100) / todaysWorkouts.size
    }

    fun getTotalDailyProgress(): Int {
        val todayIndex = (getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysHabits = habits.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        val todaysWorkouts = workouts.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        
        val totalItems = todaysHabits.size + todaysWorkouts.size
        if (totalItems == 0) return 0
        
        val totalCompleted = todaysHabits.count { it.isCompleted } + 
                             todaysWorkouts.count { it.isCompleted }
                             
        return (totalCompleted * 100) / totalItems
    }

    fun getTotalHabitsFinished() = history.values.sumOf { it.habitsCompleted } + habits.count { it.isCompleted }
    fun getTotalWorkoutsFinished() = history.values.sumOf { it.workoutsCompleted } + workouts.count { it.isCompleted }

    fun getCurrentStreak(): Int {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = getTrackingCalendar()
        var streak = 0
        var graceDaysUsed = 0
        
        if (getTotalDailyProgress() >= 100) streak = 1
        
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        var lookbackDays = 0
        while (lookbackDays < 365) {
            lookbackDays++
            val dateStr = sdf.format(calendar.time)
            val dayData = history[dateStr]
            
            if (dayData == null && !habitVacationMode && graceDaysUsed >= habitGraceDaysAllowed) break

            val isDayFullFilled = dayData != null && (dayData.habitsCompleted + dayData.workoutsCompleted) > 0 && 
                (dayData.habitsCompleted + dayData.workoutsCompleted) >= (dayData.totalHabits + dayData.totalWorkouts)

            if (isDayFullFilled) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else if (habitVacationMode) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else if (graceDaysUsed < habitGraceDaysAllowed) {
                graceDaysUsed++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        return streak
    }

    fun getGlobalCompletionRate(): Int {
        val totalAttempted = history.values.sumOf { it.totalHabits + it.totalWorkouts } + habits.size + workouts.size
        if (totalAttempted == 0) return 0
        val totalCompleted = history.values.sumOf { it.habitsCompleted + it.workoutsCompleted } + 
                             habits.count { it.isCompleted } + workouts.count { it.isCompleted }
        return (totalCompleted * 100) / totalAttempted
    }

    fun getCurrentMonthExpenditure(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { 
            val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == "Expense" && 
            transCal.get(Calendar.MONTH) == currentMonth && 
            transCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    fun getCurrentMonthIncome(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { 
            val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == "Income" && 
            transCal.get(Calendar.MONTH) == currentMonth && 
            transCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    fun getCurrentMonthSavings(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { 
            val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            it.type == "Saving" &&
            transCal.get(Calendar.MONTH) == currentMonth && 
            transCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }

    fun getHabitPerformanceByFrequency(): Map<String, Int> {
        val frequencies = listOf("Morning", "Afternoon", "Evening", "Anytime")
        return frequencies.associateWith { freq ->
            val freqHabits = habits.filter { it.frequency == freq }
            if (freqHabits.isEmpty()) -1
            else {
                (freqHabits.count { it.isCompleted } * 100) / freqHabits.size
            }
        }
    }

    fun getTaskPerformanceByCategory(): Map<String, Int> {
        val categories = taskCustomCategories
        return categories.associateWith { cat ->
            val catTasks = tasks.filter { it.category == cat }
            if (catTasks.isEmpty()) -1
            else {
                (catTasks.count { it.isCompleted } * 100) / catTasks.size
            }
        }
    }

    fun getWorkoutPerformanceByMuscleGroup(): Map<String, Int> {
        val groups = workoutMuscleGroups
        return groups.associateWith { group ->
            val groupWorkouts = workouts.filter { it.muscleGroups.contains(group) }
            if (groupWorkouts.isEmpty()) -1
            else {
                (groupWorkouts.count { it.isCompleted } * 100) / groupWorkouts.size
            }
        }
    }

    fun getWorkoutPerformanceByFrequency(): Map<String, Int> {
        val frequencies = listOf("Morning", "Afternoon", "Evening", "Anytime")
        return frequencies.associateWith { freq ->
            val freqWorkouts = workouts.filter { it.frequency == freq }
            if (freqWorkouts.isEmpty()) -1
            else {
                (freqWorkouts.count { it.isCompleted } * 100) / freqWorkouts.size
            }
        }
    }

    fun getLastSevenDaysProgress(): List<Int> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val cal = getTrackingCalendar()
        val results = mutableListOf<Int>()
        val todayStr = getTrackingDateString()

        for (i in 0 until 7) {
            val dateKey = sdf.format(cal.time)
            val progress = if (dateKey == todayStr) {
                getTotalDailyProgress()
            } else {
                val dayData = history[dateKey]
                if (dayData != null) {
                    val total = dayData.totalHabits + dayData.totalWorkouts
                    if (total > 0) ((dayData.habitsCompleted + dayData.workoutsCompleted) * 100) / total else 0
                } else 0
            }
            results.add(0, progress) // Oldest first
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return results
    }

    fun getLastSevenDaysDetailedProgress(): List<Pair<Int, Int>> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val cal = getTrackingCalendar()
        val results = mutableListOf<Pair<Int, Int>>()
        val todayStr = getTrackingDateString()

        for (i in 0 until 7) {
            val dateKey = sdf.format(cal.time)
            val progress = if (dateKey == todayStr) {
                Pair(getHabitProgress(), getWorkoutProgress())
            } else {
                val dayData = history[dateKey]
                if (dayData != null) {
                    val hProgress = if (dayData.totalHabits > 0) (dayData.habitsCompleted * 100) / dayData.totalHabits else 0
                    val wProgress = if (dayData.totalWorkouts > 0) (dayData.workoutsCompleted * 100) / dayData.totalWorkouts else 0
                    Pair(hProgress, wProgress)
                } else Pair(0, 0)
            }
            results.add(0, progress)
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return results
    }

    fun getTrackingDateString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(getTrackingCalendar().time)
    }

    fun getTrackingDateString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour < habitDayResetHour) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
    }

    fun getTrackingCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour < habitDayResetHour) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return calendar
    }

    fun getTodayCaloriesBurned(): Int {
        val todayWorkouts = workouts.filter { it.isCompleted }
        var total = 0.0
        todayWorkouts.forEach { workout ->
            total += when (workout.trackingMode) {
                "Timer" -> workout.target * 0.1
                "Reps" -> workout.target * 0.5
                "Sets" -> workout.target * 5.0
                else -> 0.0
            }
        }
        return total.toInt()
    }

    private fun sanitizeProjectNote(note: Note) {
        if (note.title == null) note.title = "Untitled Project"
        if (note.content == null) note.content = ""
        if (note.status == null) note.status = "Not Started"
        if (note.category == null) note.category = "Project"

        if (note.subFeatures == null) {
            try {
                val field = Note::class.java.getDeclaredField("subFeatures")
                field.isAccessible = true
                field.set(note, mutableListOf<ProjectFeature>())
            } catch (e: Exception) {}
        }
        
        note.subFeatures?.let { sanitizeProjectFeatures(it) }

        if (note.journalEntries == null) {
            try {
                val field = Note::class.java.getDeclaredField("journalEntries")
                field.isAccessible = true
                field.set(note, mutableListOf<JournalEntry>())
            } catch (e: Exception) {}
        }

        if (note.ideaGoals == null) {
            try {
                val field = Note::class.java.getDeclaredField("ideaGoals")
                field.isAccessible = true
                field.set(note, mutableListOf<JournalEntry>())
            } catch (e: Exception) {}
        }

        if (note.changeHistory == null) {
            try {
                val field = Note::class.java.getDeclaredField("changeHistory")
                field.isAccessible = true
                field.set(note, mutableListOf<ProjectHistory>())
            } catch (e: Exception) {}
        }
    }

    private fun sanitizeProjectFeatures(features: MutableList<ProjectFeature>) {
        // De-duplication Logic: Remove items with the same ID
        val uniqueFeatures = features.distinctBy { it.id }
        if (uniqueFeatures.size != features.size) {
            features.clear()
            features.addAll(uniqueFeatures)
        }

        features.forEach { feature ->
            if (feature == null) return@forEach

            if (feature.name == null) feature.name = "New Node"
            if (feature.details == null) feature.details = ""
            if (feature.resourceUrl == null) feature.resourceUrl = ""
            if (feature.resourcePath == null) feature.resourcePath = ""
            if (feature.blockedByNodeId == null) feature.blockedByNodeId = ""
            
            // New fields for milestone enhancements
            // Since these are primitives with defaults in data class, Gson usually handles them, 
            // but for safety if it was null in old version:
            if (feature.weight == 0) feature.weight = 1 
            // priority and hasReminder will default to 0/false if missing in JSON

            if (feature.subFeatures == null) {
                try {
                    val field = ProjectFeature::class.java.getDeclaredField("subFeatures")
                    field.isAccessible = true
                    field.set(feature, mutableListOf<ProjectFeature>())
                } catch (e: Exception) {}
            }
            
            if (feature.id == null) {
                try {
                    val field = ProjectFeature::class.java.getDeclaredField("id")
                    field.isAccessible = true
                    field.set(feature, UUID.randomUUID().toString())
                } catch (e: Exception) {}
            }

            if (feature.subFeatures != null) {
                sanitizeProjectFeatures(feature.subFeatures)
            }
        }
    }

    fun resetAppearanceIcons() {
        globalHabitIcon = R.drawable.ic_habit_tracker
        globalWorkoutIcon = R.drawable.ic_fitness
        globalTaskIcon = R.drawable.ic_task
        globalNoteIcon = R.drawable.ic_notes
        globalProjectIcon = R.drawable.ic_project
        globalFinanceIcon = R.drawable.ic_finance
    }

    fun resetAppearanceColors() {
        globalHabitColor = -1
        globalWorkoutColor = -1
        globalTaskColor = -1
        globalNoteColor = -1
        globalProjectColor = -1
        globalFinanceColor = -1

        habitAddThemeColor = -1
        workoutAddThemeColor = -1
        taskAddThemeColor = -1
        noteAddThemeColor = -1
        projectAddThemeColor = -1
        financeAddThemeColor = -1
    }

    private fun autoArchiveTasks() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        tasks.removeAll { 
            it.isCompleted && (it.completedTimestamp ?: 0L) < sevenDaysAgo 
        }
    }

    fun addActivity(action: String) {
        recentActivities.add(0, action)
        if (recentActivities.size > 10) {
            recentActivities = recentActivities.take(10).toMutableList()
        }
    }

    fun getGrowthAdvice(mood: String?): String {
        return when (mood) {
            "🔥" -> "Momentum is a flywheel. Keep pushing."
            "⚡" -> "Channel this energy into high-intensity work."
            "🧘" -> "Consistency in small rituals builds character."
            "💼" -> "15 minutes of movement is better than zero."
            "😴" -> "Active recovery keeps the streak alive."
            "🧠" -> "Focus deeply on each movement for growth."
            else -> "Daily discipline is your foundation."
        }
    }

    fun getManagementAdvice(mood: String?): String {
        return when (mood) {
            "🔥" -> "Tackle your complex projects now."
            "⚡" -> "Review priorities before diving into deep work."
            "🧘" -> "A clear workspace reduces cognitive load."
            "💼" -> "Archive finished tasks to keep roadmaps lean."
            "😴" -> "Use low energy for sorting notes or admin."
            "🧠" -> "Convert your best notes into project milestones."
            else -> "Categorize tasks to reclaim your time."
        }
    }

    fun getTodayAgendaNotifications(): Map<String, List<String>> {
        val agenda = mutableMapOf<String, MutableList<String>>()
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + (24 * 60 * 60 * 1000L)

        // 1. Scan Tasks (Due today based on reminderTime)
        val todayTasks = tasks.filter { task ->
            !task.isCompleted && task.reminderTime != null && 
            task.reminderTime!! >= todayStart && task.reminderTime!! < todayEnd
        }
        if (todayTasks.isNotEmpty()) {
            agenda["TASKS"] = todayTasks.map { it.name }.toMutableList()
        }

        // 2. Scan Projects (Notes with deadline today)
        val projectReminders = notes.filter { note ->
            note.category == "Project" && note.status != "Completed" && note.deadline != null &&
            note.deadline!! >= todayStart && note.deadline!! < todayEnd
        }
        if (projectReminders.isNotEmpty()) {
            agenda["PROJECTS"] = projectReminders.map { it.title }.toMutableList()
        }

        // 3. Scan Lists & Notes (Other notes with deadline today)
        val otherReminders = notes.filter { note ->
            note.category != "Project" && note.status != "Completed" && note.deadline != null &&
            note.deadline!! >= todayStart && note.deadline!! < todayEnd
        }
        if (otherReminders.isNotEmpty()) {
            agenda["LISTS & NOTES"] = otherReminders.map { it.title }.toMutableList()
        }

        return agenda
    }

    // --- LIFE ARCHITECTURE LOGIC ---

    fun addXP(context: Context, amount: Int): Boolean {
        userXP += amount
        val xpRequired = getXPForNextLevel()
        if (userXP >= xpRequired) {
            userXP -= xpRequired
            userLevel++
            saveData(context)
            return true // Level Up!
        }
        saveData(context)
        return false
    }

    fun getXPForNextLevel(): Int {
        return (userLevel * userLevel) * 100
    }

    fun getMoodCorrelationData(): String? {
        val successMoods = mutableMapOf<String, Int>()
        
        history.forEach { (date, data) ->
            val total = data.totalHabits + data.totalWorkouts
            if (total > 0 && ((data.habitsCompleted + data.workoutsCompleted) * 100) / total >= 100) {
                val mood = dailyMoods[date]
                if (mood != null) {
                    successMoods[mood] = successMoods.getOrDefault(mood, 0) + 1
                }
            }
        }
        
        val bestMood = successMoods.maxByOrNull { it.value }?.key ?: return null
        val count = successMoods[bestMood] ?: 0
        
        return "You are most productive when feeling $bestMood (Detected $count times)"
    }

    private fun getResourceName(context: Context, resId: Int): String {
        return try {
            context.resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getDrawableIdByName(context: Context, name: String, fallbackId: Int): Int {
        if (name.isEmpty()) return fallbackId
        return try {
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) resId else fallbackId
        } catch (e: Exception) {
            fallbackId
        }
    }

    private fun getSavedDrawable(prefs: SharedPreferences, context: Context, key: String, defaultResId: Int): Int {
        return try {
            val name = prefs.getString(key, null)
            if (name != null) {
                getDrawableIdByName(context, name, defaultResId)
            } else {
                prefs.getInt(key, defaultResId)
            }
        } catch (e: Exception) {
            try {
                prefs.getInt(key, defaultResId)
            } catch (ex: Exception) {
                defaultResId
            }
        }
    }
}
