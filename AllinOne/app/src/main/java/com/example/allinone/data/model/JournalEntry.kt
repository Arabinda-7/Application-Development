package com.example.allinone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    var text: String,
    val timestamp: Long = System.currentTimeMillis()
)