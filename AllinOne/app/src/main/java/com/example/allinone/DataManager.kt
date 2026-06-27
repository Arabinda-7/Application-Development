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
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
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
    var taskVisibleSections = mutableListOf("Tasks", "List")

    // Finance Settings
    var financeCustomCategories = mutableListOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other")
    var financeCurrency: String = "₹"

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
    var noteVisibleSections = mutableListOf("Notes", "Questions", "Daily", "Stories")
    
    // Project Advanced Settings
    var projectAutoArchive: Boolean = false
    var projectSynergySync: Boolean = false
    var projectDeadlineAlerts: Boolean = true
    var projectAnalyticsEnabled: Boolean = false
    var isAppLockEnabled: Boolean = false
    var isOledThemeEnabled: Boolean = false

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
    var globalTaskIcon: Int = R.drawable.ic_todo_list
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
    private const val KEY_BUDGET = "monthly_budget"
    private const val KEY_SAVINGS_GOAL = "monthly_savings_goal"
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
    private const val KEY_TASK_DEFAULT_SECTION = "task_default_section"
    private const val KEY_TASK_VISIBLE_SECTIONS = "task_visible_sections"
    private const val KEY_FINANCE_CUSTOM_CATEGORIES = "finance_custom_categories"
    private const val KEY_FINANCE_CURRENCY = "finance_currency"
    private const val KEY_NOTE_AUTO_CLEANUP = "note_auto_cleanup"
    private const val KEY_NOTE_SHOW_HIDDEN = "note_show_hidden"
    private const val KEY_NOTE_VISIBLE_SECTIONS = "note_visible_sections"
    private const val KEY_NOTE_DEFAULT_CAT = "note_default_cat"
    private const val KEY_NOTE_TEMPLATES = "note_templates"
    private const val KEY_PROJ_ARCHIVE = "project_auto_archive"
    private const val KEY_PROJ_SYNC = "project_synergy_sync"
    private const val KEY_PROJ_ALERTS = "project_deadline_alerts"
    private const val KEY_PROJ_ANALYTICS = "project_analytics_enabled"
    private const val KEY_APP_LOCK = "app_lock_enabled"
    private const val KEY_OLED_THEME = "oled_theme_enabled"

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
            putString(KEY_TASK_DEFAULT_SECTION, taskDefaultSection)
            putString(KEY_TASK_VISIBLE_SECTIONS, gson.toJson(taskVisibleSections))
            putString(KEY_FINANCE_CUSTOM_CATEGORIES, gson.toJson(financeCustomCategories))
            putString(KEY_FINANCE_CURRENCY, financeCurrency)
            putInt(KEY_NOTE_AUTO_CLEANUP, noteAutoCleanupDays)
            putBoolean(KEY_NOTE_SHOW_HIDDEN, noteShowHidden)
            putString(KEY_NOTE_VISIBLE_SECTIONS, gson.toJson(noteVisibleSections))
            putString(KEY_NOTE_DEFAULT_CAT, noteDefaultCategory)
            putString(KEY_NOTE_TEMPLATES, gson.toJson(noteTemplates))
            putBoolean(KEY_PROJ_ARCHIVE, projectAutoArchive)
            putBoolean(KEY_PROJ_SYNC, projectSynergySync)
            putBoolean(KEY_PROJ_ALERTS, projectDeadlineAlerts)
            putBoolean(KEY_PROJ_ANALYTICS, projectAnalyticsEnabled)
            putBoolean(KEY_APP_LOCK, isAppLockEnabled)
            putBoolean(KEY_OLED_THEME, isOledThemeEnabled)

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

            putInt(KEY_GLOBAL_HABIT_ICON, globalHabitIcon)
            putInt(KEY_GLOBAL_WORKOUT_ICON, globalWorkoutIcon)
            putInt(KEY_GLOBAL_TASK_ICON, globalTaskIcon)
            putInt(KEY_GLOBAL_PROJECT_ICON, globalProjectIcon)
            putInt(KEY_GLOBAL_NOTE_ICON, globalNoteIcon)
            putInt(KEY_GLOBAL_FINANCE_ICON, globalFinanceIcon)

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

        prefs.getString(KEY_HABITS, null)?.let {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            habits = gson.fromJson(it, type) ?: mutableListOf()
            habits.forEach { habit ->
                habit.isExpanded = false
                if (habit.completedDates == null) habit.completedDates = mutableListOf()
            }
        }

        prefs.getString(KEY_WORKOUTS, null)?.let {
            val type = object : TypeToken<MutableList<Workout>>() {}.type
            workouts = gson.fromJson(it, type) ?: mutableListOf()
            workouts.forEach { workout ->
                workout.isExpanded = false
                if (workout.completedDates == null) workout.completedDates = mutableListOf()
            }
        }

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

        prefs.getString(KEY_TRANSACTIONS, null)?.let {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            transactions = gson.fromJson(it, type) ?: mutableListOf()
        }

        prefs.getString(KEY_LEDGER, null)?.let {
            val type = object : TypeToken<MutableList<LedgerEntry>>() {}.type
            ledgerEntries = gson.fromJson(it, type) ?: mutableListOf()
        }

        monthlyBudget = prefs.getFloat(KEY_BUDGET, 0.0f).toDouble()
        monthlySavingsGoal = prefs.getFloat(KEY_SAVINGS_GOAL, 0.0f).toDouble()

        prefs.getString(KEY_MONTHLY_BUDGETS, null)?.let {
            val type = object : TypeToken<MutableMap<String, Double>>() {}.type
            monthlyBudgets = gson.fromJson(it, type) ?: mutableMapOf()
        }

        prefs.getString(KEY_MONTHLY_SAVINGS_GOALS, null)?.let {
            val type = object : TypeToken<MutableMap<String, Double>>() {}.type
            monthlySavingsGoals = gson.fromJson(it, type) ?: mutableMapOf()
        }

        prefs.getString(KEY_HISTORY, null)?.let {
            val type = object : TypeToken<MutableMap<String, DayHistory>>() {}.type
            history = gson.fromJson(it, type) ?: mutableMapOf()
        }

        taskShowCompleted = prefs.getBoolean(KEY_TASK_SHOW_COMPLETED, true)
        taskShowHidden = prefs.getBoolean(KEY_TASK_SHOW_HIDDEN, false)
        taskSortOrder = prefs.getString(KEY_TASK_SORT_ORDER, "Priority") ?: "Priority"
        prefs.getString(KEY_TASK_CUSTOM_CATEGORIES, null)?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            taskCustomCategories = gson.fromJson(it, type) ?: mutableListOf("General", "Personal", "Work", "Shopping")
        }
        taskAutoArchive = prefs.getBoolean(KEY_TASK_AUTO_ARCHIVE, false)
        taskDefaultSection = prefs.getString(KEY_TASK_DEFAULT_SECTION, "Tasks") ?: "Tasks"
        prefs.getString(KEY_TASK_VISIBLE_SECTIONS, null)?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            taskVisibleSections = gson.fromJson(it, type) ?: mutableListOf("Tasks", "List")
        }

        prefs.getString(KEY_FINANCE_CUSTOM_CATEGORIES, null)?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            financeCustomCategories = gson.fromJson(it, type) ?: mutableListOf("Food", "Transport", "Rent", "Shopping", "Entertainment")
        }
        financeCurrency = prefs.getString(KEY_FINANCE_CURRENCY, "₹") ?: "₹"
        noteAutoCleanupDays = prefs.getInt(KEY_NOTE_AUTO_CLEANUP, 0)
        noteShowHidden = prefs.getBoolean(KEY_NOTE_SHOW_HIDDEN, false)
        prefs.getString(KEY_NOTE_VISIBLE_SECTIONS, null)?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            noteVisibleSections = gson.fromJson(it, type) ?: mutableListOf("Notes", "Questions", "Daily", "Stories")
        }
        noteDefaultCategory = prefs.getString(KEY_NOTE_DEFAULT_CAT, "Notes") ?: "Notes"
        prefs.getString(KEY_NOTE_TEMPLATES, null)?.let {
            val type = object : TypeToken<MutableMap<String, String>>() {}.type
            noteTemplates = gson.fromJson(it, type) ?: noteTemplates
        }
        projectAutoArchive = prefs.getBoolean(KEY_PROJ_ARCHIVE, false)
        projectSynergySync = prefs.getBoolean(KEY_PROJ_SYNC, false)
        projectDeadlineAlerts = prefs.getBoolean(KEY_PROJ_ALERTS, true)
        projectAnalyticsEnabled = prefs.getBoolean(KEY_PROJ_ANALYTICS, false)
        isAppLockEnabled = prefs.getBoolean(KEY_APP_LOCK, false)
        isOledThemeEnabled = prefs.getBoolean(KEY_OLED_THEME, false)

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

        globalHabitIcon = prefs.getInt(KEY_GLOBAL_HABIT_ICON, R.drawable.ic_habit_tracker)
        globalWorkoutIcon = prefs.getInt(KEY_GLOBAL_WORKOUT_ICON, R.drawable.ic_workout_routine)
        globalTaskIcon = prefs.getInt(KEY_GLOBAL_TASK_ICON, R.drawable.ic_todo_list)
        globalProjectIcon = prefs.getInt(KEY_GLOBAL_PROJECT_ICON, R.drawable.ic_project)
        globalNoteIcon = prefs.getInt(KEY_GLOBAL_NOTE_ICON, R.drawable.ic_notes)
        globalFinanceIcon = prefs.getInt(KEY_GLOBAL_FINANCE_ICON, R.drawable.ic_finance)

        prefs.getString(KEY_PROJ_TEMPLATES, null)?.let {
            val type = object : TypeToken<MutableMap<String, List<String>>>() {}.type
            projectTemplates = gson.fromJson(it, type) ?: projectTemplates
        }

        habitDefaultTab = prefs.getString(KEY_HABIT_DEFAULT_TAB, "TODAY") ?: "TODAY"
        habitVacationMode = prefs.getBoolean(KEY_HABIT_VACATION_MODE, false)
        habitSortOrder = prefs.getString(KEY_HABIT_SORT_ORDER, "Time") ?: "Time"
        habitCompletionSound = prefs.getBoolean(KEY_HABIT_SOUND, true)
        habitCompletionHaptics = prefs.getBoolean(KEY_HABIT_HAPTICS, true)
        habitDayResetHour = prefs.getInt(KEY_HABIT_RESET_HOUR, 0)
        habitBulkMode = prefs.getBoolean(KEY_HABIT_BULK_MODE, false)
        habitGraceDaysAllowed = prefs.getInt(KEY_HABIT_GRACE_DAYS, 1)

        prefs.getString(KEY_WORKOUT_MUSCLE_GROUPS, null)?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            workoutMuscleGroups = gson.fromJson(it, type) ?: workoutMuscleGroups
        }
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
            val prevHabitsCompleted = habits.count { it.isCompleted }
            val prevWorkoutsCompleted = workouts.count { it.isCompleted }
            
            history[lastResetDate] = DayHistory(
                prevHabitsCompleted,
                habits.size,
                prevWorkoutsCompleted,
                workouts.size
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
            
            monthlyBudget = 0.0
            monthlySavingsGoal = 0.0
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
        val allData = AllAppData(habits, workouts, tasks, notes, history, transactions, monthlyBudget, monthlySavingsGoal)
        return Gson().toJson(allData)
    }

    fun importData(context: Context, json: String): Boolean {
        return try {
            val allData = Gson().fromJson(json, AllAppData::class.java)
            habits = allData.habits.toMutableList()
            workouts = allData.workouts.toMutableList()
            workouts.forEach { workout ->
                @Suppress("SENSELESS_COMPARISON")
                if (workout.muscleGroups == null) {
                    workout.muscleGroups = listOf("General")
                }
            }
            tasks = allData.tasks.toMutableList()
            notes = allData.notes.toMutableList()
            transactions = allData.transactions.toMutableList()
            monthlyBudget = allData.monthlyBudget
            monthlySavingsGoal = allData.monthlySavingsGoal
            history = allData.history.toMutableMap()
            
            habits.forEach { 
                it.isExpanded = false
                if (it.completedDates == null) it.completedDates = mutableListOf() 
            }
            workouts.forEach { 
                it.isExpanded = false
                if (it.completedDates == null) it.completedDates = mutableListOf() 
            }

            saveData(context)
            true
        } catch (e: Exception) {
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

    fun getTrackingDateString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(getTrackingCalendar().time)
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
        @Suppress("SENSELESS_COMPARISON")
        if (note.title == null) note.title = "Untitled Project"
        @Suppress("SENSELESS_COMPARISON")
        if (note.content == null) note.content = ""
        @Suppress("SENSELESS_COMPARISON")
        if (note.status == null) note.status = "Not Started"
        @Suppress("SENSELESS_COMPARISON")
        if (note.category == null) note.category = "Project"

        @Suppress("SENSELESS_COMPARISON")
        if (note.subFeatures == null) {
            try {
                val field = note::class.java.getDeclaredField("subFeatures")
                field.isAccessible = true
                field.set(note, mutableListOf<ProjectFeature>())
            } catch (e: Exception) {}
        }
        
        note.subFeatures?.let { sanitizeProjectFeatures(it) }

        @Suppress("SENSELESS_COMPARISON")
        if (note.changeHistory == null) {
            try {
                val field = note::class.java.getDeclaredField("changeHistory")
                field.isAccessible = true
                field.set(note, mutableListOf<ProjectHistory>())
            } catch (e: Exception) {}
        }
    }

    private fun sanitizeProjectFeatures(features: MutableList<ProjectFeature>) {
        if (features == null) return // Extreme safety
        
        features.forEach { feature ->
            @Suppress("SENSELESS_COMPARISON")
            if (feature == null) return@forEach

            @Suppress("SENSELESS_COMPARISON")
            if (feature.name == null) feature.name = "New Node"
            @Suppress("SENSELESS_COMPARISON")
            if (feature.details == null) feature.details = ""
            @Suppress("SENSELESS_COMPARISON")
            if (feature.resourceUrl == null) feature.resourceUrl = ""
            @Suppress("SENSELESS_COMPARISON")
            if (feature.resourcePath == null) feature.resourcePath = ""
            @Suppress("SENSELESS_COMPARISON")
            if (feature.blockedByNodeId == null) feature.blockedByNodeId = ""

            @Suppress("SENSELESS_COMPARISON")
            if (feature.subFeatures == null) {
                try {
                    val field = feature::class.java.getDeclaredField("subFeatures")
                    field.isAccessible = true
                    field.set(feature, mutableListOf<ProjectFeature>())
                } catch (e: Exception) {}
            }
            
            @Suppress("SENSELESS_COMPARISON")
            if (feature.id == null) {
                try {
                    val field = feature::class.java.getDeclaredField("id")
                    field.isAccessible = true
                    field.set(feature, java.util.UUID.randomUUID().toString())
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
        globalTaskIcon = R.drawable.ic_todo_list
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
}
