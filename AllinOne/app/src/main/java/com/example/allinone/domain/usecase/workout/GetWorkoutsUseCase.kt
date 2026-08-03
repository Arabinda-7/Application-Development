package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> = workoutRepository.getAllWorkouts()
}
