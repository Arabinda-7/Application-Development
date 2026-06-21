package com.example.allinone

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var type: String, // "Income" or "Expense"
    var category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
