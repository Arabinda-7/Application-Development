package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.allinone.ui.components.LoadingScreen
import com.example.allinone.ui.home.HomeScreen
import com.example.allinone.ui.home.DashboardState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private val lockLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
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
                            onQuickAddTodo = { startActivity(Intent(this@MainActivity, AddTaskActivity::class.java)) },
                            onQuickAddExpense = { startActivity(Intent(this@MainActivity, AddFinanceActivity::class.java)) },
                            onQuickAddNote = { startActivity(Intent(this@MainActivity, AddNoteActivity::class.java)) },
                            onMoodSelected = { viewModel.updateDailyMood(it) },
                            onNotificationsMarkedAsViewed = { viewModel.markNotificationsAsViewed(it) },
                            onSearchRequested = { query ->
                                MainSearchSection(this@MainActivity).performSearch(query)
                            }
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
}
