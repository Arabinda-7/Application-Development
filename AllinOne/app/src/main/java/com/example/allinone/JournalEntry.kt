package com.example.allinone

data class JournalEntry(
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)