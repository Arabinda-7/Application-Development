package com.example.allinone.data

import com.example.allinone.Habit
import com.example.allinone.R
import java.util.*

object HabitDataManager {
    var habits = mutableListOf<Habit>()
    
    var habitDefaultTab: String = "TODAY"
    var habitVacationMode: Boolean = false
    var habitSortOrder: String = "Time"
    var habitCompletionSound: Boolean = true
    var habitCompletionHaptics: Boolean = true
    var habitDayResetHour: Int = 0
    var habitBulkMode: Boolean = false
    var habitGraceDaysAllowed: Int = 1
    var habitShowCompleted: Boolean = true
    
    var globalHabitColor: Int = -1
    var habitAddThemeColor: Int = -1
    var globalHabitIcon: Int = R.drawable.ic_habit_tracker

    fun getHabitProgress(): Int {
        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaysHabits = habits.filter {
            (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex))
        }
        if (todaysHabits.isEmpty()) return 0
        val completed = todaysHabits.count { it.isCompleted }
        return (completed * 100) / todaysHabits.size
    }

    fun getHabitSpecificHeatmap(habitName: String, calendar: Calendar): Map<Int, Int> {
        val habit = habits.find { it.name == habitName } ?: return emptyMap()
        val result = mutableMapOf<Int, Int>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val tempCal = calendar.clone() as Calendar

        for (day in 1..daysInMonth) {
            tempCal.set(year, month, day)
            val dateKey = sdf.format(tempCal.time)
            val progress = habit.dailyProgress[dateKey] ?: if (habit.completedDates.contains(dateKey)) 100 else 0
            result[day - 1] = progress
        }
        return result
    }

    fun getHabitStreaks(habitName: String): Pair<Int, Int> {
        val habit = habits.find { it.name == habitName } ?: return 0 to 0
        val sortedDates = habit.completedDates.sortedDescending()
        if (sortedDates.isEmpty()) return 0 to 0

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        
        // Simplified streak logic: consecutive days in completedDates
        // For a more accurate logic, we should check repeatDays and skip days it wasn't supposed to be done.
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val today = sdf.format(Date())
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.let { sdf.format(it.time) }

        // Longest Streak calculation
        val allSorted = habit.completedDates.sorted()
        if (allSorted.isNotEmpty()) {
            var lastDate: Calendar? = null
            for (dateStr in allSorted) {
                val date = Calendar.getInstance().apply { time = sdf.parse(dateStr)!! }
                if (lastDate == null) {
                    tempStreak = 1
                } else {
                    val diff = (date.timeInMillis - lastDate.timeInMillis) / (1000 * 60 * 60 * 24)
                    if (diff <= 1) { // Same day or next day
                        if (diff == 1L) tempStreak++
                    } else {
                        // Check if intermediate days were supposed to be tracked
                        var isBroken = false
                        val checkCal = lastDate.clone() as Calendar
                        checkCal.add(Calendar.DAY_OF_YEAR, 1)
                        while (checkCal.before(date)) {
                            val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK) - 1
                            if (habit.repeatType == "SPECIFIC_DAYS" && habit.repeatDays.contains(dayOfWeek)) {
                                isBroken = true
                                break
                            }
                            checkCal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        if (isBroken) tempStreak = 1 else tempStreak++
                    }
                }
                lastDate = date
                if (tempStreak > longestStreak) longestStreak = tempStreak
            }
        }

        // Current Streak calculation
        if (habit.completedDates.contains(today) || habit.completedDates.contains(yesterday)) {
            val checkCal = Calendar.getInstance()
            if (!habit.completedDates.contains(today)) checkCal.add(Calendar.DAY_OF_YEAR, -1)
            
            while (true) {
                val dateStr = sdf.format(checkCal.time)
                if (habit.completedDates.contains(dateStr)) {
                    currentStreak++
                } else {
                    // Check if this day was supposed to be a habit day
                    val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK) - 1
                    if (habit.repeatType == "SPECIFIC_DAYS" && !habit.repeatDays.contains(dayOfWeek)) {
                        // Not a habit day, don't break streak, just continue back
                    } else {
                        break
                    }
                }
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                if (currentStreak > 1000) break // Safety
            }
        }

        return currentStreak to longestStreak
    }

    fun getHabitStreak(): Int {
        // Overall current streak across all habits (simplified: consecutive days with any habit completed)
        // This is a placeholder for the global streak
        return 0 
    }

    fun getTotalHabitsFinished(): Int {
        return habits.sumOf { it.completedDates.size }
    }

    fun getWeeklyCyclicalData(habitName: String? = null): Map<Int, Float> {
        val habitList = if (habitName != null) habits.filter { it.name == habitName } else habits
        if (habitList.isEmpty()) return emptyMap()

        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val improvedResults = mutableMapOf<Int, MutableList<Int>>()
        val cal = Calendar.getInstance()
        val today = sdf.format(cal.time)
        
        for (i in 0..90) {
            val dateStr = sdf.format(cal.time)
            if (dateStr == today) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                continue
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            
            habitList.forEach { habit ->
                if (habit.repeatType != "SPECIFIC_DAYS" || habit.repeatDays.contains(dayOfWeek)) {
                    val wasCompleted = habit.completedDates.contains(dateStr)
                    improvedResults.getOrPut(dayOfWeek) { mutableListOf() }.add(if (wasCompleted) 100 else 0)
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return improvedResults.mapValues { it.value.average().toFloat() }
    }

    fun getStabilityIndex(habitName: String? = null): Float {
        val habitList = if (habitName != null) habits.filter { it.name == habitName } else habits
        if (habitList.isEmpty()) return 100f
        
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val cal = Calendar.getInstance()
        val dailyScores = mutableListOf<Float>()

        for (i in 1..30) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dateStr = sdf.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            
            val scheduled = habitList.filter { it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayOfWeek) }
            if (scheduled.isEmpty()) continue
            
            val completed = scheduled.count { it.completedDates.contains(dateStr) }
            dailyScores.add((completed * 100f) / scheduled.size)
        }

        if (dailyScores.isEmpty()) return 100f
        val mean = dailyScores.average().toFloat()
        val variance = dailyScores.map { Math.pow((it - mean).toDouble(), 2.0) }.average().toFloat()
        val stdDev = Math.sqrt(variance.toDouble()).toFloat()
        
        return Math.max(0f, 100f - stdDev)
    }

    fun getHabitCorrelationMatrix(): List<Triple<String, String, Double>> {
        val result = mutableListOf<Triple<String, String, Double>>()
        if (habits.size < 2) return result

        for (i in habits.indices) {
            for (j in habits.indices) {
                if (i == j) continue
                val h1 = habits[i]
                val h2 = habits[j]
                
                val commonDays = h1.completedDates.filter { h2.completedDates.contains(it) }.size
                if (h1.completedDates.isEmpty()) continue
                
                val prob = commonDays.toDouble() / h1.completedDates.size
                if (prob > 0.5) {
                    result.add(Triple(h1.name, h2.name, prob))
                }
            }
        }
        return result
    }

    fun getTemporalDensityData(): Map<Int, Map<String, Int>> {
        val result = mutableMapOf<Int, MutableMap<String, Int>>()
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        
        habits.forEach { habit ->
            habit.completedDates.forEach { dateStr ->
                try {
                    val date = sdf.parse(dateStr)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                        val timeOfDay = habit.frequency ?: "Anytime"
                        val dayMap = result.getOrPut(dayOfWeek) { mutableMapOf() }
                        dayMap[timeOfDay] = (dayMap[timeOfDay] ?: 0) + 1
                    }
                } catch (e: Exception) {}
            }
        }
        return result
    }

    fun getResilienceScore(habitName: String? = null): Float {
        val habitList = if (habitName != null) habits.filter { it.name == habitName } else habits
        if (habitList.isEmpty()) return 100f
        
        var totalBreakDays = 0
        var totalRecoveries = 0
        
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        
        habitList.forEach { habit ->
            val sortedDates = habit.completedDates.mapNotNull { 
                try { sdf.parse(it) } catch (e: Exception) { null } 
            }.sorted()
            
            if (sortedDates.size < 2) return@forEach
            
            for (i in 0 until sortedDates.size - 1) {
                val d1 = sortedDates[i]
                val d2 = sortedDates[i + 1]
                val diffDays = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                
                if (diffDays > 1) {
                    // There was a break
                    totalBreakDays += (diffDays - 1).toInt()
                    totalRecoveries++
                }
            }
        }
        
        if (totalRecoveries == 0) return 100f
        val avgBreak = totalBreakDays.toFloat() / totalRecoveries
        // 1 day break = 100%, 7 days = 0% (linear scale)
        return Math.max(0f, 100f - (avgBreak - 1) * 16.6f)
    }

    fun getMonthlyMomentumHistory(habitName: String? = null): List<Pair<String, Int>> {
        val habitList = if (habitName != null) habits.filter { it.name == habitName } else habits
        val result = mutableListOf<Pair<String, Int>>()
        val sdf = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        
        val cal = Calendar.getInstance()
        for (i in 0..5) {
            val monthStr = sdf.format(cal.time)
            val monthYearStr = java.text.SimpleDateFormat("yyyyMM", java.util.Locale.getDefault()).format(cal.time)
            
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            var totalScheduled = 0
            var totalCompleted = 0
            
            val tempCal = cal.clone() as Calendar
            for (d in 1..daysInMonth) {
                tempCal.set(Calendar.DAY_OF_MONTH, d)
                val dateKey = dateSdf.format(tempCal.time)
                val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
                
                habitList.forEach { habit ->
                    if (habit.repeatType != "SPECIFIC_DAYS" || habit.repeatDays.contains(dayOfWeek)) {
                        totalScheduled++
                        if (habit.completedDates.contains(dateKey)) totalCompleted++
                    }
                }
            }
            
            val percent = if (totalScheduled == 0) 0 else (totalCompleted * 100) / totalScheduled
            result.add(monthStr to percent)
            cal.add(Calendar.MONTH, -1)
        }
        
        return result.reversed()
    }

    fun getStreakMilestoneProgress(habitName: String? = null): Triple<Int, Int, Float> {
        val currentStreak = if (habitName != null) getHabitStreaks(habitName).first else getHabitStreak()
        val milestones = listOf(7, 21, 30, 90, 100, 365)
        val nextMilestone = milestones.find { it > currentStreak } ?: (currentStreak + 30)
        val prevMilestone = milestones.lastOrNull { it <= currentStreak } ?: 0
        
        val progress = if (nextMilestone == prevMilestone) 1f 
                       else (currentStreak - prevMilestone).toFloat() / (nextMilestone - prevMilestone)
                       
        return Triple(currentStreak, nextMilestone, progress)
    }
}
