package com.example.allinone.data

import com.example.allinone.Task
import com.example.allinone.R

object TaskDataManager {
    var tasks: MutableList<Task> = java.util.Collections.synchronizedList(mutableListOf<Task>())
    
    var taskShowCompleted: Boolean = true
    var taskShowHidden: Boolean = false
    var taskSortOrder: String = "Priority"
    var taskCustomCategories = java.util.Collections.synchronizedList(mutableListOf("General", "Personal", "Work", "Shopping"))
    var taskAutoArchive: Boolean = false
    var taskDefaultSection: String = "Tasks"
    var taskVisibleSections = java.util.Collections.synchronizedList(mutableListOf("Tasks"))
    var taskEditModeEnabled: Boolean = false
    
    var globalTaskColor: Int = -1
    var taskAddThemeColor: Int = -1
    var globalTaskIcon: Int = R.drawable.ic_task
}
