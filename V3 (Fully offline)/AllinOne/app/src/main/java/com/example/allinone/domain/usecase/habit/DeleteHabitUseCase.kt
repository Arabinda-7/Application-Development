package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import javax.inject.Inject

class DeleteHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.deleteHabit(habit)
    }
}
