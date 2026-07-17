package com.example.allinone

data class SearchResult(
    val title: String,
    val section: String,
    val iconRes: Int,
    val onClick: () -> Unit
)
