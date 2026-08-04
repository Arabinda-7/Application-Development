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
import com.example.allinone.domain.repository.NoteSettings

class NotesNavigationSection(
    private val context: Context,
    private val footerView: LinearLayout,
    private val views: Map<String, View>,
    private val icons: Map<String, ImageView>,
    private val labels: Map<String, TextView>,
    private val onCategorySwitched: (String) -> Unit
) {
    fun setup(currentCategory: String, settings: NoteSettings) {
        footerView.removeAllViews()

        settings.visibleSections.forEach { category ->
            val viewToAdd = views[category]
            viewToAdd?.let {
                if (it.parent != null) (it.parent as ViewGroup).removeView(it)
                footerView.addView(it)
            }
        }

        views.forEach { (cat, view) ->
            view.visibility = if (settings.visibleSections.contains(cat)) View.VISIBLE else View.GONE
        }

        if (settings.visibleSections.size > 1) {
            footerView.visibility = View.VISIBLE
        } else {
            footerView.visibility = View.GONE
        }

        views.forEach { (cat, view) ->
            view.setOnClickListener { switchCategory(cat, currentCategory, settings) }
        }
        
        updateNavUI(currentCategory)
    }

    private fun switchCategory(category: String, currentCategory: String, settings: NoteSettings) {
        if (category == currentCategory && settings.visibleSections.size <= 1) return

        val root = footerView.parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(root, AutoTransition())
        
        // Note: Logic for reordering sections is omitted here or should be moved to a UseCase/ViewModel
        // if we want to persist the new order. For now, just switch category.
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
