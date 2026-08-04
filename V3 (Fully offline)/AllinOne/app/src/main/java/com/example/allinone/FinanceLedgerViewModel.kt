package com.example.allinone

import androidx.lifecycle.ViewModel
import com.example.allinone.data.model.LedgerEntry

class FinanceLedgerViewModel : ViewModel() {
    val activeEntries = mutableListOf<LedgerEntry>()
}
