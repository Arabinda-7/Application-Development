package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.updateHabit(habit)
    }
}
