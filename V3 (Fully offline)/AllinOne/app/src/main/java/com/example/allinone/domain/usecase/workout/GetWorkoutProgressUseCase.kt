package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import java.util.*
import javax.inject.Inject

class GetWorkoutProgressUseCase @Inject constructor() {
    operator fun invoke(workouts: List<Workout>): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysWorkouts = workouts.filter {
            (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex))
        }
        if (todaysWorkouts.isEmpty()) return 0
        val completed = todaysWorkouts.count { it.isCompleted }
        return (completed * 100) / todaysWorkouts.size
    }
}
