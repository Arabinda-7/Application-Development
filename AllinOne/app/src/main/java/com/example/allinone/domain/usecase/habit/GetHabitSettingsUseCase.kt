package com.example.allinone.domain.usecase.habit

import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.repository.HabitSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitSettingsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<HabitSettings> = repository.getHabitSettings()
}
