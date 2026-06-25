package com.example.allinone

data class Note(
    var title: String,
    var content: String,
    var color: Int = -1,
    var category: String = "Notes", // "Notes", "Stories", "Daily", "Questions", "Project"
    var isHidden: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    var status: String = "Not Started", // "Not Started", "In Progress", "Completed", "On Hold"
    var progress: Int = 0, // 0-100
    var priority: Int = 1, // 0=Low, 1=Medium, 2=High
    var isPinned: Boolean = false,
    var deadline: Long? = null,
    val subFeatures: MutableList<ProjectFeature> = mutableListOf(),
    val changeHistory: MutableList<ProjectHistory> = mutableListOf()
)