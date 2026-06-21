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
    var projects = mutableListOf<Project>()
    var transactions = mutableListOf<Transaction>()
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
    var history = mutableMapOf<String, DayHistory>()
    var monthlyBudgets = mutableMapOf<String, Double>()
    var monthlySavingsGoals = mutableMapOf<String, Double>()

    private const val PREFS_NAME = "all_in_one_prefs"
    private const val KEY_HABITS = "habits_data"
    private const val KEY_WORKOUTS = "workouts_data"
    private const val KEY_TASKS = "tasks_data"
    private const val KEY_NOTES = "notes_data"
    private const val KEY_PROJECTS = "projects_data"
    private const val KEY_TRANSACTIONS = "transactions_data"
    private const val KEY_BUDGET = "monthly_budget"
    private const val KEY_SAVINGS_GOAL = "monthly_savings_goal"
    private const val KEY_MONTHLY_BUDGETS = "monthly_budgets_data"
    private const val KEY_MONTHLY_SAVINGS_GOALS = "monthly_savings_goals_data"
    private const val KEY_HISTORY = "history_data"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"
    private const val KEY_LAST_MONTH_RESET = "last_month_reset"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveData(context: Context) {
        val prefs = getPrefs(context)
        val gson = Gson()
        
        prefs.edit().apply {
            putString(KEY_HABITS, gson.toJson(habits))
            putString(KEY_WORKOUTS, gson.toJson(workouts))
            putString(KEY_TASKS, gson.toJson(tasks))
            putString(KEY_NOTES, gson.toJson(notes))
            putString(KEY_PROJECTS, gson.toJson(projects))
            putString(KEY_TRANSACTIONS, gson.toJson(transactions))
            putFloat(KEY_BUDGET, monthlyBudget.toFloat())
            putFloat(KEY_SAVINGS_GOAL, monthlySavingsGoal.toFloat())
            putString(KEY_MONTHLY_BUDGETS, gson.toJson(monthlyBudgets))
            putString(KEY_MONTHLY_SAVINGS_GOALS, gson.toJson(monthlySavingsGoals))
            putString(KEY_HISTORY, gson.toJson(history))
            apply()
        }
    }

    fun loadData(context: Context) {
        val prefs = getPrefs(context)
        val gson = Gson()

        // ... existing loads ...
        prefs.getString(KEY_HABITS, null)?.let {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            habits = gson.fromJson(it, type) ?: mutableListOf()
            habits.forEach { habit ->
                habit.isExpanded = false // Reset expansion state on load
                @Suppress("UNNECESSARY_SAFE_CALL")
                if (habit.completedDates == null) {
                    habit.completedDates = mutableListOf()
                }
            }
        }

        prefs.getString(KEY_WORKOUTS, null)?.let {
            val type = object : TypeToken<MutableList<Workout>>() {}.type
            workouts = gson.fromJson(it, type) ?: mutableListOf()
            workouts.forEach { workout ->
                workout.isExpanded = false // Reset expansion state on load
                @Suppress("UNNECESSARY_SAFE_CALL")
                if (workout.completedDates == null) {
                    workout.completedDates = mutableListOf()
                }
            }
        }

        prefs.getString(KEY_TASKS, null)?.let {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            tasks = gson.fromJson(it, type) ?: mutableListOf()
        }

        prefs.getString(KEY_NOTES, null)?.let {
            val type = object : TypeToken<MutableList<Note>>() {}.type
            notes = gson.fromJson(it, type) ?: mutableListOf()
        }

        prefs.getString(KEY_PROJECTS, null)?.let {
            val type = object : TypeToken<MutableList<Project>>() {}.type
            projects = gson.fromJson(it, type) ?: mutableListOf()
        }

        prefs.getString(KEY_TRANSACTIONS, null)?.let {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            transactions = gson.fromJson(it, type) ?: mutableListOf()
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

        checkAndResetDailyProgress(context)
        checkAndResetMonthlyFinance(context)
    }

    private fun checkAndResetDailyProgress(context: Context) {
        val prefs = getPrefs(context)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = sdf.format(Date())
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
            // Save current budget and goal to history before resetting
            monthlyBudgets[lastResetMonth] = monthlyBudget
            monthlySavingsGoals[lastResetMonth] = monthlySavingsGoal
            
            monthlyBudget = 0.0
            monthlySavingsGoal = 0.0
            saveData(context)
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonth).apply()
        } else if (lastResetMonth.isEmpty()) {
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonth).apply()
        }
        
        // Ensure current month budget is tracked if not already
        if (!monthlyBudgets.containsKey(currentMonth)) {
            monthlyBudgets[currentMonth] = monthlyBudget
        }
        if (!monthlySavingsGoals.containsKey(currentMonth)) {
            monthlySavingsGoals[currentMonth] = monthlySavingsGoal
        }
    }

    fun exportData(): String {
        val allData = AllAppData(habits, workouts, tasks, notes, history, projects, transactions, monthlyBudget, monthlySavingsGoal)
        return Gson().toJson(allData)
    }

    fun importData(context: Context, json: String): Boolean {
        return try {
            val allData = Gson().fromJson(json, AllAppData::class.java)
            habits = allData.habits.toMutableList()
            workouts = allData.workouts.toMutableList()
            tasks = allData.tasks.toMutableList()
            notes = allData.notes.toMutableList()
            projects = allData.projects.toMutableList()
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
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) // 0=Sun
        val todaysHabits = habits.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        if (todaysHabits.isEmpty()) return 0
        return (todaysHabits.count { it.isCompleted } * 100) / todaysHabits.size
    }

    fun getWorkoutProgress(): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysWorkouts = workouts.filter { 
            it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) 
        }
        if (todaysWorkouts.isEmpty()) return 0
        return (todaysWorkouts.count { it.isCompleted } * 100) / todaysWorkouts.size
    }

    fun getTotalDailyProgress(): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
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
        val calendar = Calendar.getInstance()
        var streak = 0
        
        if (getTotalDailyProgress() >= 100) streak = 1
        
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        while (true) {
            val dateStr = sdf.format(calendar.time)
            val dayData = history[dateStr]
            if (dayData != null && (dayData.habitsCompleted + dayData.workoutsCompleted) > 0 && 
                (dayData.habitsCompleted + dayData.workoutsCompleted) >= (dayData.totalHabits + dayData.totalWorkouts)) {
                streak++
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
}
