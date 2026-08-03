package com.example.allinone.feature.habit.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.R
import com.example.allinone.data.model.Habit
import com.example.allinone.feature.habit.callbacks.HabitCallbacks
import com.example.allinone.feature.habit.utils.HabitFormatter

/**
 * HabitViewHolder: Handles layout inflation and view bindings for Habit items.
 */
class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val habitName: TextView = itemView.findViewById(R.id.habit_name)
    val habitCompleted: CheckBox = itemView.findViewById(R.id.habit_completed)
    val habitIcon: ImageView = itemView.findViewById(R.id.habit_icon)

    fun bind(habit: Habit, isCompletedOnDate: Boolean, callbacks: HabitCallbacks?) {
        habitName.text = HabitFormatter.formatTitle(habit.name)
        habitCompleted.isChecked = isCompletedOnDate

        habitCompleted.setOnCheckedChangeListener { _, isChecked ->
            callbacks?.onHabitCheckedChanged(habit, isChecked)
        }

        itemView.setOnClickListener {
            callbacks?.onHabitClicked(habit)
        }
    }
}

/**
 * HabitHeaderViewHolder: Section headers for expanded/collapsed habit categories.
 */
class HabitHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.tv_header_title)
    val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)

    fun bind(headerText: String, isExpanded: Boolean, callbacks: HabitCallbacks?) {
        title.text = headerText
        chevron.rotation = if (isExpanded) 180f else 0f
        itemView.setOnClickListener {
            callbacks?.onHeaderClicked(headerText)
        }
    }
}
