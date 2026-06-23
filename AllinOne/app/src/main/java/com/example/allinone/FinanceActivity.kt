package com.example.allinone

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.Transaction
import com.example.allinone.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.*

class FinanceActivity : AppCompatActivity() {

    private var currentMonthTransactions = mutableListOf<Transaction>()
    private lateinit var transactionAdapter: TransactionAdapter
    
    private lateinit var tvBudget: TextView
    private lateinit var tvExpenditure: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvCurrentSavings: TextView
    private lateinit var tvSavingsGoal: TextView
    private lateinit var tvDailyLimit: TextView
    private lateinit var pbBudget: android.widget.ProgressBar
    private lateinit var pbSavings: android.widget.ProgressBar
    private lateinit var tvCategorySummary: TextView
    private lateinit var tvFinanceInsight: TextView
    private lateinit var cardSavings: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        val summary = findViewById<View>(R.id.finance_summary)
        tvBudget = summary.findViewById(R.id.tv_monthly_budget)
        tvExpenditure = summary.findViewById(R.id.tv_current_expenditure)
        tvRemaining = summary.findViewById(R.id.tv_remaining_balance)
        tvCurrentSavings = summary.findViewById(R.id.tv_current_savings)
        tvSavingsGoal = summary.findViewById(R.id.tv_savings_goal)
        tvDailyLimit = summary.findViewById(R.id.tv_daily_limit)
        pbBudget = summary.findViewById(R.id.pb_budget)
        pbSavings = summary.findViewById(R.id.pb_savings)
        tvCategorySummary = summary.findViewById(R.id.tv_category_summary)
        tvFinanceInsight = summary.findViewById(R.id.tv_finance_insight)
        cardSavings = summary.findViewById(R.id.card_savings)

        val financeList = findViewById<RecyclerView>(R.id.finance_list)
        financeList.layoutManager = LinearLayoutManager(this)
        
        filterCurrentMonthTransactions()
        
        transactionAdapter = TransactionAdapter(
            currentMonthTransactions,
            onEdit = { transaction, position -> showEditTransactionDialog(transaction, position) },
            onDelete = { _, position -> deleteTransaction(position) }
        )
        financeList.adapter = transactionAdapter

        updateSummary()

        findViewById<View>(R.id.btn_finance_settings).setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 10f

            val historyBtn = menuView.findViewById<View>(R.id.menu_action_primary)
            historyBtn.visibility = View.VISIBLE
            menuView.findViewById<TextView>(R.id.tv_action_primary).text = "HISTORY"
            menuView.findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_history)
            
            // Hide task-related menu items
            menuView.findViewById<View>(R.id.menu_clear_completed).visibility = View.GONE
            menuView.findViewById<View>(R.id.menu_toggle_completed).visibility = View.GONE
            
            historyBtn.setOnClickListener {
                startActivity(android.content.Intent(this, FinanceHistoryActivity::class.java))
                popupWindow.dismiss()
            }

            menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
                showFinanceSettingsDialog()
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(it, -150, 0)
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_create_new_finance).setOnClickListener {
            showAddTransactionDialog()
        }

        findViewById<View>(R.id.finance_summary).findViewById<View>(R.id.card_budget).setOnClickListener {
            showSetBudgetDialog()
        }

        cardSavings.setOnLongClickListener {
            showSetSavingsGoalDialog()
            true
        }
    }

    private fun deleteTransaction(position: Int) {
        val transaction = currentMonthTransactions[position]
        DataManager.transactions.remove(transaction)
        currentMonthTransactions.removeAt(position)
        transactionAdapter.notifyItemRemoved(position)
        DataManager.saveData(this)
        updateSummary()
    }

    private fun filterCurrentMonthTransactions() {
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val currentMonth = sdf.format(Date())
        currentMonthTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == currentMonth
        }.sortedByDescending { it.timestamp }.toMutableList()
    }

    private fun showEditTransactionDialog(transaction: Transaction, position: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_transaction)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_trans_amount)
        val etTitle = dialog.findViewById<EditText>(R.id.et_trans_title)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_trans_type)
        val spinnerCategory = dialog.findViewById<android.widget.Spinner>(R.id.spinner_trans_category)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_trans)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_trans)
        val titleText = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val tvDate = dialog.findViewById<TextView>(R.id.tv_trans_date)
        val tvTime = dialog.findViewById<TextView>(R.id.tv_trans_time)

        titleText?.text = "Edit Transaction"
        btnSave.text = "Update"
        
        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"
        etAmount.setText(transaction.amount.toString())
        etTitle.setText(transaction.title)
        when (transaction.type) {
            "Income" -> rgType.check(R.id.radio_income)
            "Saving" -> rgType.check(R.id.radio_saving)
            else -> rgType.check(R.id.radio_expense)
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = transaction.timestamp }
        val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        tvDate.text = dateSdf.format(calendar.time)
        tvTime.text = timeSdf.format(calendar.time)

        val categories = DataManager.financeCustomCategories
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        val categoryIndex = categories.indexOf(transaction.category)
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex)
        }

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                tvDate.text = dateSdf.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                tvTime.text = timeSdf.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val title = etTitle.text.toString()
            if (title.isNotEmpty() && amount > 0) {
                val type = when (rgType.checkedRadioButtonId) {
                    R.id.radio_income -> "Income"
                    R.id.radio_saving -> "Saving"
                    else -> "Expense"
                }
                
                transaction.title = title
                transaction.amount = amount
                transaction.type = type
                transaction.category = spinnerCategory.selectedItem?.toString() ?: "General"
                transaction.timestamp = calendar.timeInMillis
                
                filterCurrentMonthTransactions()
                transactionAdapter.updateData(currentMonthTransactions)
                DataManager.saveData(this)
                updateSummary()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateSummary() {
        val currency = DataManager.financeCurrency
        val budget = DataManager.monthlyBudget
        val spent = DataManager.getCurrentMonthExpenditure()
        val income = DataManager.getCurrentMonthIncome()
        val remaining = (budget - spent) + income
        val savings = DataManager.getCurrentMonthSavings()
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

        // Calculate Daily Limit
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
        val dailyLimit = (remaining / daysRemaining).coerceAtLeast(0.0)
        tvDailyLimit.text = String.format(Locale.US, "Daily: %s%.0f", currency, dailyLimit)

        // Update Progress Bars
        pbBudget.progress = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0, 100) else 0
        pbSavings.progress = if (savingsGoal > 0) ((savings / savingsGoal) * 100).toInt().coerceIn(0, 100) else 0

        updateCategoryBreakdown()
    }

    private fun updateCategoryBreakdown() {
        val currency = DataManager.financeCurrency
        val expenses = currentMonthTransactions.filter { it.type == "Expense" }
        if (expenses.isEmpty()) {
            tvCategorySummary.text = "No expenses recorded this month."
            tvFinanceInsight.text = "Start tracking your expenses to see insights!"
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

        // Dynamic Insights
        val topCategory = categoryGroups.first()
        val topPercentage = (topCategory.second / totalSpent) * 100
        
        when {
            topPercentage > 50 -> {
                tvFinanceInsight.text = "Alert: ${topCategory.first} accounts for over 50% of your spending!"
            }
            DataManager.monthlyBudget > 0 && totalSpent > DataManager.monthlyBudget * 0.8 -> {
                tvFinanceInsight.text = "Warning: You have used 80% of your budget. Slow down!"
            }
            else -> {
                val dailyLimit = ((DataManager.monthlyBudget - totalSpent).coerceAtLeast(0.0) / 
                    (Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1))
                tvFinanceInsight.text = "You're on track! Keep your daily spend under ${currency}${dailyLimit.toInt()}."
            }
        }
    }

    private fun showFinanceSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_finance_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val itemCurrency = dialog.findViewById<View>(R.id.item_currency)
        val itemCategories = dialog.findViewById<View>(R.id.item_categories)
        val itemGoals = dialog.findViewById<View>(R.id.item_budget_goals)
        val tvCurrencySummary = dialog.findViewById<TextView>(R.id.tv_currency_summary)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_settings)

        tvCurrencySummary.text = "Tap to change (Current: ${DataManager.financeCurrency})"

        itemCurrency.setOnClickListener {
            val symbols = listOf("₹", "$", "€", "£", "¥")
            val currentIndex = symbols.indexOf(DataManager.financeCurrency)
            val nextIndex = (currentIndex + 1) % symbols.size
            val nextSymbol = symbols[nextIndex]
            
            DataManager.financeCurrency = nextSymbol
            DataManager.saveData(this)
            tvCurrencySummary.text = "Tap to change (Current: $nextSymbol)"
            updateSummary()
            android.widget.Toast.makeText(this, "Currency changed to $nextSymbol", android.widget.Toast.LENGTH_SHORT).show()
        }

        itemCategories.setOnClickListener {
            showManageFinanceCategoriesDialog()
        }

        itemGoals.setOnClickListener {
            // Re-use existing budget dialog
            showSetBudgetDialog()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showManageFinanceCategoriesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)

        fun refreshList() {
            container.removeAllViews()
            DataManager.financeCustomCategories.forEach { category ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
                itemView.findViewById<TextView>(R.id.tv_header_title).text = category
                itemView.findViewById<View>(R.id.iv_header_chevron).visibility = View.GONE
                
                itemView.setOnLongClickListener {
                    DataManager.financeCustomCategories.remove(category)
                    DataManager.saveData(this)
                    refreshList()
                    true
                }
                container.addView(itemView)
            }
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.financeCustomCategories.contains(name)) {
                DataManager.financeCustomCategories.add(name)
                DataManager.saveData(this)
                refreshList()
                etNew.text.clear()
            }
        }

        refreshList()
        dialog.show()
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etBudget = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_budget)

        val currency = DataManager.financeCurrency
        etBudget.hint = "${currency}0.00"
        etBudget.setText(DataManager.monthlyBudget.toInt().toString())
        etBudget.requestFocus()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newBudget = etBudget.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.monthlyBudget = newBudget
            DataManager.saveData(this)
            updateSummary()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etGoal = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_budget)
        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)

        val currency = DataManager.financeCurrency
        title?.text = "SET SAVINGS GOAL"
        subtext?.text = "Enter your monthly savings target"
        etGoal.hint = "${currency}0.00"
        etGoal.setText(DataManager.monthlySavingsGoal.toInt().toString())
        etGoal.requestFocus()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newGoal = etGoal.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.monthlySavingsGoal = newGoal
            DataManager.saveData(this)
            updateSummary()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddTransactionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_transaction)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_trans_amount)
        val etTitle = dialog.findViewById<EditText>(R.id.et_trans_title)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_trans_type)
        val spinnerCategory = dialog.findViewById<android.widget.Spinner>(R.id.spinner_trans_category)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_trans)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_trans)
        val tvDate = dialog.findViewById<TextView>(R.id.tv_trans_date)
        val tvTime = dialog.findViewById<TextView>(R.id.tv_trans_time)

        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"

        val calendar = Calendar.getInstance()
        val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        tvDate.text = "Today"
        tvTime.text = "Now"

        val categories = DataManager.financeCustomCategories
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                tvDate.text = dateSdf.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                tvTime.text = timeSdf.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val title = etTitle.text.toString()
            if (title.isNotEmpty() && amount > 0) {
                val type = when (rgType.checkedRadioButtonId) {
                    R.id.radio_income -> "Income"
                    R.id.radio_saving -> "Saving"
                    else -> "Expense"
                }
                
                val newTransaction = Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = spinnerCategory.selectedItem?.toString() ?: "General",
                    timestamp = calendar.timeInMillis
                )
                
                DataManager.transactions.add(0, newTransaction)
                filterCurrentMonthTransactions()
                transactionAdapter.updateData(currentMonthTransactions)
                DataManager.saveData(this)
                updateSummary()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
