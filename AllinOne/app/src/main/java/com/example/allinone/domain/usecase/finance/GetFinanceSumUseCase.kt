package com.example.allinone.domain.usecase.finance

import com.example.allinone.domain.repository.FinanceRepository
import javax.inject.Inject

class GetFinanceSumUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(type: String, startTime: Long, endTime: Long): Double {
        return repository.getSumByTypeInRange(type, startTime, endTime)
    }
}
