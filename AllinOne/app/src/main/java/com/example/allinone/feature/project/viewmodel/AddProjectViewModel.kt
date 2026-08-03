package com.example.allinone.feature.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.feature.project.domain.SaveProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AddProjectViewModel: Holds UI state and template configuration for project creation.
 */
@HiltViewModel
class AddProjectViewModel @Inject constructor(
    private val saveProjectUseCase: SaveProjectUseCase
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _selectedColor = MutableStateFlow(-1)
    val selectedColor: StateFlow<Int> = _selectedColor.asStateFlow()

    private val _goals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val goals: StateFlow<List<JournalEntry>> = _goals.asStateFlow()

    private val _subFeatures = MutableStateFlow<List<ProjectFeature>>(emptyList())
    val subFeatures: StateFlow<List<ProjectFeature>> = _subFeatures.asStateFlow()

    fun updateTitle(newTitle: String) { _title.value = newTitle }
    fun updateContent(newContent: String) { _content.value = newContent }
    fun updateColor(color: Int) { _selectedColor.value = color }

    fun addGoal(text: String) {
        if (text.isBlank()) return
        val goal = JournalEntry(timestamp = System.currentTimeMillis(), text = text.trim())
        _goals.value = _goals.value + goal
    }

    fun saveProject(onSuccess: (Note) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = saveProjectUseCase(
                existingId = -1L,
                title = _title.value,
                content = _content.value,
                progress = 0,
                isPinned = false,
                color = _selectedColor.value,
                deadline = null,
                goals = _goals.value,
                subFeatures = _subFeatures.value
            )
            result.onSuccess(onSuccess).onFailure { onError(it.message ?: "Failed to create project") }
        }
    }
}
