package com.example.allinone.data.repository

import com.example.allinone.LedgerEntry
import com.example.allinone.PersonalLedger
import com.example.allinone.Transaction
import com.example.allinone.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(private val dao: AppFinanceDao) {

    fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    suspend fun insertAllTransactions(transactions: List<Transaction>) {
        dao.insertAllTransactions(transactions.map { it.toEntity() })
    }

    suspend fun syncTransactions(transactions: List<Transaction>) {
        val entities = transactions.map { it.toEntity() }
        dao.deleteOtherTransactions(entities.map { it.id })
        dao.insertAllTransactions(entities)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    fun getAllPersonalLedgers(): Flow<List<PersonalLedger>> {
        return dao.getAllPersonalLedgers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertPersonalLedger(ledger: PersonalLedger) {
        dao.insertPersonalLedger(ledger.toEntity())
    }

    suspend fun insertAllPersonalLedgers(ledgers: List<PersonalLedger>) {
        dao.insertAllPersonalLedgers(ledgers.map { it.toEntity() })
    }

    suspend fun syncPersonalLedgers(ledgers: List<PersonalLedger>) {
        val entities = ledgers.map { it.toEntity() }
        dao.deleteOtherPersonalLedgers(entities.map { it.id })
        dao.insertAllPersonalLedgers(entities)
    }

    fun getAllLedgerEntries(): Flow<List<LedgerEntry>> {
        return dao.getAllLedgerEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertLedgerEntry(entry: LedgerEntry) {
        dao.insertLedgerEntry(entry.toEntity())
    }

    suspend fun insertAllLedgerEntries(entries: List<LedgerEntry>) {
        dao.insertAllLedgerEntries(entries.map { it.toEntity() })
    }

    suspend fun syncLedgerEntries(entries: List<LedgerEntry>) {
        val entities = entries.map { it.toEntity() }
        dao.deleteOtherLedgerEntries(entities.map { it.id })
        dao.insertAllLedgerEntries(entities)
    }

    suspend fun getSumByTypeInRange(type: String, startTime: Long, endTime: Long): Double {
        return dao.getSumByTypeInRange(type, startTime, endTime) ?: 0.0
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        title = title,
        amount = amount,
        type = type,
        category = category,
        timestamp = timestamp,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type,
        category = category,
        timestamp = timestamp,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor
    )

    private fun PersonalLedgerEntity.toDomain() = PersonalLedger(
        id = id,
        personName = personName,
        entries = entries.toMutableList(),
        timestamp = timestamp
    )

    private fun PersonalLedger.toEntity() = PersonalLedgerEntity(
        id = id,
        personName = personName,
        entries = entries,
        timestamp = timestamp
    )

    private fun LedgerEntryEntity.toDomain() = LedgerEntry(
        id = id,
        personName = personName,
        amount = amount,
        type = type,
        note = note,
        isSettled = isSettled,
        dueDate = dueDate,
        paidAmount = paidAmount,
        timestamp = timestamp,
        settlementTimestamp = settlementTimestamp,
        paymentHistory = paymentHistory.toMutableList()
    )

    private fun LedgerEntry.toEntity() = LedgerEntryEntity(
        id = id,
        personName = personName,
        amount = amount,
        type = type,
        note = note,
        isSettled = isSettled,
        dueDate = dueDate,
        paidAmount = paidAmount,
        timestamp = timestamp,
        settlementTimestamp = settlementTimestamp,
        paymentHistory = paymentHistory
    )
}
