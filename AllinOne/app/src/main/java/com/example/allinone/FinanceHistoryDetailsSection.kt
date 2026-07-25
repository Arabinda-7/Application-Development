package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class FinanceHistoryDetailsSection(
    private val context: Context,
    private val viewPager: ViewPager2,
    private val onPageSelected: (Int) -> Unit
) {
    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    fun setup(selectedYear: Int) {
        viewPager.adapter = object : RecyclerView.Adapter<MonthDetailsViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthDetailsViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_month_detail_page, parent, false)
                return MonthDetailsViewHolder(v, selectedYear)
            }
            override fun onBindViewHolder(holder: MonthDetailsViewHolder, position: Int) {
                holder.bind(monthNames[position])
            }
            override fun getItemCount() = 12
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { onPageSelected(position) }
        })
    }

    fun setCurrentItem(index: Int) {
        viewPager.setCurrentItem(index, false)
    }

    class MonthDetailsViewHolder(v: View, private val selectedYear: Int) : RecyclerView.ViewHolder(v) {
        fun bind(monthName: String) {
            val currency = DataManager.financeCurrency
            val displaySdf = SimpleDateFormat("MMMM", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
            val date = try { displaySdf.parse(monthName) ?: Date() } catch (e: Exception) { Date() }
            
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.YEAR, selectedYear)
            val monthKey = sdf.format(calendar.time)

            val filteredTransactions = DataManager.transactions.filter {
                sdf.format(Date(it.timestamp)) == monthKey
            }.toMutableList()

            val transactionsList = itemView.findViewById<RecyclerView>(R.id.month_transactions_list)
            transactionsList.layoutManager = LinearLayoutManager(itemView.context)
            transactionsList.adapter = TransactionAdapter(filteredTransactions, { _, _ -> }, { _, _ -> })
            
            val budget = DataManager.monthlyBudgets[monthKey] ?: 0.0
            val spent = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val income = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
            val remaining = (budget - spent) + income
            val savings = filteredTransactions.filter { it.type == "Saving" }.sumOf { it.amount }

            itemView.findViewById<TextView>(R.id.tv_month_title_display).text = String.format("%s %d", monthName, selectedYear)
            itemView.findViewById<TextView>(R.id.tv_monthly_budget).text = String.format(Locale.US, "%s%.0f", currency, budget)
            itemView.findViewById<TextView>(R.id.tv_current_expenditure).text = String.format(Locale.US, "%s%.0f", currency, spent)
            
            val tvRemaining = itemView.findViewById<TextView>(R.id.tv_remaining_balance)
            tvRemaining.text = String.format(Locale.US, "%s%.0f", currency, remaining)
            tvRemaining.setTextColor(if (remaining < 0) Color.parseColor("#FF5252") else Color.parseColor("#4CAF50"))

            itemView.findViewById<TextView>(R.id.tv_current_savings).text = String.format(Locale.US, "%s%.0f", currency, savings)

            val pbBudget = itemView.findViewById<ProgressBar>(R.id.pb_budget_usage)
            pbBudget.progress = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0, 100) else 0

            updateCategoryBreakdown(itemView, filteredTransactions)
        }

        private fun updateCategoryBreakdown(itemView: View, transactions: List<Transaction>) {
            val cardBreakdown = itemView.findViewById<View>(R.id.card_category_breakdown)
            val container = itemView.findViewById<LinearLayout>(R.id.container_category_list)
            container.removeAllViews()

            val expenses = transactions.filter { it.type == "Expense" }
            if (expenses.isEmpty()) { cardBreakdown.visibility = View.GONE; return }
            cardBreakdown.visibility = View.VISIBLE

            val total = expenses.sumOf { it.amount }
            val totals = expenses.groupBy { it.category }.mapValues { it.value.sumOf { it.amount } }.toList().sortedByDescending { it.second }

            totals.forEach { (cat, amt) ->
                val pct = (amt / total).toFloat()
                val density = itemView.resources.displayMetrics.density
                
                val itemLayout = LinearLayout(itemView.context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
                }

                val labelLayout = LinearLayout(itemView.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(TextView(itemView.context).apply {
                        text = cat; setTextColor(Color.WHITE); textSize = 12f
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    addView(TextView(itemView.context).apply {
                        text = String.format(Locale.US, "%s%.0f (%.0f%%)", DataManager.financeCurrency, amt, pct * 100)
                        setTextColor(Color.parseColor("#B0B0B0")); textSize = 11f
                    })
                }

                val progressContainer = FrameLayout(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (8 * density).toInt()).apply { topMargin = (6 * density).toInt() }
                    addView(View(itemView.context).apply {
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_dialog_rounded)
                        backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFFFFF"))
                    })
                    val progressView = View(itemView.context).apply {
                        layoutParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
                        background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_dialog_rounded)
                        backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    }
                    addView(progressView)
                    post {
                        val p = progressView.layoutParams as FrameLayout.LayoutParams
                        p.width = (width * pct).toInt()
                        progressView.layoutParams = p
                    }
                }

                itemLayout.addView(labelLayout); itemLayout.addView(progressContainer); container.addView(itemLayout)
            }
        }
    }
}
