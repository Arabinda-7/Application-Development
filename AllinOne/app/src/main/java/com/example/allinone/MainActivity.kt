package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

class MainActivity : BaseActivity() {

    private var dashboardState by mutableStateOf(DashboardState())
    private var isAppUnlocked by mutableStateOf(false)

    private val lockLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            isAppUnlocked = true
            handleInitializationComplete()
        } else {
            finish() // Exit if authentication fails or is cancelled
        }
    }

    // Direct Access Dialogs for Speed Dial
    private fun quickAddTask() {
        val intent = Intent(this, TaskActivity::class.java).apply {
            putExtra("SHOW_ADD_DIALOG", true)
        }
        startActivity(intent)
    }

    private fun quickAddExpense() {
        val intent = Intent(this, FinanceActivity::class.java).apply {
            putExtra("SHOW_ADD_DIALOG", true)
        }
        startActivity(intent)
    }

    private fun quickAddNote() {
        val intent = Intent(this, NotesActivity::class.java).apply {
            putExtra("SHOW_ADD_DIALOG", true)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Background Data Loading
        lifecycleScope.launch {
            DataManager.loadData(this@MainActivity)
            
            // Check Lock Requirement
            if (DataManager.isAppLockEnabled && DataManager.appLockPin != null) {
                val intent = Intent(this@MainActivity, LockActivity::class.java).apply {
                    putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_AUTH)
                }
                lockLauncher.launch(intent)
            } else {
                isAppUnlocked = true
                handleInitializationComplete()
            }
        }

        setContent {
            when {
                !dashboardState.isDataLoaded -> {
                    // Show pure black splash while loading
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
                !isAppUnlocked -> {
                    // Stay black while waiting for Biometric Prompt
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
                else -> {
                    val customDensity = remember(dashboardState.homeDisplaySize, dashboardState.fontSize) {
                        val currentDensity = this.resources.displayMetrics.density
                        val currentFontScale = this.resources.configuration.fontScale
                        
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

                    CompositionLocalProvider(LocalDensity provides customDensity) {
                        HomeScreen(
                            state = dashboardState,
                            onNavigateToHabits = { startActivity(Intent(this, HabitTrackerActivity::class.java)) },
                            onNavigateToWorkout = { startActivity(Intent(this, WorkoutRoutineActivity::class.java)) },
                            onNavigateToTodos = { startActivity(Intent(this, TaskActivity::class.java)) },
                            onNavigateToNotes = { startActivity(Intent(this, NotesActivity::class.java)) },
                            onNavigateToProjects = { startActivity(Intent(this, ProjectActivity::class.java)) },
                            onNavigateToFinance = { startActivity(Intent(this, FinanceActivity::class.java)) },
                            onNavigateToSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                            onNavigateToProfile = { startActivity(Intent(this, ProfileActivity::class.java)) },
                            onNavigateToPerformanceHistory = { startActivity(Intent(this, PerformanceHistoryActivity::class.java)) },
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
                                DataManager.saveData(this)
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

    private fun handleInitializationComplete() {
        if (!DataManager.isOnboardingCompleted) {
            startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
            finish()
            return
        }
        
        refreshState()
    }

    private fun performUniversalSearch(query: String) {
        val results = mutableListOf<SearchResult>()
        val lowQuery = query.lowercase()

        // 1. Search Habits
        DataManager.habits.filter { it.name.lowercase().contains(lowQuery) }.forEach { habit ->
            results.add(SearchResult(habit.name, "HABIT TRACKER", R.drawable.ic_habit_tracker) {
                startActivity(Intent(this, HabitTrackerActivity::class.java))
            })
        }

        // 2. Search Tasks
        DataManager.tasks.filter { it.name.lowercase().contains(lowQuery) }.forEach { task ->
            results.add(SearchResult(task.name, "TO-DO LIST", R.drawable.ic_task) {
                startActivity(Intent(this, TaskActivity::class.java))
            })
        }

        // 3. Search Notes / Projects
        DataManager.notes.filter { it.title.lowercase().contains(lowQuery) || it.content.lowercase().contains(lowQuery) }.forEach { note ->
            val section = if (note.category == "Project") "PROJECT BOARDS" else "IDEA BANK"
            val icon = if (note.category == "Project") R.drawable.ic_project else R.drawable.ic_notes
            results.add(SearchResult(note.title, section, icon) {
                val intent = if (note.category == "Project") Intent(this, ProjectActivity::class.java) else Intent(this, NotesActivity::class.java)
                startActivity(intent)
            })
        }

        // 4. Search Finance
        DataManager.transactions.filter { it.title.lowercase().contains(lowQuery) }.forEach { tx ->
            results.add(SearchResult(tx.title, "FINANCE LOGS", R.drawable.ic_finance) {
                startActivity(Intent(this, FinanceActivity::class.java))
            })
        }

        if (results.isEmpty()) {
            Toast.makeText(this, "No results found for '$query'", Toast.LENGTH_SHORT).show()
        } else {
            showSearchResultsDialog(query, results)
        }
    }

    private fun showSearchResultsDialog(query: String, results: List<SearchResult>) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_results)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_search_title)
        val rvResults = dialog.findViewById<RecyclerView>(R.id.rv_search_results)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_search)

        tvTitle.text = "RESULTS FOR: ${query.uppercase()}"
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = SearchResultsAdapter(results) { dialog.dismiss() }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun refreshState() {
        val today = DataManager.getTrackingDateString()
        
        // Virtual Mood Expiration (1 Hour Window)
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
            homeDisplaySize = DataManager.homeDisplaySize,
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
        when (section) {
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

    override fun onResume() {
        super.onResume()
        if (dashboardState.isDataLoaded && isAppUnlocked) {
            refreshState()
        }
    }

    // --- Search Adapter ---
    inner class SearchResultsAdapter(
        private val items: List<SearchResult>,
        private val onResultClick: () -> Unit
    ) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.category.text = item.section
            holder.icon.setImageResource(item.iconRes)
            
            holder.itemView.setOnClickListener {
                item.onClick()
                onResultClick()
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_result_title)
            val category: TextView = view.findViewById(R.id.tv_result_category)
            val icon: ImageView = view.findViewById(R.id.iv_result_icon)
        }
    }
}
