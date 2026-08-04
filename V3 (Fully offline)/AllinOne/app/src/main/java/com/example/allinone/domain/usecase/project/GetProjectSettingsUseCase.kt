package com.example.allinone.domain.usecase.project

import com.example.allinone.domain.repository.ProjectRepository
import com.example.allinone.domain.repository.ProjectSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectSettingsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(): Flow<ProjectSettings> = repository.getProjectSettings()
}
