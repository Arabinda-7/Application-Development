package com.example.allinone.domain.usecase.project

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getAllProjects()
}
