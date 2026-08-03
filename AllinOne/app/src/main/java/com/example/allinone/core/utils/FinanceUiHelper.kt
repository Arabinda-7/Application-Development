package com.example.allinone.core.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.allinone.DataManager
import com.example.allinone.R
import com.example.allinone.data.model.Transaction
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

object FinanceUiHelper {

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun updateSpendGraph(
        activity: Activity,
        container: LinearLayout,
        tooltipCard: MaterialCardView,
        tooltipText: TextView,
        avgLine: View,
        avgLabel: TextView,
        transactions: List<Transaction>,
        monthNames: List<String>
    ) {
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

        if (avgSpent > 0) {
            avgLine.visibility = View.VISIBLE
            avgLabel.visibility = View.VISIBLE
            val chartHeightPx = 110 * activity.resources.displayMetrics.density
            val marginFromBottom = (avgSpent / maxVal * chartHeightPx).toInt() + (20 * activity.resources.displayMetrics.density).toInt()

            val params = avgLine.layoutParams as FrameLayout.LayoutParams
            params.gravity = android.view.Gravity.BOTTOM
            params.bottomMargin = marginFromBottom
            avgLine.layoutParams = params

            val labelParams = avgLabel.layoutParams as FrameLayout.LayoutParams
            labelParams.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
            labelParams.bottomMargin = marginFromBottom + 2
            labelParams.marginStart = (8 * activity.resources.displayMetrics.density).toInt()
            avgLabel.layoutParams = labelParams
        } else {
            avgLine.visibility = View.GONE
            avgLabel.visibility = View.GONE
        }

        rotatedSpent.forEachIndexed { index, spent ->
            val savings = rotatedSavings[index]
            val actualMonthIndex = (index + startMonth) % 12

            val barWrapper = LinearLayout(activity)
            val wrapperParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            barWrapper.layoutParams = wrapperParams
            barWrapper.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            barWrapper.orientation = LinearLayout.VERTICAL

            val dualBarContainer = LinearLayout(activity)
            dualBarContainer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1f)
            dualBarContainer.gravity = android.view.Gravity.BOTTOM
            dualBarContainer.orientation = LinearLayout.HORIZONTAL

            val spentBar = LinearLayout(activity)
            spentBar.orientation = LinearLayout.VERTICAL
            spentBar.gravity = android.view.Gravity.BOTTOM
            val spentHeight = (spent / maxVal * (80 * activity.resources.displayMetrics.density)).toInt().coerceAtLeast(4)
            val spentParams = LinearLayout.LayoutParams((12 * activity.resources.displayMetrics.density).toInt(), 0)
            spentParams.setMargins(0, 0, 0, 4)
            spentBar.layoutParams = spentParams
            spentBar.background = ContextCompat.getDrawable(activity, R.drawable.bg_dialog_rounded)

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
                    val segment = View(activity)
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

            val savingsBar = View(activity)
            val savingsHeight = (savings / maxVal * (80 * activity.resources.displayMetrics.density)).toInt().coerceAtLeast(4)
            val savingsParams = LinearLayout.LayoutParams((12 * activity.resources.displayMetrics.density).toInt(), 0)
            savingsParams.setMargins(4, 0, 0, 4)
            savingsBar.layoutParams = savingsParams
            savingsBar.background = ContextCompat.getDrawable(activity, R.drawable.bg_dialog_rounded)

            val savingsBaseColor = DataManager.financeGraphSavingsColor
            savingsBar.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (savingsBaseColor != -1) savingsBaseColor else Color.parseColor("#4CAF50")
            )

            dualBarContainer.addView(spentBar)
            if (savings > 0) {
                dualBarContainer.addView(savingsBar)
            }

            val tvMonth = TextView(activity)
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
                    params.topMargin = (4 * activity.resources.displayMetrics.density).toInt()

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
}
