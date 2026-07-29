package com.example.allinone.data.repository

import com.example.allinone.Note
import com.example.allinone.data.database.AppNoteDao
import com.example.allinone.data.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val dao: AppNoteDao) {

    fun getAllNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertNote(note: Note) {
        dao.insertNote(note.toEntity())
    }

    suspend fun insertAllNotes(notes: List<Note>) {
        dao.insertAllNotes(notes.map { it.toEntity() })
    }

    suspend fun syncAll(notes: List<Note>) {
        val entities = notes.map { it.toEntity() }
        dao.deleteOthers(entities.map { it.timestamp })
        dao.insertAllNotes(entities)
    }

    suspend fun deleteNote(note: Note) {
        dao.deleteNote(note.toEntity())
    }

    private fun NoteEntity.toDomain() = Note(
        title = title,
        content = content,
        color = color,
        category = category,
        isHidden = isHidden,
        timestamp = timestamp,
        updatedAt = updatedAt,
        status = status,
        progress = progress,
        priority = priority,
        isPinned = isPinned,
        deadline = deadline,
        isArchived = isArchived,
        isDualExist = isDualExist,
        isGlobalProject = isGlobalProject,
        journalEntries = journalEntries.toMutableList(),
        ideaGoals = ideaGoals.toMutableList(),
        subFeatures = subFeatures.toMutableList(),
        changeHistory = changeHistory.toMutableList()
    )

    private fun Note.toEntity() = NoteEntity(
        timestamp = timestamp,
        title = title,
        content = content,
        color = color,
        category = category,
        isHidden = isHidden,
        updatedAt = updatedAt,
        status = status,
        progress = progress,
        priority = priority,
        isPinned = isPinned,
        deadline = deadline,
        isArchived = isArchived,
        isDualExist = isDualExist,
        journalEntries = journalEntries,
        ideaGoals = ideaGoals,
        subFeatures = subFeatures,
        changeHistory = changeHistory,
        isGlobalProject = isGlobalProject
    )
}
