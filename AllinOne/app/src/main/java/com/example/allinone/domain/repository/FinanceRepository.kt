package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import com.example.allinone.data.model.LedgerEntry
import com.example.allinone.data.model.PersonalLedger
import com.example.allinone.data.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    
    fun getPersonalLedgers(): Flow<List<PersonalLedger>>
    suspend fun addPersonalLedger(ledger: PersonalLedger)
    
    fun getLedgerEntries(): Flow<List<LedgerEntry>>
    suspend fun addLedgerEntry(entry: LedgerEntry)
    
    suspend fun getSumByTypeInRange(type: String, startTime: Long, endTime: Long): Double
    
    suspend fun syncTransactions(transactions: List<Transaction>)
    suspend fun syncPersonalLedgers(ledgers: List<PersonalLedger>)
    suspend fun syncLedgerEntries(entries: List<LedgerEntry>)

    // Settings & Shared State
    fun getFinanceSettings(): Flow<FinanceSettings>
    suspend fun updateSettings(settings: FinanceSettings)
    
    // Legacy support for single values
    fun getMonthlyBudget(): Flow<Double>
    fun getSavingsGoal(): Flow<Double>
    fun getCurrency(): Flow<String>
}

@Serializable
data class FinanceSettings(
    val monthlyBudget: Double = 0.0,
    val monthlySavingsGoal: Double = 0.0,
    val savingsGoalName: String = "Monthly Savings",
    val monthlyBudgets: Map<String, Double> = emptyMap(),
    val monthlySavingsGoals: Map<String, Double> = emptyMap(),
    val customCategories: List<String> = listOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other"),
    val categoryIcons: Map<String, Int> = emptyMap(),
    val categoryColors: Map<String, Int> = emptyMap(),
    val currency: String = "₹",
    val graphStartMonth: Int = 0,
    val graphColor: Int = -1,
    val graphSavingsColor: Int = -1,
    val isLedgerEnabled: Boolean = true,
    val globalFinanceColor: Int = -1,
    val financeAddThemeColor: Int = -1,
    val globalFinanceIcon: Int = -1
)
