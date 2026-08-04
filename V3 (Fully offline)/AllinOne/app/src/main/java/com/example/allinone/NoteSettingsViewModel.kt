package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteSettingsViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    val settings: StateFlow<NoteSettings> = repository.getNoteSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteSettings())

    fun updateSettings(newSettings: NoteSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun bulkMoveNotes(targetCategory: String) {
        viewModelScope.launch {
            val allNotes = repository.getAllNotes().first()
            allNotes.forEach { 
                repository.insertNote(it.copy(category = targetCategory))
            }
        }
    }
}
