package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var dashboardState by mutableStateOf(DashboardState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Background Data Loading
        lifecycleScope.launch {
            DataManager.loadData(this@MainActivity)
            refreshState()
        }

        setContent {
            HomeScreen(
                state = dashboardState,
                onNavigateToHabits = { startActivity(Intent(this, HabitTrackerActivity::class.java)) },
                onNavigateToWorkout = { startActivity(Intent(this, WorkoutRoutineActivity::class.java)) },
                onNavigateToTodos = { startActivity(Intent(this, ToDoListActivity::class.java)) },
                onNavigateToNotes = { startActivity(Intent(this, NotesActivity::class.java)) },
                onNavigateToProjects = { startActivity(Intent(this, ProjectActivity::class.java)) },
                onNavigateToFinance = { startActivity(Intent(this, FinanceActivity::class.java)) },
                onNavigateToSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                onColorSelected = { section, color ->
                    updateSectionColor(section, color)
                }
            )
        }
    }

    private fun refreshState() {
        dashboardState = DashboardState(
            habitProgress = DataManager.getHabitProgress(),
            workoutProgress = DataManager.getWorkoutProgress(),
            dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()),
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
            financeIcon = DataManager.globalFinanceIcon
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
        refreshState()
    }
}
