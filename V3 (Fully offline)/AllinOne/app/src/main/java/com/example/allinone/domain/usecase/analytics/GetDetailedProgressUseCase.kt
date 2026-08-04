package com.example.allinone.domain.usecase.analytics

import com.example.allinone.DayHistory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetDetailedProgressUseCase @Inject constructor() {

    fun getHabitProgress(history: Map<String, DayHistory>): List<Pair<Int, Int>> {
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

    fun getWorkoutProgress(history: Map<String, DayHistory>): List<Pair<Int, Int>> {
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
}
