package com.example.allinone.feature.idea.data

import com.example.allinone.DataManager
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * IdeaRepository: Handles data access operations for Idea notes and concepts.
 */
@Singleton
class IdeaRepository @Inject constructor(
    private val noteRepository: NoteRepository
) {
    fun getIdeaById(ideaId: Long): Note? {
        return synchronized(DataManager.projects) {
            DataManager.projects.find { it.timestamp == ideaId }
        }
    }

    suspend fun saveIdea(idea: Note) {
        noteRepository.insertNote(idea)
    }

    suspend fun deleteIdea(idea: Note) {
        synchronized(DataManager.projects) {
            DataManager.projects.remove(idea)
        }
    }
}
