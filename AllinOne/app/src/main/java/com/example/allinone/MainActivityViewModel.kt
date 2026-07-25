package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel : ViewModel() {
    var dashboardState by mutableStateOf(DashboardState())
        private set

    fun refreshState(effectiveMood: String? = null) {
        val nextMilestone = DataManager.projects
            .filter { it.category == "Project" }
            .flatMap { it.subFeatures }
            .filter { !it.isCompleted && it.dueDate != null }
            .minByOrNull { it.dueDate!! }
            ?.let { "${it.name} due ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.dueDate!!))}" }
            ?: "No upcoming milestones"

        dashboardState = DashboardState(
            userName = DataManager.userName,
            overallProgress = DataManager.getTotalDailyProgress(),
            habitProgress = DataManager.getHabitProgress(),
            workoutProgress = DataManager.getWorkoutProgress(),
            dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
            safeSpendAmount = DataManager.monthlyBudget - DataManager.getCurrentMonthExpenditure(),
            nextMilestone = nextMilestone,
            recentActions = DataManager.recentActivities.toList(),
            growthAdvice = DataManager.getGrowthAdvice(effectiveMood),
            managementAdvice = DataManager.getManagementAdvice(effectiveMood),
            todayAgenda = DataManager.getTodayAgendaNotifications(),
            currentMood = effectiveMood,
            habitColor = DataManager.globalHabitColor,
            workoutColor = DataManager.globalWorkoutColor,
            taskColor = DataManager.globalTaskColor,
            noteColor = DataManager.globalNoteColor,
            projectColor = DataManager.globalProjectColor,
            financeColor = DataManager.globalFinanceColor,
            habitIcon = DataManager.globalHabitIcon,
            workoutIcon = DataManager.globalWorkoutIcon,
            taskIcon = DataManager.globalTaskIcon,
            noteIcon = DataManager.globalNoteIcon,
            projectIcon = DataManager.globalProjectIcon,
            financeIcon = DataManager.globalFinanceIcon,
            userAvatarRes = DataManager.userAvatarRes,
            userProfileImageUri = DataManager.userProfileImageUri,
            homeFocusSize = DataManager.homeFocusSize,
            homeDisplaySize = DataManager.homeDisplaySize,
            appThemeMode = DataManager.appThemeMode,
            appAccentColor = DataManager.appAccentColor,
            appFontFamily = DataManager.appFontFamily,
            appBorderRadius = DataManager.appBorderRadius,
            appCardStyle = DataManager.appCardStyle,
            appShowShadows = DataManager.appShowShadows,
            globalDisplaySize = DataManager.displaySize,
            fontSize = DataManager.fontSize,
            isSystemAppearanceEnabled = DataManager.isSystemAppearanceEnabled,
            showHabitSection = DataManager.showHabitSection,
            showWorkoutSection = DataManager.showWorkoutSection,
            showTaskSection = DataManager.showTaskSection,
            showNoteSection = DataManager.showNoteSection,
            showProjectSection = DataManager.showProjectSection,
            showFinanceSection = DataManager.showFinanceSection,
            isAppUnlocked = DataManager.isAppUnlocked,
            isDataLoaded = true
        )
    }

    fun updateSectionColor(section: String, color: Int) {
        when (section) {
            "HABITS" -> DataManager.globalHabitColor = color
            "WORKOUT" -> DataManager.globalWorkoutColor = color
            "FINANCE" -> DataManager.globalFinanceColor = color
            "PROJECTS" -> DataManager.globalProjectColor = color
            "TODO" -> DataManager.globalTaskColor = color
            "NOTES" -> DataManager.globalNoteColor = color
        }
        refreshState(dashboardState.currentMood)
    }
}
