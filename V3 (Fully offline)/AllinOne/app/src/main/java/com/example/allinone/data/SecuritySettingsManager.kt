package com.example.allinone.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecuritySettingsManager: Manages preferences and state specifically for app security (PIN, Biometrics, etc).
 */
@Singleton
class SecuritySettingsManager @Inject constructor() {
    var isAppLockEnabled: Boolean = false
    var isBiometricLockEnabled: Boolean = false
    var isScreenshotProtectionEnabled: Boolean = false
    var appLockPin: String = ""
    var appLockQuestion: String = ""
    var appLockAnswer: String = ""
}
