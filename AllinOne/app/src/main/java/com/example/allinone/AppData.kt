package com.example.allinone

data class DayHistory(
    val habitsCompleted: Int,
    val totalHabits: Int,
    val workoutsCompleted: Int,
    val totalWorkouts: Int
)

data class AllAppData(
    val habits: List<Habit>,
    val workouts: List<Workout>,
    val tasks: List<Task>,
    val notes: List<Note>,
    val history: Map<String, DayHistory>,
    val projects: List<Project> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val monthlyBudget: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0
)
