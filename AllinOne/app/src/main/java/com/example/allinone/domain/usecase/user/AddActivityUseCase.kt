package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserRepository
import javax.inject.Inject

class AddActivityUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(activity: String) = repository.addActivity(activity)
}
