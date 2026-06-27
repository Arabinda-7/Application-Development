package com.example.allinone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class FinanceHistoryActivity : AppCompatActivity() {

    private lateinit var monthsList: RecyclerView
    private lateinit var transactionsList: RecyclerView
    private lateinit var monthAdapter: MonthAdapter
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var vpYear: ViewPager2
    private val availableYears = (2020..2030).toList()
    private var currentYearIndex: Int = availableYears.indexOf(Calendar.getInstance().get(Calendar.YEAR)).coerceAtLeast(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_history)

        vpYear = findViewById(R.id.vp_year_selector)
        setupYearViewPager()

        findViewById<View>(R.id.btn_back).setOnClickListener { handleBackNavigation() }

        monthsList = findViewById(R.id.history_months_list)
        transactionsList = findViewById(R.id.month_transactions_list)

        val monthItems = listOf(
            MonthItem("January", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("February", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("March", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("April", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("May", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("June", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("July", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("August", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("September", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("October", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("November", R.drawable.ic_finance, "#1A1A1A"),
            MonthItem("December", R.drawable.ic_finance, "#1A1A1A")
        )
        
        monthAdapter = MonthAdapter(monthItems) { selectedMonth ->
            showTransactionsForMonth(selectedMonth)
        }

        monthsList.layoutManager = GridLayoutManager(this, 4)
        monthsList.adapter = monthAdapter
        transactionsList.layoutManager = LinearLayoutManager(this)

        updateYearlyAnalytics()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackNavigation() }
        })
    }

    private fun setupYearViewPager() {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_year_page, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.findViewById<TextView>(R.id.tv_year_item).text = availableYears[position].toString()
            }
            override fun getItemCount() = availableYears.size
        }
        vpYear.adapter = adapter
        vpYear.setCurrentItem(currentYearIndex, false)
        
        vpYear.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentYearIndex = position
                updateYearlyAnalytics()
            }
        })
    }

    private fun updateYearlyAnalytics() {
        val currency = DataManager.financeCurrency
        val currentYearValue = availableYears[currentYearIndex]
        val yearKey = currentYearValue.toString()
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthCodeSdf = SimpleDateFormat("MM", Locale.getDefault())
        val monthNameSdf = SimpleDateFormat("MMMM", Locale.getDefault())

        val yearlyTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == yearKey
        }

        val totalSpent = yearlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalSavings = yearlyTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
        
        val uniqueMonthsCount = yearlyTransactions.map { 
            monthCodeSdf.format(Date(it.timestamp))
        }.distinct().size.coerceAtLeast(1)

        val avgSpent = if (uniqueMonthsCount > 0) totalSpent / uniqueMonthsCount else 0.0

        val highestMonth = yearlyTransactions
            .filter { it.type == "Expense" }
            .groupBy { monthNameSdf.format(Date(it.timestamp)) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .maxByOrNull { it.value }?.key ?: "None"

        findViewById<TextView>(R.id.tv_yearly_avg_spent).text = String.format(Locale.US, "%s%.0f", currency, avgSpent)
        findViewById<TextView>(R.id.tv_yearly_total_savings).text = String.format(Locale.US, "%s%.0f", currency, totalSavings)
        findViewById<TextView>(R.id.tv_yearly_highest_month).text = highestMonth
    }

    private fun handleBackNavigation() {
        val detailsContainer = findViewById<View>(R.id.month_details_container)
        if (detailsContainer.visibility == View.VISIBLE) {
            detailsContainer.visibility = View.GONE
            findViewById<View>(R.id.history_scroll_view).visibility = View.VISIBLE
            findViewById<View>(R.id.year_navigation_container).visibility = View.VISIBLE
        } else {
            finish()
        }
    }

    private fun showTransactionsForMonth(monthName: String) {
        val currency = DataManager.financeCurrency
        val displaySdf = SimpleDateFormat("MMMM", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val date = displaySdf.parse(monthName) ?: Date()
        
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.YEAR, availableYears[currentYearIndex])
        
        val monthKey = sdf.format(calendar.time)

        val filteredTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == monthKey
        }.toMutableList()

        transactionAdapter = TransactionAdapter(
            filteredTransactions,
            onEdit = { _, _ -> },
            onDelete = { _, _ -> }
        )
        transactionsList.adapter = transactionAdapter
        
        val budget = DataManager.monthlyBudgets[monthKey] ?: 0.0
        val spent = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val income = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val remaining = (budget - spent) + income
        val savings = filteredTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
        val savingsGoal = DataManager.monthlySavingsGoals[monthKey] ?: 0.0

        val summary = findViewById<View>(R.id.finance_summary)
        val tvMonthComparison = summary.findViewById<TextView>(R.id.tv_month_comparison)
        
        summary.findViewById<TextView>(R.id.tv_monthly_budget).text = String.format(Locale.US, "%s%.0f", currency, budget)
        summary.findViewById<TextView>(R.id.tv_current_expenditure).text = String.format(Locale.US, "%s%.0f", currency, spent)
        
        val tvRemaining = summary.findViewById<TextView>(R.id.tv_remaining_balance)
        tvRemaining.text = String.format(Locale.US, "%s%.0f", currency, remaining)
        tvRemaining.setTextColor(if (remaining < 0) android.graphics.Color.parseColor("#FF5252") else android.graphics.Color.parseColor("#4CAF50"))

        summary.findViewById<TextView>(R.id.tv_current_savings).text = String.format(Locale.US, "%s%.0f", currency, savings)
        summary.findViewById<TextView>(R.id.tv_savings_goal).text = String.format(Locale.US, "Goal: %s%.0f", currency, savingsGoal)

        val prevMonthCalendar = calendar.clone() as Calendar
        prevMonthCalendar.add(Calendar.MONTH, -1)
        val prevMonthKey = sdf.format(prevMonthCalendar.time)
        
        val prevMonthTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == prevMonthKey
        }
        val prevSpent = prevMonthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        
        if (prevSpent > 0) {
            val diff = ((spent - prevSpent) / prevSpent) * 100
            val color = if (spent > prevSpent) "#FF5252" else "#4CAF50"
            val trend = if (spent > prevSpent) "more" else "less"
            tvMonthComparison.visibility = View.VISIBLE
            tvMonthComparison.text = String.format(Locale.US, "Spent %.0f%% %s than last month", Math.abs(diff), trend)
            tvMonthComparison.setTextColor(android.graphics.Color.parseColor(color))
        } else {
            tvMonthComparison.visibility = View.GONE
        }

        findViewById<View>(R.id.history_scroll_view).visibility = View.GONE
        findViewById<View>(R.id.year_navigation_container).visibility = View.GONE
        findViewById<View>(R.id.month_details_container).visibility = View.VISIBLE
    }
}
