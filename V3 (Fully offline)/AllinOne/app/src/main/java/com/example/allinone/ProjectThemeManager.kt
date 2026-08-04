package com.example.allinone

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.example.allinone.core.utils.UIUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProjectThemeManager(
    private val auraView: View,
    private val fab: FloatingActionButton
) {
    fun applyTheme() {
        val projectColor = if (DataManager.globalProjectColor != -1) DataManager.globalProjectColor else Color.parseColor("#1A73E8")
        
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(projectColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient

        val darkenedFabColor = UIUtils.darkenColor(projectColor, 0.5f)
        fab.backgroundTintList = ColorStateList.valueOf(darkenedFabColor)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}
