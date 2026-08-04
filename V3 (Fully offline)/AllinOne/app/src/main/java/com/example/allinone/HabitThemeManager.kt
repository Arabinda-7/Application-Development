package com.example.allinone

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import com.google.android.material.card.MaterialCardView

class HabitThemeManager(
    private val context: Context,
    private val auraView: View,
    private val chips: List<RadioButton>,
    private val createButton: MaterialCardView,
    private val sectionProgressBar: ProgressBar
) {
    fun applyTheme() {
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else Color.parseColor("#FF7A59")
        
        applyAura(habitColor)
        applyChips(habitColor)
        
        createButton.strokeColor = habitColor
        sectionProgressBar.progressTintList = ColorStateList.valueOf(habitColor)
    }

    private fun applyAura(habitColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(habitColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient
    }

    private fun applyChips(habitColor: Int) {
        val density = context.resources.displayMetrics.density
        chips.forEach { chip ->
            val checkedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 19f * density
                setColor(habitColor)
            }
            
            val uncheckedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 19f * density
                setColor(Color.TRANSPARENT)
                setStroke(Math.round(1.5f * density), habitColor)
            }

            val stateListDrawable = StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                addState(intArrayOf(), uncheckedDrawable)
            }
            
            chip.background = stateListDrawable

            val textColorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(Color.WHITE, habitColor)
            )
            chip.setTextColor(textColorStateList)
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
