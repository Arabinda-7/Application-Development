package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile) = repository.updateUserProfile(profile)
}
