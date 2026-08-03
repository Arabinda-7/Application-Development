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
import com.example.allinone.assistant.executor.AssistantActionHandler
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.ui.home.DashboardState
import com.example.allinone.ui.home.components.VoiceOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject lateinit var brain: AssistantBrain
    @Inject lateinit var actionHandler: AssistantActionHandler

    private val viewModel: MainActivityViewModel by viewModels()
    private var voiceManager: VoiceInteractionManager? = null
    private var showVoiceOverlay by mutableStateOf(false)

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
        
        brain.initialize(this)
        voiceManager = VoiceInteractionManager(this)

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
                            onNavigateToHabits = { startActivity(Intent(this@MainActivity, HabitTrackerActivity::class.java)) },
                            onNavigateToWorkout = { startActivity(Intent(this@MainActivity, WorkoutRoutineActivity::class.java)) },
                            onNavigateToTodos = { startActivity(Intent(this@MainActivity, TaskActivity::class.java)) },
                            onNavigateToNotes = { startActivity(Intent(this@MainActivity, NotesActivity::class.java)) },
                            onNavigateToProjects = { startActivity(Intent(this@MainActivity, ProjectActivity::class.java)) },
                            onNavigateToFinance = { startActivity(Intent(this@MainActivity, FinanceActivity::class.java)) },
                            onNavigateToWorkspace = { startActivity(Intent(this@MainActivity, com.example.allinone.workspace.ui.activity.WorkspaceActivity::class.java)) },
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
                            onVoiceAssistantRequested = { showVoiceOverlay = true; toggleVoice() }
                        )

                        // Integrated the new Voice Feature
                        val isListening by voiceManager!!.isListening.collectAsState()
                        val partialText by voiceManager!!.partialText.collectAsState()

                        VoiceOverlay(
                            isVisible = showVoiceOverlay,
                            isListening = isListening,
                            partialText = partialText,
                            onDismiss = { showVoiceOverlay = false },
                            onMicClick = { toggleVoice() }
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

    private fun toggleVoice() {
        if (voiceManager?.isListening?.value == true) {
            voiceManager?.stopListening()
        } else {
            checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                voiceManager?.startListening(
                    onResult = { text ->
                        processCommand(text)
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun processCommand(text: String) {
        if (text.isBlank()) return
        
        lifecycleScope.launch {
            val action = brain.parseCommand(text)
            handleAssistantAction(action)
        }
    }

    private fun handleAssistantAction(action: CommandAction?) {
        if (action == null) return
        
        lifecycleScope.launch {
            val status = actionHandler.executeAction(this@MainActivity, action)
            status?.let { voiceManager?.speak(it) }
        }
    }

    override fun onDestroy() {
        voiceManager?.destroy()
        super.onDestroy()
    }
}
