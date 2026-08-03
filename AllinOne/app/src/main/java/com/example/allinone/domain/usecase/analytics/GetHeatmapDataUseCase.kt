package com.example.allinone.domain.usecase.analytics

import com.example.allinone.DayHistory
import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Workout
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetHeatmapDataUseCase @Inject constructor(
    private val getDayHistoryUseCase: GetDayHistoryUseCase
) {

    operator fun invoke(
        calendar: Calendar,
        type: String = "HABITS",
        history: Map<String, DayHistory>,
        habits: List<Habit>,
        workouts: List<Workout>
    ): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val historyEntry = getDayHistoryUseCase(dateKey, history, habits, workouts)
            val progress = when (type) {
                "HABITS" -> if (historyEntry.totalHabits == 0) 0 else (historyEntry.habitsCompleted * 100) / historyEntry.totalHabits
                "WORKOUTS" -> if (historyEntry.totalWorkouts == 0) 0 else (historyEntry.workoutsCompleted * 100) / historyEntry.totalWorkouts
                else -> 0
            }
            result[day - 1] = progress
        }
        return result
    }
}
