package com.example.allinone

import androidx.lifecycle.ViewModel

class FinanceLedgerViewModel : ViewModel() {
    val activeEntries = mutableListOf<LedgerEntry>()
}
