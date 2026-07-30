package com.example.allinone.data.repository

import com.example.allinone.data.database.AiChatDao
import com.example.allinone.data.database.AiChatEntity
import com.example.allinone.data.database.AiChatSessionEntity
import kotlinx.coroutines.flow.Flow

class AiChatRepository(private val aiChatDao: AiChatDao) {
    // Sessions
    fun getAllSessions(): Flow<List<AiChatSessionEntity>> = aiChatDao.getAllSessions()

    suspend fun createSession(title: String): Long {
        return aiChatDao.insertSession(AiChatSessionEntity(title = title))
    }

    suspend fun updateSessionTitle(sessionId: Long, newTitle: String) {
        aiChatDao.updateSession(AiChatSessionEntity(id = sessionId, title = newTitle))
    }

    suspend fun deleteSession(session: AiChatSessionEntity) {
        aiChatDao.deleteFullSession(session)
    }

    suspend fun clearAllHistory() {
        aiChatDao.clearEverything()
    }

    // Messages
    fun getMessagesBySession(sessionId: Long): Flow<List<AiChatEntity>> = 
        aiChatDao.getMessagesBySession(sessionId)

    suspend fun insertMessage(sessionId: Long, text: String, isUser: Boolean, timestamp: Long) {
        aiChatDao.insertMessage(AiChatEntity(sessionId = sessionId, text = text, isUser = isUser, timestamp = timestamp))
    }

    suspend fun searchMessages(query: String): List<AiChatEntity> = aiChatDao.searchMessages(query)
}
