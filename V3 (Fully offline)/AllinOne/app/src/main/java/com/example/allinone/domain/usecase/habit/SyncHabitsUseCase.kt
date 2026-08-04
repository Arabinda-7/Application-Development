package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import javax.inject.Inject

class SyncHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habits: List<Habit>) = repository.syncAll(habits)
}
