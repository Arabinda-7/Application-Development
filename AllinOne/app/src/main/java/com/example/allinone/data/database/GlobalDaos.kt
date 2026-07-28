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

    @Query("DELETE FROM app_tasks")
    suspend fun deleteAll()
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

    @Query("DELETE FROM app_habits")
    suspend fun deleteAll()
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

    @Query("DELETE FROM app_workouts")
    suspend fun deleteAll()
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

    @Query("DELETE FROM app_notes")
    suspend fun deleteAll()
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
}
