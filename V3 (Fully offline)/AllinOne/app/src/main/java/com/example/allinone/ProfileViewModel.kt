package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserSettings
import com.example.allinone.domain.usecase.user.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = getUserProfileUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val userSettings: StateFlow<UserSettings> = getUserSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun updateProfile(newProfile: UserProfile) {
        viewModelScope.launch {
            updateUserProfileUseCase(newProfile)
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            updateUserSettingsUseCase(newSettings)
        }
    }
}
