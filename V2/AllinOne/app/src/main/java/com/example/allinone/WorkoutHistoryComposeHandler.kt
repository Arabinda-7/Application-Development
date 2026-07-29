package com.example.allinone

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import com.example.allinone.ui.performance.PerformanceDashboardScreen
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryComposeHandler(
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
            
            val performanceData = remember(selectedDate, dataVersion) {
                val fullHistory = DataManager.calculateDayHistory(selectedDate)
                DayHistory(0, 0, fullHistory.workoutsCompleted, fullHistory.totalWorkouts, fullHistory.workoutDetails)
            }
            
            val trendData = remember(dataVersion) { 
                DataManager.getLastSevenDaysDetailedProgress().map { Pair(it.first, it.second) } 
            }
            val workoutColor = if (DataManager.globalWorkoutColor != -1) ComposeColor(DataManager.globalWorkoutColor) else ComposeColor(0xFFFFB800)

            PerformanceDashboardScreen(
                onBack = onBack,
                title = "WORKOUT HISTORY",
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
                overrideColor = workoutColor,
                isWorkoutContext = true,
                showPerformanceCard = true,
                showTrendCard = true,
                showBackgroundAura = false
            )
        }
    }
}
