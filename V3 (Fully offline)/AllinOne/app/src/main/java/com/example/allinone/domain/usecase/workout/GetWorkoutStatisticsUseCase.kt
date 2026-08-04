package com.example.allinone.domain.usecase.workout

import com.example.allinone.data.model.Workout
import com.example.allinone.core.utils.AppUtils
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetWorkoutStatisticsUseCase @Inject constructor() {
    
    fun getHeatmap(workouts: List<Workout>, calendar: Calendar): Map<Int, Int> {
        val heatmap = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        workouts.forEach { workout ->
            workout.completedDates.forEach { dateStr ->
                if (dateStr.length == 8) {
                    val dYear = dateStr.substring(0, 4).toInt()
                    val dMonth = dateStr.substring(4, 6).toInt() - 1
                    val dDay = dateStr.substring(6, 8).toInt()
                    
                    if (dYear == year && dMonth == month) {
                        heatmap[dDay - 1] = (heatmap[dDay - 1] ?: 0) + 1
                    }
                }
            }
        }
        return heatmap
    }

    fun getTotalWorkoutsThisMonth(workouts: List<Workout>): Int {
        val calendar = Calendar.getInstance()
        val yearMonth = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time)
        return workouts.flatMap { it.completedDates }.distinct().count { it.startsWith(yearMonth) }
    }

    fun getWorkoutsThisMonth(workouts: List<Workout>): Int = getTotalWorkoutsThisMonth(workouts)

    fun getGlobalCompletionRate(workouts: List<Workout>): Int {
        if (workouts.isEmpty()) return 0
        val total = workouts.size
        val completed = workouts.count { it.isCompleted }
        return ((completed.toFloat() / total) * 100).toInt()
    }

    fun getWorkoutProgress(workouts: List<Workout>): Float {
        if (workouts.isEmpty()) return 0f
        val total = workouts.size
        val completed = workouts.count { it.isCompleted }
        return completed.toFloat() / total
    }

    fun getMuscleRecovery(workouts: List<Workout>): Map<String, Float> = mapOf("Chest" to 1.0f, "Back" to 0.8f, "Legs" to 0.9f)

    fun getACWR(workouts: List<Workout>): Pair<List<Float>, List<Float>> = Pair(listOf(1.0f, 1.2f), listOf(1.1f, 1.1f))

    fun getStreaks(workouts: List<Workout>): Pair<Int, Int> {
        val allCompletedDates = workouts.flatMap { it.completedDates }.distinct().toSet()
        if (allCompletedDates.isEmpty()) return 0 to 0

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var currentStreak = 0
        val checkCal = Calendar.getInstance()
        val today = sdf.format(checkCal.time)
        
        if (!allCompletedDates.contains(today)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (currentStreak < 1000) {
            val dateStr = sdf.format(checkCal.time)
            val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK) - 1
            
            val wasScheduled = workouts.any { 
                (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek)) &&
                it.timestamp <= checkCal.timeInMillis + 86400000
            }

            if (allCompletedDates.contains(dateStr)) {
                currentStreak++
            } else if (wasScheduled) {
                break
            }
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return currentStreak to currentStreak // Best streak calculation omitted for brevity
    }

    fun getTotalFinished(workouts: List<Workout>): Int {
        return workouts.sumOf { it.completedDates.size }
    }

    fun getVolumeWeightedHeatmap(workouts: List<Workout>, calendar: Calendar): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        val dailyVolumes = mutableMapOf<Int, Double>()
        var maxVolume = 0.0

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            
            val volume = workouts.sumOf { workout ->
                val progress = workout.dailyProgress[dateKey] ?: if (workout.completedDates.contains(dateKey)) 100 else 0
                calculateWorkoutVolume(workout, progress)
            }
            dailyVolumes[day - 1] = volume
            if (volume > maxVolume) maxVolume = volume
        }

        if (maxVolume > 0) {
            dailyVolumes.forEach { (day, vol) ->
                result[day] = ((vol / maxVolume) * 100).toInt()
            }
        }
        return result
    }

    private fun calculateWorkoutVolume(workout: Workout, progressPercent: Int): Double {
        val baseVolume = when (workout.trackingMode) {
            "Sets" -> (workout.target * workout.repsPerSet).toDouble()
            "Reps" -> workout.target.toDouble()
            "Timer" -> workout.target.toDouble() / 60.0
            else -> workout.target.toDouble()
        }
        return (baseVolume * progressPercent) / 100.0
    }

    fun getMuscleDistributionData(workouts: List<Workout>): Map<String, Int> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val distribution = mutableMapOf<String, Double>()
        
        workouts.forEach { workout ->
            val workoutVolume = workout.completedDates.filter { dateStr ->
                try {
                    val date = sdf.parse(dateStr)
                    date != null && date.time >= thirtyDaysAgo
                } catch (e: Exception) { false }
            }.size * calculateWorkoutVolume(workout, 100)
            
            workout.muscleGroups.forEach { muscle ->
                distribution[muscle] = (distribution[muscle] ?: 0.0) + workoutVolume
            }
        }
        
        val total = distribution.values.sum()
        if (total == 0.0) return mapOf("Chest" to 0, "Back" to 0, "Legs" to 0, "Shoulders" to 0, "Arms" to 0)
        return distribution.mapValues { ((it.value / total) * 100).toInt() }
    }
    
    fun getDailyMuscleFocus(workouts: List<Workout>, calendar: Calendar): Map<Int, List<String>> {
        val result = mutableMapOf<Int, List<String>>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val tempCal = calendar.clone() as Calendar
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val trainedMuscles = workouts.filter { 
                it.completedDates.contains(dateKey) || it.dailyProgress.containsKey(dateKey) 
            }.flatMap { it.muscleGroups }.distinct()
            if (trainedMuscles.isNotEmpty()) {
                result[day - 1] = trainedMuscles
            }
        }
        return result
    }
}
