package com.example.allinone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Task
import com.example.allinone.domain.repository.TaskRepository
import com.example.allinone.domain.repository.TaskSettings
import com.example.allinone.domain.usecase.task.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val clearCompletedTasksUseCase: ClearCompletedTasksUseCase,
    private val repository: TaskRepository
) : ViewModel() {

    private val _currentSection = MutableStateFlow("Tasks")
    val currentSection = _currentSection.asStateFlow()

    private val _currentCategoryFilter = MutableStateFlow("All")
    val currentCategoryFilter = _currentCategoryFilter.asStateFlow()

    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery = _currentSearchQuery.asStateFlow()

    var isDeleteMode = mutableStateOf(false)

    val settings: StateFlow<TaskSettings> = repository.getTaskSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskSettings())

    val tasks: StateFlow<List<Task>> = combine(
        getTasksUseCase(),
        _currentSection,
        _currentCategoryFilter,
        _currentSearchQuery
    ) { tasks, section, category, query ->
        tasks.filter { it.section == section }
            .filter { if (category == "All") true else it.category == category }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSection(section: String) {
        _currentSection.value = section
    }

    fun setCategoryFilter(category: String) {
        _currentCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _currentSearchQuery.value = query
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            addTaskUseCase(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            clearCompletedTasksUseCase()
        }
    }
}
