package com.example.allinone

import android.content.Context
import com.example.allinone.R
import com.example.allinone.assistant.memory.AssistantMemoryRepository
import com.example.allinone.core.models.Journey
import com.example.allinone.data.BackupDataManager
import com.example.allinone.data.DataManagerFacade
import com.example.allinone.data.UserDataManager
import com.example.allinone.data.WorkspaceDataManager
import com.example.allinone.data.database.AiChatDao
import com.example.allinone.data.database.AppDatabase
import com.example.allinone.data.model.*
import com.example.allinone.data.preferences.UserPreferencesRepository
import com.example.allinone.domain.model.AgendaItem
import com.example.allinone.domain.repository.*
import com.example.allinone.domain.usecase.agenda.GetTodayAgendaUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * DataManager (Facade Bridge): Bridges legacy callers to Hilt-managed singletons 
 * and delegates domain data operations to UserDataManager, WorkspaceDataManager, and BackupDataManager.
 */
object DataManager {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DataManagerEntryPoint {
        fun taskRepository(): TaskRepository
        fun habitRepository(): HabitRepository
        fun noteRepository(): NoteRepository
        fun workoutRepository(): WorkoutRepository
        fun userRepository(): UserRepository
        fun financeRepository(): FinanceRepository
        fun projectRepository(): ProjectRepository
        fun backupRepository(): BackupRepository
        fun assistantMemoryRepository(): AssistantMemoryRepository
        fun aiChatDao(): AiChatDao
        fun getTodayAgendaUseCase(): GetTodayAgendaUseCase
        fun assistantBrain(): com.example.allinone.AssistantBrain
    }

    private var appContext: Context? = null

    fun getEntryPoint(context: Context): DataManagerEntryPoint {
        return EntryPointAccessors.fromApplication(context.applicationContext, DataManagerEntryPoint::class.java)
    }

    fun getAiChatRepository(context: Context? = null): AiChatDao? {
        val ctx = context ?: appContext ?: return null
        return AppDatabase.getDatabase(ctx.applicationContext).aiChatDao()
    }

    private val userDataManager = UserDataManager()
    private val workspaceDataManager = WorkspaceDataManager()
    private val backupDataManager = BackupDataManager()
    private val assistantSettingsManager = com.example.allinone.data.AssistantSettingsManager()

    private val syncScope = CoroutineScope(Dispatchers.IO + Job())
    private var saveJob: Job? = null
    private var isSyncing = false

    fun startSync(context: Context) {
        if (isSyncing) return
        isSyncing = true
        val entryPoint = getEntryPoint(context)
        
        syncScope.launch {
            entryPoint.habitRepository().getAllHabits().collect { list ->
                habits.clear()
                habits.addAll(list)
                notifyDataChanged()
            }
        }

        syncScope.launch {
            entryPoint.taskRepository().getTasks().collect { list ->
                tasks.clear()
                tasks.addAll(list)
                notifyDataChanged()
            }
        }
        
        syncScope.launch {
            entryPoint.workoutRepository().getAllWorkouts().collect { list ->
                workouts.clear()
                workouts.addAll(list)
                notifyDataChanged()
            }
        }
        
        syncScope.launch {
            entryPoint.userRepository().getDayHistory().collect { map ->
                history.clear()
                history.putAll(map)
                notifyDataChanged()
            }
        }
        
        syncScope.launch {
            entryPoint.userRepository().getUserProfile().collect { profile ->
                userName = profile.name
                userAvatarRes = profile.avatarRes
                dailyMoods.clear()
                dailyMoods.putAll(profile.dailyMoods)
                notifyDataChanged()
            }
        }
    }

    val dataChangeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val isDataLoaded = MutableStateFlow(false)

    val tasks: MutableList<Task> get() = userDataManager.tasks
    val habits: MutableList<Habit> get() = userDataManager.habits
    val workouts: MutableList<Workout> get() = userDataManager.workouts
    val notes: MutableList<Note> get() = userDataManager.notes
    val projects: MutableList<Note> get() = userDataManager.projects
    val transactions: MutableList<Transaction> get() = userDataManager.transactions
    val ledgerEntries: MutableList<PersonalLedgerEntry> get() = userDataManager.ledgerEntries
    val personalLedgers: MutableList<PersonalLedger> get() = userDataManager.personalLedgers
    val history: MutableMap<String, DayHistory> get() = userDataManager.history
    val monthlyBudgets: MutableMap<String, Double> = Collections.synchronizedMap(mutableMapOf())

    val currentEditingSubFeatures: MutableList<ProjectFeature> = Collections.synchronizedList(mutableListOf())
    val currentEditingIdeaSubFeatures: MutableList<ProjectFeature> get() = workspaceDataManager.currentEditingIdeaSubFeatures
    val projectTemplates: MutableMap<String, List<String>> = Collections.synchronizedMap(mutableMapOf())
    val projectCustomTags: MutableList<String> = Collections.synchronizedList(mutableListOf("TASKS", "FEATURES", "BUGS", "RESOURCES", "OTHER"))
    val predefinedJourneys: List<Journey> = emptyList()
    val dailyMoods: MutableMap<String, String> = Collections.synchronizedMap(mutableMapOf())

    // User & Identity & App Lock
    var userName: String = "User"
    var userAvatarRes: Int = -1
    var isAiAssistantEnabled: Boolean by assistantSettingsManager::isEnabled
    var isOnboardingCompleted: Boolean = false
    var isAppLockEnabled: Boolean = false
    var isBiometricLockEnabled: Boolean = false
    var isAppUnlocked: Boolean = true
    var appLockQuestion: String = ""
    var appLockAnswer: String = ""
    var appLockPin: String = ""

    // Notification & Reminder Preferences
    var isWorkspaceNotificationEnabled: Boolean = true
    var isNoteNotificationEnabled: Boolean = true
    var isProjectNotificationEnabled: Boolean = true
    var isTaskNotificationEnabled: Boolean = true
    var isHabitNotificationEnabled: Boolean = true
    var isWorkoutNotificationEnabled: Boolean = true
    var isFinanceNotificationEnabled: Boolean = true

    var isMorningReminderEnabled: Boolean = true
    var morningReminderTime: String = "08:00"
    var isNightReminderEnabled: Boolean = true
    var nightReminderTime: String = "22:00"

    // Project Settings
    var projectRoadmapsEnabled: Boolean = true
    var projectIdeasEnabled: Boolean = true
    var projectAutoSaveIdeas: Boolean = true
    var projectAutoArchive: Boolean = false
    var projectSynergySync: Boolean = true
    var projectDeadlineAlerts: Boolean = true
    var projectAnalyticsEnabled: Boolean = true

    // Section Visibility & Categories
    var noteVisibleSections: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var taskVisibleSections: MutableList<String> = Collections.synchronizedList(mutableListOf("Tasks", "List"))
    var taskDefaultSection: String = "Tasks"
    var taskCustomCategories: MutableList<String> = Collections.synchronizedList(mutableListOf("General", "Personal", "Work", "Shopping"))
    var taskEditModeEnabled: Boolean = false
    var taskShowHidden: Boolean = false
    var taskShowCompleted: Boolean = true
    var taskSortOrder: String = "MANUAL"
    var taskAutoArchive: Boolean = false

    // Finance Preferences & Goals
    var financeCurrency: String = "$"
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
    var financeSavingsGoalName: String = "Savings"
    var isFinanceLedgerEnabled: Boolean = true
    var financeGraphColor: Int = -1
    var financeGraphSavingsColor: Int = -1
    var financeGraphStartMonth: Int = 0
    var financeCustomCategories: MutableList<String> = Collections.synchronizedList(mutableListOf())

    // Voice & Assistant Preferences
    var assistantVoiceName: String by assistantSettingsManager::voiceName
    var assistantPitch: Float by assistantSettingsManager::pitch
    var assistantSpeechRate: Float by assistantSettingsManager::speechRate

    // Section Visibility Preferences
    var showHabitSection: Boolean = true
    var showWorkoutSection: Boolean = true
    var showTaskSection: Boolean = true
    var showNoteSection: Boolean = true
    var showProjectSection: Boolean = true
    var showFinanceSection: Boolean = true
    var workoutShowCompleted: Boolean = true

    // System & Theme Preferences
    var appThemeMode: String = "SYSTEM"
    var appAccentColor: Int = -1
    var appBorderRadius: Int = 12
    var displaySize: String = "Normal"
    var fontSize: String = "Normal"
    var isAssistantVoiceEnabled: Boolean by assistantSettingsManager::isVoiceEnabled
    var isScreenshotProtectionEnabled: Boolean = false

    // Color & Icon Theme Preferences
    var globalHabitColor: Int = -1
    var globalWorkoutColor: Int = -1
    var globalTaskColor: Int = -1
    var globalNoteColor: Int = -1
    var globalProjectColor: Int = -1
    var globalFinanceColor: Int = -1

    var globalHabitIcon: Int = -1
    var globalWorkoutIcon: Int = -1
    var globalTaskIcon: Int = -1
    var globalProjectIcon: Int = -1
    var globalNoteIcon: Int = -1
    var globalFinanceIcon: Int = -1

    var habitAddThemeColor: Int = -1
    var workoutAddThemeColor: Int = -1
    var taskAddThemeColor: Int = -1
    var projectAddThemeColor: Int = -1
    var noteAddThemeColor: Int = -1
    var financeAddThemeColor: Int = -1

    var startupLoadingTime: Long = 1000L

    fun calculateDayHistory(dateKey: String): DayHistory = userDataManager.calculateDayHistory(dateKey)
    fun getDayHistory(dateKey: String): DayHistory? = userDataManager.getDayHistory(dateKey)

    fun getTrackingDateString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    fun getTrackingCalendar(): Calendar = Calendar.getInstance()

    fun getUniqueFeatureName(baseName: String, existing: List<ProjectFeature>): String {
        return workspaceDataManager.getUniqueFeatureName(baseName, existing)
    }

    fun getLastSevenDaysDetailedProgress(): List<Triple<String, Int, Int>> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val result = mutableListOf<Triple<String, Int, Int>>()
        val cal = Calendar.getInstance()
        
        for (i in 0 until 7) {
            val dateKey = sdf.format(cal.time)
            val h = calculateDayHistory(dateKey)
            
            val hProgress = if (h.totalHabits > 0) (h.habitsCompleted * 100) / h.totalHabits else 0
            val wProgress = if (h.totalWorkouts > 0) (h.workoutsCompleted * 100) / h.totalWorkouts else 0
            
            result.add(Triple(dateKey, hProgress, wProgress))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result.reversed()
    }

    fun getComprehensiveTodayAgenda(): Map<String, List<AgendaItem>> = emptyMap()
    fun getComprehensiveTodayAgenda(context: Context): Map<String, List<AgendaItem>> = emptyMap()

    fun getHabitPerformanceByFrequency(): Map<String, Int> = emptyMap()
    fun getMoodCorrelationData(): Map<String, Int> = emptyMap()

    fun getCurrentStreak(): Int = 0
    fun getTotalHabitsFinished(): Int = 0

    fun addTask(task: Task) {
        tasks.add(task)
        appContext?.let { context ->
            syncScope.launch {
                getEntryPoint(context).taskRepository().addTask(task)
            }
        }
    }

    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.timestamp == task.timestamp }
        if (index != -1) tasks[index] = task
        appContext?.let { context ->
            syncScope.launch {
                getEntryPoint(context).taskRepository().updateTask(task)
            }
        }
    }

    fun deleteTask(task: Task) {
        tasks.removeIf { it.timestamp == task.timestamp }
        appContext?.let { context ->
            syncScope.launch {
                getEntryPoint(context).taskRepository().deleteTask(task)
            }
        }
    }

    fun clearAllHistory() {}
    fun addActivity(activity: String) {}
    fun saveDayNote(date: String, note: String) {}

    suspend fun exportData(context: Context, password: CharArray? = null): String {
        return getEntryPoint(context).backupRepository().exportData(password)
    }

    suspend fun importData(context: Context, json: String, password: CharArray? = null): Boolean {
        val success = getEntryPoint(context).backupRepository().importData(json, password)
        if (success) {
            refreshLegacyState(context)
        }
        return success
    }

    suspend fun refreshLegacyState(context: Context) {
        val entryPoint = getEntryPoint(context)
        
        // Sync Collections
        val habitsList = entryPoint.habitRepository().getAllHabits().first()
        habits.clear()
        habits.addAll(habitsList)
        
        val workoutsList = entryPoint.workoutRepository().getAllWorkouts().first()
        workouts.clear()
        workouts.addAll(workoutsList)
        
        val historyMap = entryPoint.userRepository().getDayHistory().first()
        history.clear()
        history.putAll(historyMap)

        val profile = entryPoint.userRepository().getUserProfile().first()
        val settings = entryPoint.userRepository().getUserSettings().first()
        
        userName = profile.name
        userAvatarRes = profile.avatarRes
        
        appThemeMode = settings.appThemeMode
        appAccentColor = settings.appAccentColor
        appBorderRadius = settings.appBorderRadius
        displaySize = settings.displaySize
        fontSize = settings.fontSize
        
        isAiAssistantEnabled = settings.isAiAssistantEnabled
        isOnboardingCompleted = settings.isOnboardingCompleted
        isAppLockEnabled = settings.isAppLockEnabled
        appLockPin = settings.appLockPin ?: ""
        
        isTaskNotificationEnabled = settings.isTaskNotificationEnabled
        isHabitNotificationEnabled = settings.isHabitNotificationEnabled
        isWorkoutNotificationEnabled = settings.isWorkoutNotificationEnabled
        isNoteNotificationEnabled = settings.isNoteNotificationEnabled
        isProjectNotificationEnabled = settings.isProjectNotificationEnabled
        isFinanceNotificationEnabled = settings.isFinanceNotificationEnabled
        
        notifyDataChanged()
    }

    fun notifyDataChanged() {
        dataChangeSignal.tryEmit(Unit)
    }

    fun checkAndSetNewTodayNotification(context: Context) {}
    fun checkAndSetNewTodayNotification(targetTime: Long? = null) {}

    fun initialize(context: Context) {
        init(context)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        syncScope.launch {
            com.example.allinone.data.repository.LegacyMigrationManager(context).migrateIfNeeded()
            backupDataManager.loadData(context)
            startSync(context)
            getEntryPoint(context).assistantBrain().initialize(context)
            isDataLoaded.value = true
        }
    }

    fun loadData(context: Context) {
        appContext = context.applicationContext
        backupDataManager.loadData(context)
    }

    fun saveDataDebounced(context: Context) {
        saveJob?.cancel()
        saveJob = syncScope.launch {
            kotlinx.coroutines.delay(1000) // 1 second debounce
            saveData(context, immediate = true)
        }
    }

    fun saveData(context: Context, immediate: Boolean = false) {
        appContext = context.applicationContext
        backupDataManager.saveData(context)
        
        // Sync critical properties back to Repository
        syncScope.launch {
            val entryPoint = getEntryPoint(context)
            val userRepo = entryPoint.userRepository()
            
            // Sync Profile
            try {
                val currentProfile = userRepo.getUserProfile().first()
                userRepo.updateUserProfile(currentProfile.copy(
                    name = userName,
                    avatarRes = userAvatarRes
                ))

                // Sync Settings
                val currentSettings = userRepo.getUserSettings().first()
                userRepo.updateUserSettings(currentSettings.copy(
                    isOnboardingCompleted = isOnboardingCompleted,
                    isAiAssistantEnabled = isAiAssistantEnabled,
                    appThemeMode = appThemeMode,
                    appAccentColor = appAccentColor,
                    displaySize = displaySize,
                    fontSize = fontSize,
                    showHabitSection = showHabitSection,
                    showWorkoutSection = showWorkoutSection,
                    showTaskSection = showTaskSection,
                    showNoteSection = showNoteSection,
                    showProjectSection = showProjectSection,
                    showFinanceSection = showFinanceSection
                ))
            } catch (e: Exception) {
                // Handle potential flow empty or other issues
            }
        }
    }
}
