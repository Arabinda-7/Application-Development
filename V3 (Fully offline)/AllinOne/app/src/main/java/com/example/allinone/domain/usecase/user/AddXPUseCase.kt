package com.example.allinone.domain.usecase.user

import com.example.allinone.domain.repository.UserRepository
import javax.inject.Inject

class AddXPUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(amount: Int): Boolean = repository.addXP(amount)
}
