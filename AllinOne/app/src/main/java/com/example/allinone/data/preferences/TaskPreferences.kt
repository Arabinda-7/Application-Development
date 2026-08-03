package com.example.allinone.data.preferences

import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskPreferences: Manages task filter settings, category lists, sorting preferences,
 * and auto-archiving behavior.
 */
@Singleton
class TaskPreferences @Inject constructor() {
    var taskShowCompleted: Boolean = true
    var taskShowHidden: Boolean = false
    var taskDefaultSection: String = "Tasks"
    var taskSortOrder: String = "Priority"
    var taskAutoArchive: Boolean = false

    val taskVisibleSections: MutableList<String> = Collections.synchronizedList(
        mutableListOf("Tasks", "Habits", "Workouts", "Notes", "Projects", "Finance")
    )
    val taskCustomCategories: MutableList<String> = Collections.synchronizedList(
        mutableListOf("Work", "Personal", "Health", "Study", "Other")
    )
}
