package com.example.allinone.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AssistantMemoryEntity)

    @Query("SELECT * FROM assistant_memories WHERE id = :id")
    suspend fun getMemoryById(id: String): AssistantMemoryEntity?

    @Query("SELECT * FROM assistant_memories WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): AssistantMemoryEntity?

    @Query("SELECT * FROM assistant_memories WHERE type = :type ORDER BY importance DESC, timestamp DESC")
    fun getMemoriesByType(type: String): Flow<List<AssistantMemoryEntity>>

    @Query("SELECT * FROM assistant_memories ORDER BY importance DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<AssistantMemoryEntity>>

    @Query("SELECT * FROM assistant_memories WHERE content LIKE '%' || :query || '%' OR `key` LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<AssistantMemoryEntity>

    @Delete
    suspend fun deleteMemory(memory: AssistantMemoryEntity)

    @Query("DELETE FROM assistant_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String)

    @Query("DELETE FROM assistant_memories WHERE timestamp < :threshold")
    suspend fun deleteOldMemories(threshold: Long)

    @Query("DELETE FROM assistant_memories")
    suspend fun deleteAll()

    @Query("SELECT * FROM assistant_memories")
    suspend fun getAllMemoriesSync(): List<AssistantMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMemories(memories: List<AssistantMemoryEntity>)
}
