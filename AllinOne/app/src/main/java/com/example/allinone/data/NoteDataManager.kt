package com.example.allinone.data

import com.example.allinone.Note
import com.example.allinone.R

object NoteDataManager {
    var notes = mutableListOf<Note>()
    
    var noteAutoCleanupDays: Int = 0
    var noteDefaultCategory: String = "Notes"
    var noteShowHidden: Boolean = false
    var noteVisibleSections = mutableListOf("Notes")
    
    var noteTemplates: MutableMap<String, String> = mutableMapOf(
        "Daily" to "1. Today I'm grateful for: \n2. Top goal for today: \n3. How I feel: ",
        "Questions" to "Question: \n\nContext: \n\nGoal: ",
        "Stories" to "Theme: \nCharacters: \n\nPlot: "
    )
    
    var globalNoteColor: Int = -1
    var noteAddThemeColor: Int = -1
    var globalNoteIcon: Int = R.drawable.ic_notes
}
