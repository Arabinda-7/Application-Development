package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.allinone.data.model.Task

class TaskListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onDataChanged: () -> Unit
) {
    val taskAdapter: TaskAdapter = TaskAdapter(DataManager.tasks) {
        DataManager.saveData(context)
        onDataChanged()
    }

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter.setShowCompleted(DataManager.taskShowCompleted)
        taskAdapter.setSortOrder(DataManager.taskSortOrder)
        recyclerView.adapter = taskAdapter
        setupSwipeActions()
    }

    private fun setupSwipeActions() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTaskAt(position) ?: return
                
                if (direction == ItemTouchHelper.RIGHT) {
                    val updatedTask = task.copy(
                        isCompleted = true,
                        completedTimestamp = System.currentTimeMillis()
                    )
                    recyclerView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                        DataManager.updateTask(updatedTask)
                    }
                } else {
                    recyclerView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                        DataManager.deleteTask(task)
                    }
                }
                onDataChanged()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    fun applyFilters(category: String, query: String) {
        taskAdapter.filter(category, query)
    }

    fun setSection(section: String) {
        taskAdapter.setSection(section)
    }

    fun setDeleteMode(enabled: Boolean) {
        taskAdapter.setDeleteMode(enabled)
    }

    fun deleteSelectedTasks() {
        taskAdapter.deleteSelectedTasks(context)
    }
}
