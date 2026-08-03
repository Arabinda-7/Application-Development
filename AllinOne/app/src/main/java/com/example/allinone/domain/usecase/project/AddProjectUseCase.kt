package com.example.allinone.domain.usecase.project

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.ProjectRepository
import javax.inject.Inject

class AddProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(project: Note) = repository.insertProject(project)
}
