package com.example.allinone.data.preferences

import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FinancePreferences: Manages finance settings, currency symbols, budgets, 
 * savings goals, and custom expense categories.
 */
@Singleton
class FinancePreferences @Inject constructor() {
    var financeCurrency: String = "₹"
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
    var financeSavingsGoalName: String = "Savings"
    var isFinanceLedgerEnabled: Boolean = true
    val monthlyBudgets: MutableMap<String, Double> = Collections.synchronizedMap(mutableMapOf())
    val financeCustomCategories: MutableList<String> = Collections.synchronizedList(
        mutableListOf("Food", "Shopping", "Transport", "Bills", "Health", "Entertainment", "Other")
    )
}
