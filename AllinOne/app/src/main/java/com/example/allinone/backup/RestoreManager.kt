package com.example.allinone.backup

import androidx.room.withTransaction
import com.example.allinone.data.database.AppDatabase
import com.example.allinone.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreManager @Inject constructor(
    private val database: AppDatabase,
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val workoutRepository: WorkoutRepository,
    private val noteRepository: NoteRepository,
    private val financeRepository: FinanceRepository,
    private val projectRepository: ProjectRepository
) {
    suspend fun importBackup(backupString: String, password: CharArray? = null): RestoreResult {
        return try {
            val json = if (password != null) {
                EncryptionUtils.decrypt(backupString, password)
            } else {
                backupString
            }
            
            val data = BackupSerializer.deserialize(json)
            val validation = BackupValidator.validate(data)
            
            if (validation is BackupValidator.ValidationResult.Invalid) {
                return RestoreResult.Error(validation.message)
            }
            
            var processedData = data
            if (processedData.version < BackupValidator.CURRENT_VERSION) {
                processedData = migrate(processedData)
            }
            
            restoreData(processedData)
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun migrate(data: BackupData): BackupData {
        // Implement version-specific migrations here
        return data
    }

    private suspend fun restoreData(data: BackupData) {
        val workspaceDao = database.workspaceDao()
        val financeDao = database.financeDao()
        val aiChatDao = database.aiChatDao()
        val assistantMemoryDao = database.assistantMemoryDao()
        
        database.withTransaction {
            // Global Entities
            database.taskDao().let { dao ->
                dao.deleteAll()
                dao.insertAllTasks(data.tasks)
            }
            database.habitDao().let { dao ->
                dao.deleteAll()
                dao.insertAllHabits(data.habits)
            }
            database.workoutDao().let { dao ->
                dao.deleteAll()
                dao.insertAllWorkouts(data.workouts)
            }
            database.noteDao().let { dao ->
                dao.deleteAll()
                dao.insertAllNotes(data.notes)
            }
            
            financeDao.let { dao ->
                dao.deleteAllTransactions()
                dao.insertAllTransactions(data.transactions)
                dao.deleteAllPersonalLedgers()
                dao.insertAllPersonalLedgers(data.personalLedgers)
                dao.deleteAllLedgerEntries()
                dao.insertAllLedgerEntries(data.ledgerEntries)
            }
            
            // Workspace Entities
            workspaceDao.let { dao ->
                dao.deleteAllProjects()
                dao.insertAllProjects(data.workspaceProjects)
                dao.deleteAllGoals()
                dao.insertAllGoals(data.workspaceGoals)
                dao.deleteAllTasks()
                dao.insertAllTasks(data.workspaceTasks)
                dao.deleteAllFeatures()
                dao.insertAllFeatures(data.workspaceFeatures)
                dao.deleteAllBugs()
                dao.insertAllBugs(data.workspaceBugs)
                dao.deleteAllIdeas()
                dao.insertAllIdeas(data.workspaceIdeas)
                dao.deleteAllNotes()
                dao.insertAllNotes(data.workspaceNotes)
                dao.deleteAllResources()
                dao.insertAllResources(data.workspaceResources)
                dao.deleteAllActivityLogs()
                dao.insertAllActivityLogs(data.workspaceLogs)
                dao.deleteAllNoteCrossReferences()
                dao.insertAllNoteCrossReferences(data.workspaceRefs)
            }

            // AI Entities
            aiChatDao.let { dao ->
                dao.clearEverything()
                dao.insertAllSessions(data.aiChatSessions)
                dao.insertAllMessages(data.aiChatMessages)
            }
            assistantMemoryDao.let { dao ->
                dao.deleteAll()
                dao.insertAllMemories(data.assistantMemories)
            }
        }

        // Restore Profile & Settings via Repositories
        data.userProfile?.let { userRepository.updateUserProfile(it) }
        data.userSettings?.let { userRepository.updateUserSettings(it) }
        data.financeSettings?.let { financeRepository.updateSettings(it) }
        data.habitSettings?.let { habitRepository.updateSettings(it) }
        data.workoutSettings?.let { workoutRepository.updateSettings(it) }
        data.noteSettings?.let { noteRepository.updateSettings(it) }
        data.projectSettings?.let { projectRepository.updateSettings(it) }
    }

    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }
}
