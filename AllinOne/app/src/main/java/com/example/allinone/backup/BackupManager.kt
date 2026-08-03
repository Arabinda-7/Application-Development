package com.example.allinone.backup

import com.example.allinone.data.database.AppDatabase
import com.example.allinone.domain.repository.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository,
    private val noteRepository: NoteRepository,
    private val financeRepository: FinanceRepository,
    private val projectRepository: ProjectRepository
) {
    suspend fun createBackup(): BackupData {
        val workspaceDao = database.workspaceDao()
        val financeDao = database.financeDao()
        val aiChatDao = database.aiChatDao()
        val assistantMemoryDao = database.assistantMemoryDao()
        
        return BackupData(
            version = BackupValidator.CURRENT_VERSION,
            timestamp = System.currentTimeMillis(),
            
            // Global Entities
            tasks = database.taskDao().getAllTasksSync(),
            habits = database.habitDao().getAllHabitsSync(),
            workouts = database.workoutDao().getAllWorkoutsSync(),
            notes = database.noteDao().getAllNotesSync(),
            transactions = financeDao.getAllTransactionsSync(),
            personalLedgers = financeDao.getAllPersonalLedgersSync(),
            ledgerEntries = financeDao.getAllLedgerEntriesSync(),
            
            // Workspace Entities
            workspaceProjects = workspaceDao.getAllProjectsSync(),
            workspaceGoals = workspaceDao.getAllGoalsSync(),
            workspaceTasks = workspaceDao.getAllTasksSync(),
            workspaceFeatures = workspaceDao.getAllFeaturesSync(),
            workspaceBugs = workspaceDao.getAllBugsSync(),
            workspaceIdeas = workspaceDao.getAllIdeasSync(),
            workspaceNotes = workspaceDao.getAllNotesSync(),
            workspaceResources = workspaceDao.getAllResourcesSync(),
            workspaceLogs = workspaceDao.getAllActivityLogsSync(),
            workspaceRefs = workspaceDao.getAllNoteCrossReferencesSync(),
            
            // AI Entities
            aiChatMessages = aiChatDao.getAllMessagesSync(),
            aiChatSessions = aiChatDao.getAllSessionsSync(),
            assistantMemories = assistantMemoryDao.getAllMemoriesSync(),
            
            // Profile & Settings
            userProfile = userRepository.getUserProfile().first(),
            userSettings = userRepository.getUserSettings().first(),
            financeSettings = financeRepository.getFinanceSettings().first(),
            habitSettings = habitRepository.getHabitSettings().first(),
            workoutSettings = workoutRepository.getWorkoutSettings().first(),
            noteSettings = noteRepository.getNoteSettings().first(),
            projectSettings = projectRepository.getProjectSettings().first()
        )
    }

    suspend fun exportBackup(password: CharArray? = null): String {
        val data = createBackup()
        val json = BackupSerializer.serialize(data)
        return if (password != null) {
            EncryptionUtils.encrypt(json, password)
        } else {
            json
        }
    }
}
