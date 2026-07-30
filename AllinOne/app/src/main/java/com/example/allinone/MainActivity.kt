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

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navigationHandler: MainNavigationHandler
    private lateinit var quickActionsHandler: MainQuickActionsHandler
    private lateinit var searchSection: MainSearchSection
    private var voiceHandler: VoiceAssistantHandler? = null

    private var isVoiceListening by mutableStateOf(false)

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
        

        // Alarm Manager Check
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31 check
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { intent ->
                    startActivity(intent)
                }
            }
        }

        initHandlers()
        initVoiceHandler()
        viewModel.refreshState(this)

        setContent {
            var animationFinished by remember { mutableStateOf(false) }
            val dashboardState = viewModel.dashboardState
            val isLoaded = dashboardState.isDataLoaded
            val isUnlocked = dashboardState.isAppUnlocked
            
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
                                    onNavigateToProjects = { navigationHandler.navigateToProjects() },
                                    onNavigateToFinance = { navigationHandler.navigateToFinance() },
                                    onNavigateToSettings = { navigationHandler.navigateToSettings() },
                                    onNavigateToAssistant = { navigationHandler.navigateToAssistant() },
                                    onNavigateToWorkspace = { navigationHandler.navigateToWorkspace() },
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
                                    isVoiceThinking = voiceHandler?.isThinking ?: false
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
            isMuted = false // Or get from preferences
        }
    }

    private fun handleVoiceCommand(command: String) {
        if (command.isBlank()) return
        
        voiceHandler?.isThinking = true
        
        lifecycleScope.launch {
            val action = AssistantBrain.parseCommand(command)
            if (action != null) {
                var response = ""
                when (action.type) {
                    "ADD_HABIT" -> {
                        val habit = Habit(name = action.payload, isCompleted = false, frequency = "Anytime")
                        DataManager.habits.add(habit)
                        DataManager.saveData(this@MainActivity)
                        response = "Created habit: ${action.payload}"
                    }
                    "ADD_TASK" -> {
                        val task = Task(name = action.payload)
                        DataManager.tasks.add(0, task)
                        DataManager.saveData(this@MainActivity)
                        response = "Added task: ${action.payload}"
                    }
                    "ADD_WORKOUT" -> {
                        val workout = Workout(name = action.payload, isCompleted = false, frequency = "Anytime")
                        DataManager.workouts.add(workout)
                        DataManager.saveData(this@MainActivity)
                        response = "Created workout: ${action.payload}"
                    }
                    "ADD_NOTE" -> {
                        val note = Note(title = action.payload, content = "")
                        DataManager.notes.add(0, note)
                        DataManager.saveData(this@MainActivity)
                        response = "Saved note: ${action.payload}"
                    }
                    "LOG_EXPENSE" -> {
                        response = "Ready to log expense: ${DataManager.financeCurrency}${action.payload}"
                        val intent = Intent(this@MainActivity, AddFinanceActivity::class.java).apply {
                            putExtra("QUICK_AMOUNT", action.payload)
                        }
                        startActivity(intent)
                    }
                    "NAVIGATE" -> {
                        response = "Opening ${action.payload}..."
                        when (action.payload) {
                            "FINANCE" -> startActivity(Intent(this@MainActivity, FinanceActivity::class.java))
                            "HABITS" -> startActivity(Intent(this@MainActivity, HabitTrackerActivity::class.java))
                        }
                    }
                    "CHAT_RESPONSE" -> {
                        response = action.payload
                    }
                }
                voiceHandler?.speak(response, response.trim().endsWith("?"))
            } else {
                voiceHandler?.speak("I'm not sure how to do that yet.")
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
