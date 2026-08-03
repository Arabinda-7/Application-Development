package com.example.allinone.domain.usecase.task

import com.example.allinone.domain.repository.TaskRepository
import javax.inject.Inject

class SetVisibleSectionsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(sections: List<String>) = repository.setVisibleSections(sections)
}
