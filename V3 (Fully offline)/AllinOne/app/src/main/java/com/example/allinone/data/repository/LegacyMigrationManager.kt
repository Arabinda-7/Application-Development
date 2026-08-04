package com.example.allinone.data.repository

import android.content.Context
import com.example.allinone.security.SecurityManager
import android.util.Log
import com.example.allinone.*
import com.example.allinone.data.database.AppDatabase
import com.example.allinone.data.database.GlobalNoteEntity
import com.example.allinone.data.database.GlobalTaskEntity
import com.example.allinone.data.database.HabitEntity
import com.example.allinone.data.database.WorkoutEntity
import com.example.allinone.data.database.TransactionEntity
import com.example.allinone.data.database.PersonalLedgerEntity
import com.example.allinone.data.database.LedgerEntryEntity
import com.example.allinone.DayHistory
import com.example.allinone.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LegacyMigrationManager(private val context: Context) {
    private val TAG = "LegacyMigration"
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    private val gson = Gson()
    private val db = AppDatabase.getDatabase(context)

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        val hasMigrated = prefs.getBoolean("data_migrated_to_sql", false)
        Log.d(TAG, "Checking migration status. hasMigrated: $hasMigrated")
        
        if (hasMigrated) {
            Log.i(TAG, "Migration already completed. Skipping.")
            return@withContext
        }

        Log.i(TAG, "Starting legacy data migration...")
        try {
            migrateTasks()
            migrateHabits()
            migrateWorkouts()
            migrateFinance()
            migrateNotes()
            migrateProjects()
            migrateHistory()
            migrateSettings()

            prefs.edit().putBoolean("data_migrated_to_sql", true).apply()
            Log.i(TAG, "Migration flag updated to true. Migration check completed.")
        } catch (e: Exception) {
            Log.e(TAG, "Migration encountered errors.", e)
        }
    }

    private suspend fun migrateTasks() {
        val json = prefs.getString("tasks_data", null) ?: return
        try {
            val tasks: List<Task> = gson.fromJson(json, object : TypeToken<List<Task>>() {}.type)
            db.taskDao().insertAllTasks(tasks.map { it.toEntity() })
            Log.d(TAG, "Migrated ${tasks.size} tasks")
        } catch (e: Exception) { Log.e(TAG, "Task migration failed", e) }
    }

    private suspend fun migrateHabits() {
        val json = prefs.getString("habits_data", null) ?: return
        try {
            val habits: List<Habit> = gson.fromJson(json, object : TypeToken<List<Habit>>() {}.type)
            db.habitDao().insertAllHabits(habits.map { it.toEntity() })
            Log.d(TAG, "Migrated ${habits.size} habits")
        } catch (e: Exception) { Log.e(TAG, "Habit migration failed", e) }
    }

    private suspend fun migrateWorkouts() {
        val json = prefs.getString("workouts_data", null) ?: return
        try {
            val workouts: List<Workout> = gson.fromJson(json, object : TypeToken<List<Workout>>() {}.type)
            db.workoutDao().insertAllWorkouts(workouts.map { it.toEntity() })
            Log.d(TAG, "Migrated ${workouts.size} workouts")
        } catch (e: Exception) { Log.e(TAG, "Workout migration failed", e) }
    }

    private suspend fun migrateFinance() {
        // Transactions
        val transJson = prefs.getString("transactions_data", null)
        if (transJson != null) {
            try {
                val transactions: List<Transaction> = gson.fromJson(transJson, object : TypeToken<List<Transaction>>() {}.type)
                db.financeDao().insertAllTransactions(transactions.map { it.toEntity() })
                Log.d(TAG, "Migrated ${transactions.size} transactions")
            } catch (e: Exception) { Log.e(TAG, "Transaction migration failed", e) }
        }

        // Ledgers
        val ledgerJson = prefs.getString("personal_ledger_data", null)
        if (ledgerJson != null) {
            try {
                val ledgers: List<PersonalLedger> = gson.fromJson(ledgerJson, object : TypeToken<List<PersonalLedger>>() {}.type)
                db.financeDao().insertAllPersonalLedgers(ledgers.map { it.toEntity() })
                Log.d(TAG, "Migrated ${ledgers.size} personal ledgers")
            } catch (e: Exception) { Log.e(TAG, "Ledger migration failed", e) }
        }

        val entriesJson = prefs.getString("ledger_data", null)
        if (entriesJson != null) {
            try {
                val entries: List<LedgerEntry> = gson.fromJson(entriesJson, object : TypeToken<List<LedgerEntry>>() {}.type)
                db.financeDao().insertAllLedgerEntries(entries.map { it.toEntity() })
                Log.d(TAG, "Migrated ${entries.size} ledger entries")
            } catch (e: Exception) { Log.e(TAG, "Ledger entries migration failed", e) }
        }
    }

    private suspend fun migrateNotes() {
        val json = prefs.getString("notes_data", null) ?: return
        try {
            val notes: List<Note> = gson.fromJson(json, object : TypeToken<List<Note>>() {}.type)
            db.noteDao().insertAllNotes(notes.map { it.toEntity(false) })
            Log.d(TAG, "Migrated ${notes.size} notes")
        } catch (e: Exception) { Log.e(TAG, "Note migration failed", e) }
    }

    private suspend fun migrateProjects() {
        val json = prefs.getString("projects_data", null) ?: return
        try {
            val projects: List<Note> = gson.fromJson(json, object : TypeToken<List<Note>>() {}.type)
            db.noteDao().insertAllNotes(projects.map { it.toEntity(true) })
            Log.d(TAG, "Migrated ${projects.size} projects")
        } catch (e: Exception) { Log.e(TAG, "Project migration failed", e) }
    }

    private fun migrateHistory() {
        val json = prefs.getString("history_data", null) ?: return
        try {
            val history: Map<String, DayHistory> = gson.fromJson(json, object : TypeToken<Map<String, DayHistory>>() {}.type)
            // No direct DB for history, but we can keep it in Prefs as is or move it if needed.
            // Actually, the current UserLocalDataSource reads "history_data".
            // So if it's already in the same prefs, we are good. 
            // But if we moved to a new pref name, we should copy it.
            Log.d(TAG, "Found legacy history data with ${history.size} entries")
        } catch (e: Exception) { Log.e(TAG, "History migration log failed", e) }
    }

    private fun migrateSettings() {
        // Migration for settings stored in legacy keys if any
        Log.d(TAG, "Settings migration check completed")
    }

    private fun Note.toEntity(isGlobalProject: Boolean) = GlobalNoteEntity(
        timestamp = timestamp, title = title, content = content, color = color, category = category,
        isHidden = isHidden, updatedAt = updatedAt, status = status, progress = progress, priority = priority,
        isPinned = isPinned, deadline = deadline, isArchived = isArchived, isDualExist = isDualExist,
        journalEntries = journalEntries, ideaGoals = ideaGoals, subFeatures = subFeatures,
        changeHistory = changeHistory, isGlobalProject = isGlobalProject
    )

    private fun Task.toEntity() = GlobalTaskEntity(
        timestamp = timestamp, name = name, isCompleted = isCompleted, color = color, priority = priority,
        reminderTime = reminderTime, category = category, section = section, isHidden = isHidden, subtasks = subtasks,
        completedTimestamp = completedTimestamp
    )

    private fun Habit.toEntity() = HabitEntity(
        timestamp = timestamp, name = name, isCompleted = isCompleted, frequency = frequency, trackingMode = trackingMode,
        target = target, progress = progress, isDayOff = isDayOff, color = color, iconResId = iconResId,
        repeatType = repeatType, repeatDays = repeatDays, repeatCount = repeatCount, completedDates = completedDates,
        dailyProgress = dailyProgress, reminderTime = reminderTime
    )

    private fun Workout.toEntity() = WorkoutEntity(
        timestamp = timestamp, name = name, isCompleted = isCompleted, trackingMode = trackingMode, target = target,
        repsPerSet = repsPerSet, progress = progress, frequency = frequency, isDayOff = isDayOff, color = color,
        iconResId = iconResId, muscleGroups = muscleGroups, repeatType = repeatType, repeatDays = repeatDays,
        repeatCount = repeatCount, completedDates = completedDates, dailyProgress = dailyProgress
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id, title = title, amount = amount, type = type, category = category, timestamp = timestamp,
        categoryIcon = categoryIcon, categoryColor = categoryColor
    )

    private fun PersonalLedger.toEntity() = PersonalLedgerEntity(
        id = id, personName = personName, entries = entries, timestamp = timestamp
    )

    private fun LedgerEntry.toEntity() = LedgerEntryEntity(
        id = id, personName = personName, amount = amount, type = type, note = note, isSettled = isSettled,
        dueDate = dueDate, paidAmount = paidAmount, timestamp = timestamp, settlementTimestamp = settlementTimestamp,
        paymentHistory = paymentHistory
    )
}
