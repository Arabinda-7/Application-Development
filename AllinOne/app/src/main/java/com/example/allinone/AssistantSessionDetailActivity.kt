package com.example.allinone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AssistantSessionDetailActivity : BaseActivity() {

    private var sessionId by mutableLongStateOf(-1L)
    private var sessionTitle by mutableStateOf("")
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isMuted by mutableStateOf(false)
    private val chatMessages = mutableStateListOf<ChatMessage>()
    private val aiChatRepo = DataManager.getAiChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionId = intent.getLongExtra("SESSION_ID", -1L)
        sessionTitle = intent.getStringExtra("SESSION_TITLE") ?: "Conversation"

        if (sessionId == -1L) {
            finish()
            return
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
                    isMuted = isMuted,
                    onMuteToggle = { isMuted = !isMuted },
                    onCommandChange = { commandInput = it },
                    onSendCommand = { handleCommand(commandInput) },
                    onMicClick = { /* Voice recognition logic can be added here too */ },
                    onBack = { finish() },
                    onHistoryClick = { /* Already in history context */ },
                    onSettingsClick = { /* Can add navigation if needed */ },
                    onFeedClick = { /* Can add navigation if needed */ },
                    onNewChatClick = {
                        // Logic to start a new chat could also be here, or just finish and let main handle it
                        finish()
                    }
                )
            }
        }
    }

    private fun handleCommand(command: String) {
        if (command.isBlank()) return
        
        val userMsg = ChatMessage(command, true)
        chatMessages.add(userMsg)
        
        lifecycleScope.launch {
            aiChatRepo?.insertMessage(sessionId, userMsg.text, userMsg.isUser, userMsg.timestamp)
            
            delay(500)
            val action = AssistantBrain.parseCommand(command)
            val response = if (action != null) {
                when (action.type) {
                    "CHAT_RESPONSE" -> action.payload
                    else -> "Executing ${action.type}: ${action.payload}"
                }
            } else {
                "I'm not sure how to do that yet."
            }
            
            val assistantMsg = ChatMessage(response, false)
            chatMessages.add(assistantMsg)
            aiChatRepo?.insertMessage(sessionId, assistantMsg.text, assistantMsg.isUser, assistantMsg.timestamp)
        }
        
        commandInput = ""
    }
}
