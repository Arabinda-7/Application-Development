package com.example.allinone

import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import com.example.allinone.ui.performance.PerformanceDashboardScreen
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryComposeHandler(
    private val composeView: ComposeView,
    private val viewModel: PerformanceHistoryViewModel,
    private val onBack: () -> Unit
) {
    fun setup() {
        composeView.setContent {
            var dataVersion by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                DataManager.dataChangeSignal.collect {
                    dataVersion++
                }
            }
            
            val performanceData = remember(viewModel.selectedDate, dataVersion) {
                calculatePerformanceData(viewModel.selectedDate)
            }
            
            val trendData = remember(dataVersion) { DataManager.getLastSevenDaysDetailedProgress() }
            val currentMood = remember(viewModel.selectedDate, dataVersion) { DataManager.dailyMoods[viewModel.selectedDate] }

            PerformanceDashboardScreen(
                onBack = onBack,
                title = "PERFORMANCE HISTORY",
                onDateSelected = { viewModel.updateSelectedDate(it) },
                selectedDate = viewModel.selectedDate,
                currentMonth = viewModel.currentMonth,
                onMonthChanged = { viewModel.currentMonth = it.clone() as Calendar },
                onShowPicker = {
                    val dialog = android.app.DatePickerDialog(
                        composeView.context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day)
                            viewModel.currentMonth = cal.clone() as Calendar
                            viewModel.selectedDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
                        },
                        viewModel.currentMonth.get(Calendar.YEAR),
                        viewModel.currentMonth.get(Calendar.MONTH),
                        viewModel.currentMonth.get(Calendar.DAY_OF_MONTH)
                    )
                    dialog.show()
                },
                performanceData = performanceData,
                trendData = trendData,
                currentMood = currentMood,
                isWorkoutContext = false
            )
        }
    }

    private fun calculatePerformanceData(dateKey: String): DayHistory {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = sdf.format(Date())

        val selectedDayEnd = try {
            val date = sdf.parse(dateKey)
            val cal = Calendar.getInstance()
            if (date != null) {
                cal.time = date
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                cal.timeInMillis
            } else System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }

        return if (dateKey == today) {
            val todayIndex = (DataManager.getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
            val todaysHabits = synchronized(DataManager.habits) {
                DataManager.habits.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) && it.timestamp <= selectedDayEnd }
            }
            val todaysWorkouts = synchronized(DataManager.workouts) {
                DataManager.workouts.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) && it.timestamp <= selectedDayEnd }
            }
            DayHistory(todaysHabits.count { it.isCompleted }, todaysHabits.size, todaysWorkouts.count { it.isCompleted }, todaysWorkouts.size, todaysWorkouts.map { w ->
                WorkoutProgressEntry(w.name, w.progress, w.target, if (w.trackingMode == "Timer") "s" else if (w.trackingMode == "Sets") "Sets" else w.trackingMode, w.color, w.isCompleted)
            })
        } else {
            val snapshot = DataManager.history[dateKey]
            if (snapshot != null) snapshot else {
                val cal = Calendar.getInstance()
                try { sdf.parse(dateKey)?.let { cal.time = it } } catch (e: Exception) {}
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1)
                val activeHabits = synchronized(DataManager.habits) {
                    DataManager.habits.filter { it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) }
                }
                val activeWorkouts = synchronized(DataManager.workouts) {
                    DataManager.workouts.filter { it.timestamp <= selectedDayEnd && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex)) }
                }
                DayHistory(activeHabits.count { it.completedDates.contains(dateKey) }, activeHabits.size, activeWorkouts.count { it.completedDates.contains(dateKey) }, activeWorkouts.size, activeWorkouts.map { w ->
                    WorkoutProgressEntry(w.name, if (w.completedDates.contains(dateKey)) w.target else 0, w.target, if (w.trackingMode == "Timer") "s" else if (w.trackingMode == "Sets") "Sets" else w.trackingMode, w.color, w.completedDates.contains(dateKey))
                })
            }
        }
    }
}
