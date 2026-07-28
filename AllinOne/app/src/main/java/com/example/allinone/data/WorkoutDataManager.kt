package com.example.allinone.data

import com.example.allinone.Workout
import com.example.allinone.R
import java.util.*

object WorkoutDataManager {
    var workouts = mutableListOf<Workout>()
    
    var workoutMuscleGroups = mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body")
    var workoutFilterType: String = "TIME"
    var workoutAutoRestTimer: Boolean = false
    var workoutWeightUnit: String = "Kg"
    var workoutDefaultMode: String = "Reps"
    var workoutRestDuration: Int = 60
    var workoutShowCompleted: Boolean = true
    
    var globalWorkoutColor: Int = -1
    var workoutAddThemeColor: Int = -1
    var globalWorkoutIcon: Int = R.drawable.ic_workout_routine

    fun getWorkoutProgress(): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysWorkouts = workouts.filter {
            (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex))
        }
        if (todaysWorkouts.isEmpty()) return 0
        val completed = todaysWorkouts.count { it.isCompleted }
        return (completed * 100) / todaysWorkouts.size
    }

    fun getWorkoutStreaks(): Pair<Int, Int> {
        val allCompletedDates = workouts.flatMap { it.completedDates }.distinct().sortedDescending()
        if (allCompletedDates.isEmpty()) return 0 to 0

        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val today = sdf.format(Date())
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.let { sdf.format(it.time) }

        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0

        // Best Streak Calculation
        val allSortedAsc = allCompletedDates.reversed()
        var lastDate: Calendar? = null
        for (dateStr in allSortedAsc) {
            val date = Calendar.getInstance().apply { time = sdf.parse(dateStr)!! }
            if (lastDate == null) {
                tempStreak = 1
            } else {
                val diff = (date.timeInMillis - lastDate.timeInMillis) / (1000 * 60 * 60 * 24)
                if (diff == 1L) {
                    tempStreak++
                } else if (diff > 1L) {
                    tempStreak = 1
                }
            }
            lastDate = date
            if (tempStreak > bestStreak) bestStreak = tempStreak
        }

        // Current Streak Calculation
        if (allCompletedDates.contains(today) || allCompletedDates.contains(yesterday)) {
            val checkCal = Calendar.getInstance()
            if (!allCompletedDates.contains(today)) checkCal.add(Calendar.DAY_OF_YEAR, -1)

            while (allCompletedDates.contains(sdf.format(checkCal.time))) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                if (currentStreak > 1000) break
            }
        }

        return currentStreak to bestStreak
    }

    fun getWorkoutStreak(): Int {
        return getWorkoutStreaks().first
    }

    fun getWorkoutsThisMonth(): Int {
        val sdf = java.text.SimpleDateFormat("yyyyMM", java.util.Locale.getDefault())
        val currentMonth = sdf.format(Date())
        return workouts.flatMap { it.completedDates }
            .distinct()
            .count { it.startsWith(currentMonth) }
    }

    fun getTotalWorkoutsFinished(): Int {
        return workouts.sumOf { it.completedDates.size }
    }
    
    fun getTodayCaloriesBurned(): Int {
        val todayWorkouts = workouts.filter { it.isCompleted }
        var total = 0.0
        todayWorkouts.forEach { workout ->
            total += when (workout.trackingMode) {
                "Timer" -> workout.target * 0.1
                "Reps" -> workout.target * 0.5
                "Sets" -> workout.target * 5.0
                else -> 0.0
            }
        }
        return total.toInt()
    }
}
