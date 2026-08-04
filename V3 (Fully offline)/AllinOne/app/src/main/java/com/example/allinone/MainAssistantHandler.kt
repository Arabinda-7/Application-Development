package com.example.allinone

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.allinone.assistant.executor.AssistantActionHandler
import com.example.allinone.assistant.model.CommandAction
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainAssistantHandler @Inject constructor(
    private val brain: AssistantBrain,
    private val actionHandler: AssistantActionHandler,
    private val voiceManager: VoiceInteractionManager
) {

    fun initialize(context: Context) {
        brain.initialize(context)
    }

    fun getVoiceManager(): VoiceInteractionManager = voiceManager
    fun getBrain(): AssistantBrain = brain
    fun getActionHandler(): AssistantActionHandler = actionHandler

    fun toggleVoice(
        activity: BaseActivity,
        onCommandProcessed: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            activity.checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                voiceManager.startListening(
                    activity = activity,
                    onResult = { text ->
                        onCommandProcessed(text)
                    },
                    onError = { error ->
                        onError(error)
                    }
                )
            }
        }
    }

    fun processCommand(
        context: Context,
        scope: LifecycleCoroutineScope,
        text: String
    ) {
        if (text.isBlank()) return
        
        scope.launch {
            val action = brain.parseCommand(text)
            handleAssistantAction(context, scope, action)
        }
    }

    private fun handleAssistantAction(
        context: Context,
        scope: LifecycleCoroutineScope,
        action: CommandAction?
    ) {
        if (action == null) return
        
        scope.launch {
            val status = actionHandler.executeAction(context, action)
            status?.let { voiceManager.speak(it) }
        }
    }

    fun destroy() {
        voiceManager.stopListening()
    }
}
