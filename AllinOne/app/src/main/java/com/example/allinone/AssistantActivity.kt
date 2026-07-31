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
import com.example.allinone.data.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class AssistantActivity : BaseActivity() {

    private var insights by mutableStateOf<List<AssistantBrain.Insight>>(emptyList())
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isMuted by mutableStateOf(!DataManager.isAssistantVoiceEnabled)
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
        ).apply {
            isMuted = this@AssistantActivity.isMuted
        }

        lifecycleScope.launch {
            insights = AssistantBrain.generateInsights(this@AssistantActivity)
            if (activeSessionId == -1L) {
                // If no active session, we don't load history into the main chat
                // Instead, we show a welcome message
                if (chatMessages.isEmpty()) {
                    val welcome = "Hi! I'm your Personal Life Assistant. How can I help you manage your day?"
                    chatMessages.add(ChatMessage(welcome, false))
                    delay(1000) // Give TTS time to init
                    if (!isMuted) {
                        voiceHandler?.speak(welcome)
                    }
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
                            DataManager.isAssistantVoiceEnabled = false
                            DataManager.saveData(this@AssistantActivity)
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
                            DataManager.isAssistantVoiceEnabled = true
                            DataManager.saveData(this@AssistantActivity)
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
                activeSessionId = aiChatRepo?.createSession(title, "chat") ?: 0L
            }
            aiChatRepo?.insertMessage(activeSessionId, userMsg.text, userMsg.isUser, userMsg.timestamp)
        }
        
        val rawCommand = command
        commandInput = ""

        lifecycleScope.launch {
            delay(500) // Artificial thinking delay
            val action = AssistantBrain.parseCommand(rawCommand)
            if (action != null) {
                var response = action.dynamicResponse ?: ""
                when (action.type) {
                    "ADD_HABIT" -> {
                        val payload = action.payload
                        val habit = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val target = parts.getOrNull(1)?.toIntOrNull() ?: 1
                            val freq = parts.getOrNull(2) ?: "Anytime"
                            Habit(name = name, isCompleted = false, frequency = freq, target = target)
                        } else {
                            Habit(name = payload, isCompleted = false, frequency = "Anytime")
                        }
                        DataManager.habits.add(habit)
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Created habit: ${habit.name}"
                    }
                    "ADD_WORKOUT" -> {
                        val payload = action.payload
                        val workout = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val mode = parts.getOrNull(1) ?: "Reps"
                            val target = parts.getOrNull(2)?.toIntOrNull() ?: 0
                            val rps = parts.getOrNull(3)?.toIntOrNull() ?: 0
                            val freq = parts.getOrNull(4) ?: "Anytime"
                            Workout(name = name, trackingMode = mode, target = target, repsPerSet = rps, frequency = freq, isCompleted = false)
                        } else {
                            Workout(name = payload, isCompleted = false, frequency = "Anytime")
                        }
                        DataManager.workouts.add(workout)
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Created workout: ${workout.name}"
                    }
                    "UPDATE_WORKOUT_PROGRESS" -> {
                        val parts = action.payload.split("|")
                        val name = parts[0]
                        val inc = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val workout = DataManager.workouts.find { it.name.equals(name, ignoreCase = true) }
                        if (workout != null) {
                            workout.progress += inc
                            if (workout.progress >= workout.target) {
                                workout.isCompleted = true
                                if (!workout.completedDates.contains(DataManager.getTrackingDateString())) {
                                    workout.completedDates.add(DataManager.getTrackingDateString())
                                }
                            }
                            DataManager.saveData(this@AssistantActivity, true)
                        }
                    }
                    "COMPLETE_WORKOUT" -> {
                        val name = action.payload
                        val workout = DataManager.workouts.find { it.name.equals(name, ignoreCase = true) }
                        if (workout != null) {
                            workout.isCompleted = true
                            workout.progress = workout.target
                            if (!workout.completedDates.contains(DataManager.getTrackingDateString())) {
                                workout.completedDates.add(DataManager.getTrackingDateString())
                            }
                            DataManager.saveData(this@AssistantActivity, true)
                        }
                    }
                    "ADD_TASK" -> {
                        val payload = action.payload
                        val task = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val subsStr = parts.getOrNull(1) ?: ""
                            val reminderStr = parts.getOrNull(2) ?: ""
                            val subtasks = if (subsStr.isNotEmpty()) subsStr.split(",").map { Subtask(it, false) }.toMutableList() else mutableListOf()
                            val reminder = reminderStr.toLongOrNull()
                            Task(name = name, subtasks = subtasks, reminderTime = reminder)
                        } else {
                            Task(name = payload)
                        }
                        DataManager.tasks.add(0, task)
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Added task: ${task.name}"
                    }
                    "MARK_TASK_COMPLETE" -> {
                        val name = action.payload
                        val task = DataManager.tasks.find { it.name.equals(name, ignoreCase = true) }
                        if (task != null) {
                            task.isCompleted = true
                            task.completedTimestamp = System.currentTimeMillis()
                            DataManager.saveData(this@AssistantActivity, true)
                        }
                    }
                    "MARK_SUBTASK_COMPLETE" -> {
                        val parts = action.payload.split("|")
                        val taskName = parts[0]
                        val subName = parts.getOrNull(1) ?: ""
                        val task = DataManager.tasks.find { it.name.equals(taskName, ignoreCase = true) }
                        val subtask = task?.subtasks?.find { it.name.equals(subName, ignoreCase = true) }
                        if (subtask != null) {
                            subtask.isCompleted = true
                            DataManager.saveData(this@AssistantActivity, true)
                            response = "Marked '$subName' as completed in '${task.name}'!"
                        }
                    }
                    "CREATE_NESTED_TASK" -> {
                        val parts = action.payload.split(":")
                        val parentTitle = parts[0]
                        val subTitles = parts.getOrNull(1)?.split("|") ?: emptyList()
                        val subtasks = subTitles.map { Subtask(it, false) }.toMutableList()
                        val task = Task(name = parentTitle, subtasks = subtasks)
                        DataManager.tasks.add(0, task)
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Created task '$parentTitle' with ${subtasks.size} subtasks."
                    }
                    "ADD_NOTE" -> {
                        val payload = action.payload
                        val note = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            Note(title = parts[0], content = parts.getOrNull(1) ?: "")
                        } else {
                            Note(title = payload, content = "")
                        }
                        DataManager.notes.add(0, note)
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Saved note: ${note.title}"
                    }
                    "SEARCH_NOTES" -> {
                        val results = DataManager.searchNotes(action.payload)
                        if (results.isNotEmpty()) {
                            val titles = results.joinToString("\n") { "• ${it.title}" }
                            response = "Found ${results.size} notes matching '${action.payload}':\n\n$titles"
                        } else {
                            response = "I couldn't find any notes matching '${action.payload}'."
                        }
                    }
                    "START_WORKOUT" -> {
                        if (response.isEmpty()) response = "Opening your workout routine..."
                        startActivity(Intent(this@AssistantActivity, WorkoutRoutineActivity::class.java))
                    }
                    "LOG_EXPENSE" -> {
                        if (response.isEmpty()) response = "Ready to log expense: ${DataManager.financeCurrency}${action.payload}"
                        val intent = Intent(this@AssistantActivity, AddFinanceActivity::class.java).apply {
                            putExtra("QUICK_AMOUNT", action.payload)
                        }
                        startActivity(intent)
                    }
                    "LOG_INCOME" -> {
                        val amount = action.payload.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            DataManager.addIncome(this@AssistantActivity, amount, "Salary")
                            if (response.isEmpty()) response = "Logged income: ${DataManager.financeCurrency}$amount"
                        }
                    }
                    "LOG_MOOD" -> {
                        val date = DataManager.getTrackingDateString()
                        DataManager.dailyMoods[date] = action.payload
                        DataManager.lastMoodTimestamp = System.currentTimeMillis()
                        DataManager.saveData(this@AssistantActivity)
                        if (response.isEmpty()) response = "Logged your mood as ${action.payload}. How are you feeling overall?"
                    }
                    "LOG_HABIT" -> {
                        val habitName = action.payload
                        val habit = DataManager.habits.find { it.name.equals(habitName, ignoreCase = true) }
                        if (habit != null) {
                            habit.isCompleted = true
                            if (!habit.completedDates.contains(DataManager.getTrackingDateString())) {
                                habit.completedDates.add(DataManager.getTrackingDateString())
                            }
                            DataManager.saveData(this@AssistantActivity, true)
                            if (response.isEmpty()) response = "Marked '$habitName' as completed!"
                        } else {
                            response = "I couldn't find the habit '$habitName' to mark as completed."
                        }
                    }
                    "SET_BUDGET" -> {
                        val budget = action.payload.toDoubleOrNull() ?: 0.0
                        if (budget > 0) {
                            DataManager.monthlyBudget = budget
                            DataManager.saveData(this@AssistantActivity)
                            if (response.isEmpty()) response = "Budget set to ${DataManager.financeCurrency}$budget"
                        }
                    }
                    "NAVIGATE" -> {
                        if (response.isEmpty()) response = "Opening ${action.payload}..."
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
                            val remaining = project.subFeatures?.count { !it.isCompleted } ?: 0
                            val deadline = project.deadline?.let {
                                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                sdf.format(Date(it))
                            } ?: "No deadline set"
                            response = "Project '${project.title}' (Due: $deadline) is $progress% complete. You have $remaining tasks remaining."
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
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val style = LocalAppStyle.current
    
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                keyboardController?.hide()
                focusManager.clearFocus()
            })
        },
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
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            })
                        },
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom)
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

