package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.executor.AssistantActionHandler
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.ui.home.DashboardState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject lateinit var brain: AssistantBrain
    @Inject lateinit var actionHandler: AssistantActionHandler

    private val viewModel: MainActivityViewModel by viewModels()
    
    private var isVoiceListening by mutableStateOf(false)
    private var isVoiceThinking by mutableStateOf(false)
    private val voiceMessages = mutableStateListOf<ChatMessage>()
    private var voiceHandler: VoiceAssistantHandler? = null

    private val lockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            DataManager.isAppUnlocked = true
            viewModel.refreshState(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        brain.initialize(this)
        initVoiceHandler()

        setContent {
            val dashboardState = viewModel.dashboardState
            val isLoaded = dashboardState.isDataLoaded
            val isUnlocked = DataManager.isAppUnlocked
            val isOnboardingCompleted = dashboardState.isOnboardingCompleted

            if (!isLoaded) {
                LoadingScreen()
            } else if (!isOnboardingCompleted) {
                Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black))
            } else {
                val appStyle = remember { AppStyle.fromDashboardState(dashboardState) }
                CompositionLocalProvider(LocalAppStyle provides appStyle) {
                    Box(modifier = Modifier.fillMaxSize().background(appStyle.backgroundColor)) {
                        HomeScreen(
                            state = dashboardState,
                            onNavigateToHabits = { startActivity(Intent(this@MainActivity, HabitTrackerActivity::class.java)) },
                            onNavigateToWorkout = { startActivity(Intent(this@MainActivity, WorkoutRoutineActivity::class.java)) },
                            onNavigateToTodos = { startActivity(Intent(this@MainActivity, TaskActivity::class.java)) },
                            onNavigateToNotes = { startActivity(Intent(this@MainActivity, NotesActivity::class.java)) },
                            onNavigateToProjects = { startActivity(Intent(this@MainActivity, ProjectActivity::class.java)) },
                            onNavigateToFinance = { startActivity(Intent(this@MainActivity, FinanceActivity::class.java)) },
                            onNavigateToSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                            onNavigateToAssistant = { startActivity(Intent(this@MainActivity, AssistantActivity::class.java)) },
                            onNavigateToProfile = { startActivity(Intent(this@MainActivity, ProfileActivity::class.java)) },
                            onNavigateToPerformanceHistory = { startActivity(Intent(this@MainActivity, PerformanceHistoryActivity::class.java)) },
                            onQuickAddTodo = { startActivity(Intent(this@MainActivity, AddTaskActivity::class.java)) },
                            onQuickAddExpense = { startActivity(Intent(this@MainActivity, AddFinanceActivity::class.java)) },
                            onQuickAddNote = { startActivity(Intent(this@MainActivity, AddNoteActivity::class.java)) },
                            onMoodSelected = { viewModel.updateDailyMood(it) },
                            onNotificationsMarkedAsViewed = { viewModel.markNotificationsAsViewed(it) },
                            onSearchRequested = { query ->
                                MainSearchSection(this@MainActivity).performSearch(query)
                            },
                            isVoiceListening = isVoiceListening,
                            isVoiceThinking = isVoiceThinking,
                            voiceMessages = voiceMessages,
                            onVoiceMicClick = { toggleVoiceListening() },
                            onVoiceSessionStarted = { startVoiceSession() }
                        )
                    }
                }
            }

            LaunchedEffect(isLoaded, isOnboardingCompleted) {
                if (isLoaded && !isOnboardingCompleted) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                } else if (isLoaded && dashboardState.isAppLockEnabled && !isUnlocked && dashboardState.appLockPin != null) {
                    val intent = Intent(this@MainActivity, LockActivity::class.java).apply {
                        putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
                    }
                    lockLauncher.launch(intent)
                }
            }
        }

        viewModel.refreshState(this)
    }

    private fun initVoiceHandler() {
        voiceHandler = VoiceAssistantHandler(
            this,
            onResults = { text ->
                isVoiceListening = false
                isVoiceThinking = true
                processVoiceCommand(text)
            },
            onListeningStateChanged = { listening ->
                isVoiceListening = listening
            },
            onError = { message ->
                isVoiceListening = false
                isVoiceThinking = false
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun startVoiceSession() {
        voiceMessages.clear()
        isVoiceThinking = false
        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
            voiceHandler?.startListening()
        }
    }

    private fun toggleVoiceListening() {
        if (isVoiceListening) {
            voiceHandler?.stopListening()
        } else {
            isVoiceThinking = false
            checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                voiceHandler?.startListening()
            }
        }
    }

    private fun processVoiceCommand(text: String) {
        if (text.isBlank()) return
        voiceMessages.add(ChatMessage(text, true))
        isVoiceThinking = true
        
        lifecycleScope.launch {
            val action = brain.parseCommand(text)
            isVoiceThinking = false
            handleAssistantAction(action)
        }
    }

    private fun handleAssistantAction(action: CommandAction?) {
        if (action == null) {
            val fallback = "I'm not sure how to help with that yet."
            voiceMessages.add(ChatMessage(fallback, false))
            voiceHandler?.speak(fallback)
            return
        }
        
        val responseText = action.dynamicResponse ?: if (action.type == "CHAT_RESPONSE") action.payload else null
        responseText?.let { 
            voiceMessages.add(ChatMessage(it, false)) 
            voiceHandler?.speak(it)
        }
        
        lifecycleScope.launch {
            val status = actionHandler.executeAction(this@MainActivity, action)
            status?.let { 
                voiceMessages.add(ChatMessage(it, false))
                voiceHandler?.speak(it)
            }
        }
    }

    override fun onPause() {
        voiceHandler?.stopListening()
        super.onPause()
    }

    override fun onDestroy() {
        voiceHandler?.shutdown()
        super.onDestroy()
    }
}
