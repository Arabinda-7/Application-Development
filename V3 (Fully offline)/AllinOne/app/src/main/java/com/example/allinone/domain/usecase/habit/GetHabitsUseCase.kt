package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> = repository.getAllHabits()
}
