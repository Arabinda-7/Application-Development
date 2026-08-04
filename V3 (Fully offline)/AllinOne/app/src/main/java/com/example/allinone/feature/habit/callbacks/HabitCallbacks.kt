package com.example.allinone.feature.habit.callbacks

import com.example.allinone.data.model.Habit

/**
 * HabitCallbacks: Listener interface for Habit RecyclerView item user actions.
 */
interface HabitCallbacks {
    fun onHabitClicked(habit: Habit)
    fun onHabitCheckedChanged(habit: Habit, isChecked: Boolean)
    fun onTimerClicked(habit: Habit)
    fun onHeaderClicked(headerText: String)
}
