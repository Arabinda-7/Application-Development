package com.example.allinone.assistant.model

data class AssistantInsight(
    val title: String,
    val description: String,
    val type: String, // "FINANCE", "PRODUCTIVITY", "MINDSET"
    val importance: Int // 0-2 (Low, Med, High)
)
