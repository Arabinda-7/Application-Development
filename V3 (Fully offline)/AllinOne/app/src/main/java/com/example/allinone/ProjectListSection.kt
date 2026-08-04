package com.example.allinone

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.data.model.Note

class ProjectListSection(
    private val context: Context,
    private val projectRecyclerView: RecyclerView,
    private val ideaRecyclerView: RecyclerView,
    private val onDataChanged: () -> Unit
) {
    private val projectAdapter: ProjectNoteAdapter
    private val ideaAdapter: NoteAdapter

    init {
        projectRecyclerView.layoutManager = GridLayoutManager(context, 2)
        projectAdapter = ProjectNoteAdapter(mutableListOf()) {
            onDataChanged()
        }
        projectRecyclerView.adapter = projectAdapter

        ideaRecyclerView.layoutManager = LinearLayoutManager(context)
        ideaAdapter = NoteAdapter(mutableListOf()) {
            onDataChanged()
        }
        ideaRecyclerView.adapter = ideaAdapter
    }

    fun updateDisplayLists(allProjects: List<Note>) {
        val projects = allProjects.filter { it.category == "Project" }.sortedByDescending { it.timestamp }
        val ideas = allProjects.filter { it.category == "Idea" }.sortedByDescending { it.timestamp }
        
        projectAdapter.updateNotes(projects)
        ideaAdapter.updateNotes(ideas)
    }

    fun setVisibility(isProjects: Boolean) {
        projectRecyclerView.visibility = if (isProjects) View.VISIBLE else View.GONE
        ideaRecyclerView.visibility = if (isProjects) View.GONE else View.VISIBLE
    }

    fun setEditMode(enabled: Boolean) {
        projectAdapter.setEditMode(enabled)
        ideaAdapter.setEditMode(enabled)
    }
}
