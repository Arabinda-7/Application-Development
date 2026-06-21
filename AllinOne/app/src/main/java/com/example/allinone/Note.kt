package com.example.allinone

data class Note(
    var title: String,
    var content: String,
    var color: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)