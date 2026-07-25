package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProjectViewModel : ViewModel() {
    var isProjectsTab by mutableStateOf(false)
    var isEditMode by mutableStateOf(false)
    var displayNotes = mutableListOf<Note>()
    var displayIdeas = mutableListOf<Note>()
}
