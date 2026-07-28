package com.example.allinone

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import java.util.*

class TaskAdapter(
    private val allTasks: MutableList<Task>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val TYPE_HEADER = 1
    }

    private var isCompletedExpanded = true
    private var isDeleteMode = false
    private var showCompleted = true
    private val displayItems = mutableListOf<Any>()
    private val expandedTasks = mutableSetOf<Task>()
    
    private var currentCategory = "All"
    private var currentSearchQuery = ""
    private var currentSortOrder = "Priority"
    private var currentSection = DataManager.taskDefaultSection

    init {
        updateDisplayList()
    }

    override fun getItemViewType(position: Int): Int {
        val item = displayItems[position]
        return if (item is String) {
            TYPE_HEADER
        } else {
            if (currentSection == "Tasks") R.layout.item_task_tasks else R.layout.item_task_list
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_task, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
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
            
            holder.taskName.text = UIUtils.formatTitleCase(task.name)
            holder.taskCompleted.isChecked = task.isCompleted
            
            // Selection for delete mode
            holder.selectionCheckbox.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            holder.selectionCheckbox.isChecked = task.isSelected
            
            // Priority Indicator & Color
            val priorityColor = when(task.priority) {
                1 -> ContextCompat.getColor(context, R.color.card_orange)
                2 -> Color.parseColor("#FF5252")
                else -> ContextCompat.getColor(context, R.color.primary_blue)
            }
            holder.priorityIndicator.setBackgroundColor(priorityColor)
            
            // Dynamic Card Styling - Priority Based
            holder.taskCard.setCardBackgroundColor(UIUtils.adjustAlpha(priorityColor, 0.1f))
            holder.taskCard.strokeColor = priorityColor
            holder.taskCard.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()
            
            val checkTint = if (task.isCompleted) Color.GRAY else priorityColor
            holder.taskCompleted.backgroundTintList = android.content.res.ColorStateList.valueOf(checkTint)
            
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
            
            val displayTime = task.reminderTime ?: task.timestamp
            val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            holder.tvReminderTime.text = sdf.format(java.util.Date(displayTime))
            holder.tvReminderTime.visibility = View.VISIBLE
            holder.tvReminderTime.setTextColor(Color.parseColor("#B3FFFFFF"))

            // Subtask expansion rendering
            if (expandedTasks.contains(task)) {
                holder.subtaskContainer.visibility = View.VISIBLE
                renderSubtasks(holder.subtaskContainer, task, position)
            } else {
                holder.subtaskContainer.visibility = View.GONE
            }

            updateVisuals(holder, task.isCompleted)

            holder.taskCompleted.setOnClickListener {
                val hasPendingSubtasks = task.subtasks.any { !it.isCompleted }
                
                if (hasPendingSubtasks && !task.isCompleted) {
                    holder.taskCompleted.isChecked = false
                    if (!expandedTasks.contains(task)) {
                        expandedTasks.add(task)
                        notifyItemChanged(position)
                        Toast.makeText(context, "Finish all subtasks to complete this task", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Pending subtasks remain", Toast.LENGTH_SHORT).show()
                    }
                } else if (holder.taskCompleted.isChecked) {
                    task.isCompleted = true
                    task.completedTimestamp = System.currentTimeMillis()
                    DataManager.addActivity("Finished Task: ${task.name}")
                    expandedTasks.remove(task)
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
                } else if (DataManager.taskEditModeEnabled) {
                    val intent = Intent(context, AddTaskActivity::class.java).apply {
                        putExtra("TASK_INDEX", allTasks.indexOf(task))
                        putExtra("SECTION", currentSection)
                    }
                    context.startActivity(intent)
                } else {
                    // Toggle expansion only if subtasks exist
                    if (task.subtasks.isNotEmpty()) {
                        if (expandedTasks.contains(task)) {
                            expandedTasks.remove(task)
                        } else {
                            expandedTasks.add(task)
                        }
                        notifyItemChanged(position)
                    }
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
        val menuView = inflater.inflate(R.layout.menu_task_item, null)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        
        val hideUnhideView = menuView.findViewById<View>(R.id.menu_hide_unhide)
        val hideUnhideText = menuView.findViewById<TextView>(R.id.tv_hide_unhide_text)
        val hideUnhideIcon = menuView.findViewById<ImageView>(R.id.iv_hide_unhide_icon)
        
        hideUnhideView.visibility = View.VISIBLE
        if (task.isHidden) {
            hideUnhideText.text = "UNHIDE"
            hideUnhideIcon.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            hideUnhideText.text = "HIDE"
            hideUnhideIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }

        hideUnhideView.setOnClickListener {
            task.isHidden = !task.isHidden
            popupWindow.dismiss()
            updateDisplayList()
            onProgressChanged()
            DataManager.saveData(context)
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(context, AddTaskActivity::class.java).apply {
                putExtra("TASK_INDEX", allTasks.indexOf(task))
                putExtra("SECTION", currentSection)
            }
            context.startActivity(intent)
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
        adapterScope.launch {
            val newList = withContext(Dispatchers.Default) {
                val list = mutableListOf<Any>()
                val filtered = synchronized(allTasks) {
                    allTasks.filter { task ->
                        val matchesCategory = if (currentCategory == "All") true else task.category == currentCategory
                        val matchesSearch = task.name.contains(currentSearchQuery, ignoreCase = true)
                        val matchesSection = task.section == currentSection
                        val isNotHidden = DataManager.taskShowHidden || !task.isHidden
                        matchesCategory && matchesSearch && matchesSection && isNotHidden
                    }
                }

                val activeTasks = when (currentSortOrder) {
                    "Newest" -> filtered.filter { !it.isCompleted }.sortedByDescending { it.timestamp }
                    "Alphabetical" -> filtered.filter { !it.isCompleted }.sortedBy { it.name.lowercase() }
                    else -> filtered.filter { !it.isCompleted }.sortedWith(compareByDescending<Task> { it.priority }.thenByDescending { it.timestamp })
                }

                val completedTasks = if (showCompleted) filtered.filter { it.isCompleted }.sortedByDescending { it.timestamp } else emptyList()

                list.addAll(activeTasks)
                if (completedTasks.isNotEmpty()) {
                    list.add("Completed ${completedTasks.size}")
                    if (isCompletedExpanded) {
                        list.addAll(completedTasks)
                    }
                }
                list
            }

            val diffResult = withContext(Dispatchers.Default) {
                DiffUtil.calculateDiff(TaskDiffCallback(displayItems, newList))
            }
            
            displayItems.clear()
            displayItems.addAll(newList)
            diffResult.dispatchUpdatesTo(this@TaskAdapter)
        }
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

    private fun renderSubtasks(container: LinearLayout, task: Task, parentPosition: Int) {
        container.removeAllViews()
        val context = container.context
        
        val priorityColor = when(task.priority) {
            1 -> ContextCompat.getColor(context, R.color.card_orange)
            2 -> Color.parseColor("#FF5252")
            else -> ContextCompat.getColor(context, R.color.primary_blue)
        }

        task.subtasks.forEach { subtask ->
            val subView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_multiple_choice, container, false)
            val ctView = subView as CheckedTextView
            ctView.text = subtask.name
            ctView.setTextColor(Color.WHITE)
            ctView.textSize = 14f
            ctView.isChecked = subtask.isCompleted
            
            ctView.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(priorityColor))
            ctView.setPadding(0, 8, 0, 8)
            
            ctView.setOnClickListener {
                subtask.isCompleted = !subtask.isCompleted
                ctView.isChecked = subtask.isCompleted
                DataManager.saveData(context)
                notifyItemChanged(parentPosition) // To update the "X/Y subtasks" progress text
            }
            container.addView(subView)
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
        val tvReminderTime: TextView = itemView.findViewById(R.id.tv_reminder_time)
        val subtaskContainer: LinearLayout = itemView.findViewById(R.id.subtask_list_container)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_header_title)
        val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)
    }

    private class TaskDiffCallback(private val oldList: List<Any>, private val newList: List<Any>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return if (oldItem is Task && newItem is Task) {
                oldItem.timestamp == newItem.timestamp
            } else if (oldItem is String && newItem is String) {
                oldItem == newItem
            } else false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }
}
