package com.example.allinone

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class FinanceHistoryActivity : AppCompatActivity() {

    private lateinit var monthAdapter: MonthAdapter
    private lateinit var vpYear: ViewPager2
    private lateinit var vpMonthDetails: ViewPager2
    private lateinit var rgMonthSelector: RadioGroup
    
    private val availableYears = (2020..2030).toList()
    private var currentYearIndex: Int = availableYears.indexOf(Calendar.getInstance().get(Calendar.YEAR)).coerceAtLeast(0)
    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_history)

        vpYear = findViewById(R.id.vp_year_selector)
        vpMonthDetails = findViewById(R.id.vp_month_details)
        rgMonthSelector = findViewById(R.id.rg_month_selector)
        
        setupYearViewPager()
        setupMonthViewPager()
        setupMonthSelector()

        findViewById<View>(R.id.btn_back).setOnClickListener { handleBackNavigation() }

        // monthsList = findViewById(R.id.history_months_list) // Removed
        val monthItems = monthNames.map { MonthItem(it, R.drawable.ic_finance, "#1A1A1A") }
        
        monthAdapter = MonthAdapter(monthItems) { selectedMonth ->
            val index = monthNames.indexOf(selectedMonth)
            if (index != -1) {
                vpMonthDetails.setCurrentItem(index, true)
                findViewById<ScrollView>(R.id.history_scroll_view).smoothScrollTo(0, findViewById<View>(R.id.rg_month_selector).top)
            }
        }

        updateYearlyAnalytics()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackNavigation() }
        })
    }

    private fun setupMonthSelector() {
        val inflater = LayoutInflater.from(this)
        monthNames.forEachIndexed { index, name ->
            val rb = inflater.inflate(R.layout.item_month_tab, rgMonthSelector, false) as RadioButton
            rb.id = View.generateViewId()
            rb.text = name.take(3).uppercase()
            rb.setOnClickListener {
                vpMonthDetails.currentItem = index
            }
            rgMonthSelector.addView(rb)
        }
    }

    private fun updateMonthSelectorSelection(index: Int) {
        val rb = rgMonthSelector.getChildAt(index) as? RadioButton
        rb?.isChecked = true
        // Scroll to the selected button
        val horizontalScroll = rgMonthSelector.parent as? HorizontalScrollView
        horizontalScroll?.smoothScrollTo(rb?.left ?: 0 - 100, 0)
    }

    private fun setupMonthViewPager() {
        vpMonthDetails.adapter = object : RecyclerView.Adapter<MonthDetailsViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthDetailsViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_month_detail_page, parent, false)
                return MonthDetailsViewHolder(view)
            }

            override fun onBindViewHolder(holder: MonthDetailsViewHolder, position: Int) {
                holder.bind(monthNames[position])
            }

            override fun getItemCount() = 12
        }

        vpMonthDetails.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateMonthSelectorSelection(position)
            }
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

        vpYear.setPageTransformer { page, position ->
            val absPos = Math.abs(position)
            page.alpha = 1.0f - (absPos * 0.5f)
            page.scaleX = 1.0f - (absPos * 0.3f)
            page.scaleY = 1.0f - (absPos * 0.3f)
        }
        
        vpYear.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (currentYearIndex != position) {
                    vpYear.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                currentYearIndex = position
                monthAdapter.updateYear(availableYears[currentYearIndex])
                updateYearlyAnalytics()
                // Refresh month details if visible
                if (findViewById<View>(R.id.month_details_container).visibility == View.VISIBLE) {
                    vpMonthDetails.adapter?.notifyDataSetChanged()
                }
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

        val emptyState = findViewById<View>(R.id.tv_empty_history)
        if (yearlyTransactions.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
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

        val pill = findViewById<View>(R.id.pill_floating_summary)
        if (yearlyTransactions.isNotEmpty()) {
            pill.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_pill_total).text = String.format(Locale.US, "Total Spent: %s%.0f", currency, totalSpent)
            findViewById<TextView>(R.id.tv_pill_savings).text = String.format(Locale.US, "Savings: %s%.0f", currency, totalSavings)
        } else {
            pill.visibility = View.GONE
        }

        updateSpendGraph(yearlyTransactions)
    }

    private fun updateSpendGraph(transactions: List<Transaction>) {
        val container = findViewById<LinearLayout>(R.id.container_spend_graph)
        container.removeAllViews()

        val sdfMonth = SimpleDateFormat("MM", Locale.getDefault())
        val monthlySpent = DoubleArray(12) { 0.0 }
        
        transactions.filter { it.type == "Expense" }.forEach {
            val monthIndex = sdfMonth.format(Date(it.timestamp)).toInt() - 1
            if (monthIndex in 0..11) {
                monthlySpent[monthIndex] += it.amount
            }
        }

        val maxSpent = monthlySpent.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        monthlySpent.forEachIndexed { index, spent ->
            val barHeightPercent = (spent / maxSpent).toFloat()
            
            val barWrapper = LinearLayout(this)
            val wrapperParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            barWrapper.layoutParams = wrapperParams
            barWrapper.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            barWrapper.orientation = LinearLayout.VERTICAL

            val bar = View(this)
            val heightPx = (barHeightPercent * (80 * resources.displayMetrics.density)).toInt().coerceAtLeast(4)
            val barParams = LinearLayout.LayoutParams((12 * resources.displayMetrics.density).toInt(), heightPx)
            barParams.setMargins(0, 0, 0, 4)
            bar.layoutParams = barParams
            bar.background = ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded)
            bar.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (spent == maxSpent && spent > 0) Color.parseColor("#4CAF50") else Color.parseColor("#33FFFFFF")
            )

            val tvMonth = TextView(this)
            tvMonth.text = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")[index]
            tvMonth.setTextColor(Color.parseColor("#80FFFFFF"))
            tvMonth.textSize = 8f
            tvMonth.gravity = android.view.Gravity.CENTER

            barWrapper.addView(bar)
            barWrapper.addView(tvMonth)
            
            barWrapper.setOnClickListener {
                Toast.makeText(this, String.format(Locale.US, "%s%.0f spent in %s", 
                    DataManager.financeCurrency, spent, 
                    SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, index) }.time)), 
                    Toast.LENGTH_SHORT).show()
            }
            
            container.addView(barWrapper)
        }
    }

    private fun handleBackNavigation() {
        finish()
    }

    private fun showMonthDetailsContainer() {
        // No longer needed as UI is consolidated
    }

    inner class MonthDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(monthName: String) {
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

            val transactionsList = itemView.findViewById<RecyclerView>(R.id.month_transactions_list)
            transactionsList.layoutManager = LinearLayoutManager(itemView.context)
            transactionsList.adapter = TransactionAdapter(
                filteredTransactions,
                onEdit = { _, _ -> },
                onDelete = { _, _ -> }
            )
            
            val budget = DataManager.monthlyBudgets[monthKey] ?: 0.0
            val spent = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val income = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
            val remaining = (budget - spent) + income
            val savings = filteredTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
            val savingsGoal = DataManager.monthlySavingsGoals[monthKey] ?: 0.0

            itemView.findViewById<TextView>(R.id.tv_monthly_budget).text = String.format(Locale.US, "%s%.0f", currency, budget)
            itemView.findViewById<TextView>(R.id.tv_current_expenditure).text = String.format(Locale.US, "%s%.0f", currency, spent)
            
            val tvRemaining = itemView.findViewById<TextView>(R.id.tv_remaining_balance)
            tvRemaining.text = String.format(Locale.US, "%s%.0f", currency, remaining)
            tvRemaining.setTextColor(if (remaining < 0) Color.parseColor("#FF5252") else Color.parseColor("#4CAF50"))

            itemView.findViewById<TextView>(R.id.tv_current_savings).text = String.format(Locale.US, "%s%.0f", currency, savings)
            itemView.findViewById<TextView>(R.id.tv_savings_goal).text = String.format(Locale.US, "Goal: %s%.0f", currency, savingsGoal)

            updateCategoryBreakdown(itemView, filteredTransactions)

            // Month-over-Month Comparison
            val tvMonthComparison = itemView.findViewById<TextView>(R.id.tv_month_comparison)
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
                tvMonthComparison.setTextColor(Color.parseColor(color))
            } else {
                tvMonthComparison.visibility = View.GONE
            }
        }

        private fun updateCategoryBreakdown(itemView: View, transactions: List<Transaction>) {
            val cardBreakdown = itemView.findViewById<View>(R.id.card_category_breakdown)
            val container = itemView.findViewById<LinearLayout>(R.id.container_category_list)
            container.removeAllViews()

            val expenseTransactions = transactions.filter { it.type == "Expense" }
            if (expenseTransactions.isEmpty()) {
                cardBreakdown.visibility = View.GONE
                return
            }

            cardBreakdown.visibility = View.VISIBLE
            val totalSpent = expenseTransactions.sumOf { it.amount }
            val categoryTotals = expenseTransactions.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList().sortedByDescending { it.second }

            categoryTotals.forEach { (category, amount) ->
                val percentage = (amount / totalSpent).toFloat()
                
                val itemLayout = LinearLayout(itemView.context)
                itemLayout.orientation = LinearLayout.VERTICAL
                itemLayout.setPadding(0, 8, 0, 8)

                val labelLayout = LinearLayout(itemView.context)
                labelLayout.orientation = LinearLayout.HORIZONTAL

                val tvCategory = TextView(itemView.context)
                tvCategory.text = category
                tvCategory.setTextColor(Color.WHITE)
                tvCategory.textSize = 12f
                tvCategory.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

                val tvAmountText = TextView(itemView.context)
                tvAmountText.text = String.format(Locale.US, "%s%.0f (%.0f%%)", DataManager.financeCurrency, amount, percentage * 100)
                tvAmountText.setTextColor(Color.parseColor("#B0B0B0"))
                tvAmountText.textSize = 11f

                labelLayout.addView(tvCategory)
                labelLayout.addView(tvAmountText)

                val progressContainer = FrameLayout(itemView.context)
                val containerParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (8 * resources.displayMetrics.density).toInt())
                containerParams.topMargin = (6 * resources.displayMetrics.density).toInt()
                progressContainer.layoutParams = containerParams

                val bg = View(itemView.context)
                bg.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                bg.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_dialog_rounded)
                bg.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFFFFF"))

                val progressView = View(itemView.context)
                val progressParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
                progressView.layoutParams = progressParams
                progressView.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_dialog_rounded)
                progressView.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                
                progressContainer.addView(bg)
                progressContainer.addView(progressView)
                
                progressContainer.post {
                    val p = progressView.layoutParams as FrameLayout.LayoutParams
                    p.width = (progressContainer.width * percentage).toInt()
                    progressView.layoutParams = p
                }

                itemLayout.addView(labelLayout)
                itemLayout.addView(progressContainer)
                container.addView(itemLayout)
            }
        }
    }
}
