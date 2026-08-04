package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AppFinanceDao
import com.example.allinone.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class FinanceBackupProvider @Inject constructor(
    private val financeDao: AppFinanceDao,
    private val financeRepository: FinanceRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "finance_module"

    override suspend fun exportData(): JsonElement {
        return buildJsonObject {
            put("transactions", json.encodeToJsonElement(financeDao.getAllTransactionsSync()))
            put("personalLedgers", json.encodeToJsonElement(financeDao.getAllPersonalLedgersSync()))
            put("ledgerEntries", json.encodeToJsonElement(financeDao.getAllLedgerEntriesSync()))
            put("settings", json.encodeToJsonElement(financeRepository.getFinanceSettings().first()))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["transactions"]?.let {
            val list: List<com.example.allinone.data.database.TransactionEntity> = json.decodeFromJsonElement(it)
            financeDao.deleteAllTransactions()
            financeDao.insertAllTransactions(list)
        }
        
        root["personalLedgers"]?.let {
            val list: List<com.example.allinone.data.database.PersonalLedgerEntity> = json.decodeFromJsonElement(it)
            financeDao.deleteAllPersonalLedgers()
            financeDao.insertAllPersonalLedgers(list)
        }

        root["ledgerEntries"]?.let {
            val list: List<com.example.allinone.data.database.LedgerEntryEntity> = json.decodeFromJsonElement(it)
            financeDao.deleteAllLedgerEntries()
            financeDao.insertAllLedgerEntries(list)
        }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.FinanceSettings = json.decodeFromJsonElement(it)
            financeRepository.updateSettings(settings)
        }
    }
}
