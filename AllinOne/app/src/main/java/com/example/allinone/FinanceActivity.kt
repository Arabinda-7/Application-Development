package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
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

        findViewById<View>(R.id.btn_finance_history).setOnClickListener {
            startActivity(android.content.Intent(this, FinanceHistoryActivity::class.java))
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
        }.toMutableList()
    }

    private fun showEditTransactionDialog(transaction: Transaction, position: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_transaction)

        val etAmount = dialog.findViewById<EditText>(R.id.et_trans_amount)
        val etTitle = dialog.findViewById<EditText>(R.id.et_trans_title)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_trans_type)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_trans)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_trans)
        val titleText = dialog.findViewById<TextView>(R.id.tv_dialog_title)

        titleText?.text = "Edit Transaction"
        etAmount.setText(transaction.amount.toString())
        etTitle.setText(transaction.title)
        when (transaction.type) {
            "Income" -> rgType.check(R.id.radio_income)
            "Saving" -> rgType.check(R.id.radio_saving)
            else -> rgType.check(R.id.radio_expense)
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
                
                transactionAdapter.notifyItemChanged(position)
                DataManager.saveData(this)
                updateSummary()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateSummary() {
        val budget = DataManager.monthlyBudget
        val spent = DataManager.getCurrentMonthExpenditure()
        val income = DataManager.getCurrentMonthIncome()
        val remaining = (budget - spent) + income
        val savings = DataManager.getCurrentMonthSavings()
        val savingsGoal = DataManager.monthlySavingsGoal

        tvBudget.text = String.format(Locale.US, "₹%.0f", budget)
        
        tvExpenditure.text = String.format(Locale.US, "₹%.0f", spent)
        tvExpenditure.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        
        tvRemaining.text = String.format(Locale.US, "₹%.0f", remaining)
        
        if (remaining < 0) {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        } else {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        tvCurrentSavings.text = String.format(Locale.US, "₹%.0f", savings)
        tvSavingsGoal.text = String.format(Locale.US, "Goal: ₹%.0f", savingsGoal)
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_set_budget)

        val etBudget = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_budget)

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
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_set_budget)

        val etGoal = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_budget)
        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)

        title?.text = "SET SAVINGS GOAL"
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
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_transaction)

        val etAmount = dialog.findViewById<EditText>(R.id.et_trans_amount)
        val etTitle = dialog.findViewById<EditText>(R.id.et_trans_title)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_trans_type)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_trans)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_trans)

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
                    type = type
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
