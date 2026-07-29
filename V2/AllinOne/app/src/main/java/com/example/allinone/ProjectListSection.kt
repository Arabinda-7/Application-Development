package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class ProjectListSection(
    private val context: Context,
    private val projectList: RecyclerView,
    private val ideaList: RecyclerView,
    private val onProjectClick: (Note) -> Unit,
    private val onIdeaClick: (Note) -> Unit,
    private val onDataChanged: () -> Unit
) {
    val projectAdapter: ProjectNoteAdapter
    val ideaAdapter: NoteAdapter

    init {
        projectList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        ideaList.layoutManager = LinearLayoutManager(context)

        projectAdapter = ProjectNoteAdapter(mutableListOf()) {
            DataManager.saveData(context)
            onDataChanged()
        }
        projectList.adapter = projectAdapter

        ideaAdapter = NoteAdapter(mutableListOf()) {
            DataManager.saveData(context)
            onDataChanged()
        }
        ideaList.adapter = ideaAdapter
    }

    fun updateDisplayLists() {
        val activeNotes = synchronized(DataManager.projects) {
            DataManager.projects.filter { !it.isArchived }
        }
        
        val roadmapList = activeNotes.filter {
            DataManager.projectDualExistEnabled || it.isDualExist || it.category == "Project" || it.subFeatures.isNotEmpty()
        }
        val visibleRoadmaps = if (DataManager.projectAutoArchive) {
            roadmapList.filter { it.status != "Completed" }
        } else {
            roadmapList
        }
        val displayNotes = visibleRoadmaps.sortedWith(compareByDescending<Note> { it.isPinned }
            .thenBy { it.status == "Completed" }
            .thenByDescending { it.timestamp })

        val ideasList = activeNotes.filter {
            (DataManager.projectDualExistEnabled || it.isDualExist || it.category == "ProjectIdea" || (it.category != "Project" && it.subFeatures.isEmpty()))
            && it.category != "Project"
        }
        val sortedIdeas = ideasList.sortedByDescending { it.timestamp }

        projectAdapter.updateNotes(displayNotes)
        ideaAdapter.updateNotes(sortedIdeas)
    }

    fun setVisibility(isProjects: Boolean) {
        projectList.visibility = if (isProjects) android.view.View.VISIBLE else android.view.View.GONE
        ideaList.visibility = if (isProjects) android.view.View.GONE else android.view.View.VISIBLE
    }
}
