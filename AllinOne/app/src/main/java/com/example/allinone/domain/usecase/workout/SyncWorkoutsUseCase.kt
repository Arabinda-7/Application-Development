package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutRepository
import javax.inject.Inject

class SyncWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workouts: List<Workout>) = repository.syncAll(workouts)
}
