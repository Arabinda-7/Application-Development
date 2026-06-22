package com.example.allinone

data class Note(
    var title: String,
    var content: String,
    var color: Int = -1,
    var category: String = "Notes", // "Notes", "Stories", "Daily", "Questions"
    val timestamp: Long = System.currentTimeMillis()
)