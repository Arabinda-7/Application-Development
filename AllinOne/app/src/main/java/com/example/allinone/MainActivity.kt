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
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.ui.home.DashboardState
import com.example.allinone.ui.home.components.VoiceOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject lateinit var assistantHandler: MainAssistantHandler

    private val viewModel: MainActivityViewModel by viewModels()
    private val navigationHandler by lazy { MainNavigationHandler(this) }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
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
                                assistantHandler.toggleVoice(this@MainActivity, 
                                    onCommandProcessed = { 
                                        showVoiceOverlay = false
                                        assistantHandler.processCommand(this@MainActivity, lifecycleScope, it) 
                                    },
                                    onError = { 
                                        showVoiceOverlay = false
                                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() 
                                    }
                                )
                            }
                        )

                        val voiceManager = assistantHandler.getVoiceManager()
                        if (voiceManager != null) {
                            val isListening by voiceManager.isListening.collectAsState()
                            val partialText by voiceManager.partialText.collectAsState()

                            VoiceOverlay(
                                isVisible = showVoiceOverlay,
                                isListening = isListening,
                                partialText = partialText,
                                onDismiss = { 
                                    showVoiceOverlay = false
                                    voiceManager.stopListening()
                                },
                                onMicClick = { 
                                    assistantHandler.toggleVoice(this@MainActivity, 
                                        onCommandProcessed = { 
                                            showVoiceOverlay = false
                                            assistantHandler.processCommand(this@MainActivity, lifecycleScope, it) 
                                        },
                                        onError = { 
                                            showVoiceOverlay = false
                                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() 
                                        }
                                    )
                                }
                            )
                        }
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


    override fun onDestroy() {
        assistantHandler.destroy()
        super.onDestroy()
    }
}
