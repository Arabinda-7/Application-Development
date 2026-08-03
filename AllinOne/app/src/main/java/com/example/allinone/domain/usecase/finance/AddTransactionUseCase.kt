package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.Transaction
import com.example.allinone.domain.repository.FinanceRepository
import com.example.allinone.domain.usecase.user.AddXPUseCase
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val addXPUseCase: AddXPUseCase
) {
    suspend operator fun invoke(transaction: Transaction): Boolean {
        financeRepository.addTransaction(transaction)
        
        // Award XP for logging finances
        addXPUseCase(5)
        
        return true
    }
}
