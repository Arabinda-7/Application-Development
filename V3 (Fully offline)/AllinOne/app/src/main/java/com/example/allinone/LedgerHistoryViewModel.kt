package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LedgerHistoryViewModel : ViewModel() {
    var isDeleteMode by mutableStateOf(false)
}
