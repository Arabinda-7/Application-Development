package com.example.allinone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.executor.AssistantActionHandler
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.ui.assistant.AssistantScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AssistantSessionDetailActivity : BaseActivity() {

    @Inject lateinit var brain: AssistantBrain
    @Inject lateinit var actionHandler: AssistantActionHandler

    private var sessionId by mutableLongStateOf(-1L)
    private var sessionTitle by mutableStateOf("")
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isThinking by mutableStateOf(false)
    private var isMuted by mutableStateOf(!DataManager.isAssistantVoiceEnabled)
    private val chatMessages = mutableStateListOf<ChatMessage>()
    private val aiChatRepo by lazy { DataManager.getAiChatRepository(this) }
    private var voiceHandler: VoiceAssistantHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionId = intent.getLongExtra("SESSION_ID", -1L)
        sessionTitle = intent.getStringExtra("SESSION_TITLE") ?: "Conversation"

        if (sessionId == -1L) {
            finish()
            return
        }

        brain.initialize(this)

        voiceHandler = VoiceAssistantHandler(
            context = this,
            onResults = { command -> handleCommand(command) },
            onListeningStateChanged = { listening -> isListening = listening },
            onError = { _ -> isListening = false }
        ).apply {
            isMuted = this@AssistantSessionDetailActivity.isMuted
        }

        lifecycleScope.launch {
            aiChatRepo?.getMessagesBySession(sessionId)?.collect { messages ->
                chatMessages.clear()
                chatMessages.addAll(messages.map { ChatMessage(it.text, it.isUser, it.timestamp) })
            }
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantScreen(
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    isListening = isListening,
                    isThinking = isThinking,
                    isMuted = isMuted,
                    onMuteToggle = { 
                        isMuted = !isMuted
                        voiceHandler?.isMuted = isMuted
                        DataManager.isAssistantVoiceEnabled = !isMuted
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                    },
                    onCommandChange = { commandInput = it },
                    onSendCommand = { handleCommand(commandInput) },
                    onMicClick = { 
                        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                            voiceHandler?.startListening()
                        }
                    },
                    onBack = { finish() },
                    onHistoryClick = { finish() },
                    onSettingsClick = { /* Settings */ },
                    onFeedClick = { /* Feed */ },
                    onNewChatClick = { finish() },
                    sessionTitle = sessionTitle
                )
            }
        }
    }

    private fun handleCommand(command: String) {
        if (command.isBlank()) return
        
        val userMsg = ChatMessage(command, true)
        chatMessages.add(userMsg)
        commandInput = ""
        isThinking = true
        
        lifecycleScope.launch {
            aiChatRepo?.insertMessage(com.example.allinone.data.database.AiChatEntity(sessionId = sessionId, text = userMsg.text, isUser = userMsg.isUser, timestamp = userMsg.timestamp))
            
            delay(500)
            val action = brain.parseCommand(command)
            isThinking = false
            
            if (action == null) {
                val fallback = "I'm not sure how to help with that yet."
                addAssistantMessage(fallback)
                return@launch
            }
            
            val responseText = action.dynamicResponse ?: if (action.type == "CHAT_RESPONSE") action.payload else null
            responseText?.let { addAssistantMessage(it) }
            
            val status = actionHandler.executeAction(this@AssistantSessionDetailActivity, action)
            status?.let { addAssistantMessage(it) }
        }
    }

    private fun addAssistantMessage(text: String) {
        chatMessages.add(ChatMessage(text, false))
        if (!isMuted) voiceHandler?.speak(text, text.trim().endsWith("?"))
        lifecycleScope.launch {
            aiChatRepo?.insertMessage(com.example.allinone.data.database.AiChatEntity(sessionId = sessionId, text = text, isUser = false, timestamp = System.currentTimeMillis()))
        }
    }

    override fun onDestroy() {
        voiceHandler?.shutdown()
        super.onDestroy()
    }
}
