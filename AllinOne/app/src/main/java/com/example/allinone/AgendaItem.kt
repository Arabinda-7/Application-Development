package com.example.allinone

data class AgendaItem(
    val title: String,
    val details: String = "",
    val path: String = "",
    val category: String = "",
    val priority: String = "",
    val navigationTarget: String = ""
)
