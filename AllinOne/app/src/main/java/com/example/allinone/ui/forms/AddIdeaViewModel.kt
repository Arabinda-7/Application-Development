package com.example.allinone.ui.forms

import androidx.lifecycle.ViewModel
import com.example.allinone.DataManager
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * AddIdeaViewModel: Encapsulates state management, form validation, and 
 * business logic for creating/editing ideas & roadmap concepts.
 */
@HiltViewModel
class AddIdeaViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _priority = MutableStateFlow(0)
    val priority: StateFlow<Int> = _priority

    private val _goals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val goals: StateFlow<List<JournalEntry>> = _goals

    private val _subFeatures = MutableStateFlow<List<ProjectFeature>>(emptyList())
    val subFeatures: StateFlow<List<ProjectFeature>> = _subFeatures

    var ideaId: Long = -1
        private set

    fun initialize(existingIdeaId: Long) {
        ideaId = existingIdeaId
        if (ideaId != -1L) {
            val idea = synchronized(DataManager.projects) {
                DataManager.projects.find { it.timestamp == ideaId }
            }
            idea?.let {
                _title.value = it.title
                _content.value = it.content
                _priority.value = it.priority
                _subFeatures.value = it.subFeatures.toList()
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun cyclePriority() {
        _priority.value = (_priority.value + 1) % 3
    }

    fun addGoal(text: String) {
        if (text.isBlank()) return
        val newGoal = JournalEntry(timestamp = System.currentTimeMillis(), text = text.trim())
        _goals.value = _goals.value + newGoal
    }

    fun removeGoal(goal: JournalEntry) {
        _goals.value = _goals.value - goal
    }

    fun isFormValid(): Boolean = _title.value.isNotBlank()
}
