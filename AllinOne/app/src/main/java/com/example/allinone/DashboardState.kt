package com.example.allinone

data class DashboardState(
    val habitProgress: Int = 0,
    val workoutProgress: Int = 0,
    val dateString: String = "",
    val habitColor: Int = -1,
    val workoutColor: Int = -1,
    val taskColor: Int = -1,
    val noteColor: Int = -1,
    val projectColor: Int = -1,
    val financeColor: Int = -1,
    val habitIcon: Int = R.drawable.ic_habit_tracker,
    val workoutIcon: Int = R.drawable.ic_workout_routine,
    val taskIcon: Int = R.drawable.ic_todo_list,
    val noteIcon: Int = R.drawable.ic_notes,
    val projectIcon: Int = R.drawable.ic_project,
    val financeIcon: Int = R.drawable.ic_finance
)
