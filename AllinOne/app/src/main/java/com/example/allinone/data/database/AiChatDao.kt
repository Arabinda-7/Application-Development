package com.example.allinone.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    // Session Queries
    @Query("SELECT * FROM ai_chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<AiChatSessionEntity>>

    @Query("SELECT * FROM ai_chat_sessions WHERE type = :type ORDER BY timestamp DESC")
    fun getSessionsByType(type: String): Flow<List<AiChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AiChatSessionEntity): Long

    @Update
    suspend fun updateSession(session: AiChatSessionEntity)

    @Delete
    suspend fun deleteSession(session: AiChatSessionEntity)

    @Query("DELETE FROM ai_chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: Long)

    // Message Queries
    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: Long): Flow<List<AiChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatEntity)

    @Query("SELECT * FROM ai_chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<AiChatEntity>

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM ai_chat_sessions")
    suspend fun clearAllSessions()

    @Transaction
    suspend fun deleteFullSession(session: AiChatSessionEntity) {
        deleteMessagesBySession(session.id)
        deleteSession(session)
    }

    @Transaction
    suspend fun clearEverything() {
        clearAllMessages()
        clearAllSessions()
    }

    @Query("DELETE FROM ai_chat_sessions WHERE timestamp < :threshold")
    suspend fun deleteOldSessions(threshold: Long)

    @Query("DELETE FROM ai_chat_messages WHERE sessionId NOT IN (SELECT id FROM ai_chat_sessions)")
    suspend fun deleteOrphanedMessages()

    @Transaction
    suspend fun cleanupOldHistory(threshold: Long) {
        deleteOldSessions(threshold)
        deleteOrphanedMessages()
    }

    @Query("SELECT * FROM ai_chat_messages")
    suspend fun getAllMessagesSync(): List<AiChatEntity>

    @Query("SELECT * FROM ai_chat_sessions")
    suspend fun getAllSessionsSync(): List<AiChatSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<AiChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<AiChatSessionEntity>)
}
