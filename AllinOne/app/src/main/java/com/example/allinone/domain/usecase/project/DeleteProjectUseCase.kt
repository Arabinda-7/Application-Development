package com.example.allinone.domain.usecase.project

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Note) {
        projectRepository.deleteProject(project)
    }
}
