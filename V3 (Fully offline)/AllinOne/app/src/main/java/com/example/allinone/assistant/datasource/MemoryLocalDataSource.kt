package com.example.allinone.assistant.datasource

import com.example.allinone.data.database.AssistantMemoryDao
import com.example.allinone.data.database.AssistantMemoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryLocalDataSource @Inject constructor(
    private val memoryDao: AssistantMemoryDao
) {
    fun getAllMemories(): Flow<List<AssistantMemoryEntity>> = memoryDao.getAllMemories()

    fun getMemoriesByType(type: String): Flow<List<AssistantMemoryEntity>> = memoryDao.getMemoriesByType(type)

    suspend fun getMemoryByKey(key: String): AssistantMemoryEntity? = memoryDao.getMemoryByKey(key)

    suspend fun insertMemory(memory: AssistantMemoryEntity) = memoryDao.insertMemory(memory)

    suspend fun searchMemories(query: String): List<AssistantMemoryEntity> = memoryDao.searchMemories(query)

    suspend fun deleteMemoryById(id: String) = memoryDao.deleteMemoryById(id)

    suspend fun deleteOldMemories(threshold: Long) = memoryDao.deleteOldMemories(threshold)
}
