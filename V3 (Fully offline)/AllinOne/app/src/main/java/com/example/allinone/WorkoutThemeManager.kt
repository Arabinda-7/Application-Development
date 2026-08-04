package com.example.allinone

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.example.allinone.core.utils.UIUtils

class WorkoutThemeManager(
    private val context: Context,
    private val auraView: View,
    private val filterGroup: RadioGroup,
    private val createButton: MaterialCardView,
    private val sectionProgressBar: ProgressBar,
    private val historyCards: List<MaterialCardView> = emptyList(),
    private val historyValues: List<TextView> = emptyList()
) {
    fun applyTheme() {
        val workoutColor = if (DataManager.globalWorkoutColor != -1) DataManager.globalWorkoutColor else Color.parseColor("#FFFFB800")
        
        applyAura(workoutColor)
        applyChips(workoutColor)
        
        createButton.strokeColor = workoutColor
        sectionProgressBar.progressTintList = ColorStateList.valueOf(workoutColor)
        
        historyCards.forEach { it.strokeColor = workoutColor }
        historyValues.forEach { it.setTextColor(workoutColor) }
    }

    private fun applyAura(workoutColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(adjustAlpha(workoutColor, 0.4f), Color.BLACK)
        )
        auraView.background = gradient
    }

    private fun applyChips(workoutColor: Int) {
        val density = context.resources.displayMetrics.density
        val darkenedColor = UIUtils.darkenColor(workoutColor, 0.5f)

        for (i in 0 until filterGroup.childCount) {
            val view = filterGroup.getChildAt(i)
            if (view is RadioButton) {
                val chip = view
                val checkedDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 19f * density
                    setColor(darkenedColor)
                }
                
                val uncheckedDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 19f * density
                    setColor(Color.TRANSPARENT)
                    setStroke(Math.round(1.5f * density), workoutColor)
                }

                val stateListDrawable = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                    addState(intArrayOf(), uncheckedDrawable)
                }
                
                chip.background = stateListDrawable
            }
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
