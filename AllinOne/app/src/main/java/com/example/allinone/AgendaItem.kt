package com.example.allinone

data class AgendaItem(
    val title: String,
    val details: String = "",
    val path: String = "",
    val category: String = "",
    val navigationTarget: String = "" // e.g., "TASK_ACTIVITY", "PROJECT_ACTIVITY", "WORKSPACE"
)
