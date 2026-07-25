package com.example.allinone

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.google.android.material.card.MaterialCardView

class FinanceLedgerThemeManager(
    private val auraView: View,
    private val btnAddLedger: View,
    private val cards: List<MaterialCardView>
) {
    fun applyTheme() {
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        
        applyAura(financeColor)
        
        val darkenedFabColor = UIUtils.darkenColor(financeColor, 0.5f)
        btnAddLedger.backgroundTintList = android.content.res.ColorStateList.valueOf(darkenedFabColor)
            
        if (cards.size >= 3) {
            cards[0].strokeColor = Color.parseColor("#FF5252")
            cards[1].strokeColor = Color.parseColor("#4CAF50")
            cards[2].strokeColor = financeColor
        }
    }

    private fun applyAura(financeColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(financeColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
