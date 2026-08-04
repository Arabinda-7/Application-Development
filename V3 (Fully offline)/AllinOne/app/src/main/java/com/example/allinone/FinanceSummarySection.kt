package com.example.allinone

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.example.allinone.data.model.Transaction
import java.util.*

class FinanceSummarySection(
    private val rootView: View
) {
    private val tvBudget: TextView = rootView.findViewById(R.id.tv_monthly_budget)
    private val tvExpenditure: TextView = rootView.findViewById(R.id.tv_current_expenditure)
    private val tvRemaining: TextView = rootView.findViewById(R.id.tv_remaining_balance)
    private val tvCurrentSavings: TextView = rootView.findViewById(R.id.tv_current_savings)
    private val tvSavingsGoal: TextView = rootView.findViewById(R.id.tv_savings_goal)
    private val tvSavingsTitle: TextView = rootView.findViewById(R.id.tv_savings_title)
    private val tvDailyLimit: TextView = rootView.findViewById(R.id.tv_daily_limit)
    private val pbBudget: ProgressBar = rootView.findViewById(R.id.pb_budget)
    private val pbSavings: ProgressBar = rootView.findViewById(R.id.pb_savings)
    private val tvCategorySummary: TextView = rootView.findViewById(R.id.tv_category_summary)

    fun update(allMonthTransactions: List<Transaction>) {
        val currency = DataManager.financeCurrency
        val budget = DataManager.monthlyBudget
        
        val spent = allMonthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val income = allMonthTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val savings = allMonthTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
        
        val remaining = (budget - spent) + income
        val savingsGoal = DataManager.monthlySavingsGoal

        tvBudget.text = String.format(Locale.US, "%s%.0f", currency, budget)
        tvExpenditure.text = String.format(Locale.US, "%s%.0f", currency, spent)
        tvExpenditure.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        tvRemaining.text = String.format(Locale.US, "%s%.0f", currency, remaining)

        if (remaining < 0) {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        } else {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        tvCurrentSavings.text = String.format(Locale.US, "%s%.0f", currency, savings)
        tvSavingsGoal.text = String.format(Locale.US, "Goal: %s%.0f", currency, savingsGoal)
        tvSavingsTitle.text = DataManager.financeSavingsGoalName.uppercase()

        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
        val dailyLimit = (remaining / daysRemaining).coerceAtLeast(0.0)
        tvDailyLimit.text = String.format(Locale.US, "Daily: %s%.0f", currency, dailyLimit)

        pbBudget.progress = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0, 100) else 0
        pbSavings.progress = if (savingsGoal > 0) ((savings / savingsGoal) * 100).toInt().coerceIn(0, 100) else 0

        updateCategoryBreakdown(allMonthTransactions)
    }

    private fun updateCategoryBreakdown(allMonthTransactions: List<Transaction>) {
        val currency = DataManager.financeCurrency
        val expenses = allMonthTransactions.filter { it.type == "Expense" }
        if (expenses.isEmpty()) {
            tvCategorySummary.text = "No expenses recorded this month."
            return
        }

        val totalSpent = expenses.sumOf { it.amount }
        val categoryGroups = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val breakdown = StringBuilder()
        categoryGroups.take(3).forEach { (category, amount) ->
            val percentage = (amount / totalSpent) * 100
            breakdown.append("$category: ${String.format(Locale.US, "%s%.0f (%.0f%%)", currency, amount, percentage)}\n")
        }
        tvCategorySummary.text = breakdown.toString().trim()
    }
}
