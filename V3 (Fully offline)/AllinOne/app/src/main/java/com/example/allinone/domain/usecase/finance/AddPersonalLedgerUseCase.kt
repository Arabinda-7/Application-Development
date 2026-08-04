package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.PersonalLedger
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class AddPersonalLedgerUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(ledger: PersonalLedger) = repository.addPersonalLedger(ledger)
}
