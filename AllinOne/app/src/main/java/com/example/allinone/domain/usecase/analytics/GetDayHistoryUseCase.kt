package com.example.allinone.domain.usecase.analytics

import com.example.allinone.*
import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Workout
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetDayHistoryUseCase @Inject constructor() {

    operator fun invoke(
        dateKey: String,
        history: Map<String, DayHistory>,
        habits: List<Habit>,
        workouts: List<Workout>
    ): DayHistory {
        val snapshot = history[dateKey]
        if (snapshot != null && dateKey != SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())) {
            return snapshot
        }
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try { sdf.parse(dateKey)?.let { cal.time = it } } catch (e: Exception) {}
        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1)
        val endOfDay = cal.apply { 
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val activeHabits = habits.filter { 
            it.timestamp <= endOfDay && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) 
        }
        val activeWorkouts = workouts.filter { 
            it.timestamp <= endOfDay && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) 
        }

        return DayHistory(
            habitsCompleted = activeHabits.count { it.completedDates.contains(dateKey) || (dateKey == sdf.format(Date()) && it.isCompleted) },
            totalHabits = activeHabits.size,
            workoutsCompleted = activeWorkouts.count { it.completedDates.contains(dateKey) || (dateKey == sdf.format(Date()) && it.isCompleted) },
            totalWorkouts = activeWorkouts.size,
            workoutDetails = activeWorkouts.map { w ->
                val progressValue = ((w.dailyProgress[dateKey] ?: 0).toLong() * w.target / 100).toInt()
                WorkoutProgressEntry(
                    w.name, 
                    progressValue, 
                    w.target, 
                    w.trackingMode, 
                    w.color, 
                    w.completedDates.contains(dateKey) || (dateKey == sdf.format(Date()) && w.isCompleted)
                )
            },
            notes = history[dateKey]?.notes ?: ""
        )
    }
}
