package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*

class FinanceHistoryViewModel : ViewModel() {
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var currentMonthIndex by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH))
    
    val availableYears: List<Int> by lazy {
        val current = Calendar.getInstance().get(Calendar.YEAR)
        (current - 5..current + 1).toList()
    }
}
