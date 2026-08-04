package com.example.allinone

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class ProjectNavigationSection(
    private val footerView: View,
    private val navProjects: View,
    private val navNotes: View,
    private val ivProjects: ImageView,
    private val tvProjects: TextView,
    private val ivNotes: ImageView,
    private val tvNotes: TextView,
    private val onTabSwitched: (Boolean) -> Unit
) {
    fun setup(isProjects: Boolean) {
        val showFooter = DataManager.projectRoadmapsEnabled && DataManager.projectIdeasEnabled
        footerView.visibility = if (showFooter) View.VISIBLE else View.GONE
        navProjects.visibility = if (DataManager.projectRoadmapsEnabled) View.VISIBLE else View.GONE
        navNotes.visibility = if (DataManager.projectIdeasEnabled) View.VISIBLE else View.GONE

        navProjects.setOnClickListener { switchTab(true) }
        navNotes.setOnClickListener { switchTab(false) }
        
        updateNavUI(isProjects)
    }

    private fun switchTab(isProjects: Boolean) {
        onTabSwitched(isProjects)
    }

    fun updateNavUI(isProjects: Boolean) {
        val activeColor = Color.WHITE
        val inactiveColor = Color.parseColor("#80FFFFFF")
        val activeBg = Color.parseColor("#33FFFFFF")
        val inactiveBg = Color.TRANSPARENT

        if (isProjects) {
            ivProjects.imageTintList = ColorStateList.valueOf(activeColor)
            ivProjects.backgroundTintList = ColorStateList.valueOf(activeBg)
            tvProjects.setTextColor(activeColor)
            tvProjects.setTypeface(null, Typeface.BOLD)

            ivNotes.imageTintList = ColorStateList.valueOf(inactiveColor)
            ivNotes.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            tvNotes.setTextColor(inactiveColor)
            tvNotes.setTypeface(null, Typeface.NORMAL)
        } else {
            ivProjects.imageTintList = ColorStateList.valueOf(inactiveColor)
            ivProjects.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            tvProjects.setTextColor(inactiveColor)
            tvProjects.setTypeface(null, Typeface.NORMAL)

            ivNotes.imageTintList = ColorStateList.valueOf(activeColor)
            ivNotes.backgroundTintList = ColorStateList.valueOf(activeBg)
            tvNotes.setTextColor(activeColor)
            tvNotes.setTypeface(null, Typeface.BOLD)
        }
    }
}
