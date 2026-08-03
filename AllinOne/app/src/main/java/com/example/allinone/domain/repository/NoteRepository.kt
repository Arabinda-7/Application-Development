package com.example.allinone.domain.repository

import kotlinx.serialization.Serializable
import com.example.allinone.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun syncAll(notes: List<Note>)
    
    // Settings & Shared State
    fun getNoteSettings(): Flow<NoteSettings>
    suspend fun updateSettings(settings: NoteSettings)
}

@Serializable
data class NoteSettings(
    val autoCleanupDays: Int = 0,
    val defaultCategory: String = "Notes",
    val showHidden: Boolean = false,
    val voiceInputEnabled: Boolean = true,
    val visibleSections: List<String> = listOf("Notes"),
    val noteTemplates: Map<String, String> = mapOf(
        "Daily" to "1. Today I'm grateful for: \n2. Top goal for today: \n3. How I feel: ",
        "Questions" to "Question: \n\nContext: \n\nGoal: ",
        "Stories" to "Theme: \nCharacters: \n\nPlot: "
    ),
    val globalNoteColor: Int = -1,
    val noteAddThemeColor: Int = -1,
    val globalNoteIcon: Int = -1
)
