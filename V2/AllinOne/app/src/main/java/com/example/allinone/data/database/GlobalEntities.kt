package com.example.allinone.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allinone.*
import java.util.*

@Entity(tableName = "app_tasks")
data class TaskEntity(
    @PrimaryKey val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val isCompleted: Boolean = false,
    val color: Int = -1,
    val priority: Int = 0,
    val reminderTime: Long? = null,
    val category: String = "General",
    val section: String = "Tasks",
    val isHidden: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val completedTimestamp: Long? = null
)

@Entity(tableName = "app_habits")
data class HabitEntity(
    @PrimaryKey val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val isCompleted: Boolean,
    val frequency: String,
    val trackingMode: String = "Reps",
    val target: Int = 0,
    val progress: Int = 0,
    val isDayOff: Boolean = false,
    val color: Int = -1,
    val iconResId: Int = -1,
    val repeatType: String = "SPECIFIC_DAYS",
    val repeatDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val repeatCount: Int = 1,
    val completedDates: List<String> = emptyList(),
    val dailyProgress: Map<String, Int> = emptyMap(),
    val reminderTime: Long? = null
)

@Entity(tableName = "app_workouts")
data class WorkoutEntity(
    @PrimaryKey val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val isCompleted: Boolean,
    val trackingMode: String = "Reps",
    val target: Int = 0,
    val repsPerSet: Int = 0,
    val progress: Int = 0,
    val frequency: String = "Anytime",
    val isDayOff: Boolean = false,
    val color: Int = -1,
    val iconResId: Int = -1,
    val muscleGroups: List<String> = listOf("General"),
    val repeatType: String = "SPECIFIC_DAYS",
    val repeatDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val repeatCount: Int = 1,
    val completedDates: List<String> = emptyList(),
    val dailyProgress: Map<String, Int> = emptyMap()
)

@Entity(tableName = "app_transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val categoryIcon: Int = -1,
    val categoryColor: Int = -1
)

@Entity(tableName = "app_personal_ledgers")
data class PersonalLedgerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personName: String,
    val entries: List<PersonalLedgerEntry> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_standalone_ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personName: String,
    val amount: Double,
    val type: String,
    val note: String = "",
    val isSettled: Boolean = false,
    val dueDate: Long? = null,
    val paidAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val settlementTimestamp: Long? = null,
    val paymentHistory: List<LedgerPayment> = emptyList()
)

@Entity(tableName = "app_notes")
data class NoteEntity(
    @PrimaryKey val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val color: Int = -1,
    val category: String = "Notes",
    val isHidden: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "Not Started",
    val progress: Int = 0,
    val priority: Int = 1,
    val isPinned: Boolean = false,
    val deadline: Long? = null,
    val isArchived: Boolean = false,
    val isDualExist: Boolean = false,
    val journalEntries: List<JournalEntry> = emptyList(),
    val ideaGoals: List<JournalEntry> = emptyList(),
    val subFeatures: List<ProjectFeature> = emptyList(),
    val changeHistory: List<ProjectHistory> = emptyList(),
    val isGlobalProject: Boolean = false
)
