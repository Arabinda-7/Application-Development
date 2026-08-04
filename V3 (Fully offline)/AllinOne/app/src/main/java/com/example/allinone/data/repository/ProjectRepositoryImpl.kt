package com.example.allinone.data.repository

import com.example.allinone.data.datasource.ProjectLocalDataSource
import com.example.allinone.data.mapper.NoteMapper
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.ProjectRepository
import com.example.allinone.domain.repository.ProjectSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val localDataSource: ProjectLocalDataSource
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<Note>> {
        return localDataSource.getAllProjects().map { entities ->
            entities.map { NoteMapper.toDomain(it) }
        }
    }

    override suspend fun insertProject(project: Note) {
        val entity = NoteMapper.toEntity(project).copy(isGlobalProject = true)
        localDataSource.insertProject(entity)
    }

    override suspend fun updateProject(project: Note) {
        val entity = NoteMapper.toEntity(project).copy(isGlobalProject = true)
        localDataSource.insertProject(entity)
    }

    override suspend fun deleteProject(project: Note) {
        val entity = NoteMapper.toEntity(project)
        localDataSource.deleteProject(entity)
    }

    override fun getProjectSettings(): Flow<ProjectSettings> = localDataSource.settings

    override suspend fun updateSettings(settings: ProjectSettings) {
        localDataSource.updateSettings(settings)
    }
}
