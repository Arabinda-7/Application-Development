package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.util.*

class TaskAdapter(
    private val allTasks: MutableList<Task>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TASK = 0
        private const val TYPE_HEADER = 1
    }

    private var isCompletedExpanded = true
    private var isDeleteMode = false
    private var showCompleted = true
    private val displayItems = mutableListOf<Any>()
    
    private var currentCategory = "All"
    private var currentSearchQuery = ""
    private var currentSortOrder = "Priority"
    private var currentSection = DataManager.taskDefaultSection

    init {
        updateDisplayList()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) TYPE_HEADER else TYPE_TASK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val headerText = displayItems[position] as String
            holder.title.text = headerText
            holder.chevron.rotation = if (isCompletedExpanded) 0f else 180f
            holder.itemView.setOnClickListener {
                isCompletedExpanded = !isCompletedExpanded
                updateDisplayList()
            }
        } else if (holder is TaskViewHolder) {
            val task = displayItems[position] as Task
            val context = holder.itemView.context
            
            holder.taskName.text = task.name
            holder.taskCompleted.isChecked = task.isCompleted
            
            // Selection for delete mode
            holder.selectionCheckbox.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            holder.selectionCheckbox.isChecked = task.isSelected
            
            // Priority Indicator
            val priorityColor = when(task.priority) {
                1 -> ContextCompat.getColor(context, R.color.card_orange)
                2 -> Color.parseColor("#FF5252")
                else -> ContextCompat.getColor(context, R.color.primary_blue)
            }
            holder.priorityIndicator.setBackgroundColor(priorityColor)
            
            // Metadata
            holder.tvCategory.text = task.category ?: "General"
            
            val subtasksList = task.subtasks ?: mutableListOf()
            if (subtasksList.isNotEmpty()) {
                val completed = subtasksList.count { it.isCompleted }
                holder.tvSubtasks.text = "$completed/${subtasksList.size} subtasks"
                holder.tvSubtasks.visibility = View.VISIBLE
            } else {
                holder.tvSubtasks.visibility = View.GONE
            }
            
            holder.ivReminder.visibility = if (task.reminderTime != null) View.VISIBLE else View.GONE
            
            updateVisuals(holder, task.isCompleted)

            holder.taskCompleted.setOnClickListener {
                if (holder.taskCompleted.isChecked) {
                    task.isCompleted = true
                    task.completedTimestamp = System.currentTimeMillis()
                    updateDisplayList()
                    DataManager.saveData(context)
                    onProgressChanged()
                } else {
                    holder.taskCompleted.isChecked = true // Prevent simple uncheck
                }
            }

            holder.itemView.setOnClickListener {
                if (isDeleteMode) {
                    task.isSelected = !task.isSelected
                    notifyItemChanged(position)
                } else {
                    (context as? ToDoListActivity)?.showAddTaskDialog(task)
                }
            }

            holder.itemView.setOnLongClickListener {
                showCustomMenu(it, task)
                true
            }
        }
    }

    private fun showCustomMenu(anchor: View, task: Task) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        
        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? ToDoListActivity)?.showAddTaskDialog(task)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allTasks.remove(task)
            updateDisplayList()
            onProgressChanged()
            DataManager.saveData(context)
            popupWindow.dismiss()
        }

        val undoView = menuView.findViewById<View>(R.id.menu_undo)
        if (task.isCompleted) {
            undoView.visibility = View.VISIBLE
            undoView.setOnClickListener {
                task.isCompleted = false
                updateDisplayList()
                onProgressChanged()
                DataManager.saveData(context)
                popupWindow.dismiss()
            }
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    fun filter(category: String, query: String) {
        currentCategory = category
        currentSearchQuery = query
        updateDisplayList()
    }

    fun setSection(section: String) {
        currentSection = section
        updateDisplayList()
    }

    fun getTaskAt(position: Int): Task? {
        return if (position in displayItems.indices && displayItems[position] is Task) {
            displayItems[position] as Task
        } else null
    }

    fun updateDisplayList() {
        displayItems.clear()
        
        val filtered = allTasks.filter { task ->
            val matchesCategory = if (currentCategory == "All") true else task.category == currentCategory
            val matchesSearch = task.name.contains(currentSearchQuery, ignoreCase = true)
            val matchesSection = task.section == currentSection
            matchesCategory && matchesSearch && matchesSection
        }

        val activeTasks = when (currentSortOrder) {
            "Newest" -> filtered.filter { !it.isCompleted }.sortedByDescending { it.timestamp }
            "Alphabetical" -> filtered.filter { !it.isCompleted }.sortedBy { it.name.lowercase() }
            else -> filtered.filter { !it.isCompleted }.sortedWith(compareByDescending<Task> { it.priority }.thenByDescending { it.timestamp })
        }

        val completedTasks = if (showCompleted) filtered.filter { it.isCompleted }.sortedByDescending { it.timestamp } else emptyList()

        displayItems.addAll(activeTasks)
        
        if (completedTasks.isNotEmpty()) {
            displayItems.add("Completed ${completedTasks.size}")
            if (isCompletedExpanded) {
                displayItems.addAll(completedTasks)
            }
        }
        notifyDataSetChanged()
    }

    fun setShowCompleted(show: Boolean) {
        showCompleted = show
        updateDisplayList()
    }

    fun setSortOrder(order: String) {
        currentSortOrder = order
        updateDisplayList()
    }

    fun setDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        if (!enabled) {
            allTasks.forEach { it.isSelected = false }
        }
        notifyDataSetChanged()
    }

    fun deleteSelectedTasks(context: Context) {
        allTasks.removeAll { it.isSelected }
        setDeleteMode(false)
        updateDisplayList()
        onProgressChanged()
        DataManager.saveData(context)
    }

    private fun updateVisuals(holder: TaskViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.taskName.paintFlags = holder.taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskCard.alpha = 0.6f
        } else {
            holder.taskName.paintFlags = holder.taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskCard.alpha = 1.0f
        }
    }

    override fun getItemCount() = displayItems.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskCompleted: CheckBox = itemView.findViewById(R.id.task_completed)
        val taskCard: MaterialCardView = itemView.findViewById(R.id.task_card)
        val selectionCheckbox: CheckBox = itemView.findViewById(R.id.task_selection_checkbox)
        val priorityIndicator: View = itemView.findViewById(R.id.priority_indicator)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_task_category)
        val tvSubtasks: TextView = itemView.findViewById(R.id.tv_subtask_progress)
        val ivReminder: ImageView = itemView.findViewById(R.id.iv_reminder_icon)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_header_title)
        val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)
    }
}
