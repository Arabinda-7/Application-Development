package com.example.allinone.data.database

import androidx.room.TypeConverter
import com.example.allinone.data.model.Subtask
import com.example.allinone.data.model.LedgerPayment
import com.example.allinone.data.model.PersonalLedgerEntry
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.data.model.ProjectHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkspaceTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())
    @TypeConverter
    fun toStringList(value: String?): List<String> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String = gson.toJson(value ?: emptyList<Int>())
    @TypeConverter
    fun toIntList(value: String?): List<Int> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<Int>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>?): String = gson.toJson(value ?: emptyMap<String, Int>())
    @TypeConverter
    fun toStringIntMap(value: String?): Map<String, Int> = try {
        if (value.isNullOrEmpty()) emptyMap()
        else gson.fromJson(value, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
    } catch (e: Exception) { emptyMap() }

    @TypeConverter
    fun fromSubtaskList(value: List<Subtask>?): String = gson.toJson(value ?: emptyList<Subtask>())
    @TypeConverter
    fun toSubtaskList(value: String?): List<Subtask> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<Subtask>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromLedgerPaymentList(value: List<LedgerPayment>?): String = gson.toJson(value ?: emptyList<LedgerPayment>())
    @TypeConverter
    fun toLedgerPaymentList(value: String?): List<LedgerPayment> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<LedgerPayment>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromPersonalLedgerEntryList(value: List<PersonalLedgerEntry>?): String = gson.toJson(value ?: emptyList<PersonalLedgerEntry>())
    @TypeConverter
    fun toPersonalLedgerEntryList(value: String?): List<PersonalLedgerEntry> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<PersonalLedgerEntry>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromJournalEntryList(value: List<JournalEntry>?): String = gson.toJson(value ?: emptyList<JournalEntry>())
    @TypeConverter
    fun toJournalEntryList(value: String?): List<JournalEntry> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<JournalEntry>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromProjectFeatureList(value: List<ProjectFeature>?): String = gson.toJson(value ?: emptyList<ProjectFeature>())
    @TypeConverter
    fun toProjectFeatureList(value: String?): List<ProjectFeature> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<ProjectFeature>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromProjectHistoryList(value: List<ProjectHistory>?): String = gson.toJson(value ?: emptyList<ProjectHistory>())
    @TypeConverter
    fun toProjectHistoryList(value: String?): List<ProjectHistory> = try {
        if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson(value, object : TypeToken<List<ProjectHistory>>() {}.type) ?: emptyList()
    } catch (e: Exception) { emptyList() }
}
