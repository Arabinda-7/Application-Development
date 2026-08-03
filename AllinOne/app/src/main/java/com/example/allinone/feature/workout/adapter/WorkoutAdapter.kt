package com.example.allinone.feature.workout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.R
import com.example.allinone.data.model.Workout

/**
 * WorkoutAdapter (Refactored Architecture): High-performance, clean adapter managing 
 * list updates and delegating item view binding to WorkoutViewHolder.
 */
class WorkoutAdapter(
    private var allWorkouts: List<Workout>,
    private val callback: WorkoutCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }

    private var displayItems = mutableListOf<Any>()

    init {
        updateList(allWorkouts)
    }

    fun updateList(newList: List<Workout>) {
        allWorkouts = newList
        displayItems.clear()
        displayItems.addAll(allWorkouts)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_header_habit, parent, false)
            WorkoutHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_workout_anytime, parent, false)
            WorkoutViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkoutHeaderViewHolder -> {
                val headerText = displayItems[position] as String
                holder.bind(headerText, isExpanded = true, callback = callback)
            }
            is WorkoutViewHolder -> {
                val workout = displayItems[position] as Workout
                holder.bind(workout, callback = callback)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size
}
