package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import com.example.allinone.domain.usecase.note.AddNoteUseCase
import com.example.allinone.domain.usecase.note.DeleteNoteUseCase
import com.example.allinone.domain.usecase.note.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val repository: NoteRepository
) : ViewModel() {

    val settings: StateFlow<NoteSettings> = repository.getNoteSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteSettings())

    suspend fun getNoteById(timestamp: Long): Note? {
        return repository.getAllNotes().first().find { it.timestamp == timestamp }
    }

    fun saveNote(note: Note, isUpdate: Boolean) {
        viewModelScope.launch {
            if (isUpdate) {
                updateNoteUseCase(note)
            } else {
                addNoteUseCase(note)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
        }
    }
}
