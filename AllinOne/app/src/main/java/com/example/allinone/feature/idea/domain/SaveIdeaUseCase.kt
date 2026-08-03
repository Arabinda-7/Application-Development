package com.example.allinone.feature.idea.domain

import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.feature.idea.data.IdeaRepository
import javax.inject.Inject

/**
 * SaveIdeaUseCase: Business logic use case for saving or updating an Idea note.
 */
class SaveIdeaUseCase @Inject constructor(
    private val repository: IdeaRepository,
    private val validator: IdeaValidator
) {
    suspend operator fun invoke(
        existingId: Long,
        title: String,
        content: String,
        priority: Int,
        goals: List<JournalEntry>,
        subFeatures: List<ProjectFeature>
    ): Result<Note> {
        val validation = validator.validateTitle(title)
        if (validation is IdeaValidator.ValidationResult.Error) {
            return Result.failure(IllegalArgumentException(validation.message))
        }

        val timestamp = if (existingId != -1L) existingId else System.currentTimeMillis()
        val ideaNote = Note(
            title = title.trim(),
            content = content.trim(),
            timestamp = timestamp,
            isGlobalProject = true,
            priority = priority,
            ideaGoals = goals.toMutableList(),
            subFeatures = subFeatures.toMutableList()
        )

        repository.saveIdea(ideaNote)
        return Result.success(ideaNote)
    }
}
