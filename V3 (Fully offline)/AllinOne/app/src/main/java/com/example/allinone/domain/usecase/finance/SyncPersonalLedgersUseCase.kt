package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.PersonalLedger
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class SyncPersonalLedgersUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(ledgers: List<PersonalLedger>) = repository.syncPersonalLedgers(ledgers)
}
