package com.example.allinone

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.allinone.data.*
import com.example.allinone.workspace.data.*
import com.example.allinone.data.repository.*
import com.example.allinone.workspace.data.AppDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

object DataManager {
    private val persistenceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var saveJob: Job? = null
    private var observationJob: Job? = null
    
    private var taskRepo: TaskRepository? = null
    private var habitRepo: HabitRepository? = null
    private var workoutRepo: WorkoutRepository? = null
    private var noteRepo: NoteRepository? = null
    private var financeRepo: FinanceRepository? = null
    private var aiChatRepo: AiChatRepository? = null

    val dataChangeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val isDataLoaded = MutableStateFlow(false)

    fun notifyDataChanged() {
        dataChangeSignal.tryEmit(Unit)
    }

    fun initialize(context: Context) {
        isDataLoaded.value = false // Lock saves during initialization
        observationJob?.cancel()
        observationJob = persistenceScope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val t = TaskRepository(db.taskDao())
                val h = HabitRepository(db.habitDao())
                val w = WorkoutRepository(db.workoutDao())
                val n = NoteRepository(db.noteDao())
                val f = FinanceRepository(db.financeDao())
                val ai = AiChatRepository(db.aiChatDao())
                
                synchronized(this@DataManager) {
                    taskRepo = t
                    habitRepo = h
                    workoutRepo = w
                    noteRepo = n
                    financeRepo = f
                    aiChatRepo = ai
                }
                
                loadData(context)
                LegacyMigrationManager(context).migrateIfNeeded()
                AssistantBrain.initialize(context)
                startDatabaseObservation()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDataLoaded.value = true
            }
        }
    }

    private fun CoroutineScope.startDatabaseObservation() {
        val tRepo = taskRepo ?: return
        val hRepo = habitRepo ?: return
        val wRepo = workoutRepo ?: return
        val nRepo = noteRepo ?: return
        val fRepo = financeRepo ?: return

        // 1. Tasks
        launch {
            tRepo.getAllTasks().collect { newList ->
                // Wipe Guard: Prevent UI from flickering to empty if Room emits empty list during transient states
                // unless it's a deliberate reset (which we handle by setting isDataLoaded to false)
                if (newList.isEmpty() && tasks.isNotEmpty() && isDataLoaded.value) return@collect 
                synchronized(tasks) {
                    tasks.clear()
                    tasks.addAll(newList)
                }
                notifyDataChanged()
            }
        }

        // 2. Habits
        launch {
            hRepo.getAllHabits().collect { newList ->
                if (newList.isEmpty() && habits.isNotEmpty() && isDataLoaded.value) return@collect 
                synchronized(habits) {
                    habits.clear()
                    habits.addAll(newList)
                }
                notifyDataChanged()
            }
        }

        // 3. Workouts
        launch {
            wRepo.getAllWorkouts().collect { newList ->
                if (newList.isEmpty() && workouts.isNotEmpty() && isDataLoaded.value) return@collect 
                synchronized(workouts) {
                    workouts.clear()
                    workouts.addAll(newList)
                }
                notifyDataChanged()
            }
        }

        // 4. Notes & Projects
        launch {
            nRepo.getAllNotes().collect { newList ->
                if (newList.isEmpty() && (notes.isNotEmpty() || projects.isNotEmpty()) && isDataLoaded.value) return@collect 
                val newNotes = newList.filter { !it.isGlobalProject }
                val newProjects = newList.filter { it.isGlobalProject }
                
                synchronized(notes) {
                    notes.clear()
                    notes.addAll(newNotes)
                }
                synchronized(projects) {
                    projects.clear()
                    projects.addAll(newProjects)
                }
                notifyDataChanged()
            }
        }

        // 5. Finance Transactions
        launch {
            fRepo.getAllTransactions().collect { newList ->
                if (newList.isEmpty() && transactions.isNotEmpty() && isDataLoaded.value) return@collect 
                synchronized(transactions) {
                    transactions.clear()
                    transactions.addAll(newList)
                }
                notifyDataChanged()
            }
        }

        // 6. Finance Ledgers
        launch {
            fRepo.getAllPersonalLedgers().collect { newList ->
                synchronized(personalLedgers) {
                    personalLedgers.clear()
                    personalLedgers.addAll(newList)
                }
                notifyDataChanged()
            }
        }

        // 7. Finance Ledger Entries
        launch {
            fRepo.getAllLedgerEntries().collect { newList ->
                synchronized(ledgerEntries) {
                    ledgerEntries.clear()
                    ledgerEntries.addAll(newList)
                }
                notifyDataChanged()
            }
        }
    }

    // Delegated Data
    var habits: MutableList<Habit> get() = HabitDataManager.habits; set(value) { HabitDataManager.habits = value }
    var workouts: MutableList<Workout> get() = WorkoutDataManager.workouts; set(value) { WorkoutDataManager.workouts = value }
    var tasks: MutableList<Task> get() = TaskDataManager.tasks; set(value) { TaskDataManager.tasks = value }
    var notes: MutableList<Note> get() = NoteDataManager.notes; set(value) { NoteDataManager.notes = value }
    var projects: MutableList<Note> get() = ProjectDataManager.projects; set(value) { ProjectDataManager.projects = value }
    var transactions: MutableList<Transaction> get() = FinanceDataManager.transactions; set(value) { FinanceDataManager.transactions = value }
    var ledgerEntries: MutableList<LedgerEntry> get() = FinanceDataManager.ledgerEntries; set(value) { FinanceDataManager.ledgerEntries = value }
    var personalLedgers: MutableList<PersonalLedger> get() = FinanceDataManager.personalLedgers; set(value) { FinanceDataManager.personalLedgers = value }
    
    var monthlyBudget: Double get() = FinanceDataManager.monthlyBudget; set(value) { FinanceDataManager.monthlyBudget = value }
    var monthlySavingsGoal: Double get() = FinanceDataManager.monthlySavingsGoal; set(value) { FinanceDataManager.monthlySavingsGoal = value }
    var financeSavingsGoalName: String get() = FinanceDataManager.financeSavingsGoalName; set(value) { FinanceDataManager.financeSavingsGoalName = value }
    var monthlyBudgets: MutableMap<String, Double> get() = FinanceDataManager.monthlyBudgets; set(value) { FinanceDataManager.monthlyBudgets = value }
    var monthlySavingsGoals: MutableMap<String, Double> get() = FinanceDataManager.monthlySavingsGoals; set(value) { FinanceDataManager.monthlySavingsGoals = value }
    
    var history = mutableMapOf<String, DayHistory>() // Keep core history here for now as it's cross-domain

    // To-Do List Settings
    var taskShowCompleted: Boolean get() = TaskDataManager.taskShowCompleted; set(value) { TaskDataManager.taskShowCompleted = value }
    var taskShowHidden: Boolean get() = TaskDataManager.taskShowHidden; set(value) { TaskDataManager.taskShowHidden = value }
    var taskSortOrder: String get() = TaskDataManager.taskSortOrder; set(value) { TaskDataManager.taskSortOrder = value }
    var taskCustomCategories: MutableList<String> get() = TaskDataManager.taskCustomCategories; set(value) { TaskDataManager.taskCustomCategories = value }
    var taskAutoArchive: Boolean get() = TaskDataManager.taskAutoArchive; set(value) { TaskDataManager.taskAutoArchive = value }
    var taskDefaultSection: String get() = TaskDataManager.taskDefaultSection; set(value) { TaskDataManager.taskDefaultSection = value }
    var taskVisibleSections: MutableList<String> get() = TaskDataManager.taskVisibleSections; set(value) { TaskDataManager.taskVisibleSections = value }
    var taskEditModeEnabled: Boolean get() = TaskDataManager.taskEditModeEnabled; set(value) { TaskDataManager.taskEditModeEnabled = value }

    // Finance Settings
    var financeCustomCategories: MutableList<String> get() = FinanceDataManager.financeCustomCategories; set(value) { FinanceDataManager.financeCustomCategories = value }
    var financeCategoryIcons: MutableMap<String, Int> get() = FinanceDataManager.financeCategoryIcons; set(value) { FinanceDataManager.financeCategoryIcons = value }
    var financeCategoryColors: MutableMap<String, Int> get() = FinanceDataManager.financeCategoryColors; set(value) { FinanceDataManager.financeCategoryColors = value }
    var financeCurrency: String get() = FinanceDataManager.financeCurrency; set(value) { FinanceDataManager.financeCurrency = value }
    var financeGraphStartMonth: Int get() = FinanceDataManager.financeGraphStartMonth; set(value) { FinanceDataManager.financeGraphStartMonth = value }
    var financeGraphColor: Int get() = FinanceDataManager.financeGraphColor; set(value) { FinanceDataManager.financeGraphColor = value }
    var financeGraphSavingsColor: Int get() = FinanceDataManager.financeGraphSavingsColor; set(value) { FinanceDataManager.financeGraphSavingsColor = value }
    var isFinanceLedgerEnabled: Boolean get() = FinanceDataManager.isFinanceLedgerEnabled; set(value) { FinanceDataManager.isFinanceLedgerEnabled = value }

    // Habit Settings
    var habitDefaultTab: String get() = HabitDataManager.habitDefaultTab; set(value) { HabitDataManager.habitDefaultTab = value }
    var habitVacationMode: Boolean get() = HabitDataManager.habitVacationMode; set(value) { HabitDataManager.habitVacationMode = value }
    var habitSortOrder: String get() = HabitDataManager.habitSortOrder; set(value) { HabitDataManager.habitSortOrder = value }
    var habitCompletionSound: Boolean get() = HabitDataManager.habitCompletionSound; set(value) { HabitDataManager.habitCompletionSound = value }
    var habitCompletionHaptics: Boolean get() = HabitDataManager.habitCompletionHaptics; set(value) { HabitDataManager.habitCompletionHaptics = value }
    var habitDayResetHour: Int get() = HabitDataManager.habitDayResetHour; set(value) { HabitDataManager.habitDayResetHour = value }
    var habitBulkMode: Boolean get() = HabitDataManager.habitBulkMode; set(value) { HabitDataManager.habitBulkMode = value }
    var habitGraceDaysAllowed: Int get() = HabitDataManager.habitGraceDaysAllowed; set(value) { HabitDataManager.habitGraceDaysAllowed = value }
    var habitShowCompleted: Boolean get() = HabitDataManager.habitShowCompleted; set(value) { HabitDataManager.habitShowCompleted = value }

    // Workout Settings
    var workoutMuscleGroups: MutableList<String> get() = WorkoutDataManager.workoutMuscleGroups; set(value) { WorkoutDataManager.workoutMuscleGroups = value }
    var workoutFilterType: String get() = WorkoutDataManager.workoutFilterType; set(value) { WorkoutDataManager.workoutFilterType = value }
    var workoutAutoRestTimer: Boolean get() = WorkoutDataManager.workoutAutoRestTimer; set(value) { WorkoutDataManager.workoutAutoRestTimer = value }
    var workoutWeightUnit: String get() = WorkoutDataManager.workoutWeightUnit; set(value) { WorkoutDataManager.workoutWeightUnit = value }
    var workoutDefaultMode: String get() = WorkoutDataManager.workoutDefaultMode; set(value) { WorkoutDataManager.workoutDefaultMode = value }
    var workoutRestDuration: Int get() = WorkoutDataManager.workoutRestDuration; set(value) { WorkoutDataManager.workoutRestDuration = value }
    var workoutShowCompleted: Boolean get() = WorkoutDataManager.workoutShowCompleted; set(value) { WorkoutDataManager.workoutShowCompleted = value }

    // Note Settings
    var noteAutoCleanupDays: Int get() = NoteDataManager.noteAutoCleanupDays; set(value) { NoteDataManager.noteAutoCleanupDays = value }
    var noteDefaultCategory: String get() = NoteDataManager.noteDefaultCategory; set(value) { NoteDataManager.noteDefaultCategory = value }
    var noteShowHidden: Boolean get() = NoteDataManager.noteShowHidden; set(value) { NoteDataManager.noteShowHidden = value }
    var noteVoiceInputEnabled: Boolean get() = NoteDataManager.noteVoiceInputEnabled; set(value) { NoteDataManager.noteVoiceInputEnabled = value }
    var noteVisibleSections: MutableList<String> get() = NoteDataManager.noteVisibleSections; set(value) { NoteDataManager.noteVisibleSections = value }
    
    // Project Advanced Settings
    var projectAutoArchive: Boolean get() = ProjectDataManager.projectAutoArchive; set(value) { ProjectDataManager.projectAutoArchive = value }
    var projectSynergySync: Boolean get() = ProjectDataManager.projectSynergySync; set(value) { ProjectDataManager.projectSynergySync = value }
    var projectDeadlineAlerts: Boolean get() = ProjectDataManager.projectDeadlineAlerts; set(value) { ProjectDataManager.projectDeadlineAlerts = value }
    var projectAnalyticsEnabled: Boolean get() = ProjectDataManager.projectAnalyticsEnabled; set(value) { ProjectDataManager.projectAnalyticsEnabled = value }
    var projectCustomTags: MutableList<String> get() = ProjectDataManager.projectCustomTags; set(value) { ProjectDataManager.projectCustomTags = value }
    var projectSortCompletedToBottom: Boolean get() = ProjectDataManager.projectSortCompletedToBottom; set(value) { ProjectDataManager.projectSortCompletedToBottom = value }
    var projectActiveExpanded: Boolean get() = ProjectDataManager.projectActiveExpanded; set(value) { ProjectDataManager.projectActiveExpanded = value }
    var projectCompletedExpanded: Boolean get() = ProjectDataManager.projectCompletedExpanded; set(value) { ProjectDataManager.projectCompletedExpanded = value }
    var ideaActiveExpanded: Boolean get() = ProjectDataManager.ideaActiveExpanded; set(value) { ProjectDataManager.ideaActiveExpanded = value }
    var ideaCompletedExpanded: Boolean get() = ProjectDataManager.ideaCompletedExpanded; set(value) { ProjectDataManager.ideaCompletedExpanded = value }
    var projectAutoSaveIdeas: Boolean get() = ProjectDataManager.projectAutoSaveIdeas; set(value) { ProjectDataManager.projectAutoSaveIdeas = value }
    var projectDualExistEnabled: Boolean get() = ProjectDataManager.projectDualExistEnabled; set(value) { ProjectDataManager.projectDualExistEnabled = value }
    var projectIdeasEnabled: Boolean get() = ProjectDataManager.projectIdeasEnabled; set(value) { ProjectDataManager.projectIdeasEnabled = value }
    var projectRoadmapsEnabled: Boolean get() = ProjectDataManager.projectRoadmapsEnabled; set(value) { ProjectDataManager.projectRoadmapsEnabled = value }
    
    // User / System Settings
    var isAppLockEnabled: Boolean get() = UserDataManager.isAppLockEnabled; set(value) { UserDataManager.isAppLockEnabled = value }
    var isBiometricLockEnabled: Boolean get() = UserDataManager.isBiometricLockEnabled; set(value) { UserDataManager.isBiometricLockEnabled = value }
    var isScreenshotProtectionEnabled: Boolean get() = UserDataManager.isScreenshotProtectionEnabled; set(value) { UserDataManager.isScreenshotProtectionEnabled = value }
    var isAppUnlocked: Boolean get() = UserDataManager.isAppUnlocked; set(value) { UserDataManager.isAppUnlocked = value }
    var isOnboardingCompleted: Boolean get() = UserDataManager.isOnboardingCompleted; set(value) { UserDataManager.isOnboardingCompleted = value }
    var appLockPin: String? get() = UserDataManager.appLockPin; set(value) { UserDataManager.appLockPin = value }
    var appLockQuestion: String? get() = UserDataManager.appLockQuestion; set(value) { UserDataManager.appLockQuestion = value }
    var appLockAnswer: String? get() = UserDataManager.appLockAnswer; set(value) { UserDataManager.appLockAnswer = value }
    
    var lastViewedNotificationDate: String get() = WorkspaceDataManager.lastViewedNotificationDate; set(value) { WorkspaceDataManager.lastViewedNotificationDate = value }
    var lastSummaryNotificationDate: String get() = WorkspaceDataManager.lastSummaryNotificationDate; set(value) { WorkspaceDataManager.lastSummaryNotificationDate = value }
    var hasNewTodayNotifications: Boolean get() = WorkspaceDataManager.hasNewTodayNotifications; set(value) { WorkspaceDataManager.hasNewTodayNotifications = value }
    var workspaceTodayAgenda: MutableMap<String, List<AgendaItem>> get() = WorkspaceDataManager.workspaceTodayAgenda; set(value) { WorkspaceDataManager.workspaceTodayAgenda = value }

    var userXP: Int get() = UserDataManager.userXP; set(value) { UserDataManager.userXP = value }
    var userLevel: Int get() = UserDataManager.userLevel; set(value) { UserDataManager.userLevel = value }
    var userName: String get() = UserDataManager.userName; set(value) { UserDataManager.userName = value }
    var userBio: String get() = UserDataManager.userBio; set(value) { UserDataManager.userBio = value }
    var userAvatarRes: Int get() = UserDataManager.userAvatarRes; set(value) { UserDataManager.userAvatarRes = value }
    var userProfileImageUri: String? get() = UserDataManager.userProfileImageUri; set(value) { UserDataManager.userProfileImageUri = value }
    var recentActivities: MutableList<String> get() = UserDataManager.recentActivities; set(value) { UserDataManager.recentActivities = value }
    var dailyMoods: MutableMap<String, String> get() = UserDataManager.dailyMoods; set(value) { UserDataManager.dailyMoods = value }
    var lastMoodTimestamp: Long get() = UserDataManager.lastMoodTimestamp; set(value) { UserDataManager.lastMoodTimestamp = value }
    var displaySize: String get() = UserDataManager.displaySize; set(value) { UserDataManager.displaySize = value }
    var homeDisplaySize: String get() = UserDataManager.homeDisplaySize; set(value) { UserDataManager.homeDisplaySize = value }
    var homeFocusSize: String get() = UserDataManager.homeFocusSize; set(value) { UserDataManager.homeFocusSize = value }
    var fontSize: String get() = UserDataManager.fontSize; set(value) { UserDataManager.fontSize = value }
    var isSystemAppearanceEnabled: Boolean get() = UserDataManager.isSystemAppearanceEnabled; set(value) { UserDataManager.isSystemAppearanceEnabled = value }
    
    var appThemeMode: String get() = UserDataManager.appThemeMode; set(value) { UserDataManager.appThemeMode = value }
    var appAccentColor: Int get() = UserDataManager.appAccentColor; set(value) { UserDataManager.appAccentColor = value }
    var appFontFamily: String get() = UserDataManager.appFontFamily; set(value) { UserDataManager.appFontFamily = value }
    var appBorderRadius: Int get() = UserDataManager.appBorderRadius; set(value) { UserDataManager.appBorderRadius = value }
    var appCardStyle: String get() = UserDataManager.appCardStyle; set(value) { UserDataManager.appCardStyle = value }
    var appShowShadows: Boolean get() = UserDataManager.appShowShadows; set(value) { UserDataManager.appShowShadows = value }
    var isDynamicColorEnabled: Boolean get() = UserDataManager.isDynamicColorEnabled; set(value) { UserDataManager.isDynamicColorEnabled = value }
    var startupLoadingTime: Int get() = UserDataManager.startupLoadingTime; set(value) { UserDataManager.startupLoadingTime = value }
    
    // Notification Settings
    var isMorningReminderEnabled: Boolean get() = NotificationDataManager.isMorningReminderEnabled; set(value) { NotificationDataManager.isMorningReminderEnabled = value }
    var morningReminderTime: String get() = NotificationDataManager.morningReminderTime; set(value) { NotificationDataManager.morningReminderTime = value }
    var isNightReminderEnabled: Boolean get() = NotificationDataManager.isNightReminderEnabled; set(value) { NotificationDataManager.isNightReminderEnabled = value }
    var nightReminderTime: String get() = NotificationDataManager.nightReminderTime; set(value) { NotificationDataManager.nightReminderTime = value }

    var showHabitSection: Boolean get() = UserDataManager.showHabitSection; set(value) { UserDataManager.showHabitSection = value }
    var showWorkoutSection: Boolean get() = UserDataManager.showWorkoutSection; set(value) { UserDataManager.showWorkoutSection = value }
    var showTaskSection: Boolean get() = UserDataManager.showTaskSection; set(value) { UserDataManager.showTaskSection = value }
    var showNoteSection: Boolean get() = UserDataManager.showNoteSection; set(value) { UserDataManager.showNoteSection = value }
    var showProjectSection: Boolean get() = UserDataManager.showProjectSection; set(value) { UserDataManager.showProjectSection = value }
    var showFinanceSection: Boolean get() = UserDataManager.showFinanceSection; set(value) { UserDataManager.showFinanceSection = value }
    var showPerformanceSection: Boolean get() = UserDataManager.showPerformanceSection; set(value) { UserDataManager.showPerformanceSection = value }
    
    var userCustomColors: MutableList<Int> get() = UserDataManager.userCustomColors; set(value) { UserDataManager.userCustomColors = value }

    // Global Colors & Icons
    var globalHabitColor: Int get() = HabitDataManager.globalHabitColor; set(value) { HabitDataManager.globalHabitColor = value }
    var globalWorkoutColor: Int get() = WorkoutDataManager.globalWorkoutColor; set(value) { WorkoutDataManager.globalWorkoutColor = value }
    var globalTaskColor: Int get() = TaskDataManager.globalTaskColor; set(value) { TaskDataManager.globalTaskColor = value }
    var globalProjectColor: Int get() = ProjectDataManager.globalProjectColor; set(value) { ProjectDataManager.globalProjectColor = value }
    var globalNoteColor: Int get() = NoteDataManager.globalNoteColor; set(value) { NoteDataManager.globalNoteColor = value }
    var globalFinanceColor: Int get() = FinanceDataManager.globalFinanceColor; set(value) { FinanceDataManager.globalFinanceColor = value }

    var habitAddThemeColor: Int get() = HabitDataManager.habitAddThemeColor; set(value) { HabitDataManager.habitAddThemeColor = value }
    var workoutAddThemeColor: Int get() = WorkoutDataManager.workoutAddThemeColor; set(value) { WorkoutDataManager.workoutAddThemeColor = value }
    var taskAddThemeColor: Int get() = TaskDataManager.taskAddThemeColor; set(value) { TaskDataManager.taskAddThemeColor = value }
    var noteAddThemeColor: Int get() = NoteDataManager.noteAddThemeColor; set(value) { NoteDataManager.noteAddThemeColor = value }
    var projectAddThemeColor: Int get() = ProjectDataManager.projectAddThemeColor; set(value) { ProjectDataManager.projectAddThemeColor = value }
    var financeAddThemeColor: Int get() = FinanceDataManager.financeAddThemeColor; set(value) { FinanceDataManager.financeAddThemeColor = value }

    var globalHabitIcon: Int get() = HabitDataManager.globalHabitIcon; set(value) { HabitDataManager.globalHabitIcon = value }
    var globalWorkoutIcon: Int get() = WorkoutDataManager.globalWorkoutIcon; set(value) { WorkoutDataManager.globalWorkoutIcon = value }
    var globalTaskIcon: Int get() = TaskDataManager.globalTaskIcon; set(value) { TaskDataManager.globalTaskIcon = value }
    var globalProjectIcon: Int get() = ProjectDataManager.globalProjectIcon; set(value) { ProjectDataManager.globalProjectIcon = value }
    var globalNoteIcon: Int get() = NoteDataManager.globalNoteIcon; set(value) { NoteDataManager.globalNoteIcon = value }
    var globalFinanceIcon: Int get() = FinanceDataManager.globalFinanceIcon; set(value) { FinanceDataManager.globalFinanceIcon = value }

    var projectTemplates: MutableMap<String, List<String>> get() = ProjectDataManager.projectTemplates; set(value) { ProjectDataManager.projectTemplates = value }
    var noteTemplates: MutableMap<String, String> get() = NoteDataManager.noteTemplates; set(value) { NoteDataManager.noteTemplates = value }

    // Logic Delegation
    fun addActivity(activity: String) = UserDataManager.addActivity(activity)
    
    fun addXP(context: Context, amount: Int): Boolean {
        val leveledUp = UserDataManager.addXP(amount)
        saveData(context)
        return leveledUp
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }

    fun getAiChatRepository() = aiChatRepo

    fun getHabitProgress() = HabitDataManager.getHabitProgress()
    fun getTotalHabitsFinished() = HabitDataManager.getTotalHabitsFinished()
    
    fun getWorkoutProgress() = WorkoutDataManager.getWorkoutProgress()
    fun getWorkoutStreaks() = WorkoutDataManager.getWorkoutStreaks()
    fun getWorkoutsThisMonth() = WorkoutDataManager.getWorkoutsThisMonth()
    fun getTotalWorkoutsFinished() = WorkoutDataManager.getTotalWorkoutsFinished()
    
    suspend fun getCurrentMonthExpenditure(): Double {
        val (start, end) = getCurrentMonthRange()
        return financeRepo?.getSumByTypeInRange("Expense", start, end) ?: 0.0
    }
    
    suspend fun getCurrentMonthIncome(): Double {
        val (start, end) = getCurrentMonthRange()
        return financeRepo?.getSumByTypeInRange("Income", start, end) ?: 0.0
    }
    
    suspend fun getCurrentMonthSavings(): Double {
        val (start, end) = getCurrentMonthRange()
        return financeRepo?.getSumByTypeInRange("Saving", start, end) ?: 0.0
    }

    fun getTotalDailyProgress(): Int {
        val hp = getHabitProgress()
        val wp = getWorkoutProgress()
        return if ((hp == 0 && wp == 0)) 0 else (hp + wp) / 2
    }

    fun getGrowthAdvice(mood: String?): String {
        return when (mood) {
            "Happy" -> "You're in a great state! Use this energy to tackle your biggest goals."
            "Stressed" -> "Take a deep breath. Focus on one small task at a time."
            "Tired" -> "Rest is productive too. Consider a shorter workout today."
            else -> "Consistency is the key to long-term growth. Keep showing up!"
        }
    }

    fun getManagementAdvice(mood: String?): String {
        return "Organize your workflow and prioritize your high-impact tasks."
    }

    suspend fun getComprehensiveTodayAgenda(context: Context): Map<String, List<AgendaItem>> = withContext(Dispatchers.IO) {
        val list = mutableListOf<AgendaItem>()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86400000

        // 1. Global Tasks
        synchronized(tasks) {
            tasks.forEach { task ->
                task.reminderTime?.let { time ->
                    if (!task.isCompleted && time in todayStart until todayEnd) {
                        list.add(AgendaItem(
                            id = task.name,
                            title = task.name,
                            time = time,
                            category = "TASKS",
                            priority = when(task.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                            navigationTarget = "TASK_ACTIVITY",
                            color = if (task.color != -1) task.color else globalTaskColor
                        ))
                    }
                }
            }
        }

        // 2. Global Projects & Subfeatures
        synchronized(projects) {
            projects.forEach { project ->
                val projColor = if (project.color != -1) project.color else globalProjectColor
                
                project.deadline?.let { deadline ->
                    if (project.status != "Completed" && deadline in todayStart until todayEnd) {
                        list.add(AgendaItem(
                            id = project.timestamp.toString(),
                            title = project.title,
                            time = deadline,
                            category = "PROJECTS",
                            priority = when(project.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                            navigationTarget = "PROJECT_ACTIVITY",
                            color = projColor
                        ))
                    }
                }
                
                project.subFeatures.forEach { f ->
                    f.dueDate?.let { dueDate ->
                        if (!f.isCompleted && dueDate in todayStart until todayEnd) {
                            list.add(AgendaItem(
                                id = f.id,
                                parentId = project.timestamp.toString(),
                                title = f.name,
                                time = dueDate,
                                category = "SUBFEATURES",
                                priority = when(f.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" },
                                navigationTarget = "PROJECT_ACTIVITY",
                                color = projColor
                            ))
                        }
                    }
                }
            }
        }

        // 4. Workspaces (Room)
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.workspaceDao()
            
            dao.getProjectsDueBetween(todayStart, todayEnd).forEach {
                it.deadline?.let { deadline ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.name, 
                        time = deadline, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = if (it.color != -1) it.color else globalProjectColor
                    ))
                }
            }
            
            dao.getGoalsDueBetween(todayStart, todayEnd).forEach {
                it.deadline?.let { deadline ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.title, 
                        time = deadline, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = if (it.color != -1) it.color else globalProjectColor
                    ))
                }
            }
            
            dao.getTasksDueBetween(todayStart, todayEnd).forEach {
                it.dueDate?.let { dueDate ->
                    list.add(AgendaItem(
                        id = it.id, 
                        title = it.title, 
                        time = dueDate, 
                        category = "WORKSPACES", 
                        navigationTarget = "WORKSPACE",
                        color = globalTaskColor
                    ))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // Group and Sort by time
        if (list.isEmpty()) return@withContext emptyMap<String, List<AgendaItem>>()
        
        list.groupBy { it.category }.mapValues { (_, items) -> 
            items.sortedBy { it.time } 
        }.toList().sortedBy { (_, items) -> items.firstOrNull()?.time ?: 0L }.toMap()
    }

    fun getTodayAgendaNotifications(): Map<String, List<AgendaItem>> {
        return workspaceTodayAgenda
    }

    fun getTrackingDateString(timestamp: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
    }

    fun getTrackingCalendar(timestamp: Long = System.currentTimeMillis()): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal
    }

    fun getUniqueFeatureName(baseName: String, existing: List<ProjectFeature>): String {
        var name = baseName
        var count = 2
        while (existing.any { it.name.equals(name, ignoreCase = true) }) {
            name = "$baseName $count"
            count++
        }
        return name
    }

    // Original Persistence Logic (Keep here for central Save/Load)
    private const val PREFS_NAME = "all_in_one_prefs"
    private const val KEY_HABITS = "habits_data"
    private const val KEY_WORKOUTS = "workouts_data"
    private const val KEY_TASKS = "tasks_data"
    private const val KEY_NOTES = "notes_data"
    private const val KEY_PROJECTS = "projects_data"
    private const val KEY_TRANSACTIONS = "transactions_data"
    private const val KEY_LEDGER = "ledger_data"
    private const val KEY_PERSONAL_LEDGER = "personal_ledger_data"
    private const val KEY_BUDGET = "monthly_budget"
    private const val KEY_SAVINGS_GOAL = "monthly_savings_goal"
    private const val KEY_SAVINGS_GOAL_NAME = "savings_goal_name"
    private const val KEY_MONTHLY_BUDGETS = "monthly_budgets_data"
    private const val KEY_MONTHLY_SAVINGS_GOALS = "monthly_savings_goals_data"
    private const val KEY_HISTORY = "history_data"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"
    private const val KEY_LAST_MONTH_RESET = "last_month_reset"
    private const val KEY_WORKOUT_FILTER_TYPE = "workout_filter_type"
    private const val KEY_WORKOUT_MUSCLE_GROUPS = "workout_muscle_groups"
    private const val KEY_TASK_SHOW_COMPLETED = "task_show_completed"
    private const val KEY_TASK_SHOW_HIDDEN = "task_show_hidden"
    private const val KEY_TASK_SORT_ORDER = "task_sort_order"
    private const val KEY_TASK_CUSTOM_CATEGORIES = "task_custom_categories"
    private const val KEY_TASK_AUTO_ARCHIVE = "task_auto_archive"
    private const val KEY_TASK_EDIT_MODE = "task_edit_mode_enabled"
    private const val KEY_TASK_DEFAULT_SECTION = "task_default_section"
    private const val KEY_TASK_VISIBLE_SECTIONS = "task_visible_sections"
    private const val KEY_FINANCE_CUSTOM_CATEGORIES = "finance_custom_categories"
    private const val KEY_FINANCE_CATEGORY_ICONS = "finance_category_icons"
    private const val KEY_FINANCE_CATEGORY_COLORS = "finance_category_colors"
    private const val KEY_FINANCE_CURRENCY = "finance_currency"
    private const val KEY_FINANCE_GRAPH_START_MONTH = "finance_graph_start_month"
    private const val KEY_FINANCE_GRAPH_COLOR = "finance_graph_color"
    private const val KEY_FINANCE_GRAPH_SAVINGS_COLOR = "finance_graph_savings_color"
    private const val KEY_FINANCE_LEDGER_ENABLED = "finance_ledger_enabled"
    private const val KEY_NOTE_AUTO_CLEANUP = "note_auto_cleanup"
    private const val KEY_NOTE_SHOW_HIDDEN = "note_show_hidden"
    private const val KEY_NOTE_VOICE_INPUT = "note_voice_input_enabled"
    private const val KEY_NOTE_VISIBLE_SECTIONS = "note_visible_sections"
    private const val KEY_NOTE_DEFAULT_CAT = "note_default_cat"
    private const val KEY_NOTE_TEMPLATES = "note_templates"
    private const val KEY_PROJ_ARCHIVE = "project_auto_archive"
    private const val KEY_PROJ_SYNC = "project_synergy_sync"
    private const val KEY_PROJ_ALERTS = "project_deadline_alerts"
    private const val KEY_PROJ_SORT_BOTTOM = "project_sort_completed_bottom"
    private const val KEY_PROJ_COMPLETED_EXP = "project_completed_expanded"
    private const val KEY_PROJ_AUTOSAVE_IDEAS = "project_autosave_ideas"
    private const val KEY_IDEA_ACTIVE_EXP = "idea_active_expanded"
    private const val KEY_IDEA_COMPLETED_EXP = "idea_completed_expanded"
    private const val KEY_PROJ_ANALYTICS = "project_analytics_enabled"
    private const val KEY_PROJ_TEMPLATES = "project_templates"
    private const val KEY_PROJ_ROADMAPS = "project_roadmaps_enabled"
    private const val KEY_HAS_NEW_TODAY_NOTIF = "has_new_today_notification"
    private const val KEY_APP_LOCK = "app_lock_enabled"
    private const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
    private const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection_enabled"
    private const val KEY_APP_LOCK_PIN = "app_lock_pin"
    private const val KEY_OLED_THEME = "oled_theme_enabled"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_RECENT_ACT = "recent_activities_data"
    private const val KEY_DAILY_MOODS = "daily_moods_data"
    private const val KEY_USER_NAME = "user_profile_name"
    private const val KEY_USER_BIO = "user_profile_bio"
    private const val KEY_USER_AVATAR = "user_profile_avatar"
    private const val KEY_USER_IMAGE_URI = "user_profile_image_uri"
    private const val KEY_CUSTOM_COLORS = "user_custom_colors_data"
    private const val KEY_PROJ_TAGS = "project_custom_tags_data"
    private const val KEY_LAST_VIEWED_NOTIF = "last_viewed_notification_date"
    private const val KEY_LAST_SUMMARY_NOTIF = "last_summary_notification_date"
    private const val KEY_PROJ_DUAL_EXIST = "project_dual_exist_enabled"
    private const val KEY_PROJ_IDEAS_ENABLED = "project_ideas_enabled"
    private const val KEY_LAST_MOOD_TIMESTAMP = "last_mood_timestamp"
    private const val KEY_DISPLAY_SIZE = "app_display_size"
    private const val KEY_HOME_DISPLAY_SIZE = "home_display_size"
    private const val KEY_FONT_SIZE = "app_font_size"
    private const val KEY_SYSTEM_APPEARANCE = "is_system_appearance_enabled"
    private const val KEY_USER_XP = "user_xp_data"
    private const val KEY_USER_LEVEL = "user_level_data"

    private const val KEY_SHOW_HABITS = "show_habit_section"
    private const val KEY_SHOW_WORKOUTS = "show_workout_section"
    private const val KEY_SHOW_TASKS = "show_task_section"
    private const val KEY_SHOW_NOTES = "show_note_section"
    private const val KEY_SHOW_PROJECTS = "show_project_section"
    private const val KEY_SHOW_FINANCE = "show_finance_section"
    private const val KEY_SHOW_PERFORMANCE = "show_performance_section"

    private const val KEY_GLOBAL_HABIT_COLOR = "global_habit_color"
    private const val KEY_GLOBAL_WORKOUT_COLOR = "global_workout_color"
    private const val KEY_GLOBAL_TASK_COLOR = "global_task_color"
    private const val KEY_GLOBAL_PROJECT_COLOR = "global_project_color"
    private const val KEY_GLOBAL_NOTE_COLOR = "global_note_color"
    private const val KEY_GLOBAL_FINANCE_COLOR = "global_finance_color"

    private const val KEY_HABIT_ADD_COLOR = "habit_add_theme_color"
    private const val KEY_WORKOUT_ADD_COLOR = "workout_add_theme_color"
    private const val KEY_TASK_ADD_COLOR = "task_add_theme_color"
    private const val KEY_NOTE_ADD_COLOR = "note_add_theme_color"
    private const val KEY_PROJECT_ADD_COLOR = "project_add_theme_color"
    private const val KEY_FINANCE_ADD_COLOR = "finance_add_theme_color"

    private const val KEY_GLOBAL_HABIT_ICON = "global_habit_icon"
    private const val KEY_GLOBAL_WORKOUT_ICON = "global_workout_icon"
    private const val KEY_GLOBAL_TASK_ICON = "global_task_icon"
    private const val KEY_GLOBAL_PROJECT_ICON = "global_project_icon"
    private const val KEY_GLOBAL_NOTE_ICON = "global_note_icon"
    private const val KEY_GLOBAL_FINANCE_ICON = "global_finance_icon"
    private const val KEY_STARTUP_LOADING_TIME = "startup_loading_time"

    private const val KEY_MORNING_REMINDER_ENABLED = "morning_reminder_enabled"
    private const val KEY_MORNING_REMINDER_TIME = "morning_reminder_time"
    private const val KEY_NIGHT_REMINDER_ENABLED = "night_reminder_enabled"
    private const val KEY_NIGHT_REMINDER_TIME = "night_reminder_time"

    private fun getPrefs(context: Context): SharedPreferences {
        return SecurityManager.getEncryptedPrefs(context)
    }

    private fun getLegacyPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun migrateToEncryptedPrefs(context: Context) {
        val legacyPrefs = getLegacyPrefs(context)
        val encryptedPrefs = getPrefs(context)
        
        if (legacyPrefs.all.isNotEmpty()) {
            val editor = encryptedPrefs.edit()
            legacyPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                }
            }
            editor.apply()
            legacyPrefs.edit().clear().apply() // Clear legacy data after migration
        }
    }

    // Shared state for sub-feature editing
    var currentEditingSubFeatures: MutableList<ProjectFeature> = mutableListOf()
    var currentEditingIdeaSubFeatures: MutableList<ProjectFeature> = mutableListOf()

    fun saveData(context: Context?, immediate: Boolean = false) {
        if (context == null) return
        val appContext = context.applicationContext
        
        if (immediate) {
            saveJob?.cancel()
            persistenceScope.launch {
                performSave(appContext)
            }
            return
        }

        saveJob?.cancel()
        saveJob = persistenceScope.launch {
            delay(500L) // Debounce for 500ms
            performSave(appContext)
        }
    }

    private suspend fun performSave(context: Context) = withContext(NonCancellable) {
        if (!isDataLoaded.value) return@withContext
        
        val tRepo = taskRepo ?: return@withContext
        val hRepo = habitRepo ?: return@withContext
        val wRepo = workoutRepo ?: return@withContext
        val nRepo = noteRepo ?: return@withContext
        val fRepo = financeRepo ?: return@withContext

        val prefs = getPrefs(context)
        val gson = Gson()
        
        // Save to Repositories (Room) using thread-safe snapshots
        val tList = synchronized(tasks) { tasks.toList() }
        val hList = synchronized(habits) { habits.toList() }
        val wList = synchronized(workouts) { workouts.toList() }
        val nList = synchronized(notes) { notes.toList() }
        val pList = synchronized(projects) { projects.toList() }
        val transList = synchronized(transactions) { transactions.toList() }
        val plList = synchronized(personalLedgers) { personalLedgers.toList() }
        val leList = synchronized(ledgerEntries) { ledgerEntries.toList() }

        try {
            if (tList.isNotEmpty()) tRepo.syncAll(tList)
            if (hList.isNotEmpty()) hRepo.syncAll(hList)
            if (wList.isNotEmpty()) wRepo.syncAll(wList)
            
            if (nList.isNotEmpty() || pList.isNotEmpty()) {
                val allNotesToSave = nList.map { it.copy(isGlobalProject = false) } + 
                                     pList.map { it.copy(isGlobalProject = true) }
                nRepo.syncAll(allNotesToSave)
            }
            
            if (transList.isNotEmpty()) fRepo.syncTransactions(transList)
            if (plList.isNotEmpty()) fRepo.syncPersonalLedgers(plList)
            if (leList.isNotEmpty()) fRepo.syncLedgerEntries(leList)
        } catch (e: Exception) { e.printStackTrace() }

        // Keep SharedPreferences only for settings and cross-domain history
        prefs.edit().apply {
            putString(KEY_SAVINGS_GOAL_NAME, financeSavingsGoalName)
            putFloat(KEY_BUDGET, monthlyBudget.toFloat())
            putFloat(KEY_SAVINGS_GOAL, monthlySavingsGoal.toFloat())
            putString(KEY_MONTHLY_BUDGETS, gson.toJson(monthlyBudgets))
            putString(KEY_MONTHLY_SAVINGS_GOALS, gson.toJson(monthlySavingsGoals))
            putString(KEY_HISTORY, gson.toJson(history))
            putBoolean(KEY_TASK_SHOW_COMPLETED, taskShowCompleted)
            putBoolean(KEY_TASK_SHOW_HIDDEN, taskShowHidden)
            putString(KEY_TASK_SORT_ORDER, taskSortOrder)
            putString(KEY_TASK_CUSTOM_CATEGORIES, gson.toJson(taskCustomCategories))
            putBoolean(KEY_TASK_AUTO_ARCHIVE, taskAutoArchive)
            putBoolean(KEY_TASK_EDIT_MODE, taskEditModeEnabled)
            putString(KEY_WORKOUT_FILTER_TYPE, workoutFilterType)
            putString(KEY_WORKOUT_MUSCLE_GROUPS, gson.toJson(workoutMuscleGroups))
            putString(KEY_TASK_DEFAULT_SECTION, taskDefaultSection)
            putString(KEY_TASK_VISIBLE_SECTIONS, gson.toJson(taskVisibleSections))
            putString(KEY_FINANCE_CUSTOM_CATEGORIES, gson.toJson(financeCustomCategories))
            putString(KEY_FINANCE_CATEGORY_ICONS, gson.toJson(financeCategoryIcons))
            putString(KEY_FINANCE_CATEGORY_COLORS, gson.toJson(financeCategoryColors))
            putString(KEY_FINANCE_CURRENCY, financeCurrency)
            putInt(KEY_FINANCE_GRAPH_START_MONTH, financeGraphStartMonth)
            putInt(KEY_FINANCE_GRAPH_COLOR, financeGraphColor)
            putInt(KEY_FINANCE_GRAPH_SAVINGS_COLOR, financeGraphSavingsColor)
            putBoolean(KEY_FINANCE_LEDGER_ENABLED, isFinanceLedgerEnabled)
            putInt(KEY_NOTE_AUTO_CLEANUP, noteAutoCleanupDays)
            putBoolean(KEY_NOTE_SHOW_HIDDEN, noteShowHidden)
            putBoolean(KEY_NOTE_VOICE_INPUT, noteVoiceInputEnabled)
            putString(KEY_NOTE_VISIBLE_SECTIONS, gson.toJson(noteVisibleSections))
            putString(KEY_NOTE_DEFAULT_CAT, noteDefaultCategory)
            putString(KEY_NOTE_TEMPLATES, gson.toJson(noteTemplates))
            putBoolean(KEY_PROJ_ARCHIVE, projectAutoArchive)
            putBoolean(KEY_PROJ_SYNC, projectSynergySync)
            putBoolean(KEY_PROJ_ALERTS, projectDeadlineAlerts)
            putBoolean(KEY_PROJ_SORT_BOTTOM, projectSortCompletedToBottom)
            putBoolean("project_active_expanded", projectActiveExpanded)
            putBoolean(KEY_PROJ_COMPLETED_EXP, projectCompletedExpanded)
            putBoolean(KEY_IDEA_ACTIVE_EXP, ideaActiveExpanded)
            putBoolean(KEY_IDEA_COMPLETED_EXP, ideaCompletedExpanded)
            putBoolean(KEY_PROJ_AUTOSAVE_IDEAS, projectAutoSaveIdeas)
            putBoolean(KEY_PROJ_ANALYTICS, projectAnalyticsEnabled)
            putBoolean(KEY_PROJ_DUAL_EXIST, projectDualExistEnabled)
            putBoolean(KEY_PROJ_IDEAS_ENABLED, projectIdeasEnabled)
            putBoolean(KEY_PROJ_ROADMAPS, projectRoadmapsEnabled)
            putBoolean(KEY_APP_LOCK, isAppLockEnabled)
            putBoolean(KEY_BIOMETRIC_LOCK, isBiometricLockEnabled)
            putBoolean(KEY_SCREENSHOT_PROTECTION, isScreenshotProtectionEnabled)
            putString(KEY_APP_LOCK_PIN, appLockPin)
            putBoolean(KEY_ONBOARDING_COMPLETED, isOnboardingCompleted)
            putString(KEY_RECENT_ACT, gson.toJson(recentActivities))
            putString(KEY_DAILY_MOODS, gson.toJson(dailyMoods))
            putLong(KEY_LAST_MOOD_TIMESTAMP, lastMoodTimestamp)
            putString(KEY_DISPLAY_SIZE, displaySize)
            putString("home_focus_size", homeFocusSize)
            putString(KEY_HOME_DISPLAY_SIZE, homeDisplaySize)
            putString(KEY_FONT_SIZE, fontSize)
            putBoolean(KEY_SYSTEM_APPEARANCE, isSystemAppearanceEnabled)
            putString("app_theme_mode", appThemeMode)
            putInt("app_accent_color", appAccentColor)
            putString("app_font_family", appFontFamily)
            putInt("app_border_radius", appBorderRadius)
            putString("app_card_style", appCardStyle)
            putBoolean("app_show_shadows", appShowShadows)
            putBoolean("is_dynamic_color_enabled", isDynamicColorEnabled)
            putInt(KEY_STARTUP_LOADING_TIME, startupLoadingTime)
            putInt(KEY_USER_XP, userXP)
            putInt(KEY_USER_LEVEL, userLevel)

            putBoolean(KEY_MORNING_REMINDER_ENABLED, isMorningReminderEnabled)
            putString(KEY_MORNING_REMINDER_TIME, morningReminderTime)
            putBoolean(KEY_NIGHT_REMINDER_ENABLED, isNightReminderEnabled)
            putString(KEY_NIGHT_REMINDER_TIME, nightReminderTime)

            putString(KEY_LAST_VIEWED_NOTIF, lastViewedNotificationDate)
            putString(KEY_LAST_SUMMARY_NOTIF, lastSummaryNotificationDate)
            putBoolean(KEY_HAS_NEW_TODAY_NOTIF, hasNewTodayNotifications)

            putBoolean(KEY_SHOW_HABITS, showHabitSection)
            putBoolean(KEY_SHOW_WORKOUTS, showWorkoutSection)
            putBoolean(KEY_SHOW_TASKS, showTaskSection)
            putBoolean(KEY_SHOW_NOTES, showNoteSection)
            putBoolean(KEY_SHOW_PROJECTS, showProjectSection)
            putBoolean(KEY_SHOW_FINANCE, showFinanceSection)
            putBoolean(KEY_SHOW_PERFORMANCE, showPerformanceSection)

            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_BIO, userBio)
            putString(KEY_USER_AVATAR, getResourceName(context, userAvatarRes))
            putString(KEY_USER_IMAGE_URI, userProfileImageUri)
            putString(KEY_CUSTOM_COLORS, gson.toJson(userCustomColors))
            putString(KEY_PROJ_TAGS, gson.toJson(projectCustomTags))

            putInt(KEY_GLOBAL_HABIT_COLOR, globalHabitColor)
            putInt(KEY_GLOBAL_WORKOUT_COLOR, globalWorkoutColor)
            putInt(KEY_GLOBAL_TASK_COLOR, globalTaskColor)
            putInt(KEY_GLOBAL_PROJECT_COLOR, globalProjectColor)
            putInt(KEY_GLOBAL_NOTE_COLOR, globalNoteColor)
            putInt(KEY_GLOBAL_FINANCE_COLOR, globalFinanceColor)

            putInt(KEY_HABIT_ADD_COLOR, habitAddThemeColor)
            putInt(KEY_WORKOUT_ADD_COLOR, workoutAddThemeColor)
            putInt(KEY_TASK_ADD_COLOR, taskAddThemeColor)
            putInt(KEY_NOTE_ADD_COLOR, noteAddThemeColor)
            putInt(KEY_PROJECT_ADD_COLOR, projectAddThemeColor)
            putInt(KEY_FINANCE_ADD_COLOR, financeAddThemeColor)

            putString(KEY_GLOBAL_HABIT_ICON, getResourceName(context, globalHabitIcon))
            putString(KEY_GLOBAL_WORKOUT_ICON, getResourceName(context, globalWorkoutIcon))
            putString(KEY_GLOBAL_TASK_ICON, getResourceName(context, globalTaskIcon))
            putString(KEY_GLOBAL_PROJECT_ICON, getResourceName(context, globalProjectIcon))
            putString(KEY_GLOBAL_NOTE_ICON, getResourceName(context, globalNoteIcon))
            putString(KEY_GLOBAL_FINANCE_ICON, getResourceName(context, globalFinanceIcon))

            putString(KEY_PROJ_TEMPLATES, gson.toJson(projectTemplates))
            apply()
        }
    }

    fun loadTasksOnly(context: Context) {
        try {
            val prefs = getPrefs(context)
            val gson = Gson()
            tasks = try {
                gson.fromJson(prefs.getString(KEY_TASKS, "[]"), object : TypeToken<MutableList<Task>>() {}.type) ?: mutableListOf()
            } catch (e: Exception) { mutableListOf() }
            tasks.forEach { if (it.subtasks == null) it.subtasks = mutableListOf() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Load logic remains complex and central for now
    suspend fun loadData(context: Context) {
        try {
            migrateToEncryptedPrefs(context)
            val prefs = getPrefs(context)
            val gson = Gson()
            
            val hasMigrated = prefs.getBoolean("data_migrated_to_sql", false)

            if (!hasMigrated) {
                // Legacy load (only if not migrated yet)
                try {
                    val h: List<Habit> = gson.fromJson(prefs.getString(KEY_HABITS, "[]"), object : TypeToken<List<Habit>>() {}.type) ?: emptyList()
                    synchronized(habits) { habits.clear(); habits.addAll(h) }
                } catch (e: Exception) { e.printStackTrace() }
                
                try {
                    val w: List<Workout> = gson.fromJson(prefs.getString(KEY_WORKOUTS, "[]"), object : TypeToken<List<Workout>>() {}.type) ?: emptyList()
                    synchronized(workouts) { workouts.clear(); workouts.addAll(w) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val t: List<Task> = gson.fromJson(prefs.getString(KEY_TASKS, "[]"), object : TypeToken<List<Task>>() {}.type) ?: emptyList()
                    synchronized(tasks) { tasks.clear(); tasks.addAll(t) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val n: List<Note> = gson.fromJson(prefs.getString(KEY_NOTES, "[]"), object : TypeToken<List<Note>>() {}.type) ?: emptyList()
                    synchronized(notes) { notes.clear(); notes.addAll(n) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val p: List<Note> = gson.fromJson(prefs.getString(KEY_PROJECTS, "[]"), object : TypeToken<List<Note>>() {}.type) ?: emptyList()
                    synchronized(projects) { projects.clear(); projects.addAll(p) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val tr: List<Transaction> = gson.fromJson(prefs.getString(KEY_TRANSACTIONS, "[]"), object : TypeToken<List<Transaction>>() {}.type) ?: emptyList()
                    synchronized(transactions) { transactions.clear(); transactions.addAll(tr) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val le: List<LedgerEntry> = gson.fromJson(prefs.getString(KEY_LEDGER, "[]"), object : TypeToken<List<LedgerEntry>>() {}.type) ?: emptyList()
                    synchronized(ledgerEntries) { ledgerEntries.clear(); ledgerEntries.addAll(le) }
                } catch (e: Exception) { e.printStackTrace() }

                try {
                    val pl: List<PersonalLedger> = gson.fromJson(prefs.getString(KEY_PERSONAL_LEDGER, "[]"), object : TypeToken<List<PersonalLedger>>() {}.type) ?: emptyList()
                    synchronized(personalLedgers) { personalLedgers.clear(); personalLedgers.addAll(pl) }
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                // Initial load from Room to avoid empty in-memory lists before observation starts
                habitRepo?.getAllHabits()?.first()?.let { 
                    synchronized(habits) { habits.clear(); habits.addAll(it) } 
                }
                workoutRepo?.getAllWorkouts()?.first()?.let { 
                    synchronized(workouts) { workouts.clear(); workouts.addAll(it) } 
                }
                taskRepo?.getAllTasks()?.first()?.let { 
                    synchronized(tasks) { tasks.clear(); tasks.addAll(it) } 
                }
                noteRepo?.getAllNotes()?.first()?.let { n ->
                    val newNotes = n.filter { !it.isGlobalProject }
                    val newProjects = n.filter { it.isGlobalProject }
                    synchronized(notes) { notes.clear(); notes.addAll(newNotes) }
                    synchronized(projects) { projects.clear(); projects.addAll(newProjects) }
                }
                financeRepo?.getAllTransactions()?.first()?.let { 
                    synchronized(transactions) { transactions.clear(); transactions.addAll(it) } 
                }
                financeRepo?.getAllPersonalLedgers()?.first()?.let { 
                    synchronized(personalLedgers) { personalLedgers.clear(); personalLedgers.addAll(it) } 
                }
                financeRepo?.getAllLedgerEntries()?.first()?.let { 
                    synchronized(ledgerEntries) { ledgerEntries.clear(); ledgerEntries.addAll(it) } 
                }
            }
            
            // Ensure new fields are initialized for older data
            habits.forEach { 
                if (it.completedDates == null) it.completedDates = mutableListOf()
                if (it.dailyProgress == null) it.dailyProgress = mutableMapOf()
                if (it.repeatDays == null) it.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
                if (it.iconResId != -1 && !UIUtils.isDrawableResource(context, it.iconResId)) it.iconResId = -1
            }

            // Ensure new fields are initialized for older data
            workouts.forEach { 
                if (it.completedDates == null) it.completedDates = mutableListOf()
                if (it.dailyProgress == null) it.dailyProgress = mutableMapOf()
                if (it.repeatDays == null) it.repeatDays = listOf(0, 1, 2, 3, 4, 5, 6)
                if (it.muscleGroups == null) it.muscleGroups = listOf("General")
                if (it.iconResId != -1 && !UIUtils.isDrawableResource(context, it.iconResId)) it.iconResId = -1
            }

            // Ensure new fields are initialized for older data
            tasks.forEach {
                if (it.subtasks == null) {
                    try {
                        val field = it.javaClass.getDeclaredField("subtasks")
                        field.isAccessible = true
                        field.set(it, mutableListOf<Subtask>())
                    } catch (e: Exception) {}
                }
            }

            projects.forEach { it.isGlobalProject = true }

            monthlyBudget = prefs.getFloat(KEY_BUDGET, 0.0f).toDouble()
            monthlySavingsGoal = prefs.getFloat(KEY_SAVINGS_GOAL, 0.0f).toDouble()
            financeSavingsGoalName = prefs.getString(KEY_SAVINGS_GOAL_NAME, "Monthly Savings") ?: "Monthly Savings"
            
            monthlyBudgets = try {
                gson.fromJson(prefs.getString(KEY_MONTHLY_BUDGETS, "{}"), object : TypeToken<MutableMap<String, Double>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            monthlySavingsGoals = try {
                gson.fromJson(prefs.getString(KEY_MONTHLY_SAVINGS_GOALS, "{}"), object : TypeToken<MutableMap<String, Double>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }
            
            history = try {
                gson.fromJson(prefs.getString(KEY_HISTORY, "{}"), object : TypeToken<MutableMap<String, DayHistory>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }
            
            taskShowCompleted = prefs.getBoolean(KEY_TASK_SHOW_COMPLETED, true)
            taskShowHidden = prefs.getBoolean(KEY_TASK_SHOW_HIDDEN, false)
            taskSortOrder = prefs.getString(KEY_TASK_SORT_ORDER, "Priority") ?: "Priority"
            taskCustomCategories = try {
                gson.fromJson(prefs.getString(KEY_TASK_CUSTOM_CATEGORIES, "[\"General\", \"Personal\", \"Work\", \"Shopping\"]"), object : TypeToken<MutableList<String>>() {}.type)
            } catch (e: Exception) { mutableListOf("General", "Personal", "Work", "Shopping") }

            taskAutoArchive = prefs.getBoolean(KEY_TASK_AUTO_ARCHIVE, false)
            taskEditModeEnabled = prefs.getBoolean(KEY_TASK_EDIT_MODE, false)
            taskDefaultSection = prefs.getString(KEY_TASK_DEFAULT_SECTION, "Tasks") ?: "Tasks"
            taskVisibleSections = try {
                gson.fromJson(prefs.getString(KEY_TASK_VISIBLE_SECTIONS, "[\"Tasks\"]"), object : TypeToken<MutableList<String>>() {}.type)
            } catch (e: Exception) { mutableListOf("Tasks") }

            workoutFilterType = prefs.getString(KEY_WORKOUT_FILTER_TYPE, "TIME") ?: "TIME"
            workoutMuscleGroups = try {
                gson.fromJson(prefs.getString(KEY_WORKOUT_MUSCLE_GROUPS, "[\"Chest\", \"Back\", \"Legs\", \"Shoulders\", \"Arms\", \"Cardio\", \"Full Body\"]"), object : TypeToken<MutableList<String>>() {}.type) ?: mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body")
            } catch (e: Exception) { mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body") }

            financeCustomCategories = try {
                gson.fromJson(prefs.getString(KEY_FINANCE_CUSTOM_CATEGORIES, "[\"Food\", \"Rent\", \"Transport\", \"Shopping\", \"Entertainment\", \"Health\", \"Other\"]"), object : TypeToken<MutableList<String>>() {}.type)
            } catch (e: Exception) { mutableListOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other") }

            financeCategoryIcons = try {
                gson.fromJson(prefs.getString(KEY_FINANCE_CATEGORY_ICONS, "{}"), object : TypeToken<MutableMap<String, Int>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            financeCategoryColors = try {
                gson.fromJson(prefs.getString(KEY_FINANCE_CATEGORY_COLORS, "{}"), object : TypeToken<MutableMap<String, Int>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            financeCurrency = prefs.getString(KEY_FINANCE_CURRENCY, "₹") ?: "₹"
            financeGraphStartMonth = prefs.getInt(KEY_FINANCE_GRAPH_START_MONTH, 0)
            financeGraphColor = prefs.getInt(KEY_FINANCE_GRAPH_COLOR, -1)
            financeGraphSavingsColor = prefs.getInt(KEY_FINANCE_GRAPH_SAVINGS_COLOR, -1)
            isFinanceLedgerEnabled = prefs.getBoolean(KEY_FINANCE_LEDGER_ENABLED, true)

            noteAutoCleanupDays = prefs.getInt(KEY_NOTE_AUTO_CLEANUP, 0)
            noteShowHidden = prefs.getBoolean(KEY_NOTE_SHOW_HIDDEN, false)
            noteVoiceInputEnabled = prefs.getBoolean(KEY_NOTE_VOICE_INPUT, true)
            noteVisibleSections = try {
                gson.fromJson(prefs.getString(KEY_NOTE_VISIBLE_SECTIONS, "[\"Notes\"]"), object : TypeToken<MutableList<String>>() {}.type) ?: mutableListOf("Notes")
            } catch (e: Exception) { mutableListOf("Notes") }

            noteDefaultCategory = prefs.getString(KEY_NOTE_DEFAULT_CAT, "Notes") ?: "Notes"
            noteTemplates = try {
                gson.fromJson(prefs.getString(KEY_NOTE_TEMPLATES, "{}"), object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            projectAutoArchive = prefs.getBoolean(KEY_PROJ_ARCHIVE, false)
            projectSynergySync = prefs.getBoolean(KEY_PROJ_SYNC, false)
            projectDeadlineAlerts = prefs.getBoolean(KEY_PROJ_ALERTS, true)
            projectSortCompletedToBottom = prefs.getBoolean(KEY_PROJ_SORT_BOTTOM, true)
            projectActiveExpanded = prefs.getBoolean("project_active_expanded", true)
            projectCompletedExpanded = prefs.getBoolean(KEY_PROJ_COMPLETED_EXP, false)
            ideaActiveExpanded = prefs.getBoolean(KEY_IDEA_ACTIVE_EXP, true)
            ideaCompletedExpanded = prefs.getBoolean(KEY_IDEA_COMPLETED_EXP, false)
            projectAutoSaveIdeas = prefs.getBoolean(KEY_PROJ_AUTOSAVE_IDEAS, true)
            projectAnalyticsEnabled = prefs.getBoolean(KEY_PROJ_ANALYTICS, false)
            projectDualExistEnabled = prefs.getBoolean(KEY_PROJ_DUAL_EXIST, false)
            projectIdeasEnabled = prefs.getBoolean(KEY_PROJ_IDEAS_ENABLED, true)
            projectRoadmapsEnabled = prefs.getBoolean(KEY_PROJ_ROADMAPS, true)

            isAppLockEnabled = prefs.getBoolean(KEY_APP_LOCK, false)
            isBiometricLockEnabled = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
            isScreenshotProtectionEnabled = prefs.getBoolean(KEY_SCREENSHOT_PROTECTION, false)
            appLockPin = prefs.getString(KEY_APP_LOCK_PIN, null)
            appLockQuestion = prefs.getString("app_lock_question", null)
            appLockAnswer = prefs.getString("app_lock_answer", null)
            isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

            recentActivities = try {
                gson.fromJson(prefs.getString(KEY_RECENT_ACT, "[]"), object : TypeToken<MutableList<String>>() {}.type) ?: mutableListOf()
            } catch (e: Exception) { mutableListOf() }

            dailyMoods = try {
                gson.fromJson(prefs.getString(KEY_DAILY_MOODS, "{}"), object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            lastMoodTimestamp = prefs.getLong(KEY_LAST_MOOD_TIMESTAMP, 0L)
            displaySize = prefs.getString(KEY_DISPLAY_SIZE, "S") ?: "S"
            homeFocusSize = prefs.getString("home_focus_size", "M") ?: "M"
            homeDisplaySize = prefs.getString(KEY_HOME_DISPLAY_SIZE, "S") ?: "S"
            fontSize = prefs.getString(KEY_FONT_SIZE, "S") ?: "S"
            isSystemAppearanceEnabled = prefs.getBoolean(KEY_SYSTEM_APPEARANCE, true)
            appThemeMode = prefs.getString("app_theme_mode", "DARK") ?: "DARK"
            appAccentColor = prefs.getInt("app_accent_color", -1)
            appFontFamily = prefs.getString("app_font_family", "DEFAULT") ?: "DEFAULT"
            appBorderRadius = prefs.getInt("app_border_radius", 16)
            appCardStyle = prefs.getString("app_card_style", "GLASS") ?: "GLASS"
            appShowShadows = prefs.getBoolean("app_show_shadows", true)
            isDynamicColorEnabled = prefs.getBoolean("is_dynamic_color_enabled", false)
            startupLoadingTime = prefs.getInt(KEY_STARTUP_LOADING_TIME, 2000)
            userXP = prefs.getInt(KEY_USER_XP, 0)
            userLevel = prefs.getInt(KEY_USER_LEVEL, 1)

            isMorningReminderEnabled = prefs.getBoolean(KEY_MORNING_REMINDER_ENABLED, false)
            morningReminderTime = prefs.getString(KEY_MORNING_REMINDER_TIME, "08:00") ?: "08:00"
            isNightReminderEnabled = prefs.getBoolean(KEY_NIGHT_REMINDER_ENABLED, false)
            nightReminderTime = prefs.getString(KEY_NIGHT_REMINDER_TIME, "21:00") ?: "21:00"

            lastViewedNotificationDate = prefs.getString(KEY_LAST_VIEWED_NOTIF, "") ?: ""
            lastSummaryNotificationDate = prefs.getString(KEY_LAST_SUMMARY_NOTIF, "") ?: ""
            hasNewTodayNotifications = prefs.getBoolean(KEY_HAS_NEW_TODAY_NOTIF, false)

            showHabitSection = prefs.getBoolean(KEY_SHOW_HABITS, true)
            showWorkoutSection = prefs.getBoolean(KEY_SHOW_WORKOUTS, true)
            showTaskSection = prefs.getBoolean(KEY_SHOW_TASKS, true)
            showNoteSection = prefs.getBoolean(KEY_SHOW_NOTES, true)
            showProjectSection = prefs.getBoolean(KEY_SHOW_PROJECTS, true)
            showFinanceSection = prefs.getBoolean(KEY_SHOW_FINANCE, true)
            showPerformanceSection = prefs.getBoolean(KEY_SHOW_PERFORMANCE, true)

            userName = prefs.getString(KEY_USER_NAME, "User") ?: "User"
            userBio = prefs.getString(KEY_USER_BIO, "") ?: ""
            userAvatarRes = getResourceId(context, prefs.getString(KEY_USER_AVATAR, "boy_avatar_profile") ?: "boy_avatar_profile", R.drawable.ic_launcher_foreground)
            userProfileImageUri = prefs.getString(KEY_USER_IMAGE_URI, null)
            userCustomColors = try {
                gson.fromJson(prefs.getString(KEY_CUSTOM_COLORS, "[]"), object : TypeToken<MutableList<Int>>() {}.type) ?: mutableListOf()
            } catch (e: Exception) { mutableListOf() }

            projectCustomTags = try {
                gson.fromJson(prefs.getString(KEY_PROJ_TAGS, "[\"TASKS\", \"NOTES\", \"FEATURES\", \"BUGS\", \"RESOURCES\"]"), object : TypeToken<MutableList<String>>() {}.type)
            } catch (e: Exception) { mutableListOf("TASKS", "NOTES", "FEATURES", "BUGS", "RESOURCES") }
            
            globalHabitColor = prefs.getInt(KEY_GLOBAL_HABIT_COLOR, -1)
            globalWorkoutColor = prefs.getInt(KEY_GLOBAL_WORKOUT_COLOR, -1)
            globalTaskColor = prefs.getInt(KEY_GLOBAL_TASK_COLOR, -1)
            globalProjectColor = prefs.getInt(KEY_GLOBAL_PROJECT_COLOR, -1)
            globalNoteColor = prefs.getInt(KEY_GLOBAL_NOTE_COLOR, -1)
            globalFinanceColor = prefs.getInt(KEY_GLOBAL_FINANCE_COLOR, -1)

            habitAddThemeColor = prefs.getInt(KEY_HABIT_ADD_COLOR, -1)
            workoutAddThemeColor = prefs.getInt(KEY_WORKOUT_ADD_COLOR, -1)
            taskAddThemeColor = prefs.getInt(KEY_TASK_ADD_COLOR, -1)
            noteAddThemeColor = prefs.getInt(KEY_NOTE_ADD_COLOR, -1)
            projectAddThemeColor = prefs.getInt(KEY_PROJECT_ADD_COLOR, -1)
            financeAddThemeColor = prefs.getInt(KEY_FINANCE_ADD_COLOR, -1)

            globalHabitIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_HABIT_ICON, "ic_habit_tracker") ?: "ic_habit_tracker", R.drawable.ic_habit_tracker)
            globalWorkoutIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_WORKOUT_ICON, "ic_workout_routine") ?: "ic_workout_routine", R.drawable.ic_workout_routine)
            globalTaskIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_TASK_ICON, "ic_task") ?: "ic_task", R.drawable.ic_task)
            globalProjectIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_PROJECT_ICON, "ic_project") ?: "ic_project", R.drawable.ic_project)
            globalNoteIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_NOTE_ICON, "ic_notes") ?: "ic_notes", R.drawable.ic_notes)
            globalFinanceIcon = getResourceId(context, prefs.getString(KEY_GLOBAL_FINANCE_ICON, "ic_finance") ?: "ic_finance", R.drawable.ic_finance)

            val savedTemplates = try {
                gson.fromJson<MutableMap<String, List<String>>>(prefs.getString(KEY_PROJ_TEMPLATES, "{}"), object : TypeToken<MutableMap<String, List<String>>>() {}.type) ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }

            if (savedTemplates.isNotEmpty()) {
                projectTemplates = savedTemplates
            }

            // Sync logic
            reconstructWorkoutHistoryFromGlobalLog()
            checkAndResetDailyStats(context)
            checkAndResetMonthlyFinance(context)
            isDataLoaded.value = true
            notifyDataChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getResourceId(context: Context, name: String, fallbackId: Int): Int {
        return try {
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (id != 0 && UIUtils.isDrawableResource(context, id)) id else fallbackId
        } catch (e: Exception) {
            fallbackId
        }
    }

    private fun getResourceName(context: Context, id: Int): String {
        return try { context.resources.getResourceEntryName(id) } catch (e: Exception) { "ic_launcher_foreground" }
    }

    private fun checkAndResetDailyStats(context: Context) {
        val today = getTrackingDateString()
        val prefs = getPrefs(context)
        val lastReset = prefs.getString(KEY_LAST_RESET_DATE, "")
        if (lastReset != null && lastReset != today && lastReset != "") {
            // Capture snapshot of the day that just ended before resetting
            val snapshot = calculateDayHistory(lastReset)
            history[lastReset] = snapshot
            
            synchronized(habits) {
                habits.forEach { 
                    it.isCompleted = false
                    it.progress = 0
                }
            }
            synchronized(workouts) {
                workouts.forEach { it.isCompleted = false }
            }
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply()
            saveData(context)
        } else if (lastReset == "") {
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply()
        }
    }

    private fun checkAndResetMonthlyFinance(context: Context) {
        val calendar = Calendar.getInstance()
        val currentMonthYear = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
        val prefs = getPrefs(context)
        val lastReset = prefs.getString(KEY_LAST_MONTH_RESET, "")
        if (lastReset != currentMonthYear) {
            prefs.edit().putString(KEY_LAST_MONTH_RESET, currentMonthYear).apply()
        }
    }

    fun saveDayNote(date: String, note: String) {
        val current = history[date] ?: DayHistory(0, 0, 0, 0)
        history[date] = current.copy(notes = note)
        notifyDataChanged()
    }

    fun getDayHistory(date: String): DayHistory? {
        return history[date]
    }

    fun calculateDayHistory(dateKey: String): DayHistory {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = sdf.format(Date())

        val selectedDayEnd = try {
            val date = sdf.parse(dateKey)
            val cal = Calendar.getInstance()
            if (date != null) {
                cal.time = date
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                cal.timeInMillis
            } else System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }

        val cal = Calendar.getInstance()
        try { sdf.parse(dateKey)?.let { cal.time = it } } catch (e: Exception) {}
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1)

        return if (dateKey == today) {
            val todaysHabits = synchronized(habits) {
                habits.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) && it.timestamp <= selectedDayEnd }
            }
            val todaysWorkouts = synchronized(workouts) {
                workouts.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) && it.timestamp <= selectedDayEnd }
            }
            DayHistory(
                todaysHabits.count { it.isCompleted },
                todaysHabits.size,
                todaysWorkouts.count { it.isCompleted },
                todaysWorkouts.size,
                todaysWorkouts.map { w ->
                    WorkoutProgressEntry(
                        w.name, 
                        w.progress, 
                        w.target, 
                        if (w.trackingMode == "Timer") "s" else if (w.trackingMode == "Sets") "Sets" else w.trackingMode, 
                        w.color, 
                        w.isCompleted
                    )
                },
                history[dateKey]?.notes
            )
        } else {
            val snapshot = history[dateKey]
            if (snapshot != null) snapshot else {
                val activeHabits = synchronized(habits) {
                    habits.filter { it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) }
                }
                val activeWorkouts = synchronized(workouts) {
                    workouts.filter { it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) }
                }
                DayHistory(
                    activeHabits.count { it.completedDates.contains(dateKey) },
                    activeHabits.size,
                    activeWorkouts.count { it.completedDates.contains(dateKey) },
                    activeWorkouts.size,
                    activeWorkouts.map { w ->
                        val progress = w.dailyProgress[dateKey] ?: if (w.completedDates.contains(dateKey)) 100 else 0
                        val progressValue = (progress.toLong() * w.target / 100).toInt()
                        WorkoutProgressEntry(
                            w.name,
                            progressValue,
                            w.target,
                            if (w.trackingMode == "Timer") "s" else if (w.trackingMode == "Sets") "Sets" else w.trackingMode,
                            w.color,
                            w.completedDates.contains(dateKey)
                        )
                    }
                )
            }
        }
    }

    fun getLastSevenDaysDetailedProgress(): List<Pair<Int, Int>> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(cal.time)
            val historyEntry = history[date]
            val progress = historyEntry?.let { 
                if (it.totalHabits == 0) 0 else (it.habitsCompleted * 100) / it.totalHabits 
            } ?: 0
            Pair(i, progress)
        }.reversed()
    }

    fun getLastSevenDaysWorkoutProgress(): List<Pair<Int, Int>> {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(cal.time)
            val historyEntry = history[date]
            val progress = historyEntry?.let { 
                if (it.totalWorkouts == 0) 0 else (it.workoutsCompleted * 100) / it.totalWorkouts 
            } ?: 0
            Pair(i, progress)
        }.reversed()
    }

    fun getHeatmapData(calendar: Calendar, type: String = "HABITS"): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val historyEntry = history[dateKey]
            val progress = when (type) {
                "HABITS" -> if (historyEntry == null || historyEntry.totalHabits == 0) 0 else (historyEntry.habitsCompleted * 100) / historyEntry.totalHabits
                "WORKOUTS" -> {
                    // Try to get from detailed workout data first
                    val todaysWorkouts = synchronized(workouts) {
                        workouts.filter {
                            (it.repeatType != "SPECIFIC_DAYS" || (it.repeatDays?.contains(tempCal.get(Calendar.DAY_OF_WEEK) - 1) ?: false)) &&
                            it.timestamp <= tempCal.timeInMillis + 86400000
                        }
                    }
                    if (todaysWorkouts.isNotEmpty()) {
                        val totalProgress = todaysWorkouts.sumOf { 
                            it.dailyProgress?.get(dateKey) ?: if (it.completedDates?.contains(dateKey) == true) 100 else 0 
                        }
                        totalProgress / todaysWorkouts.size
                    } else if (historyEntry != null && historyEntry.totalWorkouts > 0) {
                        (historyEntry.workoutsCompleted * 100) / historyEntry.totalWorkouts
                    } else 0
                }
                else -> 0
            }
            result[day - 1] = progress
        }
        return result
    }

    fun getHeatmapData(): Map<String, Int> {
        return history.mapValues { it.value.habitsCompleted }
    }

    fun getHabitPerformanceByFrequency(): Map<String, Float> {
        return emptyMap() // Placeholder
    }

    fun getMoodCorrelationData(): Map<String, Map<String, Int>> {
        return emptyMap() // Placeholder
    }

    fun getGlobalCompletionRate(type: String = "HABITS"): Int {
        if (type == "WORKOUTS") {
            // For workouts, if history is sparse, calculate from completion dates over last 30 days
            val workoutDates = synchronized(workouts) { workouts.flatMap { it.completedDates }.distinct().toSet() }
            if (workoutDates.isEmpty()) return 0
            
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val cal = Calendar.getInstance()
            var totalScheduledDays = 0
            var totalCompletedDays = 0
            
            for (i in 0 until 30) {
                val dateStr = sdf.format(cal.time)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                
                val wasScheduled = synchronized(workouts) {
                    workouts.any { 
                        (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                        it.timestamp <= cal.timeInMillis + 86400000
                    }
                }
                
                if (wasScheduled) {
                    totalScheduledDays++
                    if (workoutDates.contains(dateStr)) {
                        totalCompletedDays++
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            
            return if (totalScheduledDays > 0) (totalCompletedDays * 100) / totalScheduledDays else 0
        }

        if (history.isEmpty()) return 0
        return history.values.map { 
            when (type) {
                "HABITS" -> if (it.totalHabits == 0) 0 else (it.habitsCompleted * 100) / it.totalHabits
                else -> 0
            }
        }.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.average().toInt() }
    }

    fun getHabitStreaks(habitName: String): Pair<Int, Int> {
        return HabitDataManager.getHabitStreaks(habitName)
    }

    fun getCurrentStreak(): Int {
        return HabitDataManager.getHabitStreak()
    }
    
    suspend fun exportData(context: Context, password: CharArray? = null): String = withContext(Dispatchers.IO) {
        // Ensure Room is up to date with latest in-memory changes before exporting
        performSave(context)

        val prefs = getPrefs(context)
        val allData = prefs.all.toMutableMap()

        // Remove legacy list keys from SharedPreferences export to avoid bloat and conflicts
        val legacyKeys = setOf(
            KEY_HABITS, KEY_WORKOUTS, KEY_TASKS, KEY_NOTES, KEY_PROJECTS,
            KEY_TRANSACTIONS, KEY_LEDGER, KEY_PERSONAL_LEDGER
        )
        legacyKeys.forEach { allData.remove(it) }

        // Add Room Data
        val db = AppDatabase.getDatabase(context)
        val dao = db.workspaceDao()

        // Workspace
        allData["workspaceProjects"] = dao.getAllProjectsSync()
        allData["workspaceGoals"] = dao.getAllGoalsSync()
        allData["workspaceTasks"] = dao.getAllTasksSync()
        allData["workspaceFeatures"] = dao.getAllFeaturesSync()
        allData["workspaceBugs"] = dao.getAllBugsSync()
        allData["workspaceIdeas"] = dao.getAllIdeasSync()
        allData["workspaceNotes"] = dao.getAllNotesSync()
        allData["workspaceResources"] = dao.getAllResourcesSync()
        allData["workspaceLogs"] = dao.getAllActivityLogsSync()
        allData["workspaceRefs"] = dao.getAllNoteCrossReferencesSync()
        
        // Global
        allData["appTasks"] = db.taskDao().getAllTasksSync()
        allData["appHabits"] = db.habitDao().getAllHabitsSync()
        allData["appWorkouts"] = db.workoutDao().getAllWorkoutsSync()
        allData["appNotes"] = db.noteDao().getAllNotesSync()
        
        val fDao = db.financeDao()
        allData["appTransactions"] = fDao.getAllTransactionsSync()
        allData["appPersonalLedgers"] = fDao.getAllPersonalLedgersSync()
        allData["appLedgerEntries"] = fDao.getAllLedgerEntriesSync()

        val json = Gson().toJson(allData)
        if (password != null) {
            SecurityManager.encryptData(json, password)
        } else {
            json
        }
    }

    suspend fun importData(context: Context, dataString: String, password: CharArray? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Reset existing database
            AppDatabase.resetDatabase(context)

            val json = if (password != null) {
                SecurityManager.decryptData(dataString, password)
            } else {
                dataString
            }
            
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = Gson().fromJson(json, type)
            
            // 2. Clear all local preferences for a clean restore
            getLegacyPrefs(context).edit().clear().apply()
            val prefs = getPrefs(context)
            prefs.edit().clear().apply()
            
            val editor = prefs.edit()

            val legacyKeyMap = mapOf(
                "habits" to KEY_HABITS,
                "workouts" to KEY_WORKOUTS,
                "tasks" to KEY_TASKS,
                "notes" to KEY_NOTES,
                "projects" to KEY_PROJECTS,
                "transactions" to KEY_TRANSACTIONS,
                "ledgerEntries" to KEY_LEDGER,
                "personalLedgers" to KEY_PERSONAL_LEDGER,
                "history" to KEY_HISTORY,
                "dailyMoods" to KEY_DAILY_MOODS,
                "recentActivities" to KEY_RECENT_ACT,
                "monthlyBudget" to KEY_BUDGET,
                "monthlyBudgets" to KEY_MONTHLY_BUDGETS,
                "monthlySavingsGoal" to KEY_SAVINGS_GOAL,
                "monthlySavingsGoals" to KEY_MONTHLY_SAVINGS_GOALS
            )

            val databaseKeys = setOf(
                "workspaceProjects", "workspaceGoals", "workspaceTasks", "workspaceFeatures",
                "workspaceBugs", "workspaceIdeas", "workspaceNotes", "workspaceResources",
                "workspaceLogs", "workspaceRefs",
                "appTasks", "appHabits", "appWorkouts", "appNotes",
                "appTransactions", "appPersonalLedgers", "appLedgerEntries"
            )

            val hasRoomData = databaseKeys.any { data.containsKey(it) }

            // Keys that should NOT be restored to SharedPreferences if Room data exists in JSON
            val legacyToIgnoreIfRoomExists = mapOf(
                "appTasks" to KEY_TASKS,
                "appHabits" to KEY_HABITS,
                "appWorkouts" to KEY_WORKOUTS,
                "appNotes" to KEY_NOTES,
                "appTransactions" to KEY_TRANSACTIONS,
                "appPersonalLedgers" to KEY_PERSONAL_LEDGER,
                "appLedgerEntries" to KEY_LEDGER
            )

            val floatKeys = setOf(KEY_BUDGET, KEY_SAVINGS_GOAL)
            val longKeys = setOf(KEY_LAST_MOOD_TIMESTAMP)

            data.forEach { (key, value) ->
                val targetKey = legacyKeyMap[key] ?: key
                
                // Skip if it's a dedicated database key
                if (targetKey in databaseKeys) return@forEach
                
                // Skip legacy key if new equivalent Room data is present in the same JSON
                val isRedundantLegacy = legacyToIgnoreIfRoomExists.any { (newKey, oldKey) ->
                    oldKey == targetKey && data.containsKey(newKey)
                }
                if (isRedundantLegacy) return@forEach

                val valueToStore = when (value) {
                    is List<*>, is Map<*, *> -> {
                        val jsonValue = Gson().toJson(value)
                        when (targetKey) {
                            KEY_HABITS -> mapHabitFields(jsonValue)
                            KEY_WORKOUTS -> mapWorkoutFields(jsonValue)
                            KEY_TASKS -> mapTaskFields(jsonValue)
                            KEY_NOTES -> mapNoteFields(jsonValue, false)
                            KEY_PROJECTS -> mapNoteFields(jsonValue, true)
                            else -> jsonValue
                        }
                    }
                    is String -> {
                        // Crucial: Handle stringified JSON from even older versions
                        if (value.trim().startsWith("[") || value.trim().startsWith("{")) {
                            when (targetKey) {
                                KEY_HABITS -> mapHabitFields(value)
                                KEY_WORKOUTS -> mapWorkoutFields(value)
                                KEY_TASKS -> mapTaskFields(value)
                                KEY_NOTES -> mapNoteFields(value, false)
                                KEY_PROJECTS -> mapNoteFields(value, true)
                                else -> value
                            }
                        } else value
                    }
                    else -> value
                }

                when (valueToStore) {
                    is String -> editor.putString(targetKey, valueToStore)
                    is Boolean -> editor.putBoolean(targetKey, valueToStore)
                    is Double -> {
                        when (targetKey) {
                            in floatKeys -> editor.putFloat(targetKey, valueToStore.toFloat())
                            in longKeys -> editor.putLong(targetKey, valueToStore.toLong())
                            else -> {
                                val longValue = valueToStore.toLong()
                                if (valueToStore == longValue.toDouble()) {
                                    if (longValue in Int.MIN_VALUE..Int.MAX_VALUE) editor.putInt(targetKey, longValue.toInt())
                                    else editor.putLong(targetKey, longValue)
                                } else {
                                    editor.putFloat(targetKey, valueToStore.toFloat())
                                }
                            }
                        }
                    }
                }
            }

            // Important: Set migration flag to false to force LegacyMigrationManager 
            // to check the restored SharedPreferences data for migration.
            editor.putBoolean("data_migrated_to_sql", false)
            editor.commit() // Use commit for synchronous save before re-initialization

            // Restore Room Data
            val db = AppDatabase.getDatabase(context)
            val gson = Gson()

            db.withTransaction {
                val dao = db.workspaceDao()
                dao.deleteAllProjects()
                dao.deleteAllGoals()
                dao.deleteAllTasks()
                dao.deleteAllFeatures()
                dao.deleteAllBugs()
                dao.deleteAllIdeas()
                dao.deleteAllNotes()
                dao.deleteAllResources()
                dao.deleteAllActivityLogs()
                dao.deleteAllNoteCrossReferences()

                val tDao = db.taskDao()
                val hDao = db.habitDao()
                val wDao = db.workoutDao()
                val nDao = db.noteDao()
                val fDao = db.financeDao()
                
                tDao.deleteAll()
                hDao.deleteAll()
                wDao.deleteAll()
                nDao.deleteAll()
                fDao.deleteAllTransactions()
                fDao.deleteAllPersonalLedgers()
                fDao.deleteAllLedgerEntries()

                // Restore Workspace
                data["workspaceProjects"]?.let {
                    val listType = object : TypeToken<List<ProjectEntity>>() {}.type
                    dao.insertAllProjects(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceGoals"]?.let {
                    val listType = object : TypeToken<List<GoalEntity>>() {}.type
                    dao.insertAllGoals(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceTasks"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.workspace.data.TaskEntity>>() {}.type
                    dao.insertAllTasks(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceFeatures"]?.let {
                    val listType = object : TypeToken<List<FeatureEntity>>() {}.type
                    dao.insertAllFeatures(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceBugs"]?.let {
                    val listType = object : TypeToken<List<BugEntity>>() {}.type
                    dao.insertAllBugs(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceIdeas"]?.let {
                    val listType = object : TypeToken<List<IdeaEntity>>() {}.type
                    dao.insertAllIdeas(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceNotes"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.workspace.data.NoteEntity>>() {}.type
                    dao.insertAllNotes(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceResources"]?.let {
                    val listType = object : TypeToken<List<ResourceEntity>>() {}.type
                    dao.insertAllResources(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceLogs"]?.let {
                    val listType = object : TypeToken<List<ActivityLogEntity>>() {}.type
                    dao.insertAllActivityLogs(gson.fromJson(gson.toJson(it), listType))
                }
                data["workspaceRefs"]?.let {
                    val listType = object : TypeToken<List<NoteCrossReferenceEntity>>() {}.type
                    dao.insertAllNoteCrossReferences(gson.fromJson(gson.toJson(it), listType))
                }

                // Restore Global
                data["appTasks"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.TaskEntity>>() {}.type
                    tDao.insertAllTasks(gson.fromJson(gson.toJson(it), listType))
                }
                data["appHabits"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.HabitEntity>>() {}.type
                    hDao.insertAllHabits(gson.fromJson(gson.toJson(it), listType))
                }
                data["appWorkouts"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.WorkoutEntity>>() {}.type
                    wDao.insertAllWorkouts(gson.fromJson(gson.toJson(it), listType))
                }
                data["appNotes"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.NoteEntity>>() {}.type
                    nDao.insertAllNotes(gson.fromJson(gson.toJson(it), listType))
                }
                data["appTransactions"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.TransactionEntity>>() {}.type
                    fDao.insertAllTransactions(gson.fromJson(gson.toJson(it), listType))
                }
                data["appPersonalLedgers"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.PersonalLedgerEntity>>() {}.type
                    fDao.insertAllPersonalLedgers(gson.fromJson(gson.toJson(it), listType))
                }
                data["appLedgerEntries"]?.let {
                    val listType = object : TypeToken<List<com.example.allinone.data.database.LedgerEntryEntity>>() {}.type
                    fDao.insertAllLedgerEntries(gson.fromJson(gson.toJson(it), listType))
                }
            }

            reconstructWorkoutHistoryFromGlobalLog()
            
            withContext<Unit>(Dispatchers.Main) {
                initialize(context)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun mapHabitFields(json: String): String {
        try {
            val listType = object : TypeToken<List<MutableMap<String, Any>>>() {}.type
            val list: List<MutableMap<String, Any>> = Gson().fromJson(json, listType) ?: return json
            list.forEach { map ->
                if (map.containsKey("habitName") && !map.containsKey("name")) map["name"] = map["habitName"]!!
                if (map.containsKey("isDone") && !map.containsKey("isCompleted")) map["isCompleted"] = map["isDone"]!!
                if (!map.containsKey("repeatDays")) map["repeatDays"] = listOf(0, 1, 2, 3, 4, 5, 6)
                
                // Map History/Completion fields
                val historyKeys = listOf("history", "completedDays", "completionDates", "completionLog", "logs")
                historyKeys.forEach { oldKey ->
                    if (map.containsKey(oldKey) && (!map.containsKey("completedDates") || (map["completedDates"] as? List<*>)?.isEmpty() == true)) {
                        map["completedDates"] = map[oldKey]!!
                    }
                }
                
                if (!map.containsKey("completedDates")) map["completedDates"] = mutableListOf<String>()
                if (!map.containsKey("dailyProgress")) map["dailyProgress"] = mutableMapOf<String, Int>()
            }
            return Gson().toJson(list)
        } catch (e: Exception) { return json }
    }

    private fun mapWorkoutFields(json: String): String {
        try {
            val listType = object : TypeToken<List<MutableMap<String, Any>>>() {}.type
            val list: List<MutableMap<String, Any>> = Gson().fromJson(json, listType) ?: return json
            list.forEach { map ->
                if (map.containsKey("workoutName") && !map.containsKey("name")) map["name"] = map["workoutName"]!!
                if (map.containsKey("isDone") && !map.containsKey("isCompleted")) map["isCompleted"] = map["isDone"]!!
                if (!map.containsKey("muscleGroups")) map["muscleGroups"] = listOf("General")
                
                // Map History/Completion fields
                val historyKeys = listOf("history", "completedDays", "completionDates", "completionLog", "logs")
                historyKeys.forEach { oldKey ->
                    if (map.containsKey(oldKey) && (!map.containsKey("completedDates") || (map["completedDates"] as? List<*>)?.isEmpty() == true)) {
                        map["completedDates"] = map[oldKey]!!
                    }
                }
                
                if (!map.containsKey("completedDates")) map["completedDates"] = mutableListOf<String>()
                if (!map.containsKey("dailyProgress")) map["dailyProgress"] = mutableMapOf<String, Int>()
            }
            return Gson().toJson(list)
        } catch (e: Exception) { return json }
    }

    private fun mapTaskFields(json: String): String {
        try {
            val listType = object : TypeToken<List<MutableMap<String, Any>>>() {}.type
            val list: List<MutableMap<String, Any>> = Gson().fromJson(json, listType) ?: return json
            list.forEach { map ->
                if (map.containsKey("taskName") && !map.containsKey("name")) map["name"] = map["taskName"]!!
                if (map.containsKey("isDone") && !map.containsKey("isCompleted")) map["isCompleted"] = map["isDone"]!!
                if (!map.containsKey("subtasks")) map["subtasks"] = mutableListOf<Any>()
            }
            return Gson().toJson(list)
        } catch (e: Exception) { return json }
    }

    private fun reconstructWorkoutHistoryFromGlobalLog() {
        if (history.isEmpty() || workouts.isEmpty()) return
        
        synchronized(workouts) {
            history.forEach { (dateKey, dayData) ->
                dayData.workoutDetails?.forEach { detail ->
                    // Find matching workout by name
                    val workout = workouts.find { it.name.equals(detail.name, ignoreCase = true) }
                    if (workout != null) {
                        // 1. Add to completedDates if completed
                        if (detail.isCompleted && !workout.completedDates.contains(dateKey)) {
                            workout.completedDates.add(dateKey)
                        }
                        
                        // 2. Add to dailyProgress
                        val progressPercent = if (detail.target > 0) (detail.progress * 100) / detail.target else 100
                        if (!workout.dailyProgress.containsKey(dateKey) || workout.dailyProgress[dateKey] == 0) {
                            workout.dailyProgress[dateKey] = progressPercent
                        }
                    }
                }
            }
        }
    }

    private fun mapNoteFields(json: String, forceGlobalProject: Boolean): String {
        try {
            val listType = object : TypeToken<List<MutableMap<String, Any>>>() {}.type
            val list: List<MutableMap<String, Any>> = Gson().fromJson(json, listType) ?: return json
            list.forEach { map ->
                if (map.containsKey("noteTitle") && !map.containsKey("title")) map["title"] = map["noteTitle"]!!
                if (map.containsKey("noteBody") && !map.containsKey("content")) map["content"] = map["noteBody"]!!
                if (map.containsKey("body") && !map.containsKey("content")) map["content"] = map["body"]!!
                if (!map.containsKey("journalEntries")) map["journalEntries"] = mutableListOf<Any>()
                if (!map.containsKey("subFeatures")) map["subFeatures"] = mutableListOf<Any>()
                if (forceGlobalProject) map["isGlobalProject"] = true
            }
            return Gson().toJson(list)
        } catch (e: Exception) { return json }
    }

    fun resetAppearanceIcons(context: Context) {
        globalHabitIcon = R.drawable.ic_habit_tracker
        globalWorkoutIcon = R.drawable.ic_workout_routine
        globalTaskIcon = R.drawable.ic_task
        globalProjectIcon = R.drawable.ic_project
        globalNoteIcon = R.drawable.ic_notes
        globalFinanceIcon = R.drawable.ic_finance
        saveData(context)
    }

    fun resetAppearanceColors(context: Context) {
        appAccentColor = -1
        globalHabitColor = -1
        globalWorkoutColor = -1
        globalTaskColor = -1
        globalProjectColor = -1
        globalNoteColor = -1
        globalFinanceColor = -1
        saveData(context)
    }

    fun getVolumeWeightedHeatmap(calendar: Calendar): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        val dailyVolumes = mutableMapOf<Int, Double>()
        var maxVolume = 0.0

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            
            val volume = synchronized(workouts) {
                workouts.filter { 
                    it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
                }
            }.sumOf { workout ->
                val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                calculateWorkoutVolume(workout, progress)
            }
            dailyVolumes[day - 1] = volume
            if (volume > maxVolume) maxVolume = volume
        }

        if (maxVolume > 0) {
            dailyVolumes.forEach { (day, vol) ->
                result[day] = ((vol / maxVolume) * 100).toInt()
            }
        }
        return result
    }

    private fun calculateWorkoutVolume(workout: Workout, progressPercent: Int): Double {
        val baseVolume = when (workout.trackingMode) {
            "Sets" -> (workout.target * workout.repsPerSet).toDouble()
            "Reps" -> workout.target.toDouble()
            "Timer" -> workout.target.toDouble() / 60.0 // Normalize timer to minutes
            else -> workout.target.toDouble()
        }
        return (baseVolume * progressPercent) / 100.0
    }

    fun getMuscleDistributionData(): Map<String, Int> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        
        val distribution = mutableMapOf<String, Double>()
        
        synchronized(workouts) {
            workouts.forEach { workout ->
                val workoutVolume = workout.completedDates.filter { dateStr ->
                    try {
                        val date = sdf.parse(dateStr)
                        date != null && date.time >= thirtyDaysAgo
                    } catch (e: Exception) { false }
                }.size * calculateWorkoutVolume(workout, 100)
                
                workout.muscleGroups.forEach { muscle ->
                    distribution[muscle] = (distribution[muscle] ?: 0.0) + workoutVolume
                }
            }
        }
        
        val total = distribution.values.sum()
        if (total == 0.0) return mapOf("Chest" to 0, "Back" to 0, "Legs" to 0, "Shoulders" to 0, "Arms" to 0)
        
        return distribution.mapValues { ((it.value / total) * 100).toInt() }
    }

    fun getMuscleRecoveryStatus(): Map<String, Float> {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val status = mutableMapOf<String, Float>()
        
        val muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Arms")
        muscles.forEach { muscle ->
            var lastTrainedTime = 0L
            workouts.filter { it.muscleGroups.contains(muscle) }.forEach { workout ->
                workout.completedDates.forEach { dateStr ->
                    try {
                        val date = sdf.parse(dateStr)
                        if (date != null && date.time > lastTrainedTime) lastTrainedTime = date.time
                    } catch (e: Exception) {}
                }
            }
            
            if (lastTrainedTime == 0L) {
                status[muscle] = 1.0f
            } else {
                val hoursSince = (now - lastTrainedTime) / (1000 * 60 * 60)
                // Linear recovery over 48 hours
                val recovery = (hoursSince.toFloat() / 48f).coerceIn(0f, 1f)
                status[muscle] = recovery
            }
        }
        return status
    }

    fun getACWRData(): Pair<List<Float>, List<Float>> {
        val now = Calendar.getInstance()
        val acuteWorkload = mutableListOf<Float>()
        val chronicWorkload = mutableListOf<Float>()
        
        for (i in 0 until 14) {
            val date = now.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            
            val acute = calculateRollingWorkload(date, 7)
            val chronic = calculateRollingWorkload(date, 28)
            
            acuteWorkload.add(acute.toFloat())
            chronicWorkload.add(chronic.toFloat())
        }
        
        return Pair(acuteWorkload.reversed(), chronicWorkload.reversed())
    }

    private fun calculateRollingWorkload(endDate: Calendar, days: Int): Double {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var totalVolume = 0.0
        
        for (i in 0 until days) {
            val date = endDate.clone() as Calendar
            date.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = sdf.format(date.time)
            
            totalVolume += synchronized(workouts) {
                workouts.sumOf { workout ->
                    val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                    calculateWorkoutVolume(workout, progress)
                }
            }
        }
        return totalVolume / days
    }

    fun getTrainingStabilityScore(): Float {
        if (workouts.isEmpty()) return 1.0f
        val consistency = workouts.map { 
            if (it.repeatType == "EVERY_DAY") 1.0f else 0.7f 
        }.average().toFloat()
        return consistency
    }

    fun getMonthlyVolumeData(calendar: Calendar): List<Float> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        return (1..daysInMonth).map { day ->
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            synchronized(workouts) {
                workouts.sumOf { workout ->
                    val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                    calculateWorkoutVolume(workout, progress)
                }.toFloat()
            }
        }
    }

    fun getWorkoutDiversityData(): Map<String, Int> {
        if (workouts.isEmpty()) return emptyMap()
        return workouts.groupBy { it.trackingMode }.mapValues { it.value.size }
    }

    fun getIntensityDistribution(calendar: Calendar): List<Int> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        return (1..daysInMonth).map { day ->
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val dayWorkouts = synchronized(workouts) {
                workouts.filter { 
                    it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
                }
            }
            if (dayWorkouts.isEmpty()) 0 
            else (dayWorkouts.sumOf { it.dailyProgress[dateKey] ?: 100 } / dayWorkouts.size)
        }
    }

    fun getDailyMuscleFocus(calendar: Calendar): Map<Int, List<String>> {
        val result = mutableMapOf<Int, List<String>>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val trainedMuscles = synchronized(workouts) {
                workouts.filter { 
                    it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
                }.flatMap { it.muscleGroups }.distinct()
            }
            if (trainedMuscles.isNotEmpty()) {
                result[day - 1] = trainedMuscles
            }
        }
        return result
    }

    fun getTemporalDensityData(): Map<Int, Map<String, Int>> = HabitDataManager.getTemporalDensityData()
    fun getHabitCorrelationMatrix(): List<Triple<String, String, Double>> = HabitDataManager.getHabitCorrelationMatrix()
    fun getStabilityIndex(habitName: String? = null): Float = HabitDataManager.getStabilityIndex(habitName)
    fun getWeeklyCyclicalData(habitName: String? = null): Map<Int, Float> = HabitDataManager.getWeeklyCyclicalData(habitName)
    fun getResilienceScore(habitName: String? = null): Float = HabitDataManager.getResilienceScore(habitName)
    fun getMonthlyMomentumHistory(habitName: String? = null): List<Pair<String, Int>> = HabitDataManager.getMonthlyMomentumHistory(habitName)
    fun getStreakMilestoneProgress(habitName: String? = null): Triple<Int, Int, Float> = HabitDataManager.getStreakMilestoneProgress(habitName)
    val predefinedJourneys = listOf(
        Journey(
            title = "Morning Mastery",
            description = "Transform your mornings to transform your life.",
            category = "HABITS",
            bannerRes = R.drawable.ic_launcher_foreground,
            keyResults = listOf(JourneyResult("Better Focus", "🎯")),
            expectations = listOf("Consistency"),
            phases = listOf(JourneyPhase("1-7", "Phase 1", "Start small")),
            habitsToCreate = listOf("Wake up at 6 AM", "Drink Water")
        ),
        Journey(
            title = "Strength Builder",
            description = "Build a solid foundation of strength.",
            category = "WORKOUTS",
            bannerRes = R.drawable.ic_launcher_foreground,
            keyResults = listOf(JourneyResult("More Power", "💪")),
            expectations = listOf("Effort"),
            phases = listOf(JourneyPhase("1-14", "Intro", "Basic moves")),
            workoutsToCreate = listOf("Pushups", "Squats")
        )
    )

    fun checkAndSetNewTodayNotification(timestamp: Long?) {
        if (timestamp == null) return
        val todayStr = getTrackingDateString()
        val itemDateStr = getTrackingDateString(timestamp)
        if (todayStr == itemDateStr) {
            hasNewTodayNotifications = true
        }
    }
}
