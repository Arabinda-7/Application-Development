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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AssistantActivity : BaseActivity() {

    private var insights by mutableStateOf<List<AssistantBrain.Insight>>(emptyList())
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isMuted by mutableStateOf(true)
    private val chatMessages = mutableStateListOf<ChatMessage>()
    private var activeSessionId by mutableStateOf<Long>(-1)
    private val aiChatRepo = DataManager.getAiChatRepository()
    private var voiceHandler: VoiceAssistantHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        voiceHandler = VoiceAssistantHandler(
            context = this,
            onResults = { command -> handleCommand(command) },
            onListeningStateChanged = { listening -> isListening = listening },
            onError = { _ -> isListening = false }
        )

        lifecycleScope.launch {
            insights = AssistantBrain.generateInsights(this@AssistantActivity)
            if (activeSessionId == -1L) {
                // If no active session, we don't load history into the main chat
                // Instead, we show a welcome message
                if (chatMessages.isEmpty()) {
                    val welcome = "Hi! I'm your Personal Life Assistant. How can I help you manage your day?"
                    chatMessages.add(ChatMessage(welcome, false))
                    delay(1000) // Give TTS time to init
                    voiceHandler?.speak(welcome)
                }
            }
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            var showMuteWarning by remember { mutableStateOf(false) }

            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantScreen(
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    isListening = isListening,
                    isThinking = voiceHandler?.isThinking ?: false,
                    isMuted = isMuted,
                    onMuteToggle = { 
                        if (isMuted) {
                            showMuteWarning = true
                        } else {
                            isMuted = true
                            voiceHandler?.isMuted = true
                        }
                    },
                    onCommandChange = { commandInput = it },
                    onSendCommand = { handleCommand(commandInput) },
                    onMicClick = { startVoiceRecognition() },
                    onBack = { finish() },
                    onHistoryClick = { startActivity(Intent(this, AssistantHistoryActivity::class.java)) },
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onFeedClick = { startActivity(Intent(this, AssistantFeedActivity::class.java)) },
                    onNewChatClick = {
                        activeSessionId = -1L
                        chatMessages.clear()
                        chatMessages.add(ChatMessage("Hi! I'm your Personal Life Assistant. How can I help you manage your day?", false))
                    }
                )
            }

            if (showMuteWarning) {
                AlertDialog(
                    onDismissRequest = { showMuteWarning = false },
                    title = { Text("Enable Voice Output?", color = Color.White) },
                    text = { Text("Turning this on will allow the assistant to speak out loud. Please ensure you are in a suitable environment.", color = Color.LightGray) },
                    containerColor = Color(0xFF1E1E1E),
                    confirmButton = {
                        TextButton(onClick = {
                            isMuted = false
                            voiceHandler?.isMuted = false
                            showMuteWarning = false
                        }) {
                            Text("ENABLE", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMuteWarning = false }) {
                            Text("CANCEL", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }

    private fun startVoiceRecognition() {
        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
            voiceHandler?.startListening()
        }
    }

    private fun handleCommand(command: String) {
        if (command.isBlank()) return
        
        voiceHandler?.isThinking = true
        
        val userMsg = ChatMessage(command, true)
        if (activeSessionId == -1L) {
            // Remove the welcome message if it's the only one
            if (chatMessages.size == 1 && !chatMessages[0].isUser) {
                chatMessages.clear()
            }
        }
        chatMessages.add(userMsg)
        
        lifecycleScope.launch {
            if (activeSessionId == -1L) {
                // Create new session
                val title = if (command.length > 20) command.take(20) + "..." else command
                activeSessionId = aiChatRepo?.createSession(title) ?: 0L
            }
            aiChatRepo?.insertMessage(activeSessionId, userMsg.text, userMsg.isUser, userMsg.timestamp)
        }
        
        val rawCommand = command
        commandInput = ""

        lifecycleScope.launch {
            delay(500) // Artificial thinking delay
            val action = AssistantBrain.parseCommand(rawCommand)
            if (action != null) {
                var response = ""
                when (action.type) {
                    "ADD_HABIT" -> {
                        val habit = Habit(name = action.payload, isCompleted = false, frequency = "Anytime")
                        DataManager.habits.add(habit)
                        DataManager.saveData(this@AssistantActivity)
                        response = "Created habit: ${action.payload}"
                    }
                    "ADD_WORKOUT" -> {
                        val workout = Workout(name = action.payload, isCompleted = false, frequency = "Anytime")
                        DataManager.workouts.add(workout)
                        DataManager.saveData(this@AssistantActivity)
                        response = "Created workout: ${action.payload}"
                    }
                    "ADD_TASK" -> {
                        val task = Task(name = action.payload)
                        DataManager.tasks.add(0, task)
                        DataManager.saveData(this@AssistantActivity)
                        response = "Added task: ${action.payload}"
                    }
                    "ADD_NOTE" -> {
                        val note = Note(title = action.payload, content = "")
                        DataManager.notes.add(0, note)
                        DataManager.saveData(this@AssistantActivity)
                        response = "Saved note: ${action.payload}"
                    }
                    "START_WORKOUT" -> {
                        response = "Opening your workout routine..."
                        startActivity(Intent(this@AssistantActivity, WorkoutRoutineActivity::class.java))
                    }
                    "LOG_EXPENSE" -> {
                        response = "Ready to log expense: ${DataManager.financeCurrency}${action.payload}"
                        val intent = Intent(this@AssistantActivity, AddFinanceActivity::class.java).apply {
                            putExtra("QUICK_AMOUNT", action.payload)
                        }
                        startActivity(intent)
                    }
                    "SET_BUDGET" -> {
                        val budget = action.payload.toDoubleOrNull() ?: 0.0
                        if (budget > 0) {
                            DataManager.monthlyBudget = budget
                            DataManager.saveData(this@AssistantActivity)
                            response = "Budget set to ${DataManager.financeCurrency}$budget"
                        }
                    }
                    "NAVIGATE" -> {
                        response = "Opening ${action.payload}..."
                        when (action.payload) {
                            "FINANCE" -> startActivity(Intent(this@AssistantActivity, FinanceActivity::class.java))
                            "HABITS" -> startActivity(Intent(this@AssistantActivity, HabitTrackerActivity::class.java))
                            "SETTINGS" -> startActivity(Intent(this@AssistantActivity, SettingsActivity::class.java))
                        }
                    }
                    "PROJECT_REPORT" -> {
                        val projectName = action.payload
                        val project = synchronized(DataManager.projects) {
                            DataManager.projects.find { it.title.contains(projectName, ignoreCase = true) }
                        }
                        if (project != null) {
                            val progress = project.progress
                            val remaining = project.subFeatures.count { !it.isCompleted }
                            response = "Project '${project.title}' is $progress% complete. You have $remaining tasks remaining. I've analyzed your speed, and you're on track to finish soon!"
                        } else {
                            response = "I couldn't find a project matching '$projectName'. Try saying 'show projects' to see all your active roadmaps."
                        }
                    }
                    "CHAT_RESPONSE" -> {
                        response = action.payload
                    }
                }
                val assistantMsg = ChatMessage(response, false)
                chatMessages.add(assistantMsg)
                aiChatRepo?.insertMessage(activeSessionId, assistantMsg.text, assistantMsg.isUser, assistantMsg.timestamp)
                
                // Determine if we should auto-listen (if it's a question)
                val isQuestion = response.trim().endsWith("?")
                voiceHandler?.speak(response, isQuestion)
            } else {
                val errorMsg = ChatMessage("I'm not sure how to do that yet. Try saying 'Add task' or 'Log expense'.", false)
                chatMessages.add(errorMsg)
                aiChatRepo?.insertMessage(activeSessionId, errorMsg.text, errorMsg.isUser, errorMsg.timestamp)
                voiceHandler?.speak(errorMsg.text)
            }
        }
    }

    override fun onDestroy() {
        voiceHandler?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    chatMessages: List<ChatMessage>,
    commandInput: String,
    isListening: Boolean,
    isThinking: Boolean = false,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onCommandChange: (String) -> Unit,
    onSendCommand: () -> Unit,
    onMicClick: () -> Unit,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFeedClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    val suggestions = listOf(
        "Who are you?",
        "What can you do?",
        "Is my data safe?",
        "Add habit Drink Water",
        "Log 500 expense",
        "Show finance"
    )

    val listState = rememberLazyListState()
    var menuExpanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val style = LocalAppStyle.current
    
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ALL IN ONE AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onMuteToggle) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = if (isMuted) Color.Gray else Color.White
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(Color.Black)
                                .border(1.dp, style.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Chat", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onNewChatClick()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color(0xFF34A853)
                                )
                            )
                            DropdownMenuItem(
                                text = { Text("History", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onHistoryClick()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color(0xFF4285F4)
                                )
                            )
                            DropdownMenuItem(
                                text = { Text("Intelligent Feed", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onFeedClick()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color(0xFFEA4335)
                                )
                            )
                            DropdownMenuItem(
                                text = { Text("Settings", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onSettingsClick()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color(0xFFFBBC04)
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawLine(
                        color = style.accentColor.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            VoiceAuraGlow(
                isListening = isListening,
                isThinking = isThinking,
                accentColor = style.accentColor
            )

            Column(modifier = Modifier.fillMaxSize()) {
                
                // Conversational Area
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message)
                    }
                }

                // Interaction Area (Bottom Anchored)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp, top = 20.dp)
                            .padding(horizontal = 20.dp)
                    ) {
                        if (isListening || isThinking) {
                            GoogleVoiceBars(isListening = isListening || isThinking)
                            if (isThinking) {
                                Text(
                                    "Thinking...",
                                    color = style.accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            // Suggestions
                            AnimatedVisibility(
                                visible = showSuggestions,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    items(suggestions) { suggestion ->
                                        SuggestionChip(suggestion) { onCommandChange(suggestion) }
                                    }
                                }
                            }
                        }

                        // Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = commandInput,
                                onValueChange = onCommandChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp)),
                                placeholder = { Text("What's on your mind?", fontSize = 14.sp, color = Color.Gray) },
                                leadingIcon = {
                                    IconButton(onClick = { showSuggestions = !showSuggestions }) {
                                        Icon(
                                            imageVector = if (showSuggestions) Icons.Default.Lightbulb else Icons.Default.TipsAndUpdates,
                                            contentDescription = "Hints",
                                            tint = if (showSuggestions) Color(0xFFFBBC04) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                trailingIcon = {
                                    if (commandInput.isNotEmpty()) {
                                        IconButton(onClick = onSendCommand) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send",
                                                tint = style.accentColor
                                            )
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            FloatingActionButton(
                                onClick = onMicClick,
                                containerColor = when {
                                    isListening -> Color(0xFFEA4335)
                                    isThinking -> Color(0xFFFBBC05)
                                    else -> style.accentColor
                                },
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = if (isThinking) Icons.Default.AutoAwesome else Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

