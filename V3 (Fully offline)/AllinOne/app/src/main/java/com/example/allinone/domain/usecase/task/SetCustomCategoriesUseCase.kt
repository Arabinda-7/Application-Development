package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetCustomCategoriesUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(categories: List<String>) = repository.setCustomCategories(categories)
}
