package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile> = repository.getUserProfile()
}
