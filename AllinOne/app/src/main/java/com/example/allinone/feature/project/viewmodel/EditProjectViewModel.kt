package com.example.allinone.feature.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.feature.project.data.ProjectRepository
import com.example.allinone.feature.project.domain.SaveProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * EditProjectViewModel: Manages project form state, progress counters, subfeatures, and persistence logic.
 */
@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val saveProjectUseCase: SaveProjectUseCase
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _selectedColor = MutableStateFlow(-1)
    val selectedColor: StateFlow<Int> = _selectedColor.asStateFlow()

    private val _deadline = MutableStateFlow<Long?>(null)
    val deadline: StateFlow<Long?> = _deadline.asStateFlow()

    private val _goals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val goals: StateFlow<List<JournalEntry>> = _goals.asStateFlow()

    private val _subFeatures = MutableStateFlow<List<ProjectFeature>>(emptyList())
    val subFeatures: StateFlow<List<ProjectFeature>> = _subFeatures.asStateFlow()

    private val _existingProject = MutableStateFlow<Note?>(null)
    val existingProject: StateFlow<Note?> = _existingProject.asStateFlow()

    var projectId: Long = -1L
        private set

    fun initialize(id: Long) {
        projectId = id
        if (projectId != -1L) {
            val proj = repository.getProjectById(projectId)
            _existingProject.value = proj
            proj?.let {
                _title.value = it.title
                _content.value = it.content
                _progress.value = it.progress
                _isPinned.value = it.isPinned
                _selectedColor.value = it.color
                _deadline.value = it.deadline
                _goals.value = it.ideaGoals.toList()
                _subFeatures.value = it.subFeatures.toList()
            }
        }
    }

    fun updateTitle(newTitle: String) { _title.value = newTitle }
    fun updateContent(newContent: String) { _content.value = newContent }
    fun updateProgress(newProgress: Int) { _progress.value = newProgress.coerceIn(0, 100) }
    fun togglePinned() { _isPinned.value = !_isPinned.value }
    fun updateColor(color: Int) { _selectedColor.value = color }
    fun setDeadline(time: Long?) { _deadline.value = time }

    fun saveProject(onSuccess: (Note) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = saveProjectUseCase(
                existingId = projectId,
                title = _title.value,
                content = _content.value,
                progress = _progress.value,
                isPinned = _isPinned.value,
                color = _selectedColor.value,
                deadline = _deadline.value,
                goals = _goals.value,
                subFeatures = _subFeatures.value
            )
            result.onSuccess(onSuccess).onFailure { onError(it.message ?: "Failed to save project") }
        }
    }

    fun deleteProject(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _existingProject.value?.let { project ->
                repository.deleteProject(project)
                onDeleted()
            }
        }
    }
}
