package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetDefaultSectionUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(section: String) = repository.setDefaultSection(section)
}
