package com.example.allinone.domain.usecase.user

import com.example.allinone.DayHistory
import com.example.allinone.domain.repository.UserRepository
import javax.inject.Inject

class UpdateDayHistoryUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(history: Map<String, DayHistory>) = repository.updateDayHistory(history)
}
