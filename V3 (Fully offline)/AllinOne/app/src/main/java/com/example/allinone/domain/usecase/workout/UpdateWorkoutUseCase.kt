package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import javax.inject.Inject

class UpdateWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) {
        workoutRepository.updateWorkout(workout)
    }
}
