package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "WORKOUT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Manage Muscle Groups", "Add or remove body part tags") { 
            // Reuse the existing dialog logic if possible, or just Toast for now
            showUpcomingFeatureDialog("Manage Muscle Groups")
        })
        
        settings.add(ConfigItem("Auto-Rest Timer", "Trigger timer after set", isToggle = true, isChecked = DataManager.workoutAutoRestTimer) {
            DataManager.workoutAutoRestTimer = !DataManager.workoutAutoRestTimer
        })
        
        settings.add(ConfigItem("Workout Weight Unit", "Current: ${DataManager.workoutWeightUnit}") {
            DataManager.workoutWeightUnit = if (DataManager.workoutWeightUnit == "Kg") "Lb" else "Kg"
            loadSettings()
        })
        
        settings.add(ConfigItem("Default Tracking Mode", "Current: ${DataManager.workoutDefaultMode}") {
            val modes = listOf("Reps", "Sets", "Timer")
            DataManager.workoutDefaultMode = modes[(modes.indexOf(DataManager.workoutDefaultMode) + 1) % modes.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Rest Duration", "Current: ${DataManager.workoutRestDuration}s") {
            val durations = listOf(30, 60, 90, 120, 180)
            DataManager.workoutRestDuration = durations[(durations.indexOf(DataManager.workoutRestDuration) + 1) % durations.size]
            loadSettings()
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showUpcomingFeatureDialog(name: String) {
        android.widget.Toast.makeText(this, "$name: Feature in transition", android.widget.Toast.LENGTH_SHORT).show()
    }
}