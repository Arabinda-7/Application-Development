package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) = repository.insertWorkout(workout)
}
