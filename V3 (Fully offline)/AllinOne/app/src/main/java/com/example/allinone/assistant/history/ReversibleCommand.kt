package com.example.allinone.assistant.history

interface ReversibleCommand {
    val id: String
    val description: String
    val timestamp: Long
    suspend fun execute(): Boolean
    suspend fun undo(): Boolean
}
