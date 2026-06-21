package com.example.allinone

data class Project(
    var name: String,
    var description: String = "",
    var status: String = "Working", // "Working" or "Upcoming"
    var color: Int = -1,
    var iconResId: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)
