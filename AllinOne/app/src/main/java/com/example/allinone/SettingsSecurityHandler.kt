package com.example.allinone

import android.content.Context
import android.content.Intent
import com.example.allinone.data.model.*
import com.example.allinone.domain.repository.UserSettings
import com.example.allinone.security.SecurityManager

class SettingsSecurityHandler(
    private val activity: SettingsActivity,
    private val viewModel: SettingsViewModel,
    private val onRefresh: () -> Unit
) {
    fun getConfigItems(settings: UserSettings): List<ConfigItem> {
        val items = mutableListOf<ConfigItem>()
        
        items.add(ConfigItem("App Access Lock", "Require PIN", isToggle = true, isChecked = settings.isAppLockEnabled) {
            if (!settings.isAppLockEnabled && settings.appLockPin == null) {
                activity.startActivity(Intent(activity, LockActivity::class.java).apply { 
                    putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) 
                })
            } else {
                viewModel.updateSettings(settings.copy(isAppLockEnabled = !settings.isAppLockEnabled))
                onRefresh()
            }
        })
        
        if (settings.isAppLockEnabled && settings.appLockPin != null) {
            items.add(ConfigItem("Change PIN", "Update security code") { 
                activity.startActivity(Intent(activity, LockActivity::class.java).apply { 
                    putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_CHANGE) 
                }) 
            })
            items.add(ConfigItem("Biometric Unlock", "Use Fingerprint/Face", isToggle = true, isChecked = settings.isBiometricLockEnabled) {
                viewModel.updateSettings(settings.copy(isBiometricLockEnabled = !settings.isBiometricLockEnabled))
                onRefresh()
            })
        }
        
        items.add(ConfigItem("Screen Protection", "Block screenshots & recording", isToggle = true, isChecked = settings.isScreenshotProtectionEnabled) {
            val nextState = !settings.isScreenshotProtectionEnabled
            viewModel.updateSettings(settings.copy(isScreenshotProtectionEnabled = nextState))
            SecurityManager.setScreenshotProtection(activity, nextState)
            onRefresh()
        })
        
        return items
    }
}
