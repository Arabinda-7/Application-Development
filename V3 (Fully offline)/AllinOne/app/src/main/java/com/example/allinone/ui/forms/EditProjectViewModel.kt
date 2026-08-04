package com.example.allinone.ui.forms

import androidx.lifecycle.ViewModel
import com.example.allinone.DataManager
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * EditProjectViewModel: Manages state, validation, and persistence for editing project details,
 * roadmap features, and progress state.
 */
@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned

    private val _selectedColor = MutableStateFlow(-1)
    val selectedColor: StateFlow<Int> = _selectedColor

    private val _deadline = MutableStateFlow<Long?>(null)
    val deadline: StateFlow<Long?> = _deadline

    var projectId: Long = -1
        private set

    fun initialize(id: Long) {
        projectId = id
        if (projectId != -1L) {
            val project = synchronized(DataManager.projects) {
                DataManager.projects.find { it.timestamp == projectId }
            }
            project?.let {
                _title.value = it.title
                _content.value = it.content
                _progress.value = it.progress
                _isPinned.value = it.isPinned
                _selectedColor.value = it.color
                _deadline.value = it.deadline
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun updateProgress(newProgress: Int) {
        _progress.value = newProgress.coerceIn(0, 100)
    }

    fun togglePinned() {
        _isPinned.value = !_isPinned.value
    }

    fun updateColor(color: Int) {
        _selectedColor.value = color
    }

    fun setDeadline(time: Long?) {
        _deadline.value = time
    }

    fun isFormValid(): Boolean = _title.value.isNotBlank()
}
