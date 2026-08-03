package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.*
import javax.inject.Inject

class GetBudgetProgressUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<BudgetProgress> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startTime = calendar.timeInMillis

        return combine(
            financeRepository.getTransactions(),
            financeRepository.getMonthlyBudget()
        ) { transactions, budget ->
            val expenses = transactions
                .filter { it.type == "Expense" && it.timestamp >= startTime }
                .sumOf { it.amount }
            
            val percentage = if (budget > 0) (expenses / budget * 100).toInt() else 0
            
            BudgetProgress(
                spent = expenses,
                budget = budget,
                percentage = percentage.coerceAtMost(100),
                isOverBudget = expenses > budget
            )
        }
    }
}

data class BudgetProgress(
    val spent: Double,
    val budget: Double,
    val percentage: Int,
    val isOverBudget: Boolean
)
