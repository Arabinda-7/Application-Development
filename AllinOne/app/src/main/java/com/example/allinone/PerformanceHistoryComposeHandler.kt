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
            
            val trendData = remember(dataVersion) { DataManager.getLastSevenDaysDetailedProgress().mapIndexed { idx, pair -> Pair(idx, pair.second) } }
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
        return DataManager.calculateDayHistory(dateKey)
    }
}
