package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.executor.AssistantActionHandler
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.domain.usecase.assistant.GetAssistantInsightsUseCase
import com.example.allinone.ui.assistant.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AssistantActivity : BaseActivity() {

    @Inject lateinit var getAssistantInsightsUseCase: GetAssistantInsightsUseCase
    @Inject lateinit var brain: AssistantBrain
    @Inject lateinit var actionHandler: AssistantActionHandler
    @Inject lateinit var aiChatRepository: com.example.allinone.data.repository.AiChatRepository
    @Inject lateinit var autoCleanupAssistantHistoryUseCase: com.example.allinone.domain.usecase.assistant.AutoCleanupAssistantHistoryUseCase
    @Inject lateinit var voiceManager: VoiceInteractionManager

    private var insights by mutableStateOf<List<AssistantBrain.Insight>>(emptyList())
    private var currentSessionId by mutableLongStateOf(-1L)
    private var commandInput by mutableStateOf("")
    private var isThinking by mutableStateOf(false)
    private var isVoiceMuted by mutableStateOf(false)
    private val chatMessages = mutableStateListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        brain.initialize(this)

        lifecycleScope.launch {
            autoCleanupAssistantHistoryUseCase()
            insights = getAssistantInsightsUseCase()
            
            val isVoiceSession = intent.getBooleanExtra("START_VOICE", false)
            val prefix = if (isVoiceSession) "Voice Interaction" else "Chat"
            val type = if (isVoiceSession) "voice" else "chat"
            currentSessionId = aiChatRepository.createSession("$prefix ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}", type)
            chatMessages.clear()
            
            if (isVoiceSession) {
                toggleListening()
            }
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            val isListening by voiceManager.isListening.collectAsState()
            
            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantMainScreen(
                    insights = insights,
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    isListening = isListening,
                    isThinking = isThinking,
                    isMuted = isVoiceMuted,
                    onCommandChange = { commandInput = it },
                    onSend = { sendCommand(it) },
                    onListenToggle = { toggleListening() },
                    onMuteToggle = { isVoiceMuted = !isVoiceMuted },
                    onNewChat = { createNewChat() },
                    onHistory = { startActivity(Intent(this@AssistantActivity, AssistantHistoryActivity::class.java)) },
                    onSettings = { startActivity(Intent(this@AssistantActivity, SettingsActivity::class.java)) },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun toggleListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                voiceManager.startListening(
                    onResult = { text ->
                        sendCommand(text)
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun createNewChat(type: String = "chat") {
        lifecycleScope.launch {
            val prefix = if (type == "voice") "Voice Interaction" else "Chat"
            currentSessionId = aiChatRepository.createSession("$prefix ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}", type)
            chatMessages.clear()
        }
    }

    private fun sendCommand(text: String) {
        if (text.isBlank()) return
        chatMessages.add(ChatMessage(text, true))
        commandInput = ""
        isThinking = true
        
        lifecycleScope.launch {
            try {
                if (currentSessionId == -1L) {
                    currentSessionId = aiChatRepository.createSession("New Chat")
                }
                aiChatRepository.insertMessage(currentSessionId, text, true, System.currentTimeMillis())
                
                val action = brain.parseCommand(text)
                handleAction(action)
            } catch (e: Exception) {
                isThinking = false
                chatMessages.add(ChatMessage("Error: ${e.message}", false))
            }
        }
    }

    private fun handleAction(action: CommandAction?) {
        isThinking = false
        if (action == null) {
            val fallback = "I'm not sure how to help with that yet."
            addAssistantMessage(fallback)
            return
        }
        
        val responseText = action.dynamicResponse ?: if (action.type == "CHAT_RESPONSE") action.payload else null
        responseText?.let { addAssistantMessage(it) }
        
        lifecycleScope.launch {
            val status = actionHandler.executeAction(this@AssistantActivity, action)
            status?.let { addAssistantMessage(it) }
        }
    }

    private fun addAssistantMessage(text: String) {
        chatMessages.add(ChatMessage(text, false))
        if (!isVoiceMuted) voiceManager.speak(text)
        lifecycleScope.launch {
            if (currentSessionId != -1L) {
                aiChatRepository.insertMessage(currentSessionId, text, false, System.currentTimeMillis())
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AssistantMainScreen(
        insights: List<AssistantBrain.Insight>,
        chatMessages: List<ChatMessage>,
        commandInput: String,
        isListening: Boolean,
        isThinking: Boolean,
        isMuted: Boolean,
        onCommandChange: (String) -> Unit,
        onSend: (String) -> Unit,
        onListenToggle: () -> Unit,
        onMuteToggle: () -> Unit,
        onNewChat: () -> Unit,
        onHistory: () -> Unit,
        onSettings: () -> Unit,
        onBack: () -> Unit
    ) {
        val style = LocalAppStyle.current
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ASSISTANT", fontWeight = FontWeight.Bold, color = style.accentColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = style.accentColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = onMuteToggle) {
                            Icon(if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = null, tint = style.accentColor)
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = style.accentColor)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(style.surfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Chat", color = Color.White) },
                                onClick = { showMenu = false; onNewChat() },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = style.accentColor) }
                            )
                            DropdownMenuItem(
                                text = { Text("History", color = Color.White) },
                                onClick = { showMenu = false; onHistory() },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = style.accentColor) }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings", color = Color.White) },
                                onClick = { showMenu = false; onSettings() },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = style.accentColor) }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (insights.isNotEmpty() && !isThinking && !isListening) {
                    LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(insights) { insight ->
                            InsightCard(insight)
                        }
                    }
                }

                val listState = rememberLazyListState()
                LaunchedEffect(chatMessages.size) {
                    if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { msg ->
                        ChatBubble(msg)
                    }
                    if (isThinking) {
                        item { ThinkingIndicator() }
                    }
                }

                // Chat Input Bar (Integrated new voice listener)
                Surface(
                    color = style.surfaceColor,
                    tonalElevation = 8.dp,
                    shadowElevation = if (style.showShadows) 8.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onListenToggle) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isListening) Color.Red else style.accentColor
                            )
                        }
                        
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = onCommandChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a command...", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = style.accentColor,
                                focusedBorderColor = style.accentColor,
                                unfocusedBorderColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onSend(commandInput) },
                            enabled = commandInput.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = style.accentColor)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // voiceManager.destroy() // Singleton
        super.onDestroy()
    }
}
