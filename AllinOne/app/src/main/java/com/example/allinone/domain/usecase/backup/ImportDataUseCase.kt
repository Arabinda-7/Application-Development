package com.example.allinone.domain.usecase.backup

import com.example.allinone.domain.repository.BackupRepository
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(dataString: String, password: CharArray? = null): Boolean = 
        repository.importData(dataString, password)
}
