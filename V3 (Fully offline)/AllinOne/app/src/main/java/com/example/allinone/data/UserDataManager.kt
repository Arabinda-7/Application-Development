package com.example.allinone.data

import com.example.allinone.DayHistory
import com.example.allinone.data.model.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserDataManager: Manages in-memory user collections (tasks, habits, workouts, notes, projects, transactions)
 * and performs daily performance calculation and analytics.
 */
@Singleton
class UserDataManager @Inject constructor() {

    val tasks: MutableList<Task> = Collections.synchronizedList(mutableListOf())
    val habits: MutableList<Habit> = Collections.synchronizedList(mutableListOf())
    val workouts: MutableList<Workout> = Collections.synchronizedList(mutableListOf())
    val notes: MutableList<Note> = Collections.synchronizedList(mutableListOf())
    val projects: MutableList<Note> = Collections.synchronizedList(mutableListOf())
    val transactions: MutableList<Transaction> = Collections.synchronizedList(mutableListOf())
    val ledgerEntries: MutableList<PersonalLedgerEntry> = Collections.synchronizedList(mutableListOf())
    val personalLedgers: MutableList<PersonalLedger> = Collections.synchronizedList(mutableListOf())
    val history: MutableMap<String, DayHistory> = Collections.synchronizedMap(mutableMapOf())

    fun calculateDayHistory(dateKey: String): DayHistory {
        val habitsComp = habits.count { it.completedDates.contains(dateKey) }
        val workoutsComp = workouts.count { it.completedDates.contains(dateKey) }
        return history[dateKey] ?: DayHistory(
            habitsCompleted = habitsComp,
            totalHabits = habits.size,
            workoutsCompleted = workoutsComp,
            totalWorkouts = workouts.size
        )
    }

    fun getDayHistory(dateKey: String): DayHistory? = history[dateKey]
}
