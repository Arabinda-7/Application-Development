package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class NotesViewModel : ViewModel() {
    var currentCategory by mutableStateOf(DataManager.noteDefaultCategory)
    var isDeleteMode by mutableStateOf(false)
    var displayNotes = mutableListOf<Note>()
}
