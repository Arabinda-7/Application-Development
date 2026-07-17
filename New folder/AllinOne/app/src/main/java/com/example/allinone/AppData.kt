package com.example.allinone

data class DayHistory(
    val habitsCompleted: Int,
    val totalHabits: Int,
    val workoutsCompleted: Int,
    val totalWorkouts: Int
)

data class AllAppData(
    val habits: List<Habit>? = null,
    val workouts: List<Workout>? = null,
    val tasks: List<Task>? = null,
    val notes: List<Note>? = null,
    val history: Map<String, DayHistory>? = null,
    val transactions: List<Transaction>? = null,
    val ledgerEntries: List<LedgerEntry>? = null,
    val personalLedgers: List<PersonalLedger>? = null,
    val monthlyBudget: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val monthlyBudgets: Map<String, Double>? = null,
    val monthlySavingsGoals: Map<String, Double>? = null,
    val dailyMoods: Map<String, String>? = null
)
