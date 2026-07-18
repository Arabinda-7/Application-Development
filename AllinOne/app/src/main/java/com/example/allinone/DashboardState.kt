package com.example.allinone

data class DashboardState(
    val userName: String = "User",
    val overallProgress: Int = 0,
    val habitProgress: Int = 0,
    val workoutProgress: Int = 0,
    val dateString: String = "",
    val safeSpendAmount: Double = 0.0,
    val nextMilestone: String = "No upcoming milestones",
    val recentActions: List<String> = emptyList(),
    val growthAdvice: String = "Build consistency with daily rituals.",
    val managementAdvice: String = "Organize your workflow for peak efficiency.",
    val currentMood: String? = null,

    val habitColor: Int = -1,
    val workoutColor: Int = -1,
    val taskColor: Int = -1,
    val noteColor: Int = -1,
    val projectColor: Int = -1,
    val financeColor: Int = -1,
    val habitIcon: Int = R.drawable.ic_habit_tracker,
    val workoutIcon: Int = R.drawable.ic_workout_routine,
    val taskIcon: Int = R.drawable.ic_task,
    val noteIcon: Int = R.drawable.ic_notes,
    val projectIcon: Int = R.drawable.ic_project,
    val financeIcon: Int = R.drawable.ic_finance,
    val userAvatarRes: Int = R.drawable.icons8_profile_100,
    val userProfileImageUri: String? = null,
    val homeDisplaySize: String = "S",
    val globalDisplaySize: String = "S",
    val fontSize: String = "S",

    val showHabitSection: Boolean = true,
    val showWorkoutSection: Boolean = true,
    val showTaskSection: Boolean = true,
    val showNoteSection: Boolean = true,
    val showProjectSection: Boolean = true,
    val showFinanceSection: Boolean = true,

    val isDataLoaded: Boolean = false
)
