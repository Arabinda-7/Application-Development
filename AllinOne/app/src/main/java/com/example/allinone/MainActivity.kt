package com.example.allinone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load data from persistent storage
        DataManager.loadData(this)

        val aiChatbot = AIChatbot("YOUR_GEMINI_API_KEY") // Replace with actual key

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
                onNavigateToSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                onSendMessage = { command ->
                    lifecycleScope.launch {
                        val result = aiChatbot.processCommand(command)
                        if (result != null) {
                            Toast.makeText(this@MainActivity, "AI Processed: $result", Toast.LENGTH_LONG).show()
                            // Further logic to parse JSON and update DataManager
                        } else {
                            Toast.makeText(this@MainActivity, "AI failed to process", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
