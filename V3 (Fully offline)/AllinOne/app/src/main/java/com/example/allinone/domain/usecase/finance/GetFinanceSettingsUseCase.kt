package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import com.example.allinone.domain.repository.FinanceSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFinanceSettingsUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(): Flow<FinanceSettings> = repository.getFinanceSettings()
}
