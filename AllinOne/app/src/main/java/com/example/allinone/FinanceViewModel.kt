package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.allinone.data.model.Transaction

class FinanceViewModel : ViewModel() {
    var allMonthTransactions = mutableListOf<Transaction>()
    var filteredTransactions = mutableListOf<Transaction>()
    var currentFilter by mutableStateOf("All")
}
