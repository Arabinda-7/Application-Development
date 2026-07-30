package com.example.allinone

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProfileViewModel : ViewModel() {
    var userName by mutableStateOf(DataManager.userName)
    var userBio by mutableStateOf(DataManager.userBio)
    var userProfileImageUri by mutableStateOf(DataManager.userProfileImageUri)
    var userAvatarRes by mutableStateOf(DataManager.userAvatarRes)
    var isAppLockEnabled by mutableStateOf(DataManager.isAppLockEnabled)
    var isOledThemeEnabled by mutableStateOf(DataManager.appThemeMode == "OLED")

    fun refresh() {
        userName = DataManager.userName
        userBio = DataManager.userBio
        userProfileImageUri = DataManager.userProfileImageUri
        userAvatarRes = DataManager.userAvatarRes
        isAppLockEnabled = DataManager.isAppLockEnabled
        isOledThemeEnabled = DataManager.appThemeMode == "OLED"
    }
}
