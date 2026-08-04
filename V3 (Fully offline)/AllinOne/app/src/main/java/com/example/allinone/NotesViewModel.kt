package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import com.example.allinone.domain.usecase.note.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val autoCleanupNotesUseCase: AutoCleanupNotesUseCase,
    private val repository: NoteRepository
) : ViewModel() {

    private val _currentCategory = MutableStateFlow("Notes")
    val currentCategory = _currentCategory.asStateFlow()

    var isDeleteMode: Boolean = false

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val settings: StateFlow<NoteSettings> = repository.getNoteSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteSettings())

    val notes: StateFlow<List<Note>> = combine(
        _currentCategory,
        _searchQuery
    ) { category, query ->
        category to query
    }.flatMapLatest { (category, query) ->
        if (query.isBlank()) {
            getNotesUseCase().map { list -> list.filter { it.category == category } }
        } else {
            searchNotesUseCase(query).map { list -> list.filter { !it.isGlobalProject } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(category: String) {
        _currentCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
        }
    }

    fun applyAutoCleanup() {
        viewModelScope.launch {
            autoCleanupNotesUseCase()
        }
    }
}
