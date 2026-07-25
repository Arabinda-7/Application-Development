package com.example.allinone

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class NotesNavigationSection(
    private val context: Context,
    private val footerView: LinearLayout,
    private val views: Map<String, View>,
    private val icons: Map<String, ImageView>,
    private val labels: Map<String, TextView>,
    private val onCategorySwitched: (String) -> Unit
) {
    fun setup(currentCategory: String) {
        footerView.removeAllViews()

        DataManager.noteVisibleSections.forEach { category ->
            val viewToAdd = views[category]
            viewToAdd?.let {
                if (it.parent != null) (it.parent as ViewGroup).removeView(it)
                footerView.addView(it)
            }
        }

        views.forEach { (cat, view) ->
            view.visibility = if (DataManager.noteVisibleSections.contains(cat)) View.VISIBLE else View.GONE
        }

        if (DataManager.noteVisibleSections.size > 1) {
            footerView.visibility = View.VISIBLE
        } else {
            footerView.visibility = View.GONE
        }

        views.forEach { (cat, view) ->
            view.setOnClickListener { switchCategory(cat, currentCategory) }
        }
        
        updateNavUI(currentCategory)
    }

    private fun switchCategory(category: String, currentCategory: String) {
        if (category == currentCategory && DataManager.noteVisibleSections.size <= 1) return

        val root = footerView.parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(root, AutoTransition())
        
        val sections = DataManager.noteVisibleSections.toMutableList()
        
        if (category == currentCategory) {
            val originalOrder = listOf("Notes", "Questions", "Daily", "Stories")
            val resetOrder = originalOrder.filter { sections.contains(it) }
            DataManager.noteVisibleSections.clear()
            DataManager.noteVisibleSections.addAll(resetOrder)
        } else {
            if (sections.contains(category)) {
                sections.remove(category)
                sections.add(0, category)
                DataManager.noteVisibleSections.clear()
                DataManager.noteVisibleSections.addAll(sections)
            }
        }
        
        onCategorySwitched(category)
    }

    fun updateNavUI(currentCategory: String) {
        val activeColor = ContextCompat.getColor(context, R.color.chip_selected)
        val inactiveColor = ContextCompat.getColor(context, R.color.text_secondary)

        icons.forEach { (cat, icon) ->
            val isActive = cat == currentCategory
            val color = if (isActive) activeColor else inactiveColor
            icon.setColorFilter(color)
            labels[cat]?.setTextColor(color)
        }
    }
}
