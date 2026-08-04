package com.example.allinone.feature.project.data

import com.example.allinone.DataManager
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ProjectRepository: Handles data access operations for project notes, progress, and subfeatures.
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val noteRepository: NoteRepository
) {
    fun getProjectById(id: Long): Note? {
        return synchronized(DataManager.projects) {
            DataManager.projects.find { it.timestamp == id }
        }
    }

    suspend fun saveProject(project: Note) {
        noteRepository.insertNote(project)
    }

    suspend fun deleteProject(project: Note) {
        synchronized(DataManager.projects) {
            DataManager.projects.remove(project)
        }
    }
}
