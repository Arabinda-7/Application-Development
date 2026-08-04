package com.example.allinone.feature.task.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.DataManager
import com.example.allinone.R
import com.example.allinone.data.model.Task
import com.example.allinone.feature.task.callbacks.TaskItemCallback

/**
 * TaskAdapter (Modular Architecture): Manages list rendering and delegates 
 * item UI binding to TaskViewHolder and click events to TaskItemCallback.
 */
class TaskAdapter(
    private val allTasks: MutableList<Task>,
    private val callback: TaskItemCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }

    private var isCompletedExpanded = true
    private var isDeleteMode = false
    private val displayItems = mutableListOf<Any>()

    init {
        updateDisplayList()
    }

    fun updateDisplayList() {
        displayItems.clear()
        displayItems.addAll(allTasks)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_header_task, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_task_tasks, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerText = displayItems[position] as String
                holder.bind(headerText, isCompletedExpanded, callback)
            }
            is TaskViewHolder -> {
                val task = displayItems[position] as Task
                holder.bind(task, isDeleteMode, callback)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size
}
