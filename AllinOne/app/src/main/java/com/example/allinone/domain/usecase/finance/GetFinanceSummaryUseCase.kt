package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.model.FinanceSummary
import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetFinanceSummaryUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<FinanceSummary> {
        val range = getCurrentMonthRange()
        
        return combine(
            repository.getTransactions(),
            repository.getMonthlyBudget(),
            repository.getCurrency()
        ) { transactions, budget, currency ->
            val monthTransactions = transactions.filter { it.timestamp in range.first until range.second }
            val income = monthTransactions.filter { it.type == "Income" }.sumOf { it.amount }
            val expense = monthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val balance = income - expense
            val savings = if (income > expense) income - expense else 0.0
            
            val budgetProgress = if (budget > 0) (expense / budget).toFloat() else 0f
            
            FinanceSummary(
                totalIncome = income,
                totalExpense = expense,
                balance = balance,
                savings = savings,
                budgetProgress = budgetProgress,
                currency = currency
            )
        }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }
}
