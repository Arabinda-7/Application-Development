package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.repository.HabitSettings
import com.example.allinone.domain.usecase.habit.GetHabitProgressUseCase
import com.example.allinone.domain.usecase.habit.GetHabitStatisticsUseCase
import com.example.allinone.domain.usecase.habit.TrackHabitCompletionUseCase
import com.example.allinone.domain.usecase.user.AddActivityUseCase
import com.example.allinone.domain.usecase.user.AddXPUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HabitTrackerViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val trackHabitCompletionUseCase: TrackHabitCompletionUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val getHabitProgressUseCase: GetHabitProgressUseCase,
    private val addXPUseCase: AddXPUseCase,
    private val addActivityUseCase: AddActivityUseCase
) : ViewModel() {

    private val _selectedDateString = MutableStateFlow(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    val selectedDateString: StateFlow<String> = _selectedDateString.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("All")
    val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter.asStateFlow()

    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitSettings: StateFlow<HabitSettings> = habitRepository.getHabitSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitSettings())

    val dailyProgress: StateFlow<Int> = habits.map { 
        getHabitProgressUseCase(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var currentTab: String = "TODAY"
    var currentlySelectedHistoryDate: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    var currentGridCalendar: Calendar = Calendar.getInstance()

    fun selectDate(date: String) {
        _selectedDateString.value = date
    }

    fun setTimeFilter(filter: String) {
        _selectedTimeFilter.value = filter
    }

    fun toggleHabitCompletion(habit: Habit, progress: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            trackHabitCompletionUseCase(habit, progress, isCompleted, _selectedDateString.value)
        }
    }

    fun updateShowCompleted(show: Boolean) {
        viewModelScope.launch {
            val current = habitSettings.value
            habitRepository.updateSettings(current.copy(showCompleted = show))
        }
    }

    fun updateSettings(newSettings: HabitSettings) {
        viewModelScope.launch {
            habitRepository.updateSettings(newSettings)
        }
    }

    fun addXP(amount: Int) {
        viewModelScope.launch {
            addXPUseCase(amount)
        }
    }

    fun updateActivity(activity: String) {
        viewModelScope.launch {
            addActivityUseCase(activity)
        }
    }

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    fun getDayIndex(dateString: String): Int {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return try {
            val date = sdf.parse(dateString) ?: Date()
            calendar.time = date
            calendar.get(Calendar.DAY_OF_WEEK) - 1
        } catch (e: Exception) {
            0
        }
    }

    fun getHeatmapData(habitName: String, calendar: Calendar): Map<Int, Int> {
        val habit = habits.value.find { it.name == habitName } ?: return emptyMap()
        return getHabitStatisticsUseCase.getHeatmap(habit, calendar)
    }

    fun getStreaks(habitName: String): Pair<Int, Int> {
        val habit = habits.value.find { it.name == habitName } ?: return 0 to 0
        return getHabitStatisticsUseCase.getStreaks(habit)
    }
}
