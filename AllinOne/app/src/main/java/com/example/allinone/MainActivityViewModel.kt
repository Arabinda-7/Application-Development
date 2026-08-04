package com.example.allinone

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.database.AppDatabase
import com.example.allinone.domain.repository.*
import com.example.allinone.domain.usecase.user.GetUserProfileUseCase
import com.example.allinone.domain.usecase.user.GetUserSettingsUseCase
import com.example.allinone.domain.usecase.user.UpdateUserSettingsUseCase
import com.example.allinone.domain.usecase.user.UpdateUserProfileUseCase
import com.example.allinone.domain.usecase.habit.GetHabitProgressUseCase
import com.example.allinone.domain.usecase.workout.GetWorkoutProgressUseCase
import com.example.allinone.domain.usecase.finance.GetFinancialSummaryUseCase
import com.example.allinone.domain.usecase.assistant.GetAssistantAdviceUseCase
import com.example.allinone.domain.usecase.agenda.GetTodayAgendaUseCase
import com.example.allinone.ui.home.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository,
    private val noteRepository: NoteRepository,
    private val financeRepository: FinanceRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val getHabitProgressUseCase: GetHabitProgressUseCase,
    private val getWorkoutProgressUseCase: GetWorkoutProgressUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val getAssistantAdviceUseCase: GetAssistantAdviceUseCase,
    private val getTodayAgendaUseCase: GetTodayAgendaUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {
    var dashboardState by mutableStateOf(DashboardState())
        private set

    fun updateSelectedTab(tab: Int) {
        dashboardState = dashboardState.copy(selectedTab = tab)
    }

    fun refreshState(context: Context, effectiveMood: String? = null) {
        viewModelScope.launch {
            if (!DataManager.isDataLoaded.value) {
                DataManager.isDataLoaded.first { it }
            }

            combine(
                getUserProfileUseCase(),
                getUserSettingsUseCase(),
                habitRepository.getAllHabits(),
                workoutRepository.getAllWorkouts(),
                habitRepository.getHabitSettings(),
                workoutRepository.getWorkoutSettings(),
                noteRepository.getNoteSettings(),
                financeRepository.getFinanceSettings(),
                projectRepository.getProjectSettings(),
                taskRepository.getTaskSettings(),
                getFinancialSummaryUseCase(),
                taskRepository.getTasks(),
                projectRepository.getAllProjects(),
                getTodayAgendaUseCase()
            ) { args ->
                val profile = args[0] as UserProfile
                val settings = args[1] as UserSettings
                val habits = args[2] as List<com.example.allinone.data.model.Habit>
                val workouts = args[3] as List<com.example.allinone.data.model.Workout>
                val habitSettings = args[4] as HabitSettings
                val workoutSettings = args[5] as WorkoutSettings
                val noteSettings = args[6] as NoteSettings
                val financeSettings = args[7] as FinanceSettings
                val projectSettings = args[8] as ProjectSettings
                val taskSettings = args[9] as TaskSettings
                val financialSummary = args[10] as com.example.allinone.domain.usecase.finance.FinancialSummary
                val globalTasks = args[11] as List<com.example.allinone.data.model.Task>
                val globalProjects = args[12] as List<com.example.allinone.data.model.Note>
                val agenda = args[13] as Map<String, List<com.example.allinone.domain.model.AgendaItem>>
                
                val habitProgress = getHabitProgressUseCase(habits)
                val workoutProgress = getWorkoutProgressUseCase(workouts)
                val overallProgress = if (habitProgress == 0 && workoutProgress == 0) 0 else (habitProgress + workoutProgress) / 2
                
                DashboardCombinedData(
                    profile, settings, habitProgress, workoutProgress, overallProgress, 
                    habitSettings, workoutSettings, noteSettings, financeSettings, projectSettings, taskSettings, financialSummary, agenda
                )
            }.collect { data ->
                val profile = data.profile
                val settings = data.settings
                val habitSettings = data.habitSettings
                val workoutSettings = data.workoutSettings
                val noteSettings = data.noteSettings
                val financeSettings = data.financeSettings
                val projectSettings = data.projectSettings
                val taskSettings = data.taskSettings
                val financialSummary = data.financialSummary

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

                val agenda = data.agenda
                val db = AppDatabase.getDatabase(context)
                val hasProjects = synchronized(DataManager.projects) {
                    DataManager.projects.isNotEmpty()
                } || db.workspaceDao().getAllProjectsSync().isNotEmpty()

                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val currentMood = effectiveMood ?: profile.dailyMoods[today]

                dashboardState = DashboardState(
                    userName = profile.name,
                    overallProgress = data.overallProgress,
                    habitProgress = data.habitProgress,
                    workoutProgress = data.workoutProgress,
                    dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                    safeSpendAmount = financialSummary.budgetRemaining,
                    nextMilestone = nextMilestone,
                    recentActions = profile.recentActivities,
                    growthAdvice = getAssistantAdviceUseCase.getGrowthAdvice(currentMood),
                    managementAdvice = getAssistantAdviceUseCase.getManagementAdvice(currentMood),
                    todayAgenda = agenda,
                    currentMood = currentMood,
                    habitColor = habitSettings.globalHabitColor,
                    workoutColor = workoutSettings.globalWorkoutColor,
                    taskColor = taskSettings.globalTaskColor,
                    noteColor = noteSettings.globalNoteColor,
                    projectColor = projectSettings.globalProjectColor,
                    financeColor = financeSettings.globalFinanceColor,
                    habitIcon = habitSettings.globalHabitIcon,
                    workoutIcon = workoutSettings.globalWorkoutIcon,
                    taskIcon = taskSettings.globalTaskIcon,
                    noteIcon = noteSettings.globalNoteIcon,
                    projectIcon = projectSettings.globalProjectIcon,
                    financeIcon = financeSettings.globalFinanceIcon,
                    userAvatarRes = profile.avatarRes,
                    userProfileImageUri = profile.profileImageUri,
                    homeFocusSize = settings.homeFocusSize,
                    homeDisplaySize = settings.homeDisplaySize,
                    appThemeMode = settings.appThemeMode,
                    appAccentColor = settings.appAccentColor,
                    appFontFamily = settings.appFontFamily,
                    appBorderRadius = settings.appBorderRadius,
                    appCardStyle = settings.appCardStyle,
                    appShowShadows = settings.appShowShadows,
                    isDynamicColorEnabled = settings.isDynamicColorEnabled,
                    globalDisplaySize = settings.displaySize,
                    fontSize = settings.fontSize,
                    isSystemAppearanceEnabled = settings.isSystemAppearanceEnabled,
                    showHabitSection = settings.showHabitSection,
                    showWorkoutSection = settings.showWorkoutSection,
                    showTaskSection = settings.showTaskSection,
                    showNoteSection = settings.showNoteSection,
                    showProjectSection = settings.showProjectSection,
                    showFinanceSection = settings.showFinanceSection,
                    showPerformanceSection = settings.showPerformanceSection,
                    isAiAssistantEnabled = settings.isAiAssistantEnabled,
                    isAiVoiceChatEnabled = settings.isAiVoiceChatEnabled,
                    lastViewedNotificationDate = settings.lastViewedNotificationDate,
                    hasNewTodayNotifications = settings.hasNewTodayNotifications,
                    selectedTab = dashboardState.selectedTab,
                    hasProjects = hasProjects,
                    isAppUnlocked = DataManager.isAppUnlocked,
                    isOnboardingCompleted = settings.isOnboardingCompleted,
                    isDataLoaded = true
                )
            }
        }
    }

    fun updateSectionColor(section: String, color: Int) {
        viewModelScope.launch {
            when (section) {
                "HABITS" -> habitRepository.updateSettings(habitRepository.getHabitSettings().first().copy(globalHabitColor = color))
                "WORKOUT" -> workoutRepository.updateSettings(workoutRepository.getWorkoutSettings().first().copy(globalWorkoutColor = color))
                "FINANCE" -> financeRepository.updateSettings(financeRepository.getFinanceSettings().first().copy(globalFinanceColor = color))
                "PROJECTS" -> projectRepository.updateSettings(projectRepository.getProjectSettings().first().copy(globalProjectColor = color))
                "TODO" -> taskRepository.setGlobalTaskColor(color)
                "NOTES" -> noteRepository.updateSettings(noteRepository.getNoteSettings().first().copy(globalNoteColor = color))
            }
        }
    }

    fun markNotificationsAsViewed(dateString: String) {
        viewModelScope.launch {
            val currentSettings = getUserSettingsUseCase().first()
            updateUserSettingsUseCase(currentSettings.copy(
                lastViewedNotificationDate = dateString,
                hasNewTodayNotifications = false
            ))
        }
    }

    fun updateDailyMood(emoji: String) {
        viewModelScope.launch {
            val currentProfile = getUserProfileUseCase().first()
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val updatedMoods = currentProfile.dailyMoods.toMutableMap()
            updatedMoods[today] = emoji
            
            updateUserProfileUseCase(currentProfile.copy(
                dailyMoods = updatedMoods,
                lastMoodTimestamp = System.currentTimeMillis()
            ))
        }
    }

    private data class DashboardCombinedData(
        val profile: UserProfile,
        val settings: UserSettings,
        val habitProgress: Int,
        val workoutProgress: Int,
        val overallProgress: Int,
        val habitSettings: HabitSettings,
        val workoutSettings: WorkoutSettings,
        val noteSettings: NoteSettings,
        val financeSettings: FinanceSettings,
        val projectSettings: ProjectSettings,
        val taskSettings: TaskSettings,
        val financialSummary: com.example.allinone.domain.usecase.finance.FinancialSummary,
        val agenda: Map<String, List<com.example.allinone.domain.model.AgendaItem>>
    )
}
