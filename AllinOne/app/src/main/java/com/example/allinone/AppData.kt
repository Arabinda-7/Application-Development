package com.example.allinone

import com.example.allinone.workspace.data.*

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
    val projects: List<Note>? = null,
    val history: Map<String, DayHistory>? = null,
    val transactions: List<Transaction>? = null,
    val ledgerEntries: List<LedgerEntry>? = null,
    val personalLedgers: List<PersonalLedger>? = null,
    val monthlyBudget: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val monthlyBudgets: Map<String, Double>? = null,
    val monthlySavingsGoals: Map<String, Double>? = null,
    val dailyMoods: Map<String, String>? = null,

    // Workspace Data
    val workspaceProjects: List<ProjectEntity>? = null,
    val workspaceGoals: List<GoalEntity>? = null,
    val workspaceTasks: List<TaskEntity>? = null,
    val workspaceFeatures: List<FeatureEntity>? = null,
    val workspaceBugs: List<BugEntity>? = null,
    val workspaceIdeas: List<IdeaEntity>? = null,
    val workspaceNotes: List<NoteEntity>? = null,
    val workspaceResources: List<ResourceEntity>? = null,
    val workspaceLogs: List<ActivityLogEntity>? = null,
    val workspaceRefs: List<NoteCrossReferenceEntity>? = null
)
