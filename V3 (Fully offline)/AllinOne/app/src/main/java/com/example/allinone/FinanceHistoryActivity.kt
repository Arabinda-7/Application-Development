package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.allinone.core.utils.FinanceUiHelper
import com.example.allinone.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class FinanceHistoryActivity : BaseActivity() {

    private lateinit var tvSelectedYear: TextView
    private val availableYears = (2020..2030).toList()
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    private lateinit var containerSpendGraph: LinearLayout
    private lateinit var avgLine: View
    private lateinit var avgLabel: TextView
    private lateinit var tooltipCard: com.google.android.material.card.MaterialCardView
    private lateinit var tooltipText: TextView
    private lateinit var auraView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_history)

        tvSelectedYear = findViewById(R.id.tv_selected_year)
        tvSelectedYear.text = currentYear.toString()

        containerSpendGraph = findViewById(R.id.container_spend_graph)
        avgLine = findViewById(R.id.view_avg_line)
        avgLabel = findViewById(R.id.tv_avg_line_label)
        tooltipCard = findViewById(R.id.card_graph_tooltip)
        tooltipText = findViewById(R.id.tv_tooltip_text)
        auraView = findViewById(R.id.finance_history_aura_background)
        
        tvSelectedYear.setOnClickListener {
            showYearPickerDialog()
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_history_options).setOnClickListener {
            showHistoryOptionsMenu()
        }

        findViewById<View>(R.id.card_month_history).setOnClickListener {
            val intent = Intent(this, FinanceMonthHistoryActivity::class.java).apply {
                putExtra("year", currentYear)
                putExtra("month", Calendar.getInstance().get(Calendar.MONTH))
            }
            startActivity(intent)
        }

        updateYearlyAnalytics()
        setupKeyboardHandling(findViewById(R.id.finance_history_root), findViewById(R.id.finance_history_content_container), 12)
        updateDynamicBackground()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun showYearPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_year_roller)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 20
            window.setDimAmount(0.6f)
        }
        
        val picker = dialog.findViewById<NumberPicker>(R.id.year_number_picker)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_year)
        
        picker.minValue = availableYears.first()
        picker.maxValue = availableYears.last()
        picker.value = currentYear

        btnSave.setOnClickListener {
            currentYear = picker.value
            tvSelectedYear.text = currentYear.toString()
            updateYearlyAnalytics()
            setupKeyboardHandling(findViewById(R.id.finance_history_root), findViewById(R.id.finance_history_content_container), 12)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showHistoryOptionsMenu() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_history_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Set current color indicators
        val spendingIndicator = dialog.findViewById<View>(R.id.view_spending_indicator)
        val savingsIndicator = dialog.findViewById<View>(R.id.view_savings_indicator)
        
        val spendColor = DataManager.financeGraphColor
        spendingIndicator.backgroundTintList = ColorStateList.valueOf(if (spendColor != -1) spendColor else Color.parseColor("#FF5252"))
        
        val saveColor = DataManager.financeGraphSavingsColor
        savingsIndicator.backgroundTintList = ColorStateList.valueOf(if (saveColor != -1) saveColor else Color.parseColor("#4CAF50"))

        dialog.findViewById<View>(R.id.option_start_month).setOnClickListener {
            dialog.dismiss()
            showStartMonthPickerDialog()
        }

        dialog.findViewById<View>(R.id.option_spending_color).setOnClickListener {
            dialog.dismiss()
            showGraphColorPickerDialog(isSpending = true)
        }

        dialog.findViewById<View>(R.id.option_savings_color).setOnClickListener {
            dialog.dismiss()
            showGraphColorPickerDialog(isSpending = false)
        }

        dialog.findViewById<View>(R.id.btn_close_settings).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showGraphColorPickerDialog(isSpending: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_color_picker_history)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val grid = dialog.findViewById<GridLayout>(R.id.color_grid)
        dialog.findViewById<TextView>(R.id.tv_picker_title).text = if (isSpending) "SPENDING COLOR" else "SAVINGS COLOR"
        
        val colors = listOf(
            "#FF5252", "#FBBC05", "#4285F4", "#4CAF50",
            "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
            "#00BCD4", "#009688", "#FF9800", "#FF5722",
            "#795548", "#9E9E9E", "#607D8B", "#FFFFFF"
        )

        colors.forEach { colorHex ->
            val colorView = View(this)
            val size = (40 * resources.displayMetrics.density).toInt()
            val params = GridLayout.LayoutParams()
            params.width = size
            params.height = size
            params.setMargins(12, 12, 12, 12)
            colorView.layoutParams = params
            
            // Feature: Clearer Color Preview
            val shape = android.graphics.drawable.GradientDrawable()
            shape.shape = android.graphics.drawable.GradientDrawable.OVAL
            shape.setColor(Color.parseColor(colorHex))
            shape.setStroke(2, Color.parseColor("#33FFFFFF"))
            colorView.background = shape

            colorView.setOnClickListener {
                if (isSpending) {
                    DataManager.financeGraphColor = Color.parseColor(colorHex)
                } else {
                    DataManager.financeGraphSavingsColor = Color.parseColor(colorHex)
                }
                DataManager.saveData(this)
                updateYearlyAnalytics()
                setupKeyboardHandling(findViewById(R.id.finance_history_root), findViewById(R.id.finance_history_content_container), 12)
                dialog.dismiss()
            }
            grid.addView(colorView)
        }

        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showStartMonthPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_year_roller)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialog.findViewById<TextView>(R.id.tv_dialog_title).text = "Start Month"
        val picker = dialog.findViewById<NumberPicker>(R.id.year_number_picker)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_year)
        
        picker.minValue = 0
        picker.maxValue = 11
        picker.displayedValues = monthNames.map { it.take(3) }.toTypedArray()
        picker.value = DataManager.financeGraphStartMonth

        btnSave.setOnClickListener {
            DataManager.financeGraphStartMonth = picker.value
            DataManager.saveData(this)
            updateYearlyAnalytics()
            setupKeyboardHandling(findViewById(R.id.finance_history_root), findViewById(R.id.finance_history_content_container), 12)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun updateYearlyAnalytics() {
        val currency = DataManager.financeCurrency
        val yearKey = currentYear.toString()
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())

        val yearlyTransactions = synchronized(DataManager.transactions) {
            DataManager.transactions.filter {
                sdf.format(Date(it.timestamp)) == yearKey
            }
        }
        
        findViewById<TextView>(R.id.tv_pill_total).text = String.format(Locale.US, "Total Spent: %s%.0f", currency, yearlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount })
        findViewById<TextView>(R.id.tv_pill_savings).text = String.format(Locale.US, "Savings: %s%.0f", currency, yearlyTransactions.filter { it.type == "Saving" }.sumOf { it.amount })
        
        // Update dashboard values
        val monthCodeSdf = SimpleDateFormat("MM", Locale.getDefault())
        val monthNameSdf = SimpleDateFormat("MMMM", Locale.getDefault())
        
        val uniqueMonthsCount = yearlyTransactions.map { 
            monthCodeSdf.format(Date(it.timestamp))
        }.distinct().size.coerceAtLeast(1)

        val totalSpent = yearlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalSavings = yearlyTransactions.filter { it.type == "Saving" }.sumOf { it.amount }

        val avgSpent = totalSpent / uniqueMonthsCount
        val highestMonth = yearlyTransactions
            .filter { it.type == "Expense" }
            .groupBy { monthNameSdf.format(Date(it.timestamp)) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .maxByOrNull { it.value }?.key ?: "None"

        findViewById<TextView>(R.id.tv_yearly_avg_spent).text = String.format(Locale.US, "%s%.0f", currency, avgSpent)
        findViewById<TextView>(R.id.tv_yearly_total_savings).text = String.format(Locale.US, "%s%.0f", currency, totalSavings)
        findViewById<TextView>(R.id.tv_yearly_highest_month).text = highestMonth

        updateSpendGraph(yearlyTransactions)
    }

    private fun updateSpendGraph(transactions: List<Transaction>) {
        FinanceUiHelper.updateSpendGraph(
            this,
            containerSpendGraph,
            tooltipCard,
            tooltipText,
            avgLine,
            avgLabel,
            transactions,
            monthNames
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            DataManager.loadData(this@FinanceHistoryActivity)
            updateYearlyAnalytics()
            updateDynamicBackground()
        }
    }

    private fun updateDynamicBackground() {
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                adjustAlpha(financeColor, 0.4f),
                Color.BLACK
            )
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        return FinanceUiHelper.adjustAlpha(color, factor)
    }
}
