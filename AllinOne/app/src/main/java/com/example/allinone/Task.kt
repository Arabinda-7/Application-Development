package com.example.allinone

data class Task(
    var name: String,
    var isCompleted: Boolean = false,
    var color: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)