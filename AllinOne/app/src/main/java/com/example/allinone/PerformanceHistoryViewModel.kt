package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryViewModel : ViewModel() {
    var selectedDate by mutableStateOf(DataManager.getTrackingDateString())
    var currentMonth by mutableStateOf(Calendar.getInstance().apply { 
        try {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(DataManager.getTrackingDateString())
            if (date != null) time = date
        } catch (e: Exception) {}
    })

    fun updateSelectedDate(date: String) {
        selectedDate = date
        try {
            val cal = Calendar.getInstance()
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(date)?.let { cal.time = it }
            currentMonth = cal
        } catch (e: Exception) {}
    }
}
