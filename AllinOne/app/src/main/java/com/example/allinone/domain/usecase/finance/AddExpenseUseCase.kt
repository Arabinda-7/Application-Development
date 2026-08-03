package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.Transaction
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(amount: Double, category: String, title: String) {
        val transaction = Transaction(
            title = title,
            amount = amount,
            type = "Expense",
            category = category,
            timestamp = System.currentTimeMillis()
        )
        repository.addTransaction(transaction)
    }
}
