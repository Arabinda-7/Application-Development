package com.example.allinone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var selectedDate by remember { mutableStateOf(DataManager.getTrackingDateString()) }
            var currentMonth by remember { 
                mutableStateOf(Calendar.getInstance().apply { 
                    time = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(selectedDate) ?: Date()
                }) 
            }
            
            val performanceData = remember(selectedDate) {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val today = sdf.format(Date())
                
                // Calculate the end of the selected day in milliseconds to filter by timestamp
                val selectedDayEnd = try {
                    val date = sdf.parse(selectedDate)
                    val cal = Calendar.getInstance()
                    if (date != null) {
                        cal.time = date
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.timeInMillis
                    } else System.currentTimeMillis()
                } catch (e: Exception) { System.currentTimeMillis() }

                if (selectedDate == today) {
                    val todayIndex = (DataManager.getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
                    val todaysHabits = DataManager.habits.filter { 
                        (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) &&
                        it.timestamp <= selectedDayEnd
                    }
                    val todaysWorkouts = DataManager.workouts.filter { 
                        (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) &&
                        it.timestamp <= selectedDayEnd
                    }
                    DayHistory(
                        habitsCompleted = todaysHabits.count { it.isCompleted },
                        totalHabits = todaysHabits.size,
                        workoutsCompleted = todaysWorkouts.count { it.isCompleted },
                        totalWorkouts = todaysWorkouts.size
                    )
                } else {
                    // For past dates, we prefer the snapshot if it exists, 
                    // but we can also recalculate if we want to be perfectly accurate regarding item creation
                    val snapshot = DataManager.history[selectedDate]
                    if (snapshot != null) {
                        snapshot
                    } else {
                        // Recalculate for past dates that don't have snapshots
                        val cal = Calendar.getInstance()
                        try { sdf.parse(selectedDate)?.let { cal.time = it } } catch (e: Exception) {}
                        val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1)
                        
                        val activeHabits = DataManager.habits.filter { 
                            it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex))
                        }
                        val activeWorkouts = DataManager.workouts.filter { 
                            it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex))
                        }
                        
                        DayHistory(
                            habitsCompleted = activeHabits.count { it.completedDates.contains(selectedDate) },
                            totalHabits = activeHabits.size,
                            workoutsCompleted = activeWorkouts.count { it.completedDates.contains(selectedDate) },
                            totalWorkouts = activeWorkouts.size
                        )
                    }
                }
            }
            
            val trendData = remember { DataManager.getLastSevenDaysDetailedProgress() }
            val currentMood = remember(selectedDate) { DataManager.dailyMoods[selectedDate] }

            PerformanceDashboardScreen(
                onBack = { finish() },
                onDateSelected = { selectedDate = it },
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                onMonthChanged = { currentMonth = it.clone() as Calendar },
                onShowPicker = {
                    val dialog = android.app.DatePickerDialog(
                        this,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day)
                            currentMonth = cal.clone() as Calendar
                            selectedDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
                        },
                        currentMonth.get(Calendar.YEAR),
                        currentMonth.get(Calendar.MONTH),
                        currentMonth.get(Calendar.DAY_OF_MONTH)
                    )
                    dialog.show()
                },
                performanceData = performanceData,
                trendData = trendData,
                currentMood = currentMood
            )
        }
    }
}
