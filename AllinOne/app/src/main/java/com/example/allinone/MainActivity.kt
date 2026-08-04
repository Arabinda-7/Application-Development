package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.ui.home.DashboardState
import com.example.allinone.ui.home.components.VoiceOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject lateinit var assistantHandler: MainAssistantHandler
    @Inject lateinit var aiChatRepository: com.example.allinone.data.repository.AiChatRepository

    private val viewModel: MainActivityViewModel by viewModels()
    private val navigationHandler by lazy { MainNavigationHandler(this) }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var showVoiceOverlay by mutableStateOf(false)
    private var currentVoiceSessionId by mutableLongStateOf(-1L)
    private val voiceConversation = mutableStateListOf<ChatMessage>()

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
        
        // Immediate check for onboarding status to avoid showing the loading screen
        val prefs = com.example.allinone.security.SecurityManager.getEncryptedPrefs(this)
        if (!prefs.getBoolean("onboarding_completed", false)) {
            super.onCreate(savedInstanceState)
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        
        assistantHandler.initialize(this)

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
                val appStyle = remember(dashboardState) { AppStyle.fromDashboardState(dashboardState) }
                
                val densityValue = remember(dashboardState) { UIUtils.getIsolatedMoodDensity(dashboardState) }
                val fontScale = remember(dashboardState) { UIUtils.getFontScale(dashboardState) }
                val customDensity = Density(density = densityValue, fontScale = fontScale)

                CompositionLocalProvider(
                    LocalAppStyle provides appStyle,
                    androidx.compose.ui.platform.LocalDensity provides customDensity
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(appStyle.backgroundColor)) {
                        HomeScreen(
                            state = dashboardState,
                            onNavigateToHabits = { navigationHandler.navigateToHabits() },
                            onNavigateToWorkout = { navigationHandler.navigateToWorkout() },
                            onNavigateToTodos = { navigationHandler.navigateToTodos() },
                            onNavigateToNotes = { navigationHandler.navigateToNotes() },
                            onNavigateToProjects = { navigationHandler.navigateToProjects() },
                            onNavigateToFinance = { navigationHandler.navigateToFinance() },
                            onNavigateToWorkspace = { navigationHandler.navigateToWorkspace() },
                            onNavigateToSettings = { navigationHandler.navigateToSettings() },
                            onNavigateToAssistant = { navigationHandler.navigateToAssistant() },
                            onNavigateToProfile = { navigationHandler.navigateToProfile() },
                            onNavigateToPerformanceHistory = { navigationHandler.navigateToPerformanceHistory() },
                            onTabSelected = { viewModel.updateSelectedTab(it) },
                            onQuickAddTodo = { startActivity(Intent(this@MainActivity, AddTaskActivity::class.java)) },
                            onQuickAddExpense = { startActivity(Intent(this@MainActivity, AddFinanceActivity::class.java)) },
                            onQuickAddNote = { startActivity(Intent(this@MainActivity, AddNoteActivity::class.java)) },
                            onMoodSelected = { viewModel.updateDailyMood(it) },
                            onNotificationsMarkedAsViewed = { viewModel.markNotificationsAsViewed(it) },
                            onSearchRequested = { query ->
                                MainSearchSection(this@MainActivity).performSearch(query)
                            },
                            onVoiceAssistantRequested = { 
                                showVoiceOverlay = true
                                voiceConversation.clear()
                                lifecycleScope.launch {
                                    currentVoiceSessionId = aiChatRepository.createSession(
                                        "Voice Interaction ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())}", 
                                        "voice"
                                    )
                                }
                                assistantHandler.toggleVoice(this@MainActivity, 
                                    onCommandProcessed = { handleVoiceCommand(it) },
                                    onError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )

                        val voiceManager = assistantHandler.getVoiceManager()
                        val isListening by voiceManager.isListening.collectAsState()
                        val partialText by voiceManager.partialText.collectAsState()

                        VoiceOverlay(
                            isVisible = showVoiceOverlay,
                            isListening = isListening,
                            partialText = partialText,
                            messages = voiceConversation,
                            onDismiss = { 
                                showVoiceOverlay = false
                                currentVoiceSessionId = -1L
                                voiceManager.stopListening()
                            },
                            onMicClick = { 
                                assistantHandler.toggleVoice(this@MainActivity, 
                                    onCommandProcessed = { handleVoiceCommand(it) },
                                    onError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )
                    }
                }
            }

            LaunchedEffect(isLoaded, isOnboardingCompleted) {
                if (isLoaded && !isOnboardingCompleted) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                } else if (isLoaded) {
                    // Request all necessary permissions once loaded
                    val permissions = mutableListOf(android.Manifest.permission.RECORD_AUDIO)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    
                    checkAndRequestPermissions(permissions.toTypedArray()) { 
                        // Optional: handle results
                    }

                    if (dashboardState.isAppLockEnabled && !isUnlocked && dashboardState.appLockPin != null) {
                        val intent = Intent(this@MainActivity, LockActivity::class.java).apply {
                            putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
                        }
                        lockLauncher.launch(intent)
                    }
                }
            }
        }

        viewModel.refreshState(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSelectedTab(0)
        viewModel.refreshState(this)
    }

    private fun handleVoiceCommand(text: String) {
        voiceConversation.add(ChatMessage(text, true))
        
        lifecycleScope.launch {
            if (currentVoiceSessionId != -1L) {
                aiChatRepository.insertMessage(currentVoiceSessionId, text, true, System.currentTimeMillis())
            }

            val brain = assistantHandler.getBrain()
            val action = brain.parseCommand(text)
            
            if (action == null) {
                val fallback = "I'm not sure how to help with that yet."
                voiceConversation.add(ChatMessage(fallback, false))
                if (currentVoiceSessionId != -1L) {
                    aiChatRepository.insertMessage(currentVoiceSessionId, fallback, false, System.currentTimeMillis())
                }
                assistantHandler.getVoiceManager().speak(fallback)
                return@launch
            }
            
            val responseText = action.dynamicResponse ?: if (action.type == "CHAT_RESPONSE") action.payload else null
            responseText?.let { 
                voiceConversation.add(ChatMessage(it, false))
                if (currentVoiceSessionId != -1L) {
                    aiChatRepository.insertMessage(currentVoiceSessionId, it, false, System.currentTimeMillis())
                }
                assistantHandler.getVoiceManager().speak(it)
            }
            
            val status = assistantHandler.getActionHandler().executeAction(this@MainActivity, action)
            status?.let { 
                voiceConversation.add(ChatMessage(it, false))
                if (currentVoiceSessionId != -1L) {
                    aiChatRepository.insertMessage(currentVoiceSessionId, it, false, System.currentTimeMillis())
                }
                assistantHandler.getVoiceManager().speak(it)
            }
        }
    }


    override fun onDestroy() {
        assistantHandler.destroy()
        super.onDestroy()
    }
}
