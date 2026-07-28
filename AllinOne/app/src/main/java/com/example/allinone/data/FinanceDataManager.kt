package com.example.allinone.data

import com.example.allinone.Transaction
import com.example.allinone.LedgerEntry
import com.example.allinone.PersonalLedger
import com.example.allinone.R
import java.util.*

object FinanceDataManager {
    var transactions: MutableList<Transaction> = java.util.Collections.synchronizedList(mutableListOf<Transaction>())
    var ledgerEntries: MutableList<LedgerEntry> = java.util.Collections.synchronizedList(mutableListOf<LedgerEntry>())
    var personalLedgers: MutableList<PersonalLedger> = java.util.Collections.synchronizedList(mutableListOf<PersonalLedger>())
    
    var monthlyBudget: Double = 0.0
    var monthlySavingsGoal: Double = 0.0
    var financeSavingsGoalName: String = "Monthly Savings"
    var monthlyBudgets: MutableMap<String, Double> = java.util.Collections.synchronizedMap(mutableMapOf<String, Double>())
    var monthlySavingsGoals: MutableMap<String, Double> = java.util.Collections.synchronizedMap(mutableMapOf<String, Double>())
    
    var financeCustomCategories = java.util.Collections.synchronizedList(mutableListOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other"))
    var financeCategoryIcons: MutableMap<String, Int> = java.util.Collections.synchronizedMap(mutableMapOf<String, Int>())
    var financeCategoryColors: MutableMap<String, Int> = java.util.Collections.synchronizedMap(mutableMapOf<String, Int>())
    var financeCurrency: String = "₹"
    var financeGraphStartMonth: Int = 0
    var financeGraphColor: Int = -1
    var financeGraphSavingsColor: Int = -1
    var isFinanceLedgerEnabled: Boolean = true
    
    var globalFinanceColor: Int = -1
    var financeAddThemeColor: Int = -1
    var globalFinanceIcon: Int = R.drawable.ic_finance

    fun getCurrentMonthExpenditure(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return synchronized(transactions) {
            transactions.filter {
                val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                it.type == "Expense" && transCal.get(Calendar.MONTH) == currentMonth && transCal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
        }
    }

    fun getCurrentMonthIncome(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return synchronized(transactions) {
            transactions.filter {
                val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                it.type == "Income" && transCal.get(Calendar.MONTH) == currentMonth && transCal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
        }
    }

    fun getCurrentMonthSavings(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return synchronized(transactions) {
            transactions.filter {
                val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                it.type == "Saving" && transCal.get(Calendar.MONTH) == currentMonth && transCal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.amount }
        }
    }
}
