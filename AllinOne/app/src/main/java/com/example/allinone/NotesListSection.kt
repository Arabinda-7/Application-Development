package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.data.model.Note

class NotesListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onDataChanged: () -> Unit
) {
    val noteAdapter: NoteAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        noteAdapter = NoteAdapter(mutableListOf()) {
            // Callback for when a note is edited/changed within the adapter
            onDataChanged()
        }
        recyclerView.adapter = noteAdapter
    }

    fun updateDisplayList(notes: List<Note>) {
        noteAdapter.updateNotes(notes)
    }

    fun setDeleteMode(enabled: Boolean) {
        noteAdapter.setDeleteMode(enabled)
    }

    fun deleteSelectedNotes() {
        noteAdapter.deleteSelectedNotes(context)
    }
}
