package com.example.allinone.data.repository

import com.example.allinone.backup.BackupManager
import com.example.allinone.backup.RestoreManager
import com.example.allinone.domain.repository.BackupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager
) : BackupRepository {

    override suspend fun exportData(password: CharArray?): String {
        return backupManager.exportBackup(password)
    }

    override suspend fun importData(dataString: String, password: CharArray?): Boolean {
        val result = restoreManager.importBackup(dataString, password)
        return result is RestoreManager.RestoreResult.Success
    }
}
