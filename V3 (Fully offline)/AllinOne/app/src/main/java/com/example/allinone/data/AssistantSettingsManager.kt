package com.example.allinone.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * AssistantSettingsManager: Manages preferences and state specifically for the AI Voice Assistant.
 */
@Singleton
class AssistantSettingsManager @Inject constructor() {
    var voiceName: String = ""
    var pitch: Float = 1.0f
    var speechRate: Float = 1.0f
    var isEnabled: Boolean = true
    var isVoiceEnabled: Boolean = false
}
