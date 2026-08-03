package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.Transaction
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class SyncTransactionsUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(transactions: List<Transaction>) = repository.syncTransactions(transactions)
}
