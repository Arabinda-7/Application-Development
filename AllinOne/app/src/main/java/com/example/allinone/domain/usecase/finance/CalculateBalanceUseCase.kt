package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateBalanceUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<Double> {
        return repository.getTransactions().map { transactions ->
            transactions.sumOf { 
                if (it.type == "Income") it.amount else -it.amount
            }
        }
    }
}
