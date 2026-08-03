package com.example.allinone.domain.usecase.project

import com.example.allinone.domain.repository.ProjectRepository
import com.example.allinone.domain.repository.ProjectSettings
import javax.inject.Inject

class UpdateProjectSettingsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(settings: ProjectSettings) = repository.updateSettings(settings)
}
