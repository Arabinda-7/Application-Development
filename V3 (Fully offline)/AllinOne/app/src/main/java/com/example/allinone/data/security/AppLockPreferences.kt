package com.example.allinone.data.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppLockPreferences: Manages biometric authentication state, PIN security,
 * secret Q&A verification, and session lock state.
 */
@Singleton
class AppLockPreferences @Inject constructor() {
    var isAppLockEnabled: Boolean = false
    var isBiometricLockEnabled: Boolean = false
    var appLockQuestion: String = ""
    var appLockAnswer: String = ""
    var appLockPin: String = ""
    var isAppUnlocked: Boolean = false
}
