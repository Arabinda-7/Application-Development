package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup

class TaskFilterSection(
    private val context: Context,
    private val radioGroup: RadioGroup,
    private val onFilterChanged: (String) -> Unit
) {
    fun setup(currentFilter: String) {
        radioGroup.removeAllViews()

        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        val allCategories = mutableListOf("All")
        allCategories.addAll(DataManager.taskCustomCategories)

        allCategories.forEachIndexed { index, category ->
            val rb = RadioButton(context).apply {
                id = index + 1000 
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38f, context.resources.displayMetrics).toInt()
                val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics).toInt()
                params.setMargins(margin, 0, margin, 0)
                layoutParams = params
                
                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                setPadding(padding, 0, padding, 0)
                
                val darkenedColor = UIUtils.darkenColor(taskColor, 0.5f)

                val checkedDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 19f * context.resources.displayMetrics.density
                    setColor(darkenedColor)
                }
                val uncheckedDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 19f * context.resources.displayMetrics.density
                    setColor(Color.TRANSPARENT)
                    setStroke(Math.round(1.5f * context.resources.displayMetrics.density), taskColor)
                }
                val stateListDrawable = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                    addState(intArrayOf(), uncheckedDrawable)
                }
                background = stateListDrawable

                buttonDrawable = null
                gravity = Gravity.CENTER
                text = category.uppercase()
                setTextColor(Color.WHITE)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                
                isChecked = currentFilter == category
            }
            radioGroup.addView(rb)
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRb = group.findViewById<RadioButton>(checkedId)
            if (checkedRb != null) {
                onFilterChanged(allCategories[checkedId - 1000])
            }
        }
    }
}
