package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.ProjectSettings
import com.example.allinone.domain.usecase.project.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getProjectSettingsUseCase: com.example.allinone.domain.usecase.project.GetProjectSettingsUseCase,
    private val deleteProjectUseCase: com.example.allinone.domain.usecase.project.DeleteProjectUseCase
) : ViewModel() {

    private val _isProjectsTab = MutableStateFlow(false)
    val isProjectsTab = _isProjectsTab.asStateFlow()

    val projects: StateFlow<List<Note>> = getProjectsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<ProjectSettings> = getProjectSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProjectSettings())

    fun setProjectsTab(isProjects: Boolean) {
        _isProjectsTab.value = isProjects
    }

    fun deleteProject(note: Note) {
        viewModelScope.launch {
            deleteProjectUseCase(note)
        }
    }
}
