package com.example.allinone.data.repository

import com.example.allinone.data.database.PersonalLedgerEntity
import com.example.allinone.data.database.TransactionEntity
import com.example.allinone.data.datasource.FinanceLocalDataSource
import com.example.allinone.data.mapper.FinanceMapper
import com.example.allinone.data.model.LedgerEntry
import com.example.allinone.data.model.PersonalLedger
import com.example.allinone.data.model.Transaction
import com.example.allinone.domain.repository.FinanceRepository
import com.example.allinone.domain.repository.FinanceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val localDataSource: FinanceLocalDataSource
) : FinanceRepository {

    override fun getTransactions(): Flow<List<Transaction>> =
        localDataSource.getTransactions().map { entities ->
            entities.map { FinanceMapper.toTransaction(it) }
        }

    override suspend fun addTransaction(transaction: Transaction) {
        localDataSource.insertTransaction(FinanceMapper.toTransactionEntity(transaction))
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        localDataSource.deleteTransaction(FinanceMapper.toTransactionEntity(transaction))
    }

    override fun getPersonalLedgers(): Flow<List<PersonalLedger>> =
        localDataSource.getPersonalLedgers().map { entities ->
            entities.map { FinanceMapper.toPersonalLedger(it) }
        }

    override suspend fun addPersonalLedger(ledger: PersonalLedger) {
        localDataSource.insertPersonalLedger(FinanceMapper.toPersonalLedgerEntity(ledger))
    }

    override fun getLedgerEntries(): Flow<List<LedgerEntry>> =
        localDataSource.getLedgerEntries().map { entities ->
            entities.map { FinanceMapper.toLedgerEntry(it) }
        }

    override suspend fun addLedgerEntry(entry: LedgerEntry) {
        localDataSource.insertLedgerEntry(FinanceMapper.toLedgerEntryEntity(entry))
    }

    override suspend fun getSumByTypeInRange(type: String, startTime: Long, endTime: Long): Double =
        localDataSource.getSumByTypeInRange(type, startTime, endTime)

    override suspend fun syncTransactions(transactions: List<Transaction>) {
        localDataSource.syncTransactions(transactions.map { FinanceMapper.toTransactionEntity(it) })
    }

    override suspend fun syncPersonalLedgers(ledgers: List<PersonalLedger>) {
        localDataSource.syncPersonalLedgers(ledgers.map { FinanceMapper.toPersonalLedgerEntity(it) })
    }

    override suspend fun syncLedgerEntries(entries: List<LedgerEntry>) {
        localDataSource.syncLedgerEntries(entries.map { FinanceMapper.toLedgerEntryEntity(it) })
    }

    override fun getFinanceSettings(): Flow<FinanceSettings> = localDataSource.settings

    override suspend fun updateSettings(settings: FinanceSettings) {
        localDataSource.updateSettings(settings)
    }

    override fun getMonthlyBudget(): Flow<Double> = localDataSource.settings.map { it.monthlyBudget }

    override fun getSavingsGoal(): Flow<Double> = localDataSource.settings.map { it.monthlySavingsGoal }

    override fun getCurrency(): Flow<String> = localDataSource.settings.map { it.currency }
}
