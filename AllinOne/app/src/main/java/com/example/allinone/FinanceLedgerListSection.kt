package com.example.allinone

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.data.model.PersonalLedgerEntry as LedgerEntry

class FinanceLedgerListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onUpdate: () -> Unit,
    private val onShowMenu: (View, LedgerEntry, Boolean, () -> Unit) -> Unit,
    private val onAddPayment: (LedgerEntry) -> Unit,
    private val onConfirmSettlement: (LedgerEntry) -> Unit
) {
    val ledgerAdapter: LedgerAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        ledgerAdapter = LedgerAdapter(
            mutableListOf(),
            onUpdate = onUpdate,
            onShowMenu = onShowMenu,
            onAddPayment = onAddPayment,
            onConfirmSettlement = onConfirmSettlement
        )
        recyclerView.adapter = ledgerAdapter
    }

    fun updateActiveEntries(): List<LedgerEntry> {
        val active = DataManager.ledgerEntries.filter { !it.isSettled }
            .sortedWith(compareBy({ it.personName }, { it.timestamp }))
        ledgerAdapter.updateEntries(active)
        return active
    }
}
