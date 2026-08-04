package com.example.allinone.assistant.memory

import com.example.allinone.assistant.datasource.MemoryLocalDataSource
import com.example.allinone.assistant.mapper.MemoryMapper
import com.example.allinone.assistant.model.AssistantMemory
import com.example.allinone.assistant.model.MemoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantMemoryRepositoryImpl @Inject constructor(
    private val localDataSource: MemoryLocalDataSource
) : AssistantMemoryRepository {

    override fun getAllMemories(): Flow<List<AssistantMemory>> = 
        localDataSource.getAllMemories().map { entities ->
            entities.map { MemoryMapper.toDomain(it) }
        }

    override fun getMemoriesByType(type: MemoryType): Flow<List<AssistantMemory>> = 
        localDataSource.getMemoriesByType(type.name).map { entities ->
            entities.map { MemoryMapper.toDomain(it) }
        }

    override suspend fun getMemoryByKey(key: String): AssistantMemory? = withContext(Dispatchers.IO) {
        localDataSource.getMemoryByKey(key)?.let { MemoryMapper.toDomain(it) }
    }

    override suspend fun saveMemory(memory: AssistantMemory) = withContext(Dispatchers.IO) {
        localDataSource.insertMemory(MemoryMapper.toEntity(memory))
    }

    override suspend fun searchMemories(query: String): List<AssistantMemory> = withContext(Dispatchers.IO) {
        localDataSource.searchMemories(query).map { MemoryMapper.toDomain(it) }
    }

    override suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        localDataSource.deleteMemoryById(id)
    }

    override suspend fun clearOldMemories(days: Int) = withContext(Dispatchers.IO) {
        val threshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        localDataSource.deleteOldMemories(threshold)
    }
}
