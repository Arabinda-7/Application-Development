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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class FinanceHistoryActivity : AppCompatActivity() {

    private lateinit var tvSelectedYear: TextView
    private val availableYears = (2020..2030).toList()
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_history)

        tvSelectedYear = findViewById(R.id.tv_selected_year)
        tvSelectedYear.text = currentYear.toString()
        
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

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun showYearPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_year_roller)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val picker = dialog.findViewById<NumberPicker>(R.id.year_number_picker)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_year)
        
        picker.minValue = availableYears.first()
        picker.maxValue = availableYears.last()
        picker.value = currentYear

        btnSave.setOnClickListener {
            currentYear = picker.value
            tvSelectedYear.text = currentYear.toString()
            updateYearlyAnalytics()
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
        dialog.setContentView(R.layout.dialog_settings_color_picker)
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
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun updateYearlyAnalytics() {
        val currency = DataManager.financeCurrency
        val yearKey = currentYear.toString()
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())

        val yearlyTransactions = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == yearKey
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
        val container = findViewById<LinearLayout>(R.id.container_spend_graph)
        val avgLine = findViewById<View>(R.id.view_avg_line)
        val avgLabel = findViewById<TextView>(R.id.tv_avg_line_label)
        val tooltipCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_graph_tooltip)
        val tooltipText = findViewById<TextView>(R.id.tv_tooltip_text)
        
        container.removeAllViews()
        tooltipCard.visibility = View.GONE

        val sdfMonth = SimpleDateFormat("MM", Locale.getDefault())
        val monthlySpent = DoubleArray(12) { 0.0 }
        val monthlySavings = DoubleArray(12) { 0.0 }
        
        transactions.forEach {
            val monthIndex = sdfMonth.format(Date(it.timestamp)).toInt() - 1
            if (monthIndex in 0..11) {
                if (it.type == "Expense") monthlySpent[monthIndex] += it.amount
                else if (it.type == "Saving") monthlySavings[monthIndex] += it.amount
            }
        }

        val startMonth = DataManager.financeGraphStartMonth
        val rotatedSpent = DoubleArray(12)
        val rotatedSavings = DoubleArray(12)
        val rotatedMonthLabels = Array(12) { "" }
        val rotatedMonthFullNames = Array(12) { "" }

        for (i in 0..11) {
            val actualMonthIndex = (i + startMonth) % 12
            rotatedSpent[i] = monthlySpent[actualMonthIndex]
            rotatedSavings[i] = monthlySavings[actualMonthIndex]
            rotatedMonthLabels[i] = monthNames[actualMonthIndex].take(1)
            rotatedMonthFullNames[i] = monthNames[actualMonthIndex]
        }

        val maxVal = (rotatedSpent.maxOrNull() ?: 1.0).coerceAtLeast(rotatedSavings.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
        val avgSpent = if (rotatedSpent.count { it > 0 } > 0) rotatedSpent.sum() / rotatedSpent.count { it > 0 } else 0.0

        // Feature 3: Position Average Line
        if (avgSpent > 0) {
            avgLine.visibility = View.VISIBLE
            avgLabel.visibility = View.VISIBLE
            val chartHeightPx = 110 * resources.displayMetrics.density
            val marginFromBottom = (avgSpent / maxVal * chartHeightPx).toInt() + (20 * resources.displayMetrics.density).toInt()
            
            val params = avgLine.layoutParams as FrameLayout.LayoutParams
            params.gravity = android.view.Gravity.BOTTOM
            params.bottomMargin = marginFromBottom
            avgLine.layoutParams = params
            
            val labelParams = avgLabel.layoutParams as FrameLayout.LayoutParams
            labelParams.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
            labelParams.bottomMargin = marginFromBottom + 2
            labelParams.marginStart = (8 * resources.displayMetrics.density).toInt()
            avgLabel.layoutParams = labelParams
        } else {
            avgLine.visibility = View.GONE
            avgLabel.visibility = View.GONE
        }

        rotatedSpent.forEachIndexed { index, spent ->
            val savings = rotatedSavings[index]
            val actualMonthIndex = (index + startMonth) % 12
            
            val barWrapper = LinearLayout(this)
            val wrapperParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            barWrapper.layoutParams = wrapperParams
            barWrapper.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            barWrapper.orientation = LinearLayout.VERTICAL

            // Feature 1: Dual Bar Container
            val dualBarContainer = LinearLayout(this)
            dualBarContainer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1f)
            dualBarContainer.gravity = android.view.Gravity.BOTTOM
            dualBarContainer.orientation = LinearLayout.HORIZONTAL

            // Spending Bar (Feature 4: Stacked Categories)
            val spentBar = LinearLayout(this)
            spentBar.orientation = LinearLayout.VERTICAL
            spentBar.gravity = android.view.Gravity.BOTTOM
            val spentHeight = (spent / maxVal * (80 * resources.displayMetrics.density)).toInt().coerceAtLeast(4)
            val spentParams = LinearLayout.LayoutParams((12 * resources.displayMetrics.density).toInt(), 0)
            spentParams.setMargins(0, 0, 0, 4)
            spentBar.layoutParams = spentParams
            spentBar.background = ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded)
            
            // Get category breakdown for this month
            val monthTransactions = transactions.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.MONTH) == actualMonthIndex && it.type == "Expense"
            }
            val catBreakdown = monthTransactions.groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .toList().sortedByDescending { it.second }

            if (spent > 0 && catBreakdown.isNotEmpty()) {
                val baseColor = DataManager.financeGraphColor
                val colors = if (baseColor != -1) {
                    listOf(baseColor, adjustAlpha(baseColor, 0.7f), adjustAlpha(baseColor, 0.4f))
                } else {
                    listOf("#FF5252", "#FBBC05", "#4285F4").map { Color.parseColor(it) }
                }

                catBreakdown.take(3).forEachIndexed { i, (_, amt) ->
                    val segHeight = (amt / spent * spentHeight).toInt().coerceAtLeast(1)
                    val segment = View(this)
                    segment.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, segHeight)
                    segment.setBackgroundColor(colors[i % colors.size])
                    spentBar.addView(segment)
                }
            } else {
                val baseColor = DataManager.financeGraphColor
                spentBar.backgroundTintList = ColorStateList.valueOf(
                    if (spent == rotatedSpent.maxOrNull() && spent > 0) {
                        if (baseColor != -1) baseColor else Color.parseColor("#4CAF50")
                    } else Color.parseColor("#33FFFFFF")
                )
            }

            // Savings Bar
            val savingsBar = View(this)
            val savingsHeight = (savings / maxVal * (80 * resources.displayMetrics.density)).toInt().coerceAtLeast(4)
            val savingsParams = LinearLayout.LayoutParams((12 * resources.displayMetrics.density).toInt(), 0)
            savingsParams.setMargins(4, 0, 0, 4)
            savingsBar.layoutParams = savingsParams
            savingsBar.background = ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded)
            
            val savingsBaseColor = DataManager.financeGraphSavingsColor
            savingsBar.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (savingsBaseColor != -1) savingsBaseColor else Color.parseColor("#4CAF50")
            )

            dualBarContainer.addView(spentBar)
            if (savings > 0) {
                dualBarContainer.addView(savingsBar)
            }

            val tvMonth = TextView(this)
            tvMonth.text = rotatedMonthLabels[index]
            tvMonth.setTextColor(Color.parseColor("#80FFFFFF"))
            tvMonth.textSize = 8f
            tvMonth.gravity = android.view.Gravity.CENTER

            barWrapper.addView(dualBarContainer)
            barWrapper.addView(tvMonth)
            
            barWrapper.post {
                val sAnimator = android.animation.ValueAnimator.ofInt(0, spentHeight)
                sAnimator.addUpdateListener {
                    val p = spentBar.layoutParams
                    p.height = it.animatedValue as Int
                    spentBar.layoutParams = p
                }
                sAnimator.duration = 500
                sAnimator.start()

                if (savings > 0) {
                    val vAnimator = android.animation.ValueAnimator.ofInt(0, savingsHeight)
                    vAnimator.addUpdateListener {
                        val p = savingsBar.layoutParams
                        p.height = it.animatedValue as Int
                        savingsBar.layoutParams = p
                    }
                    vAnimator.duration = 700
                    vAnimator.start()
                }
            }

            barWrapper.setOnClickListener {
                tooltipCard.visibility = View.VISIBLE
                val topCatStr = if (catBreakdown.isNotEmpty()) " | Top: ${catBreakdown[0].first}" else ""
                tooltipText.text = String.format(Locale.US, "%s: %s%.0f spent%s", 
                    rotatedMonthFullNames[index], DataManager.financeCurrency, spent, topCatStr)
                
                tooltipCard.post {
                    val params = tooltipCard.layoutParams as FrameLayout.LayoutParams
                    params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
                    params.topMargin = (4 * resources.displayMetrics.density).toInt()
                    
                    val barWidth = container.width / 12
                    var startMargin = (index * barWidth) + (barWidth / 2) - (tooltipCard.width / 2)
                    startMargin = startMargin.coerceIn(0, container.width - tooltipCard.width)
                    
                    params.leftMargin = startMargin
                    tooltipCard.layoutParams = params
                }
                
                tooltipCard.removeCallbacks(null)
                tooltipCard.postDelayed({ tooltipCard.visibility = View.GONE }, 3000)
            }
            
            container.addView(barWrapper)
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
