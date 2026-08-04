package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetGlobalTaskIconUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(iconResId: Int) = repository.setGlobalTaskIcon(iconResId)
}
