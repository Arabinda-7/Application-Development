package com.example.allinone

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class TaskNavigationSection(
    private val footerView: LinearLayout,
    private val navTasks: View,
    private val navTodo: View,
    private val ivTasksIcon: ImageView,
    private val tvTasksLabel: TextView,
    private val ivTodoIcon: ImageView,
    private val tvTodoLabel: TextView,
    private val onSectionSwitched: (String) -> Unit
) {
    fun setup(currentSection: String) {
        footerView.removeAllViews()

        DataManager.taskVisibleSections.forEach { section ->
            val viewToAdd = when (section) {
                "Tasks" -> navTasks
                "List" -> navTodo
                else -> null
            }
            viewToAdd?.let {
                if (it.parent != null) (it.parent as ViewGroup).removeView(it)
                footerView.addView(it)
            }
        }

        navTasks.visibility = if (DataManager.taskVisibleSections.contains("Tasks")) View.VISIBLE else View.GONE
        navTodo.visibility = if (DataManager.taskVisibleSections.contains("List")) View.VISIBLE else View.GONE

        if (DataManager.taskVisibleSections.size > 1) {
            footerView.visibility = View.VISIBLE
        } else {
            footerView.visibility = View.GONE
        }

        navTasks.setOnClickListener { switchSection("Tasks", currentSection) }
        navTasks.setOnLongClickListener {
            TaskAnalyticsHandler.show(footerView.context)
            true
        }
        navTodo.setOnClickListener { switchSection("List", currentSection) }
        
        updateNavUI(currentSection)
    }

    private fun switchSection(section: String, currentSection: String) {
        if (section == currentSection && DataManager.taskVisibleSections.size <= 1) return

        val root = footerView.parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(root, AutoTransition())
        
        val sections = DataManager.taskVisibleSections.toMutableList()

        if (section == currentSection) {
            val originalOrder = listOf("Tasks", "List")
            val resetOrder = originalOrder.filter { sections.contains(it) }
            DataManager.taskVisibleSections.clear()
            DataManager.taskVisibleSections.addAll(resetOrder)
        } else {
            if (sections.contains(section)) {
                sections.remove(section)
                sections.add(0, section)
                DataManager.taskVisibleSections.clear()
                DataManager.taskVisibleSections.addAll(sections)
            }
        }

        onSectionSwitched(section)
    }

    fun updateNavUI(currentSection: String) {
        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else android.graphics.Color.parseColor("#2EC4B6")
        val inactiveColor = ContextCompat.getColor(footerView.context, R.color.text_secondary)

        val navs = mapOf(
            "Tasks" to Pair(ivTasksIcon, tvTasksLabel),
            "List" to Pair(ivTodoIcon, tvTodoLabel)
        )

        navs.forEach { (sec, views) ->
            val isActive = sec == currentSection
            val color = if (isActive) taskColor else inactiveColor
            
            views.first.setColorFilter(color)
            views.second.setTextColor(color)
        }
    }
}
