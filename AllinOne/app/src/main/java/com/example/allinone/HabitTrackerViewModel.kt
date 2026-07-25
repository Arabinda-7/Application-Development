package com.example.allinone

import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class HabitTrackerViewModel : ViewModel() {
    var selectedTimeFilter: String = "All"
    var selectedDateString: String = DataManager.getTrackingDateString()
    var currentTab: String = "TODAY"
    var currentlySelectedHistoryDate: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    var currentGridCalendar: Calendar = Calendar.getInstance()

    fun getDayIndex(dateString: String): Int {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return try {
            val date = sdf.parse(dateString) ?: Date()
            calendar.time = date
            calendar.get(Calendar.DAY_OF_WEEK) - 1
        } catch (e: Exception) {
            0
        }
    }
}
