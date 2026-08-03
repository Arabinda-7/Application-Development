package com.example.allinone.feature.habit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.R
import com.example.allinone.data.model.Habit
import com.example.allinone.feature.habit.callbacks.HabitCallbacks

/**
 * HabitAdapter (Refactored Architecture): Lightweight adapter responsible exclusively 
 * for list management and delegating item rendering to HabitViewHolder.
 */
class HabitAdapter(
    private var allHabits: List<Habit>,
    private val callbacks: HabitCallbacks? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }

    private var displayItems = mutableListOf<Any>()

    init {
        updateList(allHabits)
    }

    fun updateList(newList: List<Habit>) {
        allHabits = newList
        displayItems.clear()
        displayItems.addAll(allHabits)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_header_habit, parent, false)
            HabitHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_habit_anytime, parent, false)
            HabitViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HabitHeaderViewHolder -> {
                val headerText = displayItems[position] as String
                holder.bind(headerText, isExpanded = true, callbacks = callbacks)
            }
            is HabitViewHolder -> {
                val habit = displayItems[position] as Habit
                holder.bind(habit, isCompletedOnDate = habit.isCompleted, callbacks = callbacks)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size
}
