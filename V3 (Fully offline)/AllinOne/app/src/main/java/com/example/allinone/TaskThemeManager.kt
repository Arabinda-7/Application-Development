package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.example.allinone.core.utils.UIUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskThemeManager(
    private val context: Context,
    private val auraView: View,
    private val fab: FloatingActionButton
) {
    fun applyTheme() {
        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(taskColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient

        val darkenedFabColor = UIUtils.darkenColor(taskColor, 0.5f)
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
