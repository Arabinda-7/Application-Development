package com.example.allinone.domain.usecase.user

import com.example.allinone.DayHistory
import com.example.allinone.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDayHistoryUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<Map<String, DayHistory>> = repository.getDayHistory()
}
