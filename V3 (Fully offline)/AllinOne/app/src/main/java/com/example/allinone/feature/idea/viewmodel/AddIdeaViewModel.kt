package com.example.allinone.feature.idea.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.feature.idea.data.IdeaRepository
import com.example.allinone.feature.idea.domain.SaveIdeaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AddIdeaViewModel: Holds UI state and coordinates SaveIdeaUseCase / IdeaRepository for AddIdeaActivity.
 */
@HiltViewModel
class AddIdeaViewModel @Inject constructor(
    private val repository: IdeaRepository,
    private val saveIdeaUseCase: SaveIdeaUseCase
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _priority = MutableStateFlow(0)
    val priority: StateFlow<Int> = _priority.asStateFlow()

    private val _goals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val goals: StateFlow<List<JournalEntry>> = _goals.asStateFlow()

    private val _subFeatures = MutableStateFlow<List<ProjectFeature>>(emptyList())
    val subFeatures: StateFlow<List<ProjectFeature>> = _subFeatures.asStateFlow()

    private val _existingIdea = MutableStateFlow<Note?>(null)
    val existingIdea: StateFlow<Note?> = _existingIdea.asStateFlow()

    var ideaId: Long = -1
        private set

    fun initialize(id: Long) {
        ideaId = id
        if (ideaId != -1L) {
            val idea = repository.getIdeaById(ideaId)
            _existingIdea.value = idea
            idea?.let {
                _title.value = it.title
                _content.value = it.content
                _priority.value = it.priority
                _goals.value = it.ideaGoals.toList()
                _subFeatures.value = it.subFeatures.toList()
            }
        }
    }

    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun setContent(newContent: String) {
        _content.value = newContent
    }

    fun cyclePriority() {
        _priority.value = (_priority.value + 1) % 3
    }

    fun addGoal(text: String) {
        if (text.isBlank()) return
        val goal = JournalEntry(timestamp = System.currentTimeMillis(), text = text.trim())
        _goals.value = _goals.value + goal
    }

    fun saveIdea(onSuccess: (Note) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = saveIdeaUseCase(
                existingId = ideaId,
                title = _title.value,
                content = _content.value,
                priority = _priority.value,
                goals = _goals.value,
                subFeatures = _subFeatures.value
            )
            result.onSuccess(onSuccess).onFailure { onError(it.message ?: "Failed to save idea") }
        }
    }

    fun deleteIdea(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _existingIdea.value?.let { idea ->
                repository.deleteIdea(idea)
                onDeleted()
            }
        }
    }
}
