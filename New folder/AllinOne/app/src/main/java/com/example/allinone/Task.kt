package com.example.allinone

data class Task(
    var name: String,
    var isCompleted: Boolean = false,
    var color: Int = -1,
    val timestamp: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false,
    var priority: Int = 0, // 0=Low, 1=Medium, 2=High
    var reminderTime: Long? = null,
    var category: String = "General",
    var section: String = "Tasks", // "Tasks" or "To-Do List"
    var isHidden: Boolean = false,
    val subtasks: MutableList<Subtask> = mutableListOf(),
    var completedTimestamp: Long? = null
)
