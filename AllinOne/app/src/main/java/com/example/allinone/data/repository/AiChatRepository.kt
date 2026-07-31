package com.example.allinone.data.repository

import com.example.allinone.data.database.AiChatDao
import com.example.allinone.data.database.AiChatEntity
import com.example.allinone.data.database.AiChatSessionEntity
import kotlinx.coroutines.flow.Flow

class AiChatRepository(private val aiChatDao: AiChatDao) {
    // Sessions
    fun getAllSessions(): Flow<List<AiChatSessionEntity>> = aiChatDao.getAllSessions()

    fun getSessionsByType(type: String): Flow<List<AiChatSessionEntity>> = aiChatDao.getSessionsByType(type)

    suspend fun createSession(title: String, type: String = "chat"): Long {
        return aiChatDao.insertSession(AiChatSessionEntity(title = title, type = type))
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

    suspend fun cleanupOldHistory(days: Int) {
        val threshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        aiChatDao.cleanupOldHistory(threshold)
    }

    // Messages
    fun getMessagesBySession(sessionId: Long): Flow<List<AiChatEntity>> = 
        aiChatDao.getMessagesBySession(sessionId)

    suspend fun insertMessage(sessionId: Long, text: String, isUser: Boolean, timestamp: Long) {
        aiChatDao.insertMessage(AiChatEntity(sessionId = sessionId, text = text, isUser = isUser, timestamp = timestamp))
    }

    suspend fun searchMessages(query: String): List<AiChatEntity> = aiChatDao.searchMessages(query)
}
