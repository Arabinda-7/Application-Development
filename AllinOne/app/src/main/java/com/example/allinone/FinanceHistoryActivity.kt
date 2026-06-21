package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.example.allinone.*

class FinanceHistoryActivity : AppCompatActivity() {

    private lateinit var monthsList: RecyclerView
    private lateinit var transactionsList: RecyclerView
    private lateinit var monthAdapter: MonthAdapter
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var tvYear: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_history)

        tvYear = findViewById(R.id.tv_current_year)
        tvYear.text = Calendar.getInstance().get(Calendar.YEAR).toString()

        findViewById<View>(R.id.btn_back).setOnClickListener {
            if (transactionsList.visibility == View.VISIBLE || findViewById<View>(R.id.month_details_container).visibility == View.VISIBLE) {
                findViewById<View>(R.id.month_details_container).visibility = View.GONE
                monthsList.visibility = View.VISIBLE
                tvYear.visibility = View.VISIBLE
            } else {
                finish()
            }
        }

        monthsList = findViewById(R.id.history_months_list)
        transactionsList = findViewById(R.id.month_transactions_list)

        val monthItems = listOf(
            MonthItem("January", R.drawable.ic_habit_tracker, "#FF7043"),   // Salmon
            MonthItem("February", R.drawable.ic_workout_routine, "#FFD54F"), // Yellow
            MonthItem("March", R.drawable.ic_todo_list, "#4DB6AC"),       // Teal
            MonthItem("April", R.drawable.ic_notes, "#64B5F6"),           // Blue
            MonthItem("May", R.drawable.ic_project, "#5C6BC0"),          // Indigo
            MonthItem("June", R.drawable.ic_finance, "#81C784"),          // Green
            MonthItem("July", R.drawable.ic_water, "#4FC3F7"),            // Light Blue
            MonthItem("August", R.drawable.ic_sleep, "#9575CD"),           // Purple
            MonthItem("September", R.drawable.ic_meditation, "#FF8A65"),   // Deep Orange
            MonthItem("October", R.drawable.ic_fitness, "#A1887F"),        // Brown
            MonthItem("November", R.drawable.ic_book, "#90A4AE"),          // Blue Grey
            MonthItem("December", R.drawable.ic_communication, "#F06292")  // Pink
        )
        
        monthAdapter = MonthAdapter(monthItems) { selectedMonth ->
            showTransactionsForMonth(selectedMonth)
        }

        monthsList.layoutManager = GridLayoutManager(this, 4)
        monthsList.adapter = monthAdapter

        transactionsList.layoutManager = LinearLayoutManager(this)
    }

    private fun showTransactionsForMonth(monthName: String) {
        val displaySdf = SimpleDateFormat("MMMM", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val date = displaySdf.parse(monthName) ?: Date()
        
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        calendar.time = date
        calendar.set(Calendar.YEAR, year)
        
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
        
        // Update Summary
        val budget = DataManager.monthlyBudgets[monthKey] ?: 0.0
        val spent = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val income = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val remaining = (budget - spent) + income
        val savings = filteredTransactions.filter { it.type == "Saving" }.sumOf { it.amount }
        val savingsGoal = DataManager.monthlySavingsGoals[monthKey] ?: 0.0

        val summary = findViewById<View>(R.id.finance_summary)
        summary.findViewById<TextView>(R.id.tv_monthly_budget).text = String.format(Locale.US, "₹%.0f", budget)
        summary.findViewById<TextView>(R.id.tv_current_expenditure).text = String.format(Locale.US, "₹%.0f", spent)
        
        val tvRemaining = summary.findViewById<TextView>(R.id.tv_remaining_balance)
        tvRemaining.text = String.format(Locale.US, "₹%.0f", remaining)
        if (remaining < 0) {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        } else {
            tvRemaining.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        summary.findViewById<TextView>(R.id.tv_current_savings).text = String.format(Locale.US, "₹%.0f", savings)
        summary.findViewById<TextView>(R.id.tv_savings_goal).text = String.format(Locale.US, "Goal: ₹%.0f", savingsGoal)

        monthsList.visibility = View.GONE
        tvYear.visibility = View.GONE
        findViewById<View>(R.id.month_details_container).visibility = View.VISIBLE
    }
}
