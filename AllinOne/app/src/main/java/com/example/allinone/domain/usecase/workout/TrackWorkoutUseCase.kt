package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import com.example.allinone.domain.usecase.user.AddXPUseCase
import javax.inject.Inject

class TrackWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val addXPUseCase: AddXPUseCase
) {
    suspend operator fun invoke(workout: Workout, progress: Int, isCompleted: Boolean, dateKey: String) {
        val updatedWorkout = workout.copy(
            progress = progress,
            isCompleted = isCompleted
        ).apply {
            dailyProgress[dateKey] = progress
            if (isCompleted) {
                if (!completedDates.contains(dateKey)) {
                    completedDates.add(dateKey)
                }
            } else {
                completedDates.remove(dateKey)
            }
        }
        
        workoutRepository.updateWorkout(updatedWorkout)
        
        if (isCompleted) {
            addXPUseCase(15) // Workouts give more XP
        }
    }
}
