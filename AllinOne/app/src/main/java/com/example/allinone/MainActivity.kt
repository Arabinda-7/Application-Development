package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Background Data Loading to prevent ANR
        lifecycleScope.launch {
            DataManager.loadData(this@MainActivity)
        }

        setContent {
            val state = DashboardState(
                habitProgress = DataManager.getHabitProgress(),
                workoutProgress = DataManager.getWorkoutProgress(),
                dateString = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
            )

            HomeScreen(
                state = state,
                onNavigateToHabits = { startActivity(Intent(this, HabitTrackerActivity::class.java)) },
                onNavigateToWorkout = { startActivity(Intent(this, WorkoutRoutineActivity::class.java)) },
                onNavigateToTodos = { startActivity(Intent(this, ToDoListActivity::class.java)) },
                onNavigateToNotes = { startActivity(Intent(this, NotesActivity::class.java)) },
                onNavigateToProjects = { startActivity(Intent(this, ProjectActivity::class.java)) },
                onNavigateToFinance = { startActivity(Intent(this, FinanceActivity::class.java)) },
                onNavigateToSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Content will refresh if we used a ViewModel with StateFlow, 
        // for now setContent is called once. 
        // In a real app, we'd use a ViewModel.
    }
}
