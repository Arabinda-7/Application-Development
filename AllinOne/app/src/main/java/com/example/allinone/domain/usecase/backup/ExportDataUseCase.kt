package com.example.allinone.domain.usecase.backup

import com.example.allinone.domain.repository.BackupRepository
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(password: CharArray? = null): String = repository.exportData(password)
}
