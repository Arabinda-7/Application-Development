package com.example.allinone.feature.project.domain

import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.feature.project.data.ProjectRepository
import javax.inject.Inject

/**
 * SaveProjectUseCase: Business logic for validating and saving/updating project details.
 */
class SaveProjectUseCase @Inject constructor(
    private val repository: ProjectRepository,
    private val validator: ProjectValidator
) {
    suspend operator fun invoke(
        existingId: Long,
        title: String,
        content: String,
        progress: Int,
        isPinned: Boolean,
        color: Int,
        deadline: Long?,
        goals: List<JournalEntry>,
        subFeatures: List<ProjectFeature>
    ): Result<Note> {
        val titleVal = validator.validateTitle(title)
        if (titleVal is ProjectValidator.ValidationResult.Error) {
            return Result.failure(IllegalArgumentException(titleVal.message))
        }

        val progressVal = validator.validateProgress(progress)
        if (progressVal is ProjectValidator.ValidationResult.Error) {
            return Result.failure(IllegalArgumentException(progressVal.message))
        }

        val timestamp = if (existingId != -1L) existingId else System.currentTimeMillis()
        val project = Note(
            title = title.trim(),
            content = content.trim(),
            timestamp = timestamp,
            progress = progress,
            isPinned = isPinned,
            color = color,
            deadline = deadline,
            isGlobalProject = true,
            ideaGoals = goals.toMutableList(),
            subFeatures = subFeatures.toMutableList()
        )

        repository.saveProject(project)
        return Result.success(project)
    }
}
