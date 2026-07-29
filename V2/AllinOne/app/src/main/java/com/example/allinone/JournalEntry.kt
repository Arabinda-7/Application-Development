package com.example.allinone

data class JournalEntry(
    var text: String,
    val timestamp: Long = System.currentTimeMillis()
)