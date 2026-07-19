package com.example.allinone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private var dashboardState by mutableStateOf(DashboardState())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initial Data Load
        DataManager.loadData(this)
        refreshState()

        setContent {
            val isLoaded = dashboardState.isDataLoaded
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    !isLoaded -> {
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
                                    onNavigateToProfile = { startActivity(Intent(this@MainActivity, ProfileActivity::class.java)) },
                                    onNavigateToPerformanceHistory = { startActivity(Intent(this@MainActivity, PerformanceHistoryActivity::class.java)) },
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

                if (!DataManager.isOnboardingCompleted) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun refreshState() {
        val today = DataManager.getTrackingDateString()
        val currentTime = System.currentTimeMillis()
        val isMoodExpired = DataManager.lastMoodTimestamp != 0L && (currentTime - DataManager.lastMoodTimestamp) > 3600000
        val effectiveMood = if (isMoodExpired) null else DataManager.dailyMoods[today]

        val nextMilestone = DataManager.notes
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
            recentActions = DataManager.recentActivities,
            growthAdvice = DataManager.getGrowthAdvice(effectiveMood),
            managementAdvice = DataManager.getManagementAdvice(effectiveMood),
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
            showHabitSection = DataManager.showHabitSection,
            showWorkoutSection = DataManager.showWorkoutSection,
            showTaskSection = DataManager.showTaskSection,
            showNoteSection = DataManager.showNoteSection,
            showProjectSection = DataManager.showProjectSection,
            showFinanceSection = DataManager.showFinanceSection,
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
        
        // Notes/Projects
        DataManager.notes.filter { it.title.contains(query, true) || it.content.contains(query, true) }.forEach {
            results.add(SearchResult(if (it.category == "Project") "PROJECT" else "NOTE", it.title, it.content.take(50)))
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
                    "PROJECT" -> startActivity(Intent(this@MainActivity, ProjectActivity::class.java))
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
