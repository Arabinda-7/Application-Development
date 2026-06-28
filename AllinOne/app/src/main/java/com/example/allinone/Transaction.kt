package com.example.allinone

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var type: String, // "Income" or "Expense"
    var category: String = "General",
    var timestamp: Long = System.currentTimeMillis(),
    var categoryIcon: Int = -1,
    var categoryColor: Int = -1
)

data class FinanceCategory(
    val name: String,
    var icon: Int = R.drawable.ic_finance,
    var color: Int = -1
)
