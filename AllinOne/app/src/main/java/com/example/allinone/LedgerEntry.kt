package com.example.allinone

data class LedgerEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    var personName: String,
    var amount: Double,
    var type: String, // "Borrowed" or "Lent"
    var note: String = "",
    var isSettled: Boolean = false,
    var dueDate: Long? = null,
    var paidAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    var settlementTimestamp: Long? = null
)
