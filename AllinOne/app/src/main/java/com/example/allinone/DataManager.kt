package com.example.allinone

import android.content.Context
import android.content.SharedPreferences
import com.example.allinone.data.*
import com.example.allinone.workspace.data.WorkspaceDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object DataManager {
    val dataChangeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun notifyDataChanged() {
        dataChangeSignal.tryEmit(Unit)
    }

    // Delegated Data
    var habits: MutableList<Habit> get() = HabitDataManager.habits; set(value) { HabitDataManager.habits = value }
    var workouts: MutableList<Workout> get() = WorkoutDataManager.workouts; set(value) { WorkoutDataManager.workouts = value }
    var tasks: MutableList<Task> get() = TaskDataManager.tasks; set(value) { TaskDataManager.tasks = value }
    var notes: MutableList<Note> get() = NoteDataManager.notes; set(value) { NoteDataManager.notes = value }
    var projects: MutableList<Note> get() = ProjectDataManager.projects; set(value) { ProjectDataManager.projects = value }
    var transactions: MutableList<Transaction> get() = FinanceDataManager.transactions; set(value) { FinanceDataManager.transactions = value }
    var ledgerEntries: MutableList<LedgerEntry> get() = FinanceDataManager.ledgerEntries; set(value) { FinanceDataManager.ledgerEntries = value }
    var personalLedgers: MutableList<PersonalLedger> get() = FinanceDataManager.personalLedgers; set(value) { FinanceDataManager.personalLedgers = value }
    
    var monthlyBudget: Double get() = FinanceDataManager.monthlyBudget; set(value) { FinanceDataManager.monthlyBudget = value }
    var monthlySavingsGoal: Double get() = FinanceDataManager.monthlySavingsGoal; set(value) { FinanceDataManager.monthlySavingsGoal = value }
    var financeSavingsGoalName: String get() = FinanceDataManager.financeSavingsGoalName; set(value) { FinanceDataManager.financeSavingsGoalName = value }
    var monthlyBudgets: MutableMap<String, Double> get() = FinanceDataManager.monthlyBudgets; set(value) { FinanceDataManager.monthlyBudgets = value }
    var monthlySavingsGoals: MutableMap<String, Double> get() = FinanceDataManager.monthlySavingsGoals; set(value) { FinanceDataManager.monthlySavingsGoals = value }
    
    var history = mutableMapOf<String, DayHistory>() // Keep core history here for now as it's cross-domain

    // To-Do List Settings
    var taskShowCompleted: Boolean get() = TaskDataManager.taskShowCompleted; set(value) { TaskDataManager.taskShowCompleted = value }
    var taskShowHidden: Boolean get() = TaskDataManager.taskShowHidden; set(value) { TaskDataManager.taskShowHidden = value }
    var taskSortOrder: String get() = TaskDataManager.taskSortOrder; set(value) { TaskDataManager.taskSortOrder = value }
    var taskCustomCategories: MutableList<String> get() = TaskDataManager.taskCustomCategories; set(value) { TaskDataManager.taskCustomCategories = value }
    var taskAutoArchive: Boolean get() = TaskDataManager.taskAutoArchive; set(value) { TaskDataManager.taskAutoArchive = value }
    var taskDefaultSection: String get() = TaskDataManager.taskDefaultSection; set(value) { TaskDataManager.taskDefaultSection = value }
    var taskVisibleSections: MutableList<String> get() = TaskDataManager.taskVisibleSections; set(value) { TaskDataManager.taskVisibleSections = value }
    var taskEditModeEnabled: Boolean get() = TaskDataManager.taskEditModeEnabled; set(value) { TaskDataManager.taskEditModeEnabled = value }

    // Finance Settings
    var financeCustomCategories: MutableList<String> get() = FinanceDataManager.financeCustomCategories; set(value) { FinanceDataManager.financeCustomCategories = value }
    var financeCategoryIcons: MutableMap<String, Int> get() = FinanceDataManager.financeCategoryIcons; set(value) { FinanceDataManager.financeCategoryIcons = value }
    var financeCategoryColors: MutableMap<String, Int> get() = FinanceDataManager.financeCategoryColors; set(value) { FinanceDataManager.financeCategoryColors = value }
    var financeCurrency: String get() = FinanceDataManager.financeCurrency; set(value) { FinanceDataManager.financeCurrency = value }
    var financeGraphStartMonth: Int get() = FinanceDataManager.financeGraphStartMonth; set(value) { FinanceDataManager.financeGraphStartMonth = value }
    var financeGraphColor: Int get() = FinanceDataManager.financeGraphColor; set(value) { FinanceDataManager.financeGraphColor = value }
    var financeGraphSavingsColor: Int get() = FinanceDataManager.financeGraphSavingsColor; set(value) { FinanceDataManager.financeGraphSavingsColor = value }
    var isFinanceLedgerEnabled: Boolean get() = FinanceDataManager.isFinanceLedgerEnabled; set(value) { FinanceDataManager.isFinanceLedgerEnabled = value }

    // Habit Settings
    var habitDefaultTab: String get() = HabitDataManager.habitDefaultTab; set(value) { HabitDataManager.habitDefaultTab = value }
    var habitVacationMode: Boolean get() = HabitDataManager.habitVacationMode; set(value) { HabitDataManager.habitVacationMode = value }
    var habitSortOrder: String get() = HabitDataManager.habitSortOrder; set(value) { HabitDataManager.habitSortOrder = value }
    var habitCompletionSound: Boolean get() = HabitDataManager.habitCompletionSound; set(value) { HabitDataManager.habitCompletionSound = value }
    var habitCompletionHaptics: Boolean get() = HabitDataManager.habitCompletionHaptics; set(value) { HabitDataManager.habitCompletionHaptics = value }
    var habitDayResetHour: Int get() = HabitDataManager.habitDayResetHour; set(value) { HabitDataManager.habitDayResetHour = value }
    var habitBulkMode: Boolean get() = HabitDataManager.habitBulkMode; set(value) { HabitDataManager.habitBulkMode = value }
    var habitGraceDaysAllowed: Int get() = HabitDataManager.habitGraceDaysAllowed; set(value) { HabitDataManager.habitGraceDaysAllowed = value }
    var habitShowCompleted: Boolean get() = HabitDataManager.habitShowCompleted; set(value) { HabitDataManager.habitShowCompleted = value }

    // Workout Settings
    var workoutMuscleGroups: MutableList<String> get() = WorkoutDataManager.workoutMuscleGroups; set(value) { WorkoutDataManager.workoutMuscleGroups = value }
    var workoutAutoRestTimer: Boolean get() = WorkoutDataManager.workoutAutoRestTimer; set(value) { WorkoutDataManager.workoutAutoRestTimer = value }
    var workoutWeightUnit: String get() = WorkoutDataManager.workoutWeightUnit; set(value) { WorkoutDataManager.workoutWeightUnit = value }
    var workoutDefaultMode: String get() = WorkoutDataManager.workoutDefaultMode; set(value) { WorkoutDataManager.workoutDefaultMode = value }
    var workoutRestDuration: Int get() = WorkoutDataManager.workoutRestDuration; set(value) { WorkoutDataManager.workoutRestDuration = value }
    var workoutShowCompleted: Boolean get() = WorkoutDataManager.workoutShowCompleted; set(value) { WorkoutDataManager.workoutShowCompleted = value }

    // Note Settings
    var noteAutoCleanupDays: Int get() = NoteDataManager.noteAutoCleanupDays; set(value) { NoteDataManager.noteAutoCleanupDays = value }
    var noteDefaultCategory: String get() = NoteDataManager.noteDefaultCategory; set(value) { NoteDataManager.noteDefaultCategory = value }
    var noteShowHidden: Boolean get() = NoteDataManager.noteShowHidden; set(value) { NoteDataManager.noteShowHidden = value }
    var noteVisibleSections: MutableList<String> get() = NoteDataManager.noteVisibleSections; set(value) { NoteDataManager.noteVisibleSections = value }
    
    // Project Advanced Settings
    var projectAutoArchive: Boolean get() = ProjectDataManager.projectAutoArchive; set(value) { ProjectDataManager.projectAutoArchive = value }
    var projectSynergySync: Boolean get() = ProjectDataManager.projectSynergySync; set(value) { ProjectDataManager.projectSynergySync = value }
    var projectDeadlineAlerts: Boolean get() = ProjectDataManager.projectDeadlineAlerts; set(value) { ProjectDataManager.projectDeadlineAlerts = value }
    var projectAnalyticsEnabled: Boolean get() = ProjectDataManager.projectAnalyticsEnabled; set(value) { ProjectDataManager.projectAnalyticsEnabled = value }
    var projectCustomTags: MutableList<String> get() = ProjectDataManager.projectCustomTags; set(value) { ProjectDataManager.projectCustomTags = value }
    var projectSortCompletedToBottom: Boolean get() = ProjectDataManager.projectSortCompletedToBottom; set(value) { ProjectDataManager.projectSortCompletedToBottom = value }
    var projectActiveExpanded: Boolean get() = ProjectDataManager.projectActiveExpanded; set(value) { ProjectDataManager.projectActiveExpanded = value }
    var projectCompletedExpanded: Boolean get() = ProjectDataManager.projectCompletedExpanded; set(value) { ProjectDataManager.projectCompletedExpanded = value }
    var ideaActiveExpanded: Boolean get() = ProjectDataManager.ideaActiveExpanded; set(value) { ProjectDataManager.ideaActiveExpanded = value }
    var ideaCompletedExpanded: Boolean get() = ProjectDataManager.ideaCompletedExpanded; set(value) { ProjectDataManager.ideaCompletedExpanded = value }
    var projectAutoSaveIdeas: Boolean get() = ProjectDataManager.projectAutoSaveIdeas; set(value) { ProjectDataManager.projectAutoSaveIdeas = value }
    var projectDualExistEnabled: Boolean get() = ProjectDataManager.projectDualExistEnabled; set(value) { ProjectDataManager.projectDualExistEnabled = value }
    var projectIdeasEnabled: Boolean get() = ProjectDataManager.projectIdeasEnabled; set(value) { ProjectDataManager.projectIdeasEnabled = value }
    var projectRoadmapsEnabled: Boolean get() = ProjectDataManager.projectRoadmapsEnabled; set(value) { ProjectDataManager.projectRoadmapsEnabled = value }
    
    // User / System Settings
    var isAppLockEnabled: Boolean get() = UserDataManager.isAppLockEnabled; set(value) { UserDataManager.isAppLockEnabled = value }
    var isAppUnlocked: Boolean get() = UserDataManager.isAppUnlocked; set(value) { UserDataManager.isAppUnlocked = value }
    var isOledThemeEnabled: Boolean = false // Legacy
    var isOnboardingCompleted: Boolean get() = UserDataManager.isOnboardingCompleted; set(value) { UserDataManager.isOnboardingCompleted = value }
    var appLockPin: String? get() = UserDataManager.appLockPin; set(value) { UserDataManager.appLockPin = value }
    var appLockQuestion: String? get() = UserDataManager.appLockQuestion; set(value) { UserDataManager.appLockQuestion = value }
    var appLockAnswer: String? get() = UserDataManager.appLockAnswer; set(value) { UserDataManager.appLockAnswer = value }
    
    var lastViewedNotificationDate: String get() = WorkspaceDataManager.lastViewedNotificationDate; set(value) { WorkspaceDataManager.lastViewedNotificationDate = value }
    var lastSummaryNotificationDate: String get() = WorkspaceDataManager.lastSummaryNotificationDate; set(value) { WorkspaceDataManager.lastSummaryNotificationDate = value }
    var workspaceTodayAgenda: MutableMap<String, List<AgendaItem>> get() = WorkspaceDataManager.workspaceTodayAgenda; set(value) { WorkspaceDataManager.workspaceTodayAgenda = value }

    var userXP: Int get() = UserDataManager.userXP; set(value) { UserDataManager.userXP = value }
    var userLevel: Int get() = UserDataManager.userLevel; set(value) { UserDataManager.userLevel = value }
    var userName: String get() = UserDataManager.userName; set(value) { UserDataManager.userName = value }
    var userBio: String get() = UserDataManager.userBio; set(value) { UserDataManager.userBio = value }
    var userAvatarRes: Int get() = UserDataManager.userAvatarRes; set(value) { UserDataManager.userAvatarRes = value }
    var userProfileImageUri: String? get() = UserDataManager.userProfileImageUri; set(value) { UserDataManager.userProfileImageUri = value }
    var recentActivities: MutableList<String> get() = UserDataManager.recentActivities; set(value) { UserDataManager.recentActivities = value }
    var dailyMoods: MutableMap<String, String> get() = UserDataManager.dailyMoods; set(value) { UserDataManager.dailyMoods = value }
    var lastMoodTimestamp: Long get() = UserDataManager.lastMoodTimestamp; set(value) { UserDataManager.lastMoodTimestamp = value }
    var displaySize: String get() = UserDataManager.displaySize; set(value) { UserDataManager.displaySize = value }
    var homeDisplaySize: String get() = UserDataManager.homeDisplaySize; set(value) { UserDataManager.homeDisplaySize = value }
    var homeFocusSize: String get() = UserDataManager.homeFocusSize; set(value) { UserDataManager.homeFocusSize = value }
    var fontSize: String get() = UserDataManager.fontSize; set(value) { UserDataManager.fontSize = value }
    var isSystemAppearanceEnabled: Boolean get() = UserDataManager.isSystemAppearanceEnabled; set(value) { UserDataManager.isSystemAppearanceEnabled = value }
    
    var appThemeMode: String get() = UserDataManager.appThemeMode; set(value) { UserDataManager.appThemeMode = value }
    var appAccentColor: Int get() = UserDataManager.appAccentColor; set(value) { UserDataManager.appAccentColor = value }
    var appFontFamily: String get() = UserDataManager.appFontFamily; set(value) { UserDataManager.appFontFamily = value }
    var appBorderRadius: Int get() = UserDataManager.appBorderRadius; set(value) { UserDataManager.appBorderRadius = value }
    var appCardStyle: String get() = UserDataManager.appCardStyle; set(value) { UserDataManager.appCardStyle = value }
    var appShowShadows: Boolean get() = UserDataManager.appShowShadows; set(value) { UserDataManager.appShowShadows = value }
    var startupLoadingTime: Int get() = UserDataManager.startupLoadingTime; set(value) { UserDataManager.startupLoadingTime = value }
    
    var showHabitSection: Boolean get() = UserDataManager.showHabitSection; set(value) { UserDataManager.showHabitSection = value }
    var showWorkoutSection: Boolean get() = UserDataManager.showWorkoutSection; set(value) { UserDataManager.showWorkoutSection = value }
    var showTaskSection: Boolean get() = UserDataManager.showTaskSection; set(value) { UserDataManager.showTaskSection = value }
    var showNoteSection: Boolean get() = UserDataManager.showNoteSection; set(value) { UserDataManager.showNoteSection = value }
    var showProjectSection: Boolean get() = UserDataManager.showProjectSection; set(value) { UserDataManager.showProjectSection = value }
    var showFinanceSection: Boolean get() = UserDataManager.showFinanceSection; set(value) { UserDataManager.showFinanceSection = value }
    
    var userCustomColors: MutableList<Int> get() = UserDataManager.userCustomColors; set(value) { UserDataManager.userCustomColors = value }

    // Global Colors & Icons
    var globalHabitColor: Int get() = HabitDataManager.globalHabitColor; set(value) { HabitDataManager.globalHabitColor = value }
    var globalWorkoutColor: Int get() = WorkoutDataManager.globalWorkoutColor; set(value) { WorkoutDataManager.globalWorkoutColor = value }
    var globalTaskColor: Int get() = TaskDataManager.globalTaskColor; set(value) { TaskDataManager.globalTaskColor = value }
    var globalProjectColor: Int get() = ProjectDataManager.globalProjectColor; set(value) { ProjectDataManager.globalProjectColor = value }
    var globalNoteColor: Int get() = NoteDataManager.globalNoteColor; set(value) { NoteDataManager.globalNoteColor = value }
    var globalFinanceColor: Int get() = FinanceDataManager.globalFinanceColor; set(value) { FinanceDataManager.globalFinanceColor = value }

    var habitAddThemeColor: Int get() = HabitDataManager.habitAddThemeColor; set(value) { HabitDataManager.habitAddThemeColor = value }
    var workoutAddThemeColor: Int get() = WorkoutDataManager.workoutAddThemeColor; set(value) { WorkoutDataManager.workoutAddThemeColor = value }
    var taskAddThemeColor: Int get() = TaskDataManager.taskAddThemeColor; set(value) { TaskDataManager.taskAddThemeColor = value }
    var noteAddThemeColor: Int get() = NoteDataManager.noteAddThemeColor; set(value) { NoteDataManager.noteAddThemeColor = value }
    var projectAddThemeColor: Int get() = ProjectDataManager.projectAddThemeColor; set(value) { ProjectDataManager.projectAddThemeColor = value }
    var financeAddThemeColor: Int get() = FinanceDataManager.financeAddThemeColor; set(value) { FinanceDataManager.financeAddThemeColor = value }

    var globalHabitIcon: Int get() = HabitDataManager.globalHabitIcon; set(value) { HabitDataManager.globalHabitIcon = value }
    var globalWorkoutIcon: Int get() = WorkoutDataManager.globalWorkoutIcon; set(value) { WorkoutDataManager.globalWorkoutIcon = value }
    var globalTaskIcon: Int get() = TaskDataManager.globalTaskIcon; set(value) { TaskDataManager.globalTaskIcon = value }
    var globalProjectIcon: Int get() = ProjectDataManager.globalProjectIcon; set(value) { ProjectDataManager.globalProjectIcon = value }
    var globalNoteIcon: Int get() = NoteDataManager.globalNoteIcon; set(value) { NoteDataManager.globalNoteIcon = value }
    var globalFinanceIcon: Int get() = FinanceDataManager.globalFinanceIcon; set(value) { FinanceDataManager.globalFinanceIcon = value }

    var projectTemplates: MutableMap<String, List<String>> get() = ProjectDataManager.projectTemplates; set(value) { ProjectDataManager.projectTemplates = value }
    var noteTemplates: MutableMap<String, String> get() = NoteDataManager.noteTemplates; set(value) { NoteDataManager.noteTemplates = value }

    // Logic Delegation
    fun addActivity(activity: String) = UserDataManager.addActivity(activity)
    
    fun addXP(context: Context, amount: Int): Boolean {
        val leveledUp = UserDataManager.addXP(amount)
        saveData(context)
        return leveledUp
    }

    fun getHabitProgress() = HabitDataManager.getHabitProgress()
    fun getHabitStreak() = HabitDataManager.getHabitStreak()
    fun getTotalHabitsFinished() = HabitDataManager.getTotalHabitsFinished()
    
    fun getWorkoutProgress() = WorkoutDataManager.getWorkoutProgress()
    fun getWorkoutStreak() = WorkoutDataManager.getWorkoutStreak()
    fun getTotalWorkoutsFinished() = WorkoutDataManager.getTotalWorkoutsFinished()
    fun getTodayCaloriesBurned() = WorkoutDataManager.getTodayCaloriesBurned()
    
    fun getCurrentMonthExpenditure() = FinanceDataManager.getCurrentMonthExpenditure()
    fun getCurrentMonthIncome() = FinanceDataManager.getCurrentMonthIncome()
    fun getCurrentMonthSavings() = FinanceDataManager.getCurrentMonthSavings()

    fun getTotalDailyProgress(): Int {
        val hp = getHabitProgress()
        val wp = getWorkoutProgress()
        return if (hp == 0 && wp == 0) 0 else (hp + wp) / 2
    }

    fun getGrowthAdvice(mood: String?): String {
        return when (mood) {
            "Happy" -> "You're in a great state! Use this energy to tackle your biggest goals."
            "Stressed" -> "Take a deep breath. Focus on one small task at a time."
            "Tired" -> "Rest is productive too. Consider a shorter workout today."
            else -> "Consistency is the key to long-term growth. Keep showing up!"
        }
    }

    fun getManagementAdvice(mood: String?): String {
        return "Organize your workflow and prioritize your high-impact tasks."
    }

    fun getTodayAgendaNotifications(): Map<String, List<AgendaItem>> {
        return workspaceTodayAgenda
    }

    fun getTrackingDateString(timestamp: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
    }

    fun getTrackingCalendar(timestamp: Long = System.currentTimeMillis()): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal
    }

    fun getUniqueFeatureName(baseName: String, existing: List<ProjectFeature>): String {
        var name = baseName
        var count = 1
        while (existing.any { it.name == name }) {
            name = "$baseName (${count++})"
        }
        return name
    }

    // Original Persistence Logic (Keep here for central Save/Load)
    private const val PREFS_NAME = "all_in_one_prefs"
    private const val KEY_HABITS = "habits_data"
    private const val KEY_WORKOUTS = "workouts_data"
    private const val KEY_TASKS = "tasks_data"
    private const val KEY_NOTES = "notes_data"
    private const val KEY_PROJECTS = "projects_data"
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
    private const val KEY_PROJ_TEMPLATES = "project_templates"
    private const val KEY_PROJ_ROADMAPS = "project_roadmaps_enabled"
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
    private const val KEY_STARTUP_LOADING_TIME = "startup_loading_time"

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
            putString(KEY_PROJECTS, gson.toJson(projects))
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
            putString("app_lock_question", appLockQuestion)
            putString("app_lock_answer", appLockAnswer)
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
            putInt(KEY_STARTUP_LOADING_TIME, startupLoadingTime)
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
            apply()
        }
        notifyDataChanged()
    }

    // Load logic remains complex and central for now
    fun loadData(context: Context) {
        val prefs = getPrefs(context)
        val gson = Gson()
        
        habits = gson.fromJson(prefs.getString(KEY_HABITS, "[]"), object : TypeToken<MutableList<Habit>>() {}.type) ?: mutableListOf()
        // Ensure new fields are initialized for older data
        habits.forEach { 
            if (it.completedDates == null) it.completedDates = mutableListOf()
            if (it.dailyProgress == null) it.dailyProgress = mutableMapOf()
            if (it.repeatDays == null) it.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
        }
        workouts = gson.fromJson(prefs.getString(KEY_WORKOUTS, "[]"), object : TypeToken<MutableList<Workout>>() {}.type) ?: mutableListOf()
        // Ensure new fields are initialized for older data
        workouts.forEach { 
            if (it.completedDates == null) it.completedDates = mutableListOf()
            if (it.dailyProgress == null) it.dailyProgress = mutableMapOf()
            if (it.repeatDays == null) it.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
            if (it.muscleGroups == null) it.muscleGroups = listOf("General")
        }
        tasks = gson.fromJson(prefs.getString(KEY_TASKS, "[]"), object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
        // Ensure new fields are initialized for older data
        tasks.forEach {
            if (it.subtasks == null) it.subtasks = mutableListOf()
        }
        notes = gson.fromJson(prefs.getString(KEY_NOTES, "[]"), object : TypeToken<MutableList<Note>>() {}.type) ?: mutableListOf()
        projects = gson.fromJson(prefs.getString(KEY_PROJECTS, "[]"), object : TypeToken<MutableList<Note>>() {}.type) ?: mutableListOf()
        transactions = gson.fromJson(prefs.getString(KEY_TRANSACTIONS, "[]"), object : TypeToken<MutableList<Transaction>>() {}.type) ?: mutableListOf()
        ledgerEntries = gson.fromJson(prefs.getString(KEY_LEDGER, "[]"), object : TypeToken<MutableList<LedgerEntry>>() {}.type) ?: mutableListOf()
        personalLedgers = gson.fromJson(prefs.getString(KEY_PERSONAL_LEDGER, "[]"), object : TypeToken<MutableList<PersonalLedger>>() {}.type) ?: mutableListOf()
        
        monthlyBudget = prefs.getFloat(KEY_BUDGET, 0.0f).toDouble()
        monthlySavingsGoal = prefs.getFloat(KEY_SAVINGS_GOAL, 0.0f).toDouble()
        financeSavingsGoalName = prefs.getString(KEY_SAVINGS_GOAL_NAME, "Monthly Savings") ?: "Monthly Savings"
        
        monthlyBudgets = gson.fromJson(prefs.getString(KEY_MONTHLY_BUDGETS, "{}"), object : TypeToken<MutableMap<String, Double>>() {}.type) ?: mutableMapOf()
        monthlySavingsGoals = gson.fromJson(prefs.getString(KEY_MONTHLY_SAVINGS_GOALS, "{}"), object : TypeToken<MutableMap<String, Double>>() {}.type) ?: mutableMapOf()
        
        history = gson.fromJson(prefs.getString(KEY_HISTORY, "{}"), object : TypeToken<MutableMap<String, DayHistory>>() {}.type) ?: mutableMapOf()
        
        taskShowCompleted = prefs.getBoolean(KEY_TASK_SHOW_COMPLETED, true)
        taskShowHidden = prefs.getBoolean(KEY_TASK_SHOW_HIDDEN, false)
        taskSortOrder = prefs.getString(KEY_TASK_SORT_ORDER, "Priority") ?: "Priority"
        taskCustomCategories = gson.fromJson(prefs.getString(KEY_TASK_CUSTOM_CATEGORIES, "[\"General\", \"Personal\", \"Work\", \"Shopping\"]"), object : TypeToken<MutableList<String>>() {}.type)
        taskAutoArchive = prefs.getBoolean(KEY_TASK_AUTO_ARCHIVE, false)
        taskEditModeEnabled = prefs.getBoolean(KEY_TASK_EDIT_MODE, false)
        taskDefaultSection = prefs.getString(KEY_TASK_DEFAULT_SECTION, "Tasks") ?: "Tasks"
        taskVisibleSections = gson.fromJson(prefs.getString(KEY_TASK_VISIBLE_SECTIONS, "[\"Tasks\"]"), object : TypeToken<MutableList<String>>() {}.type)

        financeCustomCategories = gson.fromJson(prefs.getString(KEY_FINANCE_CUSTOM_CATEGORIES, "[\"Food\", \"Rent\", \"Transport\", \"Shopping\", \"Entertainment\", \"Health\", \"Other\"]"), object : TypeToken<MutableList<String>>() {}.type)
        financeCategoryIcons = gson.fromJson(prefs.getString(KEY_FINANCE_CATEGORY_ICONS, "{}"), object : TypeToken<MutableMap<String, Int>>() {}.type) ?: mutableMapOf()
        financeCategoryColors = gson.fromJson(prefs.getString(KEY_FINANCE_CATEGORY_COLORS, "{}"), object : TypeToken<MutableMap<String, Int>>() {}.type) ?: mutableMapOf()
        financeCurrency = prefs.getString(KEY_FINANCE_CURRENCY, "₹") ?: "₹"
        financeGraphStartMonth = prefs.getInt(KEY_FINANCE_GRAPH_START_MONTH, 0)
        financeGraphColor = prefs.getInt(KEY_FINANCE_GRAPH_COLOR, -1)
        financeGraphSavingsColor = prefs.getInt(KEY_FINANCE_GRAPH_SAVINGS_COLOR, -1)
        isFinanceLedgerEnabled = prefs.getBoolean(KEY_FINANCE_LEDGER_ENABLED, true)

        noteAutoCleanupDays = prefs.getInt(KEY_NOTE_AUTO_CLEANUP, 0)
        noteShowHidden = prefs.getBoolean(KEY_NOTE_SHOW_HIDDEN, false)
        noteVisibleSections = gson.fromJson(prefs.getString(KEY_NOTE_VISIBLE_SECTIONS, "[\"Notes\"]"), object : TypeToken<MutableList<String>>() {}.type) ?: mutableListOf("Notes")
        noteDefaultCategory = prefs.getString(KEY_NOTE_DEFAULT_CAT, "Notes") ?: "Notes"
        noteTemplates = gson.fromJson(prefs.getString(KEY_NOTE_TEMPLATES, "{}"), object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf()

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
        projectRoadmapsEnabled = prefs.getBoolean(KEY_PROJ_ROADMAPS, true)

        isAppLockEnabled = prefs.getBoolean(KEY_APP_LOCK, false)
        appLockPin = prefs.getString(KEY_APP_LOCK_PIN, null)
        appLockQuestion = prefs.getString("app_lock_question", null)
        appLockAnswer = prefs.getString("app_lock_answer", null)
        isOledThemeEnabled = prefs.getBoolean(KEY_OLED_THEME, false)
        isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        recentActivities = gson.fromJson(prefs.getString(KEY_RECENT_ACT, "[]"), object : TypeToken<MutableList<String>>() {}.type) ?: mutableListOf()
        dailyMoods = gson.fromJson(prefs.getString(KEY_DAILY_MOODS, "{}"), object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf()
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
        startupLoadingTime = prefs.getInt(KEY_STARTUP_LOADING_TIME, 2000)
        userXP = prefs.getInt(KEY_USER_XP, 0)
        userLevel = prefs.getInt(KEY_USER_LEVEL, 1)

        showHabitSection = prefs.getBoolean(KEY_SHOW_HABITS, true)
        showWorkoutSection = prefs.getBoolean(KEY_SHOW_WORKOUTS, true)
        showTaskSection = prefs.getBoolean(KEY_SHOW_TASKS, true)
        showNoteSection = prefs.getBoolean(KEY_SHOW_NOTES, true)
        showProjectSection = prefs.getBoolean(KEY_SHOW_PROJECTS, true)
        showFinanceSection = prefs.getBoolean(KEY_SHOW_FINANCE, true)

        userName = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        userBio = prefs.getString(KEY_USER_BIO, "") ?: ""
        userAvatarRes = getResourceId(context, prefs.getString(KEY_USER_AVATAR, "boy_avatar_profile") ?: "boy_avatar_profile")
        userProfileImageUri = prefs.getString(KEY_USER_IMAGE_URI, null)
        userCustomColors = gson.fromJson(prefs.getString(KEY_CUSTOM_COLORS, "[]"), object : TypeToken<MutableList<Int>>() {}.type) ?: mutableListOf()
        projectCustomTags = gson.fromJson(prefs.getString(KEY_PROJ_TAGS, "[\"TASKS\", \"NOTES\", \"FEATURES\", \"BUGS\", \"RESOURCES\"]"), object : TypeToken<MutableList<String>>() {}.type)
        
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

        globalHabitIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_HABIT_ICON, "ic_habit_tracker") ?: "ic_habit_tracker")
        globalWorkoutIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_WORKOUT_ICON, "ic_workout_routine") ?: "ic_workout_routine")
        globalTaskIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_TASK_ICON, "ic_task") ?: "ic_task")
        globalProjectIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_PROJECT_ICON, "ic_project") ?: "ic_project")
        globalNoteIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_NOTE_ICON, "ic_notes") ?: "ic_notes")
        globalFinanceIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_FINANCE_ICON, "ic_finance") ?: "ic_finance")

        projectTemplates = gson.fromJson(prefs.getString(KEY_PROJ_TEMPLATES, "{}"), object : TypeToken<MutableMap<String, List<String>>>() {}.type) ?: mutableMapOf()

        // Sync logic
        checkAndResetDailyStats(context)
        checkAndResetMonthlyFinance(context)
    }

    private fun getResourceId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName).let { if (it == 0) R.drawable.ic_launcher_foreground else it }
    }

    private fun getResourceName(context: Context, id: Int): String {
        return try { context.resources.getResourceEntryName(id) } catch (e: Exception) { "ic_launcher_foreground" }
    }

    private fun checkAndResetDailyStats(context: Context) {
        val today = getTrackingDateString()
        val prefs = getPrefs(context)
        val lastReset = prefs.getString(KEY_LAST_RESET_DATE, "")
        if (lastReset != today) {
            habits.forEach { 
                it.isCompleted = false
                it.progress = 0
            }
            workouts.forEach { it.isCompleted = false }
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply()
            saveData(context)
        }
    }

    private fun checkAndResetMonthlyFinance(context: Context) {
        val calendar = Calendar.getInstance()
        val currentMonthYear = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
        val prefs = getPrefs(context)
        val lastReset = prefs.getString(KEY_LAST_MONTH_RESET, "")
        if (lastReset != currentMonthYear) {
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonthYear).apply()
        }
    }

    fun saveDayNote(date: String, note: String) {
        val current = history[date] ?: DayHistory(0, 0, 0, 0)
        history[date] = current.copy(notes = note)
        notifyDataChanged()
    }

    fun getDayHistory(date: String): DayHistory? {
        return history[date]
    }

    fun getLastSevenDaysDetailedProgress(): List<Pair<Int, Int>> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(cal.time)
            val historyEntry = history[date]
            val progress = historyEntry?.let { 
                if (it.totalHabits == 0) 0 else (it.habitsCompleted * 100) / it.totalHabits 
            } ?: 0
            Pair(i, progress)
        }.reversed()
    }

    fun getHeatmapData(calendar: Calendar, type: String = "HABITS"): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val historyEntry = history[dateKey]
            val progress = when (type) {
                "HABITS" -> if (historyEntry == null || historyEntry.totalHabits == 0) 0 else (historyEntry.habitsCompleted * 100) / historyEntry.totalHabits
                "WORKOUTS" -> {
                    // Try to get from detailed workout data first
                    val todaysWorkouts = workouts.filter {
                        (it.repeatType != "SPECIFIC_DAYS" || (it.repeatDays?.contains(tempCal.get(Calendar.DAY_OF_WEEK) - 1) ?: false)) &&
                        it.timestamp <= tempCal.timeInMillis + 86400000
                    }
                    if (todaysWorkouts.isNotEmpty()) {
                        val totalProgress = todaysWorkouts.sumOf { 
                            it.dailyProgress?.get(dateKey) ?: if (it.completedDates?.contains(dateKey) == true) 100 else 0 
                        }
                        totalProgress / todaysWorkouts.size
                    } else if (historyEntry != null && historyEntry.totalWorkouts > 0) {
                        (historyEntry.workoutsCompleted * 100) / historyEntry.totalWorkouts
                    } else 0
                }
                else -> 0
            }
            result[day - 1] = progress
        }
        return result
    }

    fun getHeatmapData(): Map<String, Int> {
        return history.mapValues { it.value.habitsCompleted }
    }

    fun getHabitPerformanceByFrequency(): Map<String, Float> {
        return emptyMap() // Placeholder
    }

    fun getMoodCorrelationData(): Map<String, Map<String, Int>> {
        return emptyMap() // Placeholder
    }

    fun getGlobalCompletionRate(type: String = "HABITS"): Int {
        if (history.isEmpty()) return 0
        return history.values.map { 
            when (type) {
                "HABITS" -> if (it.totalHabits == 0) 0 else (it.habitsCompleted * 100) / it.totalHabits
                "WORKOUTS" -> if (it.totalWorkouts == 0) 0 else (it.workoutsCompleted * 100) / it.totalWorkouts
                else -> 0
            }
        }.average().toInt()
    }

    fun getHabitStreaks(habitName: String): Pair<Int, Int> {
        return HabitDataManager.getHabitStreaks(habitName)
    }

    fun getCurrentStreak(): Int {
        return HabitDataManager.getHabitStreak()
    }
    
    fun exportData(context: Context): String {
        val prefs = getPrefs(context)
        val allData = prefs.all
        return Gson().toJson(allData)
    }

    fun importData(context: Context, json: String): Boolean {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = Gson().fromJson(json, type)
            val prefs = getPrefs(context)
            val editor = prefs.edit()
            data.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Double -> {
                        if (value == value.toInt().toDouble()) editor.putInt(key, value.toInt())
                        else editor.putFloat(key, value.toFloat())
                    }
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                }
            }
            editor.apply()
            loadData(context)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetAppearanceIcons(context: Context) {
        globalHabitIcon = R.drawable.ic_habit_tracker
        globalWorkoutIcon = R.drawable.ic_workout_routine
        globalTaskIcon = R.drawable.ic_task
        globalProjectIcon = R.drawable.ic_project
        globalNoteIcon = R.drawable.ic_notes
        globalFinanceIcon = R.drawable.ic_finance
        saveData(context)
    }

    fun resetAppearanceColors(context: Context) {
        appAccentColor = -1
        globalHabitColor = -1
        globalWorkoutColor = -1
        globalTaskColor = -1
        globalProjectColor = -1
        globalNoteColor = -1
        globalFinanceColor = -1
        saveData(context)
    }

    fun getVolumeWeightedHeatmap(calendar: Calendar): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        val dailyVolumes = mutableMapOf<Int, Double>()
        var maxVolume = 0.0

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            
            val volume = workouts.filter { 
                it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
            }.sumOf { workout ->
                val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                calculateWorkoutVolume(workout, progress)
            }
            dailyVolumes[day - 1] = volume
            if (volume > maxVolume) maxVolume = volume
        }

        if (maxVolume > 0) {
            dailyVolumes.forEach { (day, vol) ->
                result[day] = ((vol / maxVolume) * 100).toInt()
            }
        }
        return result
    }

    private fun calculateWorkoutVolume(workout: Workout, progressPercent: Int): Double {
        val baseVolume = when (workout.trackingMode) {
            "Sets" -> (workout.target * workout.repsPerSet).toDouble()
            "Reps" -> workout.target.toDouble()
            "Timer" -> workout.target.toDouble() / 60.0 // Normalize timer to minutes
            else -> workout.target.toDouble()
        }
        return (baseVolume * progressPercent) / 100.0
    }

    fun getMuscleDistributionData(): Map<String, Int> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        
        val distribution = mutableMapOf<String, Double>()
        
        workouts.forEach { workout ->
            val workoutVolume = workout.completedDates.filter { dateStr ->
                try {
                    val date = sdf.parse(dateStr)
                    date != null && date.time >= thirtyDaysAgo
                } catch (e: Exception) { false }
            }.size * calculateWorkoutVolume(workout, 100)
            
            workout.muscleGroups.forEach { muscle ->
                distribution[muscle] = (distribution[muscle] ?: 0.0) + workoutVolume
            }
        }
        
        val total = distribution.values.sum()
        if (total == 0.0) return mapOf("Chest" to 0, "Back" to 0, "Legs" to 0, "Shoulders" to 0, "Arms" to 0)
        
        return distribution.mapValues { ((it.value / total) * 100).toInt() }
    }

    fun getMuscleRecoveryStatus(): Map<String, Float> {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val status = mutableMapOf<String, Float>()
        
        val muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Arms")
        muscles.forEach { muscle ->
            var lastTrainedTime = 0L
            workouts.filter { it.muscleGroups.contains(muscle) }.forEach { workout ->
                workout.completedDates.forEach { dateStr ->
                    try {
                        val date = sdf.parse(dateStr)
                        if (date != null && date.time > lastTrainedTime) lastTrainedTime = date.time
                    } catch (e: Exception) {}
                }
            }
            
            if (lastTrainedTime == 0L) {
                status[muscle] = 1.0f
            } else {
                val hoursSince = (now - lastTrainedTime) / (1000 * 60 * 60)
                // Linear recovery over 48 hours
                val recovery = (hoursSince.toFloat() / 48f).coerceIn(0f, 1f)
                status[muscle] = recovery
            }
        }
        return status
    }

    fun getACWRData(): Pair<List<Float>, List<Float>> {
        val now = Calendar.getInstance()
        val acuteWorkload = mutableListOf<Float>()
        val chronicWorkload = mutableListOf<Float>()
        
        for (i in 0 until 14) {
            val date = now.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            
            val acute = calculateRollingWorkload(date, 7)
            val chronic = calculateRollingWorkload(date, 28)
            
            acuteWorkload.add(acute.toFloat())
            chronicWorkload.add(chronic.toFloat())
        }
        
        return Pair(acuteWorkload.reversed(), chronicWorkload.reversed())
    }

    private fun calculateRollingWorkload(endDate: Calendar, days: Int): Double {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var totalVolume = 0.0
        
        for (i in 0 until days) {
            val date = endDate.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = sdf.format(date.time)
            
            totalVolume += workouts.sumOf { workout ->
                val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                calculateWorkoutVolume(workout, progress)
            }
        }
        return totalVolume / days
    }

    fun getTrainingStabilityScore(): Float {
        if (workouts.isEmpty()) return 1.0f
        val consistency = workouts.map { 
            if (it.repeatType == "EVERY_DAY") 1.0f else 0.7f 
        }.average().toFloat()
        return consistency
    }

    fun getMonthlyVolumeData(calendar: Calendar): List<Float> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        return (1..daysInMonth).map { day ->
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            workouts.sumOf { workout ->
                val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                calculateWorkoutVolume(workout, progress)
            }.toFloat()
        }
    }

    fun getWorkoutDiversityData(): Map<String, Int> {
        if (workouts.isEmpty()) return emptyMap()
        return workouts.groupBy { it.trackingMode }.mapValues { it.value.size }
    }

    fun getIntensityDistribution(calendar: Calendar): List<Int> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        return (1..daysInMonth).map { day ->
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val dayWorkouts = workouts.filter { 
                it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
            }
            if (dayWorkouts.isEmpty()) 0 
            else (dayWorkouts.sumOf { it.dailyProgress[dateKey] ?: 100 } / dayWorkouts.size)
        }
    }

    fun getDailyMuscleFocus(calendar: Calendar): Map<Int, List<String>> {
        val result = mutableMapOf<Int, List<String>>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val trainedMuscles = workouts.filter { 
                it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
            }.flatMap { it.muscleGroups }.distinct()
            if (trainedMuscles.isNotEmpty()) {
                result[day - 1] = trainedMuscles
            }
        }
        return result
    }

    fun getTemporalDensityData(): Map<Int, Map<String, Int>> = HabitDataManager.getTemporalDensityData()
    fun getHabitCorrelationMatrix(): List<Triple<String, String, Double>> = HabitDataManager.getHabitCorrelationMatrix()
    fun getStabilityIndex(habitName: String? = null): Float = HabitDataManager.getStabilityIndex(habitName)
    fun getWeeklyCyclicalData(habitName: String? = null): Map<Int, Float> = HabitDataManager.getWeeklyCyclicalData(habitName)
    fun getResilienceScore(habitName: String? = null): Float = HabitDataManager.getResilienceScore(habitName)
    fun getMonthlyMomentumHistory(habitName: String? = null): List<Pair<String, Int>> = HabitDataManager.getMonthlyMomentumHistory(habitName)
    fun getStreakMilestoneProgress(habitName: String? = null): Triple<Int, Int, Float> = HabitDataManager.getStreakMilestoneProgress(habitName)
    val predefinedJourneys = listOf(
        Journey(
            title = "Morning Mastery",
            description = "Transform your mornings to transform your life.",
            category = "HABITS",
            bannerRes = R.drawable.ic_launcher_foreground,
            keyResults = listOf(JourneyResult("Better Focus", "🎯")),
            expectations = listOf("Consistency"),
            phases = listOf(JourneyPhase("1-7", "Phase 1", "Start small")),
            habitsToCreate = listOf("Wake up at 6 AM", "Drink Water")
        ),
        Journey(
            title = "Strength Builder",
            description = "Build a solid foundation of strength.",
            category = "WORKOUTS",
            bannerRes = R.drawable.ic_launcher_foreground,
            keyResults = listOf(JourneyResult("More Power", "💪")),
            expectations = listOf("Effort"),
            phases = listOf(JourneyPhase("1-14", "Intro", "Basic moves")),
            workoutsToCreate = listOf("Pushups", "Squats")
        )
    )

    fun updateWorkspaceAgenda(context: Context) {}
}
