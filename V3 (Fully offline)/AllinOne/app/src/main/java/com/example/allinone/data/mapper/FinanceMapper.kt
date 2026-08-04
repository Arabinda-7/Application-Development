package com.example.allinone.data.mapper

import com.example.allinone.data.database.TransactionEntity
import com.example.allinone.data.database.PersonalLedgerEntity
import com.example.allinone.data.database.LedgerEntryEntity
import com.example.allinone.data.model.Transaction
import com.example.allinone.data.model.PersonalLedger
import com.example.allinone.data.model.LedgerEntry

object FinanceMapper {
    fun toTransaction(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.id,
            title = entity.title,
            amount = entity.amount,
            type = entity.type,
            category = entity.category,
            timestamp = entity.timestamp,
            categoryIcon = entity.categoryIcon,
            categoryColor = entity.categoryColor
        )
    }

    fun toTransactionEntity(domain: Transaction): TransactionEntity {
        return TransactionEntity(
            id = domain.id,
            title = domain.title,
            amount = domain.amount,
            type = domain.type,
            category = domain.category,
            timestamp = domain.timestamp,
            categoryIcon = domain.categoryIcon,
            categoryColor = domain.categoryColor
        )
    }

    fun toPersonalLedger(entity: PersonalLedgerEntity): PersonalLedger {
        return PersonalLedger(
            id = entity.id,
            personName = entity.personName,
            entries = entity.entries.toMutableList(),
            timestamp = entity.timestamp
        )
    }

    fun toPersonalLedgerEntity(domain: PersonalLedger): PersonalLedgerEntity {
        return PersonalLedgerEntity(
            id = domain.id,
            personName = domain.personName,
            entries = domain.entries.toList(),
            timestamp = domain.timestamp
        )
    }

    fun toLedgerEntry(entity: LedgerEntryEntity): LedgerEntry {
        return LedgerEntry(
            id = entity.id,
            personName = entity.personName,
            amount = entity.amount,
            type = entity.type,
            note = entity.note,
            isSettled = entity.isSettled,
            dueDate = entity.dueDate,
            paidAmount = entity.paidAmount,
            timestamp = entity.timestamp,
            settlementTimestamp = entity.settlementTimestamp,
            paymentHistory = entity.paymentHistory.toMutableList()
        )
    }

    fun toLedgerEntryEntity(domain: LedgerEntry): LedgerEntryEntity {
        return LedgerEntryEntity(
            id = domain.id,
            personName = domain.personName,
            amount = domain.amount,
            type = domain.type,
            note = domain.note,
            isSettled = domain.isSettled,
            dueDate = domain.dueDate,
            paidAmount = domain.paidAmount,
            timestamp = domain.timestamp,
            settlementTimestamp = domain.settlementTimestamp,
            paymentHistory = domain.paymentHistory.toList()
        )
    }
}
