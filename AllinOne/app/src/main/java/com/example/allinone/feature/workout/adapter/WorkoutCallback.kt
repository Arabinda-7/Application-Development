package com.example.allinone.feature.workout.adapter

import com.example.allinone.data.model.Workout

/**
 * WorkoutCallback: Callback listener interface for workout item interactions.
 */
interface WorkoutCallback {
    fun onWorkoutClicked(workout: Workout)
    fun onWorkoutProgressChanged(workout: Workout, newProgress: Int)
    fun onWorkoutTimerStart(workout: Workout, seconds: Int)
    fun onWorkoutDelete(workout: Workout)
    fun onHeaderClicked(headerText: String)
}
