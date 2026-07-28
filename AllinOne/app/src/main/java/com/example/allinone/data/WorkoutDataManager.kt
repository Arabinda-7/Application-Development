package com.example.allinone.data

import com.example.allinone.Workout
import com.example.allinone.R
import java.util.*

object WorkoutDataManager {
    var workouts: MutableList<Workout> = java.util.Collections.synchronizedList(mutableListOf<Workout>())
    
    var workoutMuscleGroups = java.util.Collections.synchronizedList(mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body"))
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
        val todaysWorkouts = synchronized(workouts) {
            workouts.filter {
                (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex))
            }
        }
        if (todaysWorkouts.isEmpty()) return 0
        val completed = todaysWorkouts.count { it.isCompleted }
        return (completed * 100) / todaysWorkouts.size
    }

    fun getWorkoutStreaks(): Pair<Int, Int> {
        val allCompletedDates = synchronized(workouts) {
            workouts.flatMap { it.completedDates }.distinct().toSet()
        }
        if (allCompletedDates.isEmpty()) return 0 to 0

        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        
        // 1. Calculate Current Streak
        var currentStreak = 0
        val checkCal = Calendar.getInstance()
        val today = sdf.format(checkCal.time)
        
        // If nothing today, check yesterday to see if streak is still alive
        if (!allCompletedDates.contains(today)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (currentStreak < 1000) {
            val dateStr = sdf.format(checkCal.time)
            val dayOfWeek = (checkCal.get(Calendar.DAY_OF_WEEK) - 1)
            
            val wasAnythingScheduled = synchronized(workouts) {
                workouts.any { 
                    (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                    it.timestamp <= checkCal.timeInMillis + 86400000
                }
            }

            if (allCompletedDates.contains(dateStr)) {
                currentStreak++
            } else if (wasAnythingScheduled) {
                // Was scheduled but not completed -> streak breaks
                break
            } else {
                // Not scheduled -> streak continues through this day
            }
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        // 2. Calculate Best Streak (Simplified for now, but respects gaps)
        var bestStreak = 0
        var tempStreak = 0
        
        // We iterate from the first workout ever to today
        val firstWorkoutTime = synchronized(workouts) { workouts.minByOrNull { it.timestamp }?.timestamp } ?: return currentStreak to currentStreak
        val iterCal = Calendar.getInstance().apply { timeInMillis = firstWorkoutTime }
        val endCal = Calendar.getInstance()

        while (!iterCal.after(endCal)) {
            val dateStr = sdf.format(iterCal.time)
            val dayOfWeek = (iterCal.get(Calendar.DAY_OF_WEEK) - 1)
            
            val wasAnythingScheduled = synchronized(workouts) {
                workouts.any { 
                    (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                    it.timestamp <= iterCal.timeInMillis + 86400000
                }
            }

            if (allCompletedDates.contains(dateStr)) {
                tempStreak++
            } else if (wasAnythingScheduled) {
                tempStreak = 0
            }

            if (tempStreak > bestStreak) bestStreak = tempStreak
            iterCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return currentStreak to bestStreak
    }

    fun getWorkoutStreak(): Int {
        return getWorkoutStreaks().first
    }

    fun getWorkoutsThisMonth(): Int {
        val sdf = java.text.SimpleDateFormat("yyyyMM", java.util.Locale.getDefault())
        val currentMonth = sdf.format(Date())
        return synchronized(workouts) {
            workouts.flatMap { it.completedDates }
                .distinct()
                .count { it.startsWith(currentMonth) }
        }
    }

    fun getTotalWorkoutsFinished(): Int {
        return synchronized(workouts) { workouts.sumOf { it.completedDates.size } }
    }
    
    fun getTodayCaloriesBurned(): Int {
        val todayWorkouts = synchronized(workouts) { workouts.filter { it.isCompleted } }
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
