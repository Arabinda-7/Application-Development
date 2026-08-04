package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import com.example.allinone.domain.repository.FinanceSettings
import javax.inject.Inject

class UpdateFinanceSettingsUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(settings: FinanceSettings) = repository.updateSettings(settings)
}
