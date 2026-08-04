package com.example.allinone.data.repository

import com.example.allinone.data.datasource.NoteLocalDataSource
import com.example.allinone.data.mapper.NoteMapper
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val localDataSource: NoteLocalDataSource
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return localDataSource.getAllNotes().map { entities ->
            entities.map { NoteMapper.toDomain(it) }
        }
    }

    override suspend fun insertNote(note: Note) {
        localDataSource.insertNote(NoteMapper.toEntity(note))
    }

    override suspend fun updateNote(note: Note) {
        localDataSource.insertNote(NoteMapper.toEntity(note))
    }

    override suspend fun deleteNote(note: Note) {
        localDataSource.deleteNote(NoteMapper.toEntity(note))
    }

    override suspend fun syncAll(notes: List<Note>) {
        val entities = notes.map { NoteMapper.toEntity(it) }
        localDataSource.insertAllNotes(entities)
        localDataSource.deleteOthers(entities.map { it.timestamp })
    }

    override fun getNoteSettings(): Flow<NoteSettings> = localDataSource.settings

    override suspend fun updateSettings(settings: NoteSettings) {
        localDataSource.updateSettings(settings)
    }
}
