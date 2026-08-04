package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class GetCustomCategoriesUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): List<String> = repository.getCustomCategories()
}
