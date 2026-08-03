package com.example.allinone.feature.task.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.allinone.R
import com.example.allinone.core.utils.UIUtils

/**
 * TaskFormatter: Pure helper functions for formatting task UI properties and priority colors.
 */
object TaskFormatter {

    fun formatTitle(title: String): String {
        return UIUtils.formatTitleCase(title)
    }

    fun getPriorityColor(context: Context, priority: Int): Int {
        return when (priority) {
            1 -> ContextCompat.getColor(context, R.color.card_orange)
            2 -> Color.parseColor("#FF5252")
            else -> ContextCompat.getColor(context, R.color.primary_blue)
        }
    }
}
