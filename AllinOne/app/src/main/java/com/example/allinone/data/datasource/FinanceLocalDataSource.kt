package com.example.allinone.data.datasource

import android.content.Context
import com.example.allinone.data.database.AppFinanceDao
import com.example.allinone.data.database.TransactionEntity
import com.example.allinone.data.database.PersonalLedgerEntity
import com.example.allinone.data.database.LedgerEntryEntity
import com.example.allinone.domain.repository.FinanceSettings
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AppFinanceDao,
    private val gson: Gson
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<FinanceSettings> = _settings.asStateFlow()

    fun getTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()
    suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    fun getPersonalLedgers(): Flow<List<PersonalLedgerEntity>> = dao.getAllPersonalLedgers()
    suspend fun insertPersonalLedger(ledger: PersonalLedgerEntity) = dao.insertPersonalLedger(ledger)

    fun getLedgerEntries(): Flow<List<LedgerEntryEntity>> = dao.getAllLedgerEntries()
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity) = dao.insertLedgerEntry(entry)

    suspend fun syncTransactions(transactions: List<TransactionEntity>) {
        dao.deleteAllTransactions()
        dao.insertAllTransactions(transactions)
    }

    suspend fun syncPersonalLedgers(ledgers: List<PersonalLedgerEntity>) {
        dao.deleteAllPersonalLedgers()
        dao.insertAllPersonalLedgers(ledgers)
    }

    suspend fun syncLedgerEntries(entries: List<LedgerEntryEntity>) {
        dao.deleteAllLedgerEntries()
        dao.insertAllLedgerEntries(entries)
    }

    suspend fun getSumByTypeInRange(type: String, startTime: Long, endTime: Long): Double {
        return dao.getSumByTypeInRange(type, startTime, endTime) ?: 0.0
    }

    fun updateSettings(newSettings: FinanceSettings) {
        prefs.edit().apply {
            putFloat("monthly_budget", newSettings.monthlyBudget.toFloat())
            putFloat("monthly_savings_goal", newSettings.monthlySavingsGoal.toFloat())
            putString("savings_goal_name", newSettings.savingsGoalName)
            putString("monthly_budgets_data", gson.toJson(newSettings.monthlyBudgets))
            putString("monthly_savings_goals_data", gson.toJson(newSettings.monthlySavingsGoals))
            putString("finance_custom_categories", gson.toJson(newSettings.customCategories))
            putString("finance_category_icons", gson.toJson(newSettings.categoryIcons))
            putString("finance_category_colors", gson.toJson(newSettings.categoryColors))
            putString("finance_currency", newSettings.currency)
            putInt("finance_graph_start_month", newSettings.graphStartMonth)
            putInt("finance_graph_color", newSettings.graphColor)
            putInt("finance_graph_savings_color", newSettings.graphSavingsColor)
            putBoolean("is_finance_ledger_enabled", newSettings.isLedgerEnabled)
            putInt("global_finance_color", newSettings.globalFinanceColor)
            putInt("finance_add_theme_color", newSettings.financeAddThemeColor)
            putInt("global_finance_icon", newSettings.globalFinanceIcon)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): FinanceSettings {
        val budgetMapType = object : TypeToken<Map<String, Double>>() {}.type
        val stringListType = object : TypeToken<List<String>>() {}.type
        val stringIntMapType = object : TypeToken<Map<String, Int>>() {}.type

        return FinanceSettings(
            monthlyBudget = prefs.getFloat("monthly_budget", 0f).toDouble(),
            monthlySavingsGoal = prefs.getFloat("monthly_savings_goal", 0f).toDouble(),
            savingsGoalName = prefs.getString("savings_goal_name", "Monthly Savings") ?: "Monthly Savings",
            monthlyBudgets = try { gson.fromJson(prefs.getString("monthly_budgets_data", "{}"), budgetMapType) } catch (e: Exception) { emptyMap() },
            monthlySavingsGoals = try { gson.fromJson(prefs.getString("monthly_savings_goals_data", "{}"), budgetMapType) } catch (e: Exception) { emptyMap() },
            customCategories = try { gson.fromJson(prefs.getString("finance_custom_categories", "[\"Food\", \"Rent\", \"Transport\", \"Shopping\", \"Entertainment\", \"Health\", \"Other\"]"), stringListType) } catch (e: Exception) { listOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Other") },
            categoryIcons = try { gson.fromJson(prefs.getString("finance_category_icons", "{}"), stringIntMapType) } catch (e: Exception) { emptyMap() },
            categoryColors = try { gson.fromJson(prefs.getString("finance_category_colors", "{}"), stringIntMapType) } catch (e: Exception) { emptyMap() },
            currency = prefs.getString("finance_currency", "₹") ?: "₹",
            graphStartMonth = prefs.getInt("finance_graph_start_month", 0),
            graphColor = prefs.getInt("finance_graph_color", -1),
            graphSavingsColor = prefs.getInt("finance_graph_savings_color", -1),
            isLedgerEnabled = prefs.getBoolean("is_finance_ledger_enabled", true),
            globalFinanceColor = prefs.getInt("global_finance_color", -1),
            financeAddThemeColor = prefs.getInt("finance_add_theme_color", -1),
            globalFinanceIcon = prefs.getInt("global_finance_icon", -1)
        )
    }
}
