package com.example.allinone

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import com.example.allinone.ui.performance.PerformanceDashboardScreen
import java.text.SimpleDateFormat
import java.util.*

class HabitHistoryComposeHandler(
    private val composeView: ComposeView,
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

            var selectedDate by remember { mutableStateOf(DataManager.getTrackingDateString()) }
            var currentMonth by remember { 
                mutableStateOf(Calendar.getInstance().apply { 
                    try {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(selectedDate)
                        if (date != null) time = date
                    } catch (e: Exception) {}
                }) 
            }
            
            val habits = remember(dataVersion) { DataManager.habits.toList() }
            var selectedHabitName by remember { mutableStateOf<String?>(null) }
            
            val performanceData = remember(selectedDate, selectedHabitName, dataVersion) {
                if (selectedHabitName == null) {
                    val raw = DataManager.getDayHistory(selectedDate)
                    DayHistory(raw?.habitsCompleted ?: 0, raw?.totalHabits ?: 0, 0, 0, raw?.workoutDetails)
                } else {
                    val habit = habits.find { it.name == selectedHabitName }
                    val isCompleted = habit?.completedDates?.contains(selectedDate) == true
                    DayHistory(if (isCompleted) 1 else 0, 1, 0, 0, null)
                }
            }
            
            val trendData = remember(selectedHabitName, dataVersion) { 
                if (selectedHabitName == null) {
                    DataManager.getLastSevenDaysDetailedProgress().mapIndexed { index, pair -> Pair(index, pair.second) }
                } else {
                    val habit = habits.find { it.name == selectedHabitName }
                    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    (0..6).map { i ->
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -i)
                        val date = sdf.format(cal.time)
                        val progress = if (habit?.completedDates?.contains(date) == true) 100 else 0
                        Pair(i, progress)
                    }.reversed()
                }
            }
            val habitColor = if (DataManager.globalHabitColor != -1) ComposeColor(DataManager.globalHabitColor) else ComposeColor(0xFFFF7A59)

            PerformanceDashboardScreen(
                onBack = onBack,
                title = "HABIT HISTORY",
                onDateSelected = { selectedDate = it },
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                onMonthChanged = { currentMonth = it.clone() as Calendar },
                onShowPicker = {
                    val dialog = android.app.DatePickerDialog(
                        composeView.context,
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
                currentMood = null,
                overrideColor = habitColor,
                isWorkoutContext = false,
                showPerformanceCard = true,
                showTrendCard = true,
                showBackgroundAura = false,
                showFilterSelector = false,
                habits = habits,
                selectedHabitName = selectedHabitName,
                onHabitSelected = { selectedHabitName = it },
                onSaveNote = { date, note -> DataManager.saveDayNote(date, note) }
            )
        }
    }
}
