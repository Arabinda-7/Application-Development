package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotesListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onDataChanged: () -> Unit
) {
    val noteAdapter: NoteAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        noteAdapter = NoteAdapter(mutableListOf()) {
            DataManager.saveData(context)
            onDataChanged()
        }
        recyclerView.adapter = noteAdapter
    }

    fun updateDisplayList(category: String) {
        val filtered = DataManager.notes.filter { 
            it.category == category && (DataManager.noteShowHidden || !it.isHidden) 
        }.sortedByDescending { it.timestamp }
        
        noteAdapter.updateNotes(filtered)
    }

    fun setDeleteMode(enabled: Boolean) {
        noteAdapter.setDeleteMode(enabled)
    }

    fun deleteSelectedNotes() {
        noteAdapter.deleteSelectedNotes(context)
    }
}
