package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.domain.repository.UserSettings
import com.example.allinone.domain.usecase.user.GetUserSettingsUseCase
import com.example.allinone.domain.usecase.user.UpdateUserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<UserSettings> = getUserSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            updateUserSettingsUseCase(newSettings)
        }
    }
}
