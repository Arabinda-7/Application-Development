package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TaskViewModel : ViewModel() {
    var currentCategoryFilter by mutableStateOf("All")
    var currentSearchQuery by mutableStateOf("")
    var currentSection by mutableStateOf(DataManager.taskDefaultSection)
    var isDeleteMode by mutableStateOf(false)
}
