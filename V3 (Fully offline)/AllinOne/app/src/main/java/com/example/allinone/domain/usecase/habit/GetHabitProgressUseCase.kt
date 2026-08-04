package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import java.util.*
import javax.inject.Inject

class GetHabitProgressUseCase @Inject constructor() {
    operator fun invoke(habits: List<Habit>): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysHabits = habits.filter {
            (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex))
        }
        if (todaysHabits.isEmpty()) return 0
        val completed = todaysHabits.count { it.isCompleted }
        return (completed * 100) / todaysHabits.size
    }

    fun getOverallProgress(habits: List<Habit>, workouts: List<com.example.allinone.data.model.Workout>): Int {
        val hp = invoke(habits)
        // Need to calculate workout progress too
        return hp // Placeholder
    }
}
