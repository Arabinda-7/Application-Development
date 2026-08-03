package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.domain.usecase.assistant.GetAssistantInsightsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AssistantActivity : BaseActivity() {

    @Inject lateinit var getAssistantInsightsUseCase: GetAssistantInsightsUseCase

    private var insights by mutableStateOf<List<AssistantBrain.Insight>>(emptyList())
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isMuted by mutableStateOf(false)
    private val chatMessages = mutableStateListOf<ChatMessage>()
    private var voiceHandler: VoiceAssistantHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            insights = getAssistantInsightsUseCase()
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantScreen(
                    insights = insights,
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    isListening = isListening,
                    isMuted = isMuted,
                    onCommandChange = { commandInput = it },
                    onSend = { sendCommand(it) },
                    onListenToggle = { toggleListening() },
                    onMuteToggle = { isMuted = !isMuted },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun sendCommand(text: String) {
        if (text.isBlank()) return
        chatMessages.add(ChatMessage(text, true))
        commandInput = ""
        
        lifecycleScope.launch {
            val action = AssistantBrain.parseCommand(text)
            handleAction(action)
        }
    }

    private fun handleAction(action: CommandAction?) {
        if (action == null) {
            chatMessages.add(ChatMessage("I'm not sure how to help with that yet.", false))
            return
        }
        
        action.dynamicResponse?.let { chatMessages.add(ChatMessage(it, false)) }
        
        when (action.type) {
            "CHAT_RESPONSE" -> { /* Already handled */ }
            "ADD_HABIT" -> { /* Logic to call repository */ }
            // ... other actions
        }
    }

    private fun toggleListening() {
        isListening = !isListening
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AssistantScreen(
        insights: List<AssistantBrain.Insight>,
        chatMessages: List<ChatMessage>,
        commandInput: String,
        isListening: Boolean,
        isMuted: Boolean,
        onCommandChange: (String) -> Unit,
        onSend: (String) -> Unit,
        onListenToggle: () -> Unit,
        onMuteToggle: () -> Unit,
        onBack: () -> Unit
    ) {
        val style = LocalAppStyle.current
        Scaffold(
            containerColor = style.backgroundColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ASSISTANT", fontWeight = FontWeight.Bold, color = style.accentColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = style.accentColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Insights List
                if (insights.isNotEmpty()) {
                    LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(insights) { insight ->
                            InsightCard(insight)
                        }
                    }
                }

                // Chat Messages
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
                }

                // Input Bar
                ChatInputBar(commandInput, onCommandChange, onSend, isListening, onListenToggle, isMuted, onMuteToggle)
            }
        }
    }

    @Composable
    fun InsightCard(insight: AssistantBrain.Insight) {
        val style = LocalAppStyle.current
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
            border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.3f)),
            modifier = Modifier.width(280.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(insight.title, fontWeight = FontWeight.Bold, color = style.accentColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(insight.description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
    }

    @Composable
    fun ChatBubble(msg: ChatMessage) {
        val style = LocalAppStyle.current
        val alignment = if (msg.isUser) Alignment.End else Alignment.Start
        val color = if (msg.isUser) style.accentColor else style.surfaceColor
        
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
            Surface(
                color = color,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    msg.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (msg.isUser) Color.Black else Color.White
                )
            }
        }
    }

    @Composable
    fun ChatInputBar(
        input: String,
        onInputChange: (String) -> Unit,
        onSend: (String) -> Unit,
        isListening: Boolean,
        onListenToggle: () -> Unit,
        isMuted: Boolean,
        onMuteToggle: () -> Unit
    ) {
        val style = LocalAppStyle.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a command...") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = style.surfaceColor,
                    focusedContainerColor = style.surfaceColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onSend(input) }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = style.accentColor)
            }
        }
    }
}
