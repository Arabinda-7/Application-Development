package com.example.allinone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Subtask(
    var name: String,
    var isCompleted: Boolean = false
)
