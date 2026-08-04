package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.Transaction
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(amount: Double, category: String, source: String = "Income") {
        val transaction = Transaction(
            title = source,
            amount = amount,
            type = "Income",
            category = category,
            timestamp = System.currentTimeMillis()
        )
        repository.addTransaction(transaction)
    }
}
