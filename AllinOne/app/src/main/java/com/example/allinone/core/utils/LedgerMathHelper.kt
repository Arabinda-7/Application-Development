package com.example.allinone.core.utils

import com.example.allinone.data.model.LedgerPayment
import com.example.allinone.data.model.PersonalLedgerEntry
import java.util.*

object LedgerMathHelper {

    fun calculateActiveSums(entries: List<PersonalLedgerEntry>): Pair<Double, Double> {
        val activeEntries = entries.filter { !it.isSettled }
        val owe = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
        val owed = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
        return Pair(owe, owed)
    }

    fun performAutoReconciliation(
        entries: MutableList<PersonalLedgerEntry>,
        totalOwe: Double,
        totalOwed: Double
    ): Boolean {
        val now = System.currentTimeMillis()
        val activeEntries = entries.filter { !it.isSettled }.sortedBy { it.timestamp }
        
        if (activeEntries.isEmpty()) return false

        val smallerSideAmount = if (totalOwed > totalOwe) totalOwe else totalOwed
        val typeToSettle = if (totalOwed > totalOwe) "Borrowed" else "Lent"
        
        // 1. Settle all entries of the smaller side
        activeEntries.filter { it.type == typeToSettle }.forEach {
            it.isSettled = true
            it.settlementTimestamp = now
            it.note += " (Offset via Check)"
        }

        // 2. Partial settlement of the larger side (FIFO)
        var remainingToOffset = smallerSideAmount
        val largerSideType = if (typeToSettle == "Borrowed") "Lent" else "Borrowed"
        
        activeEntries.filter { it.type == largerSideType }.forEach { entry ->
            if (remainingToOffset > 0) {
                val availableForOffset = entry.amount - entry.paidAmount
                val offsetApplied = Math.min(availableForOffset, remainingToOffset)
                
                entry.paidAmount += offsetApplied
                entry.paymentHistory.add(LedgerPayment(offsetApplied, now))
                remainingToOffset -= offsetApplied
                
                if (entry.paidAmount >= entry.amount) {
                    entry.isSettled = true
                    entry.settlementTimestamp = now
                }
            }
        }
        return true
    }
}
