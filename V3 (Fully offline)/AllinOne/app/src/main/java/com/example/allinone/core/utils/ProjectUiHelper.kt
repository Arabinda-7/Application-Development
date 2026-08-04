package com.example.allinone.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.allinone.AddSubFeatureActivity
import com.example.allinone.R
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.ProjectFeature
import java.text.SimpleDateFormat
import java.util.*

object ProjectUiHelper {

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    fun refreshGoalsUI(
        activity: Activity,
        container: LinearLayout,
        goals: MutableList<JournalEntry>,
        onUpdate: () -> Unit
    ) {
        container.removeAllViews()
        goals.sortedByDescending { it.timestamp }.forEach { goal ->
            val layout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 4.dpToPx(activity), 0, 4.dpToPx(activity))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            val tv = TextView(activity).apply {
                text = goal.text
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnEdit = ImageView(activity).apply {
                setImageResource(R.drawable.icons8_edit_pencil_100)
                val iconSize = 20.dpToPx(activity)
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                setPadding(2.dpToPx(activity), 2.dpToPx(activity), 2.dpToPx(activity), 2.dpToPx(activity))
                imageTintList = ColorStateList.valueOf(Color.GRAY)
                setOnClickListener { showEditGoalDialog(activity, goal, onUpdate) }
            }
            val btnDel = ImageView(activity).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                val iconSize = 20.dpToPx(activity)
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply { marginStart = 8.dpToPx(activity) }
                imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
                setOnClickListener {
                    android.app.AlertDialog.Builder(activity)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to remove this goal?")
                        .setPositiveButton("DELETE") { _, _ ->
                            goals.remove(goal)
                            onUpdate()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            }
            layout.addView(tv)
            layout.addView(btnEdit)
            layout.addView(btnDel)
            container.addView(layout)
        }
    }

    fun renderGoalsReadOnly(
        activity: Activity,
        container: LinearLayout,
        goals: List<JournalEntry>
    ) {
        container.removeAllViews()
        goals.forEach { goal ->
            val tvGoal = TextView(activity).apply {
                text = "• ${goal.text}"
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(0, 4.dpToPx(activity), 0, 4.dpToPx(activity))
            }
            container.addView(tvGoal)
        }
    }

    private fun showEditGoalDialog(activity: Activity, goal: JournalEntry, onUpdate: () -> Unit) {
        val et = EditText(activity).apply {
            setText(goal.text)
            setPadding(24.dpToPx(activity), 16.dpToPx(activity), 24.dpToPx(activity), 16.dpToPx(activity))
        }
        android.app.AlertDialog.Builder(activity)
            .setTitle("Edit Goal")
            .setView(et)
            .setPositiveButton("UPDATE") { _, _ ->
                val newText = et.text.toString().trim()
                if (newText.isNotEmpty()) {
                    goal.text = newText
                    onUpdate()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    fun createSubfeatureFilterBar(
        activity: Activity,
        currentFilter: String,
        onFilterChanged: (String) -> Unit
    ): View {
        val filterBar = HorizontalScrollView(activity).apply {
            scrollBarSize = 0
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 8.dpToPx(activity))
            }
        }
        val filterContainer = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        val categories = listOf("ALL", "TASKS", "FEATURES", "BUGS", "RESOURCES", "OTHER")

        categories.forEach { cat ->
            val chip = TextView(activity).apply {
                text = cat
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(12.dpToPx(activity), 6.dpToPx(activity), 12.dpToPx(activity), 6.dpToPx(activity))
                val isSelected = currentFilter == cat
                setTextColor(if (isSelected) Color.WHITE else Color.GRAY)
                background = ContextCompat.getDrawable(activity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(if (isSelected) Color.parseColor("#1A73E8") else Color.parseColor("#11FFFFFF"))

                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 8.dpToPx(activity)
                }

                setOnClickListener {
                    onFilterChanged(cat)
                }
            }
            filterContainer.addView(chip)
        }
        filterBar.addView(filterContainer)
        return filterBar
    }

    fun addSectionHeader(
        activity: Activity,
        container: LinearLayout,
        title: String,
        isExpanded: Boolean,
        onClick: () -> Unit
    ) {
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(4.dpToPx(activity), 8.dpToPx(activity), 4.dpToPx(activity), 4.dpToPx(activity))
            setOnClickListener { onClick() }
        }
        val tv = TextView(activity).apply {
            text = title.uppercase()
            setTextColor(Color.GRAY)
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val iv = ImageView(activity).apply {
            setImageResource(if (isExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(16.dpToPx(activity), 16.dpToPx(activity))
        }
        header.addView(tv)
        header.addView(iv)
        container.addView(header)
    }

    fun createSubFeatureItem(
        activity: Activity,
        sub: ProjectFeature,
        onEdit: (ProjectFeature) -> Unit,
        onLongClick: (View, ProjectFeature) -> Unit,
        onToggleExpansion: (ProjectFeature) -> Unit = {}
    ): View {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dpToPx(activity), 0, 4.dpToPx(activity))
            isClickable = true
            isFocusable = true
            // Some activities use glass_card_bg, others don't. We'll use a subtle background if available.
            try {
                background = ContextCompat.getDrawable(activity, R.drawable.glass_card_bg)
                backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            } catch (e: Exception) {}
        }

        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvName = TextView(activity).apply {
            text = "${sub.position}. ${sub.name}"
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            if (sub.isCompleted) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                alpha = 0.5f
            }
        }

        val containerMeta = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        if (sub.tag.isNotEmpty()) {
            val tvTag = TextView(activity).apply {
                text = sub.tag.uppercase()
                setTextColor(Color.parseColor("#1A73E8"))
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(4.dpToPx(activity), 2.dpToPx(activity), 4.dpToPx(activity), 2.dpToPx(activity))
                background = ContextCompat.getDrawable(activity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A73E8")).withAlpha(30)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.marginEnd = 4.dpToPx(activity)
                layoutParams = params
            }
            containerMeta.addView(tvTag)
        }

        val priorityText = when(sub.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" }
        val priorityColor = when(sub.priority) { 2 -> Color.RED; 1 -> Color.parseColor("#FFB800"); else -> Color.parseColor("#2EC4B6") }
        val tvPriority = TextView(activity).apply {
            text = priorityText
            setTextColor(priorityColor)
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4.dpToPx(activity), 2.dpToPx(activity), 4.dpToPx(activity), 2.dpToPx(activity))
            background = ContextCompat.getDrawable(activity, R.drawable.priority_chip_bg)
            backgroundTintList = ColorStateList.valueOf(priorityColor).withAlpha(30)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.marginEnd = 4.dpToPx(activity)
            layoutParams = params
        }
        containerMeta.addView(tvPriority)

        val tvDate = TextView(activity).apply {
            sub.dueDate?.let {
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
                setTextColor(Color.RED)
                textSize = 12f
                setPadding(4.dpToPx(activity), 0, 8.dpToPx(activity), 0)
            } ?: run {
                visibility = View.GONE
            }
        }

        val btnEdit = ImageView(activity).apply {
            setImageResource(R.drawable.icons8_edit_pencil_100)
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            setPadding(2.dpToPx(activity), 2.dpToPx(activity), 2.dpToPx(activity), 2.dpToPx(activity))
            val s = 24.dpToPx(activity)
            layoutParams = LinearLayout.LayoutParams(s, s)
            setOnClickListener { onEdit(sub) }
        }

        val tvNote = TextView(activity).apply {
            text = sub.details
            setTextColor(Color.GRAY)
            textSize = 12f
            setPadding(32.dpToPx(activity), 4.dpToPx(activity), 32.dpToPx(activity), 8.dpToPx(activity))
            visibility = if (sub.isExpanded && sub.details.isNotEmpty()) View.VISIBLE else View.GONE
        }

        header.addView(tvName)
        header.addView(containerMeta)
        header.addView(tvDate)
        header.addView(btnEdit)
        layout.addView(header)
        layout.addView(tvNote)

        layout.setOnClickListener {
            if (sub.details.isNotEmpty()) {
                onToggleExpansion(sub)
            }
        }

        layout.setOnLongClickListener { view ->
            onLongClick(view, sub)
            true
        }

        return layout
    }

    fun handleSubfeatureExpansion(
        sub: ProjectFeature,
        expandedIds: LinkedList<String>,
        allFeatures: List<ProjectFeature>,
        maxExpanded: Int = 2
    ) {
        if (sub.isExpanded) {
            sub.isExpanded = false
            expandedIds.remove(sub.id)
        } else {
            if (expandedIds.size >= maxExpanded) {
                val oldestId = expandedIds.pollFirst()
                allFeatures.find { it.id == oldestId }?.isExpanded = false
            }
            sub.isExpanded = true
            expandedIds.addLast(sub.id)
        }
    }
}
