package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

class WorkoutViewModel : ViewModel() {
    var selectedTimeFilter by mutableStateOf("All")
    var selectedDateString by mutableStateOf(DataManager.getTrackingDateString())
    var currentTab by mutableStateOf("TODAY")
    var currentlySelectedHistoryDate by mutableStateOf(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    var currentGridCalendar by mutableStateOf(Calendar.getInstance())
    var currentlyTimingWorkoutPosition by mutableStateOf(-1)
}
