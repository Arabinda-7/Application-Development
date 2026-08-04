package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserRepository
import com.example.allinone.domain.repository.UserSettings
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(settings: UserSettings) = repository.updateUserSettings(settings)
}
