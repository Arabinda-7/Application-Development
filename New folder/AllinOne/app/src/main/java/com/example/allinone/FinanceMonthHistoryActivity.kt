package com.example.allinone

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class FinanceMonthHistoryActivity : AppCompatActivity() {

    private lateinit var vpMonthDetails: ViewPager2
    private lateinit var rgMonthSelector: RadioGroup
    private lateinit var tvYear: TextView
    
    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val availableYears = (2020..2030).toList()
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_month_history)

        selectedYear = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR))

        vpMonthDetails = findViewById(R.id.vp_month_details)
        rgMonthSelector = findViewById(R.id.rg_month_selector)
        tvYear = findViewById(R.id.tv_current_year)
        
        tvYear.text = selectedYear.toString()
        tvYear.setOnClickListener {
            showYearPickerDialog()
        }

        setupMonthViewPager()
        setupMonthSelector()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        // Default to current month or intent month
        val targetMonth = intent.getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH))
        vpMonthDetails.setCurrentItem(targetMonth, false)
        updateMonthSelectorSelection(targetMonth)
    }

    private fun showYearPickerDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_year_roller)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val picker = dialog.findViewById<NumberPicker>(R.id.year_number_picker)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_year)
        
        picker.minValue = availableYears.first()
        picker.maxValue = availableYears.last()
        picker.value = selectedYear

        btnSave.setOnClickListener {
            selectedYear = picker.value
            tvYear.text = selectedYear.toString()
            vpMonthDetails.adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }
        
        dialog.show()
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
        val rb = rgMonthSelector.getChildAt(index) as? RadioButton ?: return
        rgMonthSelector.check(rb.id)
        
        // Scroll to center the selected button
        rgMonthSelector.post {
            val horizontalScroll = rgMonthSelector.parent as? HorizontalScrollView
            val scrollX = rb.left - (horizontalScroll?.width ?: 0) / 2 + (rb.width / 2)
            horizontalScroll?.smoothScrollTo(scrollX, 0)
        }
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

    inner class MonthDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(monthName: String) {
            val currency = DataManager.financeCurrency
            val displaySdf = SimpleDateFormat("MMMM", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
            val date = displaySdf.parse(monthName) ?: Date()
            
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.YEAR, selectedYear)
            
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

            itemView.findViewById<TextView>(R.id.tv_month_title_display).text = String.format("%s %d", monthName, selectedYear)
            itemView.findViewById<TextView>(R.id.tv_monthly_budget).text = String.format(Locale.US, "%s%.0f", currency, budget)
            itemView.findViewById<TextView>(R.id.tv_current_expenditure).text = String.format(Locale.US, "%s%.0f", currency, spent)
            
            val tvRemaining = itemView.findViewById<TextView>(R.id.tv_remaining_balance)
            tvRemaining.text = String.format(Locale.US, "%s%.0f", currency, remaining)
            tvRemaining.setTextColor(if (remaining < 0) Color.parseColor("#FF5252") else Color.parseColor("#4CAF50"))

            itemView.findViewById<TextView>(R.id.tv_current_savings).text = String.format(Locale.US, "%s%.0f", currency, savings)

            val pbBudget = itemView.findViewById<ProgressBar>(R.id.pb_budget_usage)
            if (budget > 0) {
                pbBudget.progress = ((spent / budget) * 100).toInt().coerceIn(0, 100)
            } else {
                pbBudget.progress = 0
            }

            updateCategoryBreakdown(itemView, filteredTransactions)
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
