package com.example.allinone.domain.usecase.finance

import com.example.allinone.data.model.LedgerEntry
import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLedgerEntriesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<List<LedgerEntry>> = repository.getLedgerEntries()
}
