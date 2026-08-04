package com.example.allinone.domain.model

data class AgendaItem(
    val id: String = "",
    val parentId: String? = null,
    val title: String,
    val details: String = "",
    val time: Long = 0,
    val category: String = "", // "TASKS", "PROJECTS", "SUBFEATURES", "WORKSPACES"
    val priority: String = "",
    val navigationTarget: String = "",
    val color: Int = -1
)
