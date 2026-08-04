package com.example.allinone

import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.usecase.workout.GetWorkoutStatisticsUseCase
import com.example.allinone.domain.usecase.habit.GetHabitStatisticsUseCase
import java.text.SimpleDateFormat
import java.util.*

fun DataManager.getWorkoutProgress(): Int {
    if (workouts.isEmpty()) return 0
    val total = workouts.size
    val completed = workouts.count { it.isCompleted }
    return ((completed.toFloat() / total) * 100).toInt()
}

fun DataManager.getVolumeWeightedHeatmap(calendar: Calendar): List<Int> {
    return getHeatmapData(calendar, "WORKOUTS")
}

fun DataManager.getHabitStreaks(habitName: String): Pair<Int, Int>? {
    val habit = habits.find { it.name == habitName } ?: return null
    return GetHabitStatisticsUseCase().getStreaks(habit)
}

fun DataManager.getWorkoutStreaks(): Pair<Int, Int> {
    return GetWorkoutStatisticsUseCase().getStreaks(workouts)
}

fun DataManager.getWeeklyCyclicalData(habitName: String? = null): Map<Int, Float> {
    val filtered = if (habitName == null) habits else habits.filter { it.name == habitName }
    return GetHabitStatisticsUseCase().getWeeklyCyclicalData(filtered)
}

fun DataManager.getStabilityIndex(habitName: String? = null): Float {
    val filtered = if (habitName == null) habits else habits.filter { it.name == habitName }
    return GetHabitStatisticsUseCase().getStabilityIndex(filtered)
}

fun DataManager.getResilienceScore(habitName: String? = null): Float {
    val filtered = if (habitName == null) habits else habits.filter { it.name == habitName }
    return GetHabitStatisticsUseCase().getResilienceScore(filtered)
}

fun DataManager.getMonthlyMomentumHistory(habitName: String? = null): List<Pair<String, Int>> {
    val filtered = if (habitName == null) habits else habits.filter { it.name == habitName }
    return GetHabitStatisticsUseCase().getMonthlyMomentumHistory(filtered)
}

fun DataManager.getStreakMilestoneProgress(habitName: String? = null): Triple<Int, Int, Float> {
    return GetHabitStatisticsUseCase().getStreakMilestoneProgress(habits, habitName)
}

fun DataManager.getMuscleDistributionData(): Map<String, Int> {
    return GetWorkoutStatisticsUseCase().getMuscleDistributionData(workouts)
}

fun DataManager.getMuscleRecoveryStatus(): Map<String, Float> = GetWorkoutStatisticsUseCase().getMuscleRecovery(workouts)
fun DataManager.getACWRData(): Pair<List<Float>, List<Float>> = GetWorkoutStatisticsUseCase().getACWR(workouts)
fun DataManager.getTrainingStabilityScore(): Float = 88.0f // Placeholder
fun DataManager.getMonthlyVolumeData(calendar: Calendar): List<Float> {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val tempCal = calendar.clone() as Calendar
    
    return (1..days).map { d ->
        tempCal.set(year, month, d)
        val dateKey = sdf.format(tempCal.time)
        workouts.sumOf { w ->
            val progress = w.dailyProgress[dateKey] ?: if (w.completedDates.contains(dateKey)) 100 else 0
            val baseVolume = if (w.trackingMode == "Sets") (w.target * w.repsPerSet).toDouble() else w.target.toDouble()
            (baseVolume * progress) / 100.0
        }.toFloat()
    }
}

fun DataManager.getWorkoutDiversityData(): Map<String, Int> {
    val diversity = workouts.groupBy { it.muscleGroups.firstOrNull() ?: "General" }.mapValues { it.value.size }
    val total = diversity.values.sum().coerceAtLeast(1)
    return diversity.mapValues { (it.value * 100) / total }
}

fun DataManager.getHeatmapData(calendar: Calendar, type: String = "ALL"): List<Int> {
    val stats = GetHabitStatisticsUseCase()
    val wStats = GetWorkoutStatisticsUseCase()
    val days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    return when(type) {
        "HABITS" -> {
            val combined = mutableMapOf<Int, Int>()
            habits.forEach { h ->
                val hMap = stats.getHeatmap(h, calendar)
                hMap.forEach { (d, p) -> combined[d] = (combined[d] ?: 0) + p }
            }
            (0 until days).map { d -> if (habits.isEmpty()) 0 else (combined[d] ?: 0) / habits.size }
        }
        "WORKOUTS" -> {
            val hMap = wStats.getVolumeWeightedHeatmap(workouts, calendar)
            (0 until days).map { d -> hMap[d] ?: 0 }
        }
        else -> {
            val hCombined = mutableMapOf<Int, Int>()
            habits.forEach { h ->
                val hMap = stats.getHeatmap(h, calendar)
                hMap.forEach { (d, p) -> hCombined[d] = (hCombined[d] ?: 0) + p }
            }
            val wMap = wStats.getVolumeWeightedHeatmap(workouts, calendar)
            (0 until days).map { d -> 
                val hAvg = if (habits.isEmpty()) 0 else (hCombined[d] ?: 0) / habits.size
                val wVal = wMap[d] ?: 0
                (hAvg + wVal) / (if (habits.isNotEmpty() && workouts.isNotEmpty()) 2 else 1)
            }
        }
    }
}

fun DataManager.getIntensityDistribution(calendar: Calendar): List<Int> {
    return getHeatmapData(calendar, "WORKOUTS")
}

fun DataManager.getDailyMuscleFocus(calendar: Calendar): Map<Int, List<String>> {
    return GetWorkoutStatisticsUseCase().getDailyMuscleFocus(workouts, calendar)
}

fun DataManager.getTemporalDensityData(): Map<Int, Map<String, Int>> = GetHabitStatisticsUseCase().getTemporalDensityData(habits)
fun DataManager.getHabitCorrelationMatrix(): List<Triple<String, String, Double>> = GetHabitStatisticsUseCase().getHabitCorrelationMatrix(habits)
fun DataManager.getLastSevenDaysWorkoutProgress(): List<Pair<String, Int>> = emptyList()
