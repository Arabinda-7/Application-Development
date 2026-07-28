package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.allinone.MainSplashScreenHandler.SplashScreen
import com.example.allinone.ui.home.HomeScreen
import kotlinx.coroutines.delay

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navigationHandler: MainNavigationHandler
    private lateinit var quickActionsHandler: MainQuickActionsHandler
    private lateinit var searchSection: MainSearchSection

    private val lockLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            DataManager.isAppUnlocked = true
            viewModel.refreshState()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!DataManager.isOnboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        if (DataManager.isAppLockEnabled && !DataManager.isAppUnlocked && DataManager.appLockPin != null) {
            val intent = Intent(this, LockActivity::class.java).apply {
                putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
            }
            lockLauncher.launch(intent)
            overridePendingTransition(0, 0)
        } else {
            DataManager.isAppUnlocked = true
        }

        initHandlers()
        viewModel.refreshState()

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            val splashProgress = remember { Animatable(0f) }
            
            val dashboardState = viewModel.dashboardState
            val isLoaded = dashboardState.isDataLoaded
            val isUnlocked = dashboardState.isAppUnlocked
            
            LaunchedEffect(Unit) {
                DataManager.dataChangeSignal.collect {
                    viewModel.refreshState(viewModel.dashboardState.currentMood)
                }
            }
            
            LaunchedEffect(isUnlocked) {
                if (isUnlocked) {
                    val totalTime = DataManager.startupLoadingTime
                    splashProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = totalTime,
                            easing = LinearEasing
                        )
                    )
                    delay(500)
                    showSplash = false
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    showSplash -> {
                        SplashScreen(splashProgress.value)
                    }
                    !isLoaded || !isUnlocked -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
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

                        val appStyle = remember(dashboardState.appThemeMode, dashboardState.appAccentColor, dashboardState.appBorderRadius, dashboardState.appShowShadows, dashboardState.appFontFamily) {
                            val isOled = dashboardState.appThemeMode == "OLED"
                            val isLight = dashboardState.appThemeMode == "LIGHT"
                            AppStyle(
                                borderRadius = dashboardState.appBorderRadius.dp,
                                accentColor = if (dashboardState.appAccentColor != -1) Color(dashboardState.appAccentColor) else Color(0xFF1A73E8),
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
                                    onNavigateToWorkspace = { navigationHandler.navigateToWorkspace() },
                                    onNavigateToProfile = { navigationHandler.navigateToProfile() },
                                    onNavigateToPerformanceHistory = { navigationHandler.navigateToPerformanceHistory() },
                                    onQuickAddTodo = { quickActionsHandler.quickAddTask() },
                                    onQuickAddExpense = { quickActionsHandler.quickAddExpense() },
                                    onQuickAddNote = { quickActionsHandler.quickAddNote() },
                                    onColorSelected = { section, color ->
                                        viewModel.updateSectionColor(section, color)
                                    },
                                    onMoodSelected = { emoji ->
                                        val today = DataManager.getTrackingDateString()
                                        DataManager.dailyMoods[today] = emoji
                                        DataManager.lastMoodTimestamp = System.currentTimeMillis()
                                        DataManager.saveData(this@MainActivity)
                                        viewModel.refreshState(emoji)
                                    },
                                    onSearchRequested = { query ->
                                        searchSection.performSearch(query)
                                    }
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshState(viewModel.dashboardState.currentMood)
    }
}
