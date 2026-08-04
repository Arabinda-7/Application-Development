package com.example.allinone.domain.usecase.habit

import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.repository.HabitSettings
import javax.inject.Inject

class UpdateHabitSettingsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(settings: HabitSettings) = repository.updateSettings(settings)
}
