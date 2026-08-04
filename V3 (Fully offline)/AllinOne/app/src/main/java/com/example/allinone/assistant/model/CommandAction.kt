package com.example.allinone.assistant.model

data class CommandAction(
    val type: String?,
    val payload: String? = null,
    var dynamicResponse: String? = null,
    val parameters: Map<String, Any>? = null
)
