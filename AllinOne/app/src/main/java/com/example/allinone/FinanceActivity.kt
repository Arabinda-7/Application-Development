package com.example.allinone

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.Transaction
import com.example.allinone.TransactionAdapter
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class FinanceActivity : AppCompatActivity() {

    private var allMonthTransactions = mutableListOf<Transaction>()
    private var filteredTransactions = mutableListOf<Transaction>()
    private lateinit var transactionAdapter: TransactionAdapter
    
    private lateinit var tvBudget: TextView
    private lateinit var tvExpenditure: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvCurrentSavings: TextView
    private lateinit var tvSavingsGoal: TextView
    private lateinit var tvSavingsTitle: TextView
    private lateinit var tvDailyLimit: TextView
    private lateinit var pbBudget: android.widget.ProgressBar
    private lateinit var pbSavings: android.widget.ProgressBar
    private lateinit var tvCategorySummary: TextView
    private lateinit var tvFinanceInsight: TextView
    private lateinit var cardSavings: View
    private lateinit var layoutEmptyState: View
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        val summary = findViewById<View>(R.id.finance_summary)
        tvBudget = summary.findViewById(R.id.tv_monthly_budget)
        tvExpenditure = summary.findViewById(R.id.tv_current_expenditure)
        tvRemaining = summary.findViewById(R.id.tv_remaining_balance)
        tvCurrentSavings = summary.findViewById(R.id.tv_current_savings)
        tvSavingsGoal = summary.findViewById(R.id.tv_savings_goal)
        tvSavingsTitle = summary.findViewById(R.id.tv_savings_title)
        tvDailyLimit = summary.findViewById(R.id.tv_daily_limit)
        pbBudget = summary.findViewById(R.id.pb_budget)
        pbSavings = summary.findViewById(R.id.pb_savings)
        tvCategorySummary = summary.findViewById(R.id.tv_category_summary)
        tvFinanceInsight = summary.findViewById(R.id.tv_finance_insight)
        cardSavings = summary.findViewById(R.id.card_savings)
        layoutEmptyState = findViewById(R.id.layout_empty_state)

        val financeList = findViewById<RecyclerView>(R.id.finance_list)
        financeList.layoutManager = LinearLayoutManager(this)
        
        transactionAdapter = TransactionAdapter(
            filteredTransactions,
            onEdit = { transaction, _ -> showAddTransactionDialog(transaction) },
            onDelete = { transaction, _ -> 
                val idx = filteredTransactions.indexOf(transaction)
                if (idx != -1) deleteTransaction(idx)
            }
        )
        financeList.adapter = transactionAdapter

        loadCurrentMonthTransactions()

        // Feature 1: Category Filters Logic
        setupFilters()

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

        findViewById<View>(R.id.btn_finance_ledger).setOnClickListener {
            startActivity(android.content.Intent(this, LedgerActivity::class.java))
        }
        findViewById<View>(R.id.btn_finance_ledger).visibility = if (DataManager.isFinanceLedgerEnabled) View.VISIBLE else View.GONE

        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_finance)
        if (DataManager.financeAddThemeColor != -1) {
            btnCreate.backgroundTintList = android.content.res.ColorStateList.valueOf(DataManager.financeAddThemeColor)
        }
        btnCreate.setOnClickListener {
            showAddTransactionDialog()
        }

        findViewById<View>(R.id.finance_summary).findViewById<View>(R.id.card_budget).setOnClickListener {
            showSetBudgetDialog()
        }

        cardSavings.setOnClickListener {
            showSetSavingsGoalDialog()
        }
        cardSavings.setOnLongClickListener {
            showSetSavingsGoalDialog()
            true
        }
    }

    private fun setupFilters() {
        val chipGroup = findViewById<ChipGroup>(R.id.cg_finance_filters)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                currentFilter = when (chipId) {
                    R.id.chip_filter_expense -> "Expense"
                    R.id.chip_filter_income -> "Income"
                    R.id.chip_filter_saving -> "Saving"
                    else -> "All"
                }
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        filteredTransactions = if (currentFilter == "All") {
            allMonthTransactions.toMutableList()
        } else {
            allMonthTransactions.filter { it.type == currentFilter }.toMutableList()
        }
        transactionAdapter.updateData(filteredTransactions)
        updateEmptyState()
    }

    private fun deleteTransaction(position: Int) {
        if (position < 0 || position >= filteredTransactions.size) return
        val transaction = filteredTransactions[position]
        DataManager.transactions.remove(transaction)
        allMonthTransactions.remove(transaction)
        filteredTransactions.removeAt(position)
        transactionAdapter.notifyItemRemoved(position)
        DataManager.saveData(this)
        updateSummary()
        updateEmptyState()
    }

    private fun loadCurrentMonthTransactions() {
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val currentMonth = sdf.format(Date())
        allMonthTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == currentMonth
        }.sortedByDescending { it.timestamp }.toMutableList()
        applyFilter()
    }

    private fun updateEmptyState() {
        if (filteredTransactions.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun showAddTransactionDialog(existingTransaction: Transaction? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_transaction)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_trans_amount)
        val chipGroup = dialog.findViewById<com.google.android.material.chip.ChipGroup>(R.id.cg_trans_category)
        val etCustomNote = dialog.findViewById<EditText>(R.id.et_trans_custom_category)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_trans_type)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_trans)
        if (DataManager.financeAddThemeColor != -1) {
            btnSave.setTextColor(DataManager.financeAddThemeColor)
        }
        val btnClose = dialog.findViewById<View>(R.id.btn_close_trans)
        val titleText = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val tvDate = dialog.findViewById<TextView>(R.id.tv_trans_date)
        val tvTime = dialog.findViewById<TextView>(R.id.tv_trans_time)

        if (existingTransaction != null) {
            titleText?.text = "Edit Transaction"
            btnSave.text = "SAVE"
            etAmount.setText(existingTransaction.amount.toString())
            if (existingTransaction.category == "Other") {
                etCustomNote.visibility = View.VISIBLE
                etCustomNote.setText(existingTransaction.title)
            }
            when (existingTransaction.type) {
                "Income" -> rgType.check(R.id.radio_income)
                "Saving" -> rgType.check(R.id.radio_saving)
                else -> rgType.check(R.id.radio_expense)
            }
        }

        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"

        val calendar = Calendar.getInstance()
        if (existingTransaction != null) calendar.timeInMillis = existingTransaction.timestamp
        
        val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())

        tvDate.text = if (existingTransaction == null) "Today" else dateSdf.format(calendar.time)
        tvTime.text = if (existingTransaction == null) "Now" else timeSdf.format(calendar.time)

        val categories = DataManager.financeCustomCategories
        // Feature: Sync category sequence with Manage Categories (Alphabetical + Other last)
        val sortedCategories = categories.filter { it != "Other" }.sorted() + categories.filter { it == "Other" }
        
        var selectedCategoryName = existingTransaction?.category ?: "Other"

        sortedCategories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = category
            chip.isCheckable = true
            chip.isChecked = (category == selectedCategoryName)
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(Color.WHITE)
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategoryName = category
                    if (category == "Other") {
                        etCustomNote.visibility = View.VISIBLE
                        etCustomNote.requestFocus()
                    } else {
                        etCustomNote.visibility = View.GONE
                    }
                }
            }
            chipGroup.addView(chip)
        }

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y); calendar.set(Calendar.MONTH, m); calendar.set(Calendar.DAY_OF_MONTH, d)
                tvDate.text = dateSdf.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
                tvTime.text = timeSdf.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val finalTitle = if (selectedCategoryName == "Other") {
                etCustomNote.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Other Expense"
            } else {
                selectedCategoryName
            }

            if (amount > 0) {
                val type = when (rgType.checkedRadioButtonId) {
                    R.id.radio_income -> "Income"; R.id.radio_saving -> "Saving"; else -> "Expense"
                }
                
                if (existingTransaction == null) {
                    val newTransaction = Transaction(title = finalTitle, amount = amount, type = type, category = selectedCategoryName, timestamp = calendar.timeInMillis)
                    DataManager.transactions.add(0, newTransaction)
                    DataManager.addActivity("Logged $type: ${DataManager.financeCurrency}$amount")
                } else {
                    existingTransaction.title = finalTitle; existingTransaction.amount = amount; existingTransaction.type = type; existingTransaction.category = selectedCategoryName; existingTransaction.timestamp = calendar.timeInMillis
                    DataManager.addActivity("Updated $type: ${DataManager.financeCurrency}$amount")
                }
                
                filterCurrentMonthTransactions()
                transactionAdapter.updateData(filteredTransactions)
                DataManager.saveData(this); updateSummary(); dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun filterCurrentMonthTransactions() {
        loadCurrentMonthTransactions()
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
        tvSavingsTitle.text = DataManager.financeSavingsGoalName.uppercase()

        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
        val dailyLimit = (remaining / daysRemaining).coerceAtLeast(0.0)
        tvDailyLimit.text = String.format(Locale.US, "Daily: %s%.0f", currency, dailyLimit)

        pbBudget.progress = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0, 100) else 0
        pbSavings.progress = if (savingsGoal > 0) ((savings / savingsGoal) * 100).toInt().coerceIn(0, 100) else 0

        updateCategoryBreakdown()
    }

    private fun updateCategoryBreakdown() {
        val currency = DataManager.financeCurrency
        val expenses = allMonthTransactions.filter { it.type == "Expense" }
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

        val topCategory = categoryGroups.first()
        val topPercentage = (topCategory.second / totalSpent) * 100

        when {
            topPercentage > 50 -> {
                tvFinanceInsight.text = String.format(Locale.US, "Alert: %s accounts for over 50%% of your spending!", topCategory.first)
            }
            DataManager.monthlyBudget > 0 && totalSpent > DataManager.monthlyBudget * 0.8 -> {
                tvFinanceInsight.text = "Warning: You have used 80% of your budget. Slow down!"
            }
            else -> {
                val dailyLimit = ((DataManager.monthlyBudget - totalSpent).coerceAtLeast(0.0) /
                    (Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1))
                tvFinanceInsight.text = String.format(Locale.US, "You're on track! Keep your daily spend under %s%d.", currency, dailyLimit.toInt())
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
        val itemToggleLedger = dialog.findViewById<View>(R.id.item_toggle_ledger)
        val switchLedger = dialog.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_ledger_enabled)
        val tvCurrencySummary = dialog.findViewById<TextView>(R.id.tv_currency_summary)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_settings)

        tvCurrencySummary.text = "Tap to change (Current: ${DataManager.financeCurrency})"

        switchLedger.isChecked = DataManager.isFinanceLedgerEnabled
        itemToggleLedger.setOnClickListener {
            switchLedger.toggle()
            DataManager.isFinanceLedgerEnabled = switchLedger.isChecked
            DataManager.saveData(this)
            findViewById<View>(R.id.btn_finance_ledger).visibility = if (DataManager.isFinanceLedgerEnabled) View.VISIBLE else View.GONE
        }
        switchLedger.setOnCheckedChangeListener { _, isChecked ->
            DataManager.isFinanceLedgerEnabled = isChecked
            DataManager.saveData(this)
            findViewById<View>(R.id.btn_finance_ledger).visibility = if (isChecked) View.VISIBLE else View.GONE
        }

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

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val btnToggleDelete = dialog.findViewById<ImageButton>(R.id.btn_toggle_delete_mode)

        var isDeleteMode = false

        fun refreshList() {
            container.removeAllViews()

            // Feature 1: Sort categories, but keep "Other" at the bottom
            val sortedList = DataManager.financeCustomCategories.filter { it != "Other" }.sorted() +
                            DataManager.financeCustomCategories.filter { it == "Other" }

            sortedList.forEach { category ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_finance_category_manage, container, false)

                val tvName = itemView.findViewById<TextView>(R.id.tv_cat_name)
                val btnDelete = itemView.findViewById<View>(R.id.btn_delete_cat)
                val ivIcon = itemView.findViewById<ImageView>(R.id.iv_cat_icon)

                tvName.text = category
                ivIcon.setImageResource(R.drawable.ic_finance)

                // Feature 2: Clicking the cross removes the category (Only in delete mode)
                if (category == "Other" || !isDeleteMode) {
                    btnDelete.visibility = View.GONE
                } else {
                    btnDelete.visibility = View.VISIBLE
                    btnDelete.setOnClickListener {
                        DataManager.financeCustomCategories.remove(category)
                        DataManager.financeCategoryIcons.remove(category)
                        DataManager.financeCategoryColors.remove(category)
                        DataManager.saveData(this)
                        refreshList()
                    }
                }

                container.addView(itemView)
            }
        }

        btnToggleDelete.setOnClickListener {
            isDeleteMode = !isDeleteMode
            btnToggleDelete.imageTintList = android.content.res.ColorStateList.valueOf(
                if (isDeleteMode) Color.parseColor("#FF5252") else Color.WHITE
            )
            refreshList()
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.financeCustomCategories.contains(name)) {
                // Feature 1: Position at last (but handled by sort logic in refreshList)
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
        dialog.setContentView(R.layout.dialog_add_ledger) // Re-use simplified layout
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etGoal = dialog.findViewById<EditText>(R.id.et_ledger_amount)
        val etName = dialog.findViewById<EditText>(R.id.et_person_name)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_ledger)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_ledger)
        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)

        // Hide unused fields
        dialog.findViewById<View>(R.id.tv_person_label).visibility = View.VISIBLE // We'll use this for Goal Name
        (dialog.findViewById<View>(R.id.tv_person_label) as TextView).text = "GOAL FOR"

        dialog.findViewById<View>(R.id.rg_ledger_type).visibility = View.GONE
        dialog.findViewById<View>(R.id.tv_ledger_due_date).visibility = View.GONE
        dialog.findViewById<View>(R.id.et_ledger_note).visibility = View.GONE

        title?.text = "SET SAVINGS GOAL"
        etGoal.hint = "${DataManager.financeCurrency}0.00"
        etGoal.setText(DataManager.monthlySavingsGoal.toInt().toString())

        etName.hint = "e.g. New Bike, iPhone"
        etName.setText(DataManager.financeSavingsGoalName)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newGoal = etGoal.text.toString().toDoubleOrNull() ?: 0.0
            val newName = etName.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Savings Goal"

            DataManager.monthlySavingsGoal = newGoal
            DataManager.financeSavingsGoalName = newName
            DataManager.saveData(this)
            updateSummary()
            dialog.dismiss()
        }

        dialog.show()
    }
}
