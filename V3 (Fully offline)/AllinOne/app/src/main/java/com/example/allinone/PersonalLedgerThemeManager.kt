package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.example.allinone.core.utils.UIUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PersonalLedgerThemeManager(
    private val auraView: View,
    private val fab: FloatingActionButton
) {
    fun applyTheme() {
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(financeColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient

        val darkenedFabColor = UIUtils.darkenColor(financeColor, 0.5f)
        fab.backgroundTintList = android.content.res.ColorStateList.valueOf(darkenedFabColor)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
