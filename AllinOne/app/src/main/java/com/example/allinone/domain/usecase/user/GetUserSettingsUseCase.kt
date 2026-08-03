package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserRepository
import com.example.allinone.domain.repository.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<UserSettings> = repository.getUserSettings()
}
