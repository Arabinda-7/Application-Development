package com.example.allinone.data

import com.example.allinone.Workout
import com.example.allinone.R
import java.util.*

object WorkoutDataManager {
    var workouts = mutableListOf<Workout>()
    
    var workoutMuscleGroups = mutableListOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Cardio", "Full Body")
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

    fun getWorkoutStreak(): Int {
        return 0
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
