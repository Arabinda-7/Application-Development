package com.example.allinone.domain.repository

interface BackupRepository {
    suspend fun exportData(password: CharArray? = null): String
    suspend fun importData(dataString: String, password: CharArray? = null): Boolean
}
