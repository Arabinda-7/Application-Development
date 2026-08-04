package com.example.allinone.domain.model

data class FinanceSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savings: Double,
    val budgetProgress: Float,
    val currency: String
)
