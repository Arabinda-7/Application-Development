package com.example.allinone

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class ProfileSecurityHubSection(
    private val rootView: View,
    private val onAppLockToggled: (Boolean) -> Unit,
    private val onBiometricToggled: (Boolean) -> Unit,
    private val onScreenshotToggled: (Boolean) -> Unit,
    private val onOledToggled: (Boolean) -> Unit
) {
    fun setup(isAppLockEnabled: Boolean, isBiometricEnabled: Boolean, isScreenshotEnabled: Boolean, isOledEnabled: Boolean) {
        setupToggle(
            R.id.item_app_lock,
            R.drawable.baseline_settings_24,
            "App Access Lock",
            isAppLockEnabled,
            onAppLockToggled
        )

        setupToggle(
            R.id.item_biometric_lock,
            R.drawable.icons8_padlock_100,
            "Biometric Unlock",
            isBiometricEnabled,
            onBiometricToggled
        )

        setupToggle(
            R.id.item_screenshot_protection,
            R.drawable.icons8_protection_mask_100_4,
            "Screen Protection",
            isScreenshotEnabled,
            onScreenshotToggled
        )

        setupToggle(
            R.id.item_oled_mode,
            R.drawable.ic_habit_tracker,
            "OLED Theme",
            isOledEnabled,
            onOledToggled
        )
    }

    private fun setupToggle(containerId: Int, icon: Int, title: String, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
        val container = rootView.findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.iv_setting_icon).setImageResource(icon)
        container.findViewById<TextView>(R.id.tv_setting_title).text = title
        val sw = container.findViewById<SwitchCompat>(R.id.sw_profile_toggle)
        sw.isChecked = isChecked
        
        container.setOnClickListener {
            val newState = !sw.isChecked
            sw.isChecked = newState
            onToggle(newState)
        }
    }
}
