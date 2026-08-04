package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetSortOrderUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(order: String) = repository.setSortOrder(order)
}
