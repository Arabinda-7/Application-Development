package com.example.allinone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.allinone.workspace.ui.activity.WorkspaceActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private var dashboardState by mutableStateOf(DashboardState())

    private val lockLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            DataManager.isAppUnlocked = true
            refreshState()
        } else {
            finish() // Close app if lock is bypassed or failed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initial Data Load
        DataManager.loadData(this)

        // Check Onboarding
        if (!DataManager.isOnboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        // Check App Lock
        if (DataManager.isAppLockEnabled && !DataManager.isAppUnlocked && DataManager.appLockPin != null) {
            val intent = Intent(this, LockActivity::class.java).apply {
                putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
            }
            lockLauncher.launch(intent)
            overridePendingTransition(0, 0)
        } else {
            DataManager.isAppUnlocked = true
        }

        refreshState()

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var splashProgress by remember { mutableStateOf(0f) }
            
            val isLoaded = dashboardState.isDataLoaded
            val isUnlocked = dashboardState.isAppUnlocked
            
            LaunchedEffect(Unit) {
                DataManager.dataChangeSignal.collect {
                    refreshState()
                }
            }
            
            LaunchedEffect(isUnlocked) {
                if (isUnlocked) {
                    val totalTime = DataManager.startupLoadingTime.toFloat()
                    val startTime = System.currentTimeMillis()
                    
                    // Processing loop to update progress
                    while (System.currentTimeMillis() - startTime < totalTime) {
                        val elapsed = System.currentTimeMillis() - startTime
                        splashProgress = (elapsed / totalTime).coerceIn(0f, 1f)
                        
                        delay(16) // ~60fps update
                    }
                    
                    splashProgress = 1f
                    delay(500) // Brief pause to allow animation to reach 100%
                    showSplash = false
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    showSplash -> {
                        SplashScreen(splashProgress)
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
                                    isLight -> {
                                        if (dashboardState.appCardStyle == "GLASS") Color.White.copy(alpha = 0.8f) else Color(0xFFF5F5F5)
                                    }
                                    else -> {
                                        if (dashboardState.appCardStyle == "GLASS") Color.White.copy(alpha = 0.05f) else Color(0xFF1A1A1A)
                                    }
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
                                    onNavigateToHabits = { startActivity(Intent(this@MainActivity, HabitTrackerActivity::class.java)) },
                                    onNavigateToWorkout = { startActivity(Intent(this@MainActivity, WorkoutRoutineActivity::class.java)) },
                                    onNavigateToTodos = { startActivity(Intent(this@MainActivity, TaskActivity::class.java)) },
                                    onNavigateToNotes = { startActivity(Intent(this@MainActivity, NotesActivity::class.java)) },
                                    onNavigateToProjects = { startActivity(Intent(this@MainActivity, ProjectActivity::class.java)) },
                                    onNavigateToFinance = { startActivity(Intent(this@MainActivity, FinanceActivity::class.java)) },
                                    onNavigateToSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                                    onNavigateToWorkspace = { startActivity(Intent(this@MainActivity, WorkspaceActivity::class.java)) },
                                    onNavigateToProfile = { 
                                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                                    },
                                    onNavigateToPerformanceHistory = { 
                                        if (DataManager.showHabitSection || DataManager.showWorkoutSection) {
                                            startActivity(Intent(this@MainActivity, PerformanceHistoryActivity::class.java))
                                        }
                                    },
                                    onQuickAddTodo = { quickAddTask() },
                                    onQuickAddExpense = { quickAddExpense() },
                                    onQuickAddNote = { quickAddNote() },
                                    onColorSelected = { section, color ->
                                        updateSectionColor(section, color)
                                    },
                                    onMoodSelected = { emoji ->
                                        val today = DataManager.getTrackingDateString()
                                        DataManager.dailyMoods[today] = emoji
                                        DataManager.lastMoodTimestamp = System.currentTimeMillis()
                                        DataManager.saveData(this@MainActivity)
                                        refreshState()
                                    },
                                    onSearchRequested = { query ->
                                        performUniversalSearch(query)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SplashScreen(progress: Float) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Large Determinate Circular Progress around the icon
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(160.dp),
                        color = Color(0xFF1A73E8),
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFF1A73E8).copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        color = Color(0xFF1A73E8).copy(alpha = 0.05f)
                    ) {}
                    
                    Icon(
                        Icons.Default.RocketLaunch,
                        null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(56.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "All In One",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                
                Text(
                    if (progress < 0.3f) "Initializing Core..." 
                    else if (progress < 0.7f) "Optimizing Ecosystem..." 
                    else "Ready to Launch",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    color = Color(0xFF1A73E8).copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    private fun refreshState() {
        val today = DataManager.getTrackingDateString()
        val currentTime = System.currentTimeMillis()
        val isMoodExpired = DataManager.lastMoodTimestamp != 0L && (currentTime - DataManager.lastMoodTimestamp) > 3600000
        val effectiveMood = if (isMoodExpired) null else DataManager.dailyMoods[today]

        lifecycleScope.launch {
            DataManager.updateWorkspaceAgenda(this@MainActivity)
            val agenda = DataManager.getTodayAgendaNotifications()
            val totalDeadlines = agenda.values.sumOf { it.size }
            
            if (totalDeadlines > 0 && DataManager.lastSummaryNotificationDate != today) {
                ReminderReceiver.showSummaryNotification(this@MainActivity, totalDeadlines)
                DataManager.lastSummaryNotificationDate = today
                DataManager.saveData(this@MainActivity)
            }
            
            // Re-trigger state update to ensure agenda is reflected in UI
            updateDashboardState(effectiveMood)
        }

        updateDashboardState(effectiveMood)
    }

    private fun updateDashboardState(effectiveMood: String?) {
        val nextMilestone = DataManager.projects
            .filter { it.category == "Project" }
            .flatMap { it.subFeatures }
            .filter { !it.isCompleted && it.dueDate != null }
            .minByOrNull { it.dueDate!! }
            ?.let { "${it.name} due ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(java.util.Date(it.dueDate!!))}" }
            ?: "No upcoming milestones"

        dashboardState = DashboardState(
            userName = DataManager.userName,
            overallProgress = DataManager.getTotalDailyProgress(),
            habitProgress = DataManager.getHabitProgress(),
            workoutProgress = DataManager.getWorkoutProgress(),
            dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
            safeSpendAmount = DataManager.monthlyBudget - DataManager.getCurrentMonthExpenditure(),
            nextMilestone = nextMilestone,
            recentActions = DataManager.recentActivities.toList(),
            growthAdvice = DataManager.getGrowthAdvice(effectiveMood),
            managementAdvice = DataManager.getManagementAdvice(effectiveMood),
            todayAgenda = DataManager.getTodayAgendaNotifications(),
            currentMood = effectiveMood,
            habitColor = DataManager.globalHabitColor,
            workoutColor = DataManager.globalWorkoutColor,
            taskColor = DataManager.globalTaskColor,
            noteColor = DataManager.globalNoteColor,
            projectColor = DataManager.globalProjectColor,
            financeColor = DataManager.globalFinanceColor,
            habitIcon = DataManager.globalHabitIcon,
            workoutIcon = DataManager.globalWorkoutIcon,
            taskIcon = DataManager.globalTaskIcon,
            noteIcon = DataManager.globalNoteIcon,
            projectIcon = DataManager.globalProjectIcon,
            financeIcon = DataManager.globalFinanceIcon,
            userAvatarRes = DataManager.userAvatarRes,
            userProfileImageUri = DataManager.userProfileImageUri,
            homeFocusSize = DataManager.homeFocusSize,
            homeDisplaySize = DataManager.homeDisplaySize,
            appThemeMode = DataManager.appThemeMode,
            appAccentColor = DataManager.appAccentColor,
            appFontFamily = DataManager.appFontFamily,
            appBorderRadius = DataManager.appBorderRadius,
            appCardStyle = DataManager.appCardStyle,
            appShowShadows = DataManager.appShowShadows,
            globalDisplaySize = DataManager.displaySize,
            fontSize = DataManager.fontSize,
            isSystemAppearanceEnabled = DataManager.isSystemAppearanceEnabled,
            showHabitSection = DataManager.showHabitSection,
            showWorkoutSection = DataManager.showWorkoutSection,
            showTaskSection = DataManager.showTaskSection,
            showNoteSection = DataManager.showNoteSection,
            showProjectSection = DataManager.showProjectSection,
            showFinanceSection = DataManager.showFinanceSection,
            isAppUnlocked = DataManager.isAppUnlocked,
            isDataLoaded = true
        )
    }

    private fun updateSectionColor(section: String, color: Int) {
        when(section) {
            "HABIT" -> DataManager.globalHabitColor = color
            "WORKOUT" -> DataManager.globalWorkoutColor = color
            "TASK" -> DataManager.globalTaskColor = color
            "NOTE" -> DataManager.globalNoteColor = color
            "PROJECT" -> DataManager.globalProjectColor = color
            "FINANCE" -> DataManager.globalFinanceColor = color
        }
        DataManager.saveData(this)
        refreshState()
    }

    private fun quickAddTask() {
        val intent = Intent(this, TaskActivity::class.java).apply { putExtra("QUICK_ADD", true) }
        startActivity(intent)
    }

    private fun quickAddExpense() {
        val intent = Intent(this, FinanceActivity::class.java).apply { putExtra("QUICK_ADD", true) }
        startActivity(intent)
    }

    private fun quickAddNote() {
        val intent = Intent(this, NotesActivity::class.java).apply { putExtra("QUICK_ADD", true) }
        startActivity(intent)
    }

    private fun performUniversalSearch(query: String) {
        val results = mutableListOf<SearchResult>()
        
        // Tasks
        DataManager.tasks.filter { it.name.contains(query, true) }.forEach { 
            results.add(SearchResult("TASK", it.name, it.category ?: "General")) 
        }
        
        // Notes
        DataManager.notes.filter { it.title.contains(query, true) || it.content.contains(query, true) }.forEach {
            results.add(SearchResult("NOTE", it.title, it.content.take(50)))
        }

        // Projects
        DataManager.projects.filter { it.title.contains(query, true) || it.content.contains(query, true) }.forEach {
            results.add(SearchResult(if (it.category == "Project") "PROJECT" else "IDEA", it.title, it.content.take(50)))
        }

        showSearchResultsDialog(query, results)
    }

    private fun showSearchResultsDialog(query: String, results: List<SearchResult>) {
        val dialog = android.app.Dialog(this, R.style.SeamlessDialog)
        dialog.setContentView(R.layout.dialog_search_results)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvHeader = dialog.findViewById<android.widget.TextView>(R.id.tv_search_title)
        tvHeader.text = "RESULTS FOR '$query'"
        
        val rvResults = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_search_results)
        rvResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvResults.adapter = SearchResultsAdapter(results) { dialog.dismiss() }

        dialog.findViewById<android.view.View>(R.id.btn_close_search).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    data class SearchResult(val type: String, val title: String, val subtitle: String)

    override fun onResume() {
        super.onResume()
        DataManager.loadData(this)
        refreshState()
    }

    inner class SearchResultsAdapter(private val items: List<SearchResult>, private val onSelect: () -> Unit) : 
        androidx.recyclerview.widget.RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.title.setTextColor(android.graphics.Color.WHITE)
            holder.subtitle.text = "[${item.type}] ${item.subtitle}"
            holder.subtitle.setTextColor(android.graphics.Color.GRAY)
            holder.itemView.setOnClickListener {
                onSelect()
                when(item.type) {
                    "TASK" -> startActivity(Intent(this@MainActivity, TaskActivity::class.java))
                    "NOTE" -> startActivity(Intent(this@MainActivity, NotesActivity::class.java))
                    "PROJECT", "IDEA" -> startActivity(Intent(this@MainActivity, ProjectActivity::class.java))
                }
            }
        }

        override fun getItemCount() = items.size
        inner class ViewHolder(v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
            val title: android.widget.TextView = v.findViewById(android.R.id.text1)
            val subtitle: android.widget.TextView = v.findViewById(android.R.id.text2)
        }
    }
}
