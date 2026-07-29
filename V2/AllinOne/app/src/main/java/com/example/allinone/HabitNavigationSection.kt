package com.example.allinone

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class HabitNavigationSection(
    private val context: Context,
    private val root: ViewGroup,
    private val todayLayout: View,
    private val historyLayout: View,
    private val composeView: View,
    private val onTabSwitched: (String) -> Unit
) {
    fun setup() {
        // Footer navigation removed
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
        // Navigation UI removed
    }
}
