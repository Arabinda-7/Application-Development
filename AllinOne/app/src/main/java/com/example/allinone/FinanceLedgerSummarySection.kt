package com.example.allinone

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.example.allinone.data.model.PersonalLedgerEntry as LedgerEntry
import java.util.*

class FinanceLedgerSummarySection(
    private val rootView: View
) {
    private val tvTotalBorrowed: TextView = rootView.findViewById(R.id.tv_total_borrowed)
    private val tvTotalLent: TextView = rootView.findViewById(R.id.tv_total_lent)
    private val tvNetBalance: TextView = rootView.findViewById(R.id.tv_net_balance)

    fun update(activeEntries: List<LedgerEntry>) {
        val totalBorrow = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
        val totalLent = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
        
        val netBalance = totalLent - totalBorrow
        val currency = DataManager.financeCurrency
        
        tvTotalBorrowed.text = String.format(Locale.US, "%s%.0f", currency, totalBorrow)
        tvTotalLent.text = String.format(Locale.US, "%s%.0f", currency, totalLent)
        
        tvNetBalance.text = String.format(Locale.US, "%s%.0f", currency, Math.abs(netBalance))
        tvNetBalance.setTextColor(if (netBalance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#FF5252"))
    }
}
