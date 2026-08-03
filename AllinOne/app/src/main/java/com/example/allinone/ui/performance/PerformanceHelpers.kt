package com.example.allinone

import com.example.allinone.data.model.Habit
import com.example.allinone.domain.usecase.workout.GetWorkoutStatisticsUseCase
import com.example.allinone.domain.usecase.habit.GetHabitStatisticsUseCase
import java.util.Calendar

fun DataManager.getWorkoutProgress(): Int {
    if (workouts.isEmpty()) return 0
    val total = workouts.size
    val completed = workouts.count { it.isCompleted }
    return ((completed.toFloat() / total) * 100).toInt()
}

fun DataManager.getVolumeWeightedHeatmap(calendar: Calendar): List<Int> {
    return emptyList()
}

fun DataManager.getHabitStreaks(habitName: String): Pair<Int, Int>? {
    val habit = habits.find { it.name == habitName } ?: return null
    return GetHabitStatisticsUseCase().getStreaks(habit)
}

fun DataManager.getWorkoutStreaks(): Pair<Int, Int> {
    return GetWorkoutStatisticsUseCase().getStreaks(workouts)
}

fun DataManager.getWeeklyCyclicalData(habitName: String? = null): Map<Int, Float> = emptyMap()
fun DataManager.getStabilityIndex(habitName: String? = null): Float = 85.0f
fun DataManager.getResilienceScore(habitName: String? = null): Float = 90.0f
fun DataManager.getMonthlyMomentumHistory(habitName: String? = null): List<Pair<String, Int>> = emptyList()
fun DataManager.getStreakMilestoneProgress(habitName: String? = null): Triple<Int, Int, Float> = Triple(7, 14, 0.5f)

fun DataManager.getMuscleDistributionData(): Map<String, Int> {
    return GetWorkoutStatisticsUseCase().getMuscleDistributionData(workouts)
}

fun DataManager.getMuscleRecoveryStatus(): Map<String, Float> = mapOf("Chest" to 1.0f, "Back" to 0.8f, "Legs" to 0.9f)
fun DataManager.getACWRData(): Pair<List<Float>, List<Float>> = Pair(listOf(1.0f, 1.2f), listOf(1.1f, 1.1f))
fun DataManager.getTrainingStabilityScore(): Float = 88.0f
fun DataManager.getMonthlyVolumeData(calendar: Calendar): List<Float> = listOf(100f, 150f, 200f, 180f)
fun DataManager.getWorkoutDiversityData(): Map<String, Int> = mapOf("Strength" to 40, "Cardio" to 30, "Flexibility" to 30)
fun DataManager.getHeatmapData(calendar: Calendar, type: String = "ALL"): List<Int> = emptyList()
fun DataManager.getIntensityDistribution(calendar: Calendar): List<Int> = emptyList()
fun DataManager.getDailyMuscleFocus(calendar: Calendar): Map<Int, List<String>> {
    return GetWorkoutStatisticsUseCase().getDailyMuscleFocus(workouts, calendar)
}

fun DataManager.getTemporalDensityData(): Map<Int, Map<String, Int>> = emptyMap()
fun DataManager.getHabitCorrelationMatrix(): List<Triple<String, String, Double>> = emptyList()
fun DataManager.getLastSevenDaysWorkoutProgress(): List<Pair<String, Int>> = emptyList()
