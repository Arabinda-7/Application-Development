package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.*
import javax.inject.Inject

class GetFinancialSummaryUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<FinancialSummary> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        return combine(
            financeRepository.getTransactions(),
            financeRepository.getMonthlyBudget(),
            financeRepository.getSavingsGoal()
        ) { transactions, budget, savingsGoal ->
            val monthTransactions = transactions.filter { it.timestamp in startTime..endTime }
            
            val income = monthTransactions.filter { it.type == "Income" }.sumOf { it.amount }
            val expense = monthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val savings = monthTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
            
            FinancialSummary(
                totalIncome = income,
                totalExpense = expense,
                totalSavings = savings,
                netBalance = income - expense,
                monthlyBudget = budget,
                savingsGoal = savingsGoal,
                budgetRemaining = budget - expense
            )
        }
    }
}

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val totalSavings: Double,
    val netBalance: Double,
    val monthlyBudget: Double,
    val savingsGoal: Double,
    val budgetRemaining: Double
)
