package com.example.allinone

data class ProjectFeature(
    var name: String,
    var isCompleted: Boolean = false,
    var details: String = "",
    var position: Int = 0
)
