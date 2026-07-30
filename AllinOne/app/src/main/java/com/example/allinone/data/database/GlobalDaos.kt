package com.example.allinone.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppTaskDao {
    @Query("SELECT * FROM app_tasks ORDER BY priority DESC, timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM app_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()

    @Query("DELETE FROM app_tasks WHERE timestamp NOT IN (:ids)")
    suspend fun deleteOthers(ids: List<Long>)

    @Query("DELETE FROM app_tasks")
    suspend fun deleteAll()

    @Query("SELECT * FROM app_tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>
}

@Dao
interface AppHabitDao {
    @Query("SELECT * FROM app_habits ORDER BY timestamp DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<HabitEntity>)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM app_habits WHERE timestamp NOT IN (:ids)")
    suspend fun deleteOthers(ids: List<Long>)

    @Query("DELETE FROM app_habits")
    suspend fun deleteAll()

    @Query("SELECT * FROM app_habits")
    suspend fun getAllHabitsSync(): List<HabitEntity>
}

@Dao
interface AppWorkoutDao {
    @Query("SELECT * FROM app_workouts ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkouts(workouts: List<WorkoutEntity>)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM app_workouts WHERE timestamp NOT IN (:ids)")
    suspend fun deleteOthers(ids: List<Long>)

    @Query("DELETE FROM app_workouts")
    suspend fun deleteAll()

    @Query("SELECT * FROM app_workouts")
    suspend fun getAllWorkoutsSync(): List<WorkoutEntity>
}

@Dao
interface AppNoteDao {
    @Query("SELECT * FROM app_notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<NoteEntity>)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM app_notes WHERE timestamp NOT IN (:ids)")
    suspend fun deleteOthers(ids: List<Long>)

    @Query("DELETE FROM app_notes")
    suspend fun deleteAll()

    @Query("SELECT * FROM app_notes")
    suspend fun getAllNotesSync(): List<NoteEntity>
}

@Dao
interface AppFinanceDao {
    @Query("SELECT * FROM app_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM app_personal_ledgers ORDER BY timestamp DESC")
    fun getAllPersonalLedgers(): Flow<List<PersonalLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalLedger(ledger: PersonalLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPersonalLedgers(ledgers: List<PersonalLedgerEntity>)

    @Query("SELECT * FROM app_standalone_ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLedgerEntries(entries: List<LedgerEntryEntity>)

    @Query("DELETE FROM app_transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM app_transactions WHERE id NOT IN (:ids)")
    suspend fun deleteOtherTransactions(ids: List<String>)

    @Query("DELETE FROM app_personal_ledgers WHERE id NOT IN (:ids)")
    suspend fun deleteOtherPersonalLedgers(ids: List<String>)

    @Query("DELETE FROM app_standalone_ledger_entries WHERE id NOT IN (:ids)")
    suspend fun deleteOtherLedgerEntries(ids: List<String>)

    @Query("SELECT SUM(amount) FROM app_transactions WHERE type = :type AND timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getSumByTypeInRange(type: String, startTime: Long, endTime: Long): Double?

    @Query("SELECT * FROM app_transactions")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM app_personal_ledgers")
    suspend fun getAllPersonalLedgersSync(): List<PersonalLedgerEntity>

    @Query("SELECT * FROM app_standalone_ledger_entries")
    suspend fun getAllLedgerEntriesSync(): List<LedgerEntryEntity>

    @Query("DELETE FROM app_personal_ledgers")
    suspend fun deleteAllPersonalLedgers()

    @Query("DELETE FROM app_standalone_ledger_entries")
    suspend fun deleteAllLedgerEntries()
}
