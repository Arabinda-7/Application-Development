package com.example.allinone

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class HabitNavigationSection(
    private val context: Context,
    private val root: ViewGroup,
    private val todayLayout: View,
    private val historyLayout: View,
    private val composeView: View,
    private val ivToday: ImageView,
    private val tvTodayNav: TextView,
    private val ivHistory: ImageView,
    private val tvHistoryNav: TextView,
    private val onTabSwitched: (String) -> Unit
) {
    fun setup() {
        root.findViewById<View>(R.id.nav_today).setOnClickListener { switchTab("TODAY") }
        root.findViewById<View>(R.id.nav_history).setOnClickListener { switchTab("HISTORY") }
    }

    fun switchTab(tab: String) {
        TransitionManager.beginDelayedTransition(root, AutoTransition())
        
        if (tab == "TODAY") {
            todayLayout.visibility = View.VISIBLE
            historyLayout.visibility = View.GONE
            composeView.visibility = View.GONE
        } else {
            todayLayout.visibility = View.GONE
            historyLayout.visibility = View.VISIBLE
            composeView.visibility = View.GONE
        }
        updateNavUI(tab)
        onTabSwitched(tab)
    }

    fun updateNavUI(active: String) {
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")
        val todayColor = if (active == "TODAY") habitColor else ContextCompat.getColor(context, R.color.text_secondary)
        val historyColor = if (active == "HISTORY") habitColor else ContextCompat.getColor(context, R.color.text_secondary)
        
        ivToday.setColorFilter(todayColor)
        tvTodayNav.setTextColor(todayColor)
        ivHistory.setColorFilter(historyColor)
        tvHistoryNav.setTextColor(historyColor)
    }
}
