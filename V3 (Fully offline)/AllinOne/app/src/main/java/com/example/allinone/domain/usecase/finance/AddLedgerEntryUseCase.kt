package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.LedgerEntry
import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class AddLedgerEntryUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(entry: LedgerEntry) = repository.addLedgerEntry(entry)
}
