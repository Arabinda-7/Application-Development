package com.example.allinone

import android.content.Intent
import com.example.allinone.domain.repository.UserSettings
import androidx.activity.result.ActivityResultLauncher

class SettingsAiHandler(
    private val activity: SettingsActivity,
    private val viewModel: SettingsViewModel,
    private val aiIntroLauncher: ActivityResultLauncher<Intent>,
    private val onRefresh: () -> Unit
) {
    fun getConfigItems(settings: UserSettings): List<ConfigItem> {
        val items = mutableListOf<ConfigItem>()
        
        items.add(ConfigItem("Enable AI Assistant", "Global toggle for chat and voice", isToggle = true, isChecked = settings.isAiAssistantEnabled) {
            if (!settings.isAiAssistantEnabled) {
                aiIntroLauncher.launch(Intent(activity, AiAssistantIntroActivity::class.java))
            } else {
                viewModel.updateSettings(settings.copy(isAiAssistantEnabled = false))
                onRefresh()
            }
        })

        if (settings.isAiAssistantEnabled) {
            items.add(ConfigItem("Voice Output", "Allow assistant to speak in text chat", isToggle = true, isChecked = settings.isAssistantVoiceEnabled) {
                viewModel.updateSettings(settings.copy(isAssistantVoiceEnabled = !settings.isAssistantVoiceEnabled))
                onRefresh()
            })
            items.add(ConfigItem("Auto-Cleanup History", "Delete conversations older than 7 days", isToggle = true, isChecked = settings.isAssistantAutoCleanupEnabled) {
                viewModel.updateSettings(settings.copy(isAssistantAutoCleanupEnabled = !settings.isAssistantAutoCleanupEnabled))
                onRefresh()
            })
        }
        
        return items
    }
}
