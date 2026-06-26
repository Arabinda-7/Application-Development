package com.example.allinone

data class ProjectFeature(
    var name: String,
    var isCompleted: Boolean = false,
    var details: String = "",
    var position: Int = 0,
    var resourceUrl: String = "",
    var resourcePath: String = "",
    var blockedByNodeId: String = "",
    val subFeatures: MutableList<ProjectFeature> = mutableListOf(),
    val id: String = java.util.UUID.randomUUID().toString(),
    var isExpanded: Boolean = false
)
