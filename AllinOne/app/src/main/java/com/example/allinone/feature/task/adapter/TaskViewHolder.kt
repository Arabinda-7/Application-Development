package com.example.allinone.feature.task.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.R
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.data.model.Task
import com.example.allinone.feature.task.callbacks.TaskItemCallback
import com.example.allinone.feature.task.utils.TaskFormatter
import com.google.android.material.card.MaterialCardView

/**
 * TaskViewHolder: Handles view references and binding UI logic for task items.
 */
class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val taskCard: MaterialCardView = itemView.findViewById(R.id.task_card)
    val taskName: TextView = itemView.findViewById(R.id.task_name)
    val taskCompleted: CheckBox = itemView.findViewById(R.id.task_completed)
    val selectionCheckbox: CheckBox = itemView.findViewById(R.id.task_selection_checkbox)
    val priorityIndicator: View = itemView.findViewById(R.id.priority_indicator)

    fun bind(task: Task, isDeleteMode: Boolean, callback: TaskItemCallback?) {
        val context = itemView.context
        taskName.text = TaskFormatter.formatTitle(task.name)
        taskCompleted.isChecked = task.isCompleted
        
        selectionCheckbox.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        selectionCheckbox.isChecked = task.isSelected

        val priorityColor = TaskFormatter.getPriorityColor(context, task.priority)
        priorityIndicator.setBackgroundColor(priorityColor)

        taskCard.setCardBackgroundColor(UIUtils.adjustAlpha(priorityColor, 0.1f))
        taskCard.strokeColor = priorityColor
        taskCard.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()

        taskCompleted.setOnCheckedChangeListener { _, isChecked ->
            callback?.onTaskCheckedChanged(task, isChecked)
        }

        itemView.setOnClickListener {
            callback?.onTaskClicked(task)
        }

        itemView.setOnLongClickListener {
            callback?.onTaskLongClicked(task) ?: false
        }
    }
}

/**
 * HeaderViewHolder: Renders expandable section headers in task lists.
 */
class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.tv_header_title)
    val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)

    fun bind(headerText: String, isExpanded: Boolean, callback: TaskItemCallback?) {
        title.text = headerText
        chevron.rotation = if (isExpanded) 0f else 180f
        itemView.setOnClickListener {
            callback?.onHeaderClicked()
        }
    }
}
