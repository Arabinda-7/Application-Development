package com.example.allinone

data class ProjectHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // e.g., "Status Changed", "Progress Updated", "Sub-feature Added"
    val description: String // e.g., "Changed status from To-Do to In Progress"
)
