package com.example.allinone.feature.task.callbacks

import com.example.allinone.data.model.Task

/**
 * TaskItemCallback: Listener interface for Task item user interactions.
 */
interface TaskItemCallback {
    fun onTaskClicked(task: Task)
    fun onTaskCheckedChanged(task: Task, isChecked: Boolean)
    fun onHeaderClicked()
    fun onTaskLongClicked(task: Task): Boolean
}
