package com.example.allinone

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.allinone.workspace.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel : ViewModel() {
    var dashboardState by mutableStateOf(DashboardState())
        private set

    fun refreshState(context: Context, effectiveMood: String? = null) {
        viewModelScope.launch {
            // Wait for data to be loaded if it's not already
            if (!DataManager.isDataLoaded.value) {
                DataManager.isDataLoaded.first { it }
            }

            val nextMilestone = synchronized(DataManager.projects) {
                DataManager.projects
                    .filter { it.category == "Project" }
                    .flatMap { it.subFeatures }
                    .filter { !it.isCompleted && it.dueDate != null }
                    .minByOrNull { it.dueDate ?: Long.MAX_VALUE }
                    ?.let { feature ->
                        feature.dueDate?.let { dueDate ->
                            "${feature.name} due ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(dueDate))}"
                        }
                    }
                    ?: "No upcoming milestones"
            }

            val agenda = DataManager.getComprehensiveTodayAgenda(context)
            val currentSpent = DataManager.getCurrentMonthExpenditure()
            val db = AppDatabase.getDatabase(context)
            val hasProjects = synchronized(DataManager.projects) {
                DataManager.projects.isNotEmpty()
            } || db.workspaceDao().getAllProjectsSync().isNotEmpty()

            dashboardState = DashboardState(
                userName = DataManager.userName,
                overallProgress = DataManager.getTotalDailyProgress(),
                habitProgress = DataManager.getHabitProgress(),
                workoutProgress = DataManager.getWorkoutProgress(),
                dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                safeSpendAmount = DataManager.monthlyBudget - currentSpent,
                nextMilestone = nextMilestone,
                recentActions = DataManager.recentActivities.toList(),
                growthAdvice = DataManager.getGrowthAdvice(effectiveMood),
                managementAdvice = DataManager.getManagementAdvice(effectiveMood),
                todayAgenda = agenda,
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
                isDynamicColorEnabled = DataManager.isDynamicColorEnabled,
                globalDisplaySize = DataManager.displaySize,
                fontSize = DataManager.fontSize,
                isSystemAppearanceEnabled = DataManager.isSystemAppearanceEnabled,
                showHabitSection = DataManager.showHabitSection,
                showWorkoutSection = DataManager.showWorkoutSection,
                showTaskSection = DataManager.showTaskSection,
                showNoteSection = DataManager.showNoteSection,
                showProjectSection = DataManager.showProjectSection,
                showFinanceSection = DataManager.showFinanceSection,
                showPerformanceSection = DataManager.showPerformanceSection,
                hasProjects = hasProjects,
                isAppUnlocked = DataManager.isAppUnlocked,
                isDataLoaded = true
            )
        }
    }

    fun updateSectionColor(context: Context, section: String, color: Int) {
        when (section) {
            "HABITS" -> DataManager.globalHabitColor = color
            "WORKOUT" -> DataManager.globalWorkoutColor = color
            "FINANCE" -> DataManager.globalFinanceColor = color
            "PROJECTS" -> DataManager.globalProjectColor = color
            "TODO" -> DataManager.globalTaskColor = color
            "NOTES" -> DataManager.globalNoteColor = color
        }
        refreshState(context, dashboardState.currentMood)
    }
}
