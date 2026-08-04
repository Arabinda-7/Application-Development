package com.example.allinone.core.models

data class SearchResult(
    val title: String,
    val section: String,
    val iconRes: Int,
    val onClick: () -> Unit
)
