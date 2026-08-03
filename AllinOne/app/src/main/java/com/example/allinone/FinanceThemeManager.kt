package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.RadioButton
import com.example.allinone.core.utils.UIUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FinanceThemeManager(
    private val context: Context,
    private val auraView: View,
    private val cards: List<MaterialCardView>,
    private val chips: List<RadioButton>,
    private val fab: FloatingActionButton
) {
    fun applyTheme() {
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        
        applyAura(financeColor)
        applyCards(financeColor)
        applyChips(financeColor)
        
        val darkenedFabColor = UIUtils.darkenColor(financeColor, 0.5f)
        fab.backgroundTintList = android.content.res.ColorStateList.valueOf(darkenedFabColor)
    }

    private fun applyAura(financeColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(financeColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient
    }

    private fun applyCards(financeColor: Int) {
        val strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cards.forEach { card ->
            card.setCardBackgroundColor(Color.TRANSPARENT)
            card.strokeColor = financeColor
            card.strokeWidth = strokeWidth
        }
    }

    private fun applyChips(financeColor: Int) {
        val darkenedColor = UIUtils.darkenColor(financeColor, 0.5f)
        val density = context.resources.displayMetrics.density

        chips.forEach { chip ->
            val checkedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 19f * density
                setColor(darkenedColor)
            }
            
            val uncheckedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 19f * density
                setColor(Color.TRANSPARENT)
                setStroke(Math.round(1.5f * density), financeColor)
            }

            val stateListDrawable = StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                addState(intArrayOf(), uncheckedDrawable)
            }
            chip.background = stateListDrawable
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
