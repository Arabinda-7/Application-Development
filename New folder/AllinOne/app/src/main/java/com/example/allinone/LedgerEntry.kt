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
    var settlementTimestamp: Long? = null,
    var paymentHistory: MutableList<LedgerPayment> = mutableListOf(),
    var isExpanded: Boolean = false
)

data class LedgerPayment(
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class PersonalLedger(
    val id: String = java.util.UUID.randomUUID().toString(),
    var personName: String,
    var entries: MutableList<PersonalLedgerEntry> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis()
)

data class PersonalLedgerEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    var amount: Double,
    var type: String, // Borrowed or Lent
    var note: String = "",
    var isSettled: Boolean = false,
    var dueDate: Long? = null,
    var paidAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    var settlementTimestamp: Long? = null,
    var paymentHistory: MutableList<LedgerPayment> = mutableListOf(),
    var isExpanded: Boolean = false
)
