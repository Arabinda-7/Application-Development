package com.example.allinone.domain.usecase.habit

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TrackHabitCompletionUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit, progress: Int, isCompleted: Boolean, date: String? = null) {
        val today = date ?: SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val updatedHabit = habit.copy(
            progress = progress,
            isCompleted = isCompleted
        )
        
        // Update history
        if (isCompleted) {
            if (!updatedHabit.completedDates.contains(today)) {
                updatedHabit.completedDates.add(today)
            }
        } else {
            updatedHabit.completedDates.remove(today)
        }
        
        updatedHabit.dailyProgress[today] = progress
        
        habitRepository.updateHabit(updatedHabit)
    }
}
