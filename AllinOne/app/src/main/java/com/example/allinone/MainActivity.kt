package com.example.allinone


import android.app.AlarmManager
import android.content.Context
import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.data.ChatMessage
import java.util.Calendar

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navigationHandler: MainNavigationHandler
    private lateinit var quickActionsHandler: MainQuickActionsHandler
    private lateinit var searchSection: MainSearchSection
    private var voiceHandler: VoiceAssistantHandler? = null

    private var isVoiceListening by mutableStateOf(false)
    private var activeVoiceSessionId by mutableLongStateOf(-1L)
    private val voiceMessages = mutableStateListOf<ChatMessage>()
    private val aiChatRepo = DataManager.getAiChatRepository()
    private var permissionsRequested = false

    private val lockLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            DataManager.isAppUnlocked = true
            viewModel.refreshState(this)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Immediate redirect to Onboarding if not completed to bypass LoadingScreen
        val prefs = SecurityManager.getEncryptedPrefs(this)
        val isCompleted = DataManager.isOnboardingCompleted || prefs.getBoolean("onboarding_completed", false)
        if (!isCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // Removed setKeepOnScreenCondition to allow custom LoadingScreen to animate immediately
        

        initHandlers()
        initVoiceHandler()
        viewModel.refreshState(this)

        setContent {
            var animationFinished by remember { mutableStateOf(false) }
            val dashboardState = viewModel.dashboardState
            val isLoaded = dashboardState.isDataLoaded
            val isUnlocked = dashboardState.isAppUnlocked
            
            LaunchedEffect(animationFinished) {
                if (animationFinished) {
                    requestAppPermissions()
                }
            }

            LaunchedEffect(isLoaded) {
                if (isLoaded) {
                    if (DataManager.isAppLockEnabled && !DataManager.isAppUnlocked && DataManager.appLockPin != null) {
                        val intent = Intent(this@MainActivity, LockActivity::class.java).apply {
                            putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
                        }
                        lockLauncher.launch(intent)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            overrideActivityTransition(android.app.Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
                        } else {
                            @Suppress("DEPRECATION")
                            overridePendingTransition(0, 0)
                        }
                    } else {
                        DataManager.isAppUnlocked = true
                        viewModel.refreshState(this@MainActivity)
                    }
                }
            }

            LaunchedEffect(Unit) {
                DataManager.dataChangeSignal.collect {
                    viewModel.refreshState(this@MainActivity, viewModel.dashboardState.currentMood)
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    !isLoaded || !isUnlocked || !animationFinished -> {
                        LoadingScreen(onFinished = { animationFinished = true })
                    }
                    else -> {
                        val customDensity = remember(dashboardState.homeDisplaySize, dashboardState.fontSize) {
                            val currentDensity = this@MainActivity.resources.displayMetrics.density
                            val currentFontScale = this@MainActivity.resources.configuration.fontScale
                            val dScale = when(dashboardState.homeDisplaySize) {
                                "XS" -> 0.85f
                                "L" -> 1.15f
                                else -> 1.0f
                            }
                            val fScale = when(dashboardState.fontSize) {
                                "XS" -> 0.85f
                                "L" -> 1.25f
                                else -> 1.0f
                            }
                            Density(density = currentDensity * dScale, fontScale = currentFontScale * fScale)
                        }

                        val context = LocalContext.current
                        val isDarkTheme = when (dashboardState.appThemeMode) {
                            "LIGHT" -> false
                            "DARK", "OLED" -> true
                            else -> isSystemInDarkTheme()
                        }

                        val appStyle = remember(dashboardState.appThemeMode, dashboardState.appAccentColor, dashboardState.appBorderRadius, dashboardState.appShowShadows, dashboardState.appFontFamily, dashboardState.isDynamicColorEnabled) {
                            val isOled = dashboardState.appThemeMode == "OLED"
                            val isLight = dashboardState.appThemeMode == "LIGHT"
                            
                            val dynamicColor = dashboardState.isDynamicColorEnabled
                            val colorScheme = if (dynamicColor) {
                                if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                            } else null

                            val accentColor = when {
                                colorScheme != null -> colorScheme.primary
                                dashboardState.appAccentColor != -1 -> Color(dashboardState.appAccentColor)
                                else -> Color(0xFF1A73E8)
                            }

                            AppStyle(
                                borderRadius = dashboardState.appBorderRadius.dp,
                                accentColor = accentColor,
                                surfaceColor = when {
                                    isOled -> Color.Black
                                    isLight -> if (dashboardState.appCardStyle == "GLASS") Color.White.copy(alpha = 0.8f) else Color(0xFFF5F5F5)
                                    else -> if (dashboardState.appCardStyle == "GLASS") Color.White.copy(alpha = 0.05f) else Color(0xFF1A1A1A)
                                },
                                backgroundColor = when {
                                    isOled -> Color.Black
                                    isLight -> Color.White
                                    else -> Color.Black
                                },
                                isOled = isOled,
                                showShadows = dashboardState.appShowShadows,
                                isDynamicColorEnabled = dashboardState.isDynamicColorEnabled,
                                fontFamily = when(dashboardState.appFontFamily) {
                                    "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                                    "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                    "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                    else -> androidx.compose.ui.text.font.FontFamily.Default
                                },
                                cardStyle = dashboardState.appCardStyle
                            )
                        }

                        CompositionLocalProvider(
                            LocalDensity provides customDensity,
                            LocalAppStyle provides appStyle
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(fontFamily = appStyle.fontFamily)
                            ) {
                                HomeScreen(
                                    state = dashboardState,
                                    onNavigateToHabits = { navigationHandler.navigateToHabits() },
                                    onNavigateToWorkout = { navigationHandler.navigateToWorkout() },
                                    onNavigateToTodos = { navigationHandler.navigateToTodos() },
                                    onNavigateToNotes = { navigationHandler.navigateToNotes() },
                                    onNavigateToProjects = { 
                                        if (dashboardState.hasProjects) {
                                            navigationHandler.navigateToProjects()
                                        } else {
                                            android.widget.Toast.makeText(this@MainActivity, "Please create or import a project to access this section.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onNavigateToFinance = { navigationHandler.navigateToFinance() },
                                    onNavigateToSettings = { navigationHandler.navigateToSettings() },
                                    onNavigateToAssistant = { navigationHandler.navigateToAssistant() },
                                    onNavigateToWorkspace = { 
                                        if (dashboardState.hasProjects) {
                                            navigationHandler.navigateToWorkspace()
                                        } else {
                                            android.widget.Toast.makeText(this@MainActivity, "Please create or import a project to access this section.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onNavigateToProfile = { navigationHandler.navigateToProfile() },
                                    onNavigateToPerformanceHistory = { navigationHandler.navigateToPerformanceHistory() },
                                    onQuickAddTodo = { quickActionsHandler.quickAddTask() },
                                    onQuickAddExpense = { quickActionsHandler.quickAddExpense() },
                                    onQuickAddNote = { quickActionsHandler.quickAddNote() },
                                    onColorSelected = { section, color ->
                                        viewModel.updateSectionColor(this@MainActivity, section, color)
                                    },
                                    onMoodSelected = { emoji ->
                                        val today = DataManager.getTrackingDateString()
                                        DataManager.dailyMoods[today] = emoji
                                        DataManager.lastMoodTimestamp = System.currentTimeMillis()
                                        DataManager.saveData(this@MainActivity)
                                        viewModel.refreshState(this@MainActivity, emoji)
                                    },
                                    onSearchRequested = { query ->
                                        searchSection.performSearch(query)
                                    },
                                    isVoiceListening = isVoiceListening,
                                    onVoiceMicClick = {
                                        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                                            voiceHandler?.startListening()
                                        }
                                    },
                                    isVoiceThinking = voiceHandler?.isThinking ?: false,
                                    onVoiceSessionStarted = {
                                        startVoiceAssistantSession()
                                    },
                                    voiceMessages = voiceMessages
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initHandlers() {
        navigationHandler = MainNavigationHandler(this)
        quickActionsHandler = MainQuickActionsHandler(this)
        searchSection = MainSearchSection(this)
    }

    private fun requestAppPermissions() {
        if (permissionsRequested) return
        permissionsRequested = true

        // Alarm Manager Check
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { intent ->
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.add(android.Manifest.permission.RECORD_AUDIO)
        
        checkAndRequestPermissions(permissions.toTypedArray()) { _ ->
            // Optional: log or handle result
        }
    }

    private fun initVoiceHandler() {
        voiceHandler = VoiceAssistantHandler(
            context = this,
            onResults = { command ->
                handleVoiceCommand(command)
            },
            onListeningStateChanged = { listening ->
                isVoiceListening = listening
            },
            onError = { _ ->
                isVoiceListening = false
            }
        ).apply {
            isMuted = false // Voice-to-voice is always audible when triggered
        }
    }

    private fun startVoiceAssistantSession() {
        voiceMessages.clear()
        activeVoiceSessionId = -1L
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        
        val greeting = "$timeGreeting! I'm here. How can I help you manage your day?"
        val assistantMsg = ChatMessage(greeting, false)
        voiceMessages.add(assistantMsg)
        
        // We don't save the greeting to DB immediately, wait for first user interaction to create session
        // or we can create it now:
        lifecycleScope.launch {
            activeVoiceSessionId = aiChatRepo?.createSession("Voice Session", "voice") ?: -1L
            aiChatRepo?.insertMessage(activeVoiceSessionId, assistantMsg.text, assistantMsg.isUser, assistantMsg.timestamp)
        }
        
        voiceHandler?.speak(greeting)
    }

    private fun handleVoiceCommand(command: String) {
        if (command.isBlank()) return
        
        voiceHandler?.isThinking = true
        
        val userMsg = ChatMessage(command, true)
        voiceMessages.add(userMsg)

        lifecycleScope.launch {
            if (activeVoiceSessionId == -1L) {
                val title = if (command.length > 20) command.take(20) + "..." else command
                activeVoiceSessionId = aiChatRepo?.createSession(title, "voice") ?: -1L
            }
            aiChatRepo?.insertMessage(activeVoiceSessionId, userMsg.text, userMsg.isUser, userMsg.timestamp)
        }
        
        lifecycleScope.launch {
            val action = AssistantBrain.parseCommand(command)
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
                        DataManager.saveData(this@MainActivity)
                        if (response.isEmpty()) response = "Created habit: ${habit.name}"
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
                        DataManager.saveData(this@MainActivity)
                        if (response.isEmpty()) response = "Added task: ${task.name}"
                    }
                    "MARK_TASK_COMPLETE" -> {
                        val name = action.payload
                        val task = DataManager.tasks.find { it.name.equals(name, ignoreCase = true) }
                        if (task != null) {
                            task.isCompleted = true
                            task.completedTimestamp = System.currentTimeMillis()
                            DataManager.saveData(this@MainActivity, true)
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
                            DataManager.saveData(this@MainActivity, true)
                            response = "Marked '$subName' as completed in '${task.name}'!"
                        }
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
                        DataManager.saveData(this@MainActivity)
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
                            DataManager.saveData(this@MainActivity, true)
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
                            DataManager.saveData(this@MainActivity, true)
                        }
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
                        DataManager.saveData(this@MainActivity)
                        if (response.isEmpty()) response = "Saved note: ${note.title}"
                    }
                    "LOG_HABIT" -> {
                        val habitName = action.payload
                        val habit = DataManager.habits.find { it.name.equals(habitName, ignoreCase = true) }
                        if (habit != null) {
                            habit.isCompleted = true
                            if (!habit.completedDates.contains(DataManager.getTrackingDateString())) {
                                habit.completedDates.add(DataManager.getTrackingDateString())
                            }
                            DataManager.saveData(this@MainActivity, true)
                            if (response.isEmpty()) response = "Marked '$habitName' as completed!"
                        }
                    }
                    "LOG_EXPENSE" -> {
                        if (response.isEmpty()) response = "Ready to log expense: ${DataManager.financeCurrency}${action.payload}"
                        val intent = Intent(this@MainActivity, AddFinanceActivity::class.java).apply {
                            putExtra("QUICK_AMOUNT", action.payload)
                        }
                        startActivity(intent)
                    }
                    "NAVIGATE" -> {
                        if (response.isEmpty()) response = "Opening ${action.payload}..."
                        when (action.payload) {
                            "FINANCE" -> startActivity(Intent(this@MainActivity, FinanceActivity::class.java))
                            "HABITS" -> startActivity(Intent(this@MainActivity, HabitTrackerActivity::class.java))
                        }
                    }
                    "CHAT_RESPONSE" -> {
                        response = action.payload
                    }
                }
                
                val assistantMsg = ChatMessage(response, false)
                voiceMessages.add(assistantMsg)
                aiChatRepo?.insertMessage(activeVoiceSessionId, assistantMsg.text, assistantMsg.isUser, assistantMsg.timestamp)

                voiceHandler?.speak(response, response.trim().endsWith("?"))
            } else {
                val errorResponse = "I'm not sure how to do that yet."
                val assistantMsg = ChatMessage(errorResponse, false)
                voiceMessages.add(assistantMsg)
                aiChatRepo?.insertMessage(activeVoiceSessionId, assistantMsg.text, assistantMsg.isUser, assistantMsg.timestamp)
                voiceHandler?.speak(errorResponse)
            }
        }
    }

    override fun onDestroy() {
        voiceHandler?.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState(this, viewModel.dashboardState.currentMood)
    }
}
