package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HabitSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_habit)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "HABIT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Display Preferences", isHeader = true))
        settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.habitDefaultTab}", options = listOf("TODAY", "WEEK", "ALL"), selectedIndex = listOf("TODAY", "WEEK", "ALL").indexOf(DataManager.habitDefaultTab), onOptionSelected = { index ->
            val tabs = listOf("TODAY", "WEEK", "ALL")
            DataManager.habitDefaultTab = tabs[index]
            loadSettings()
        }))
        
        settings.add(ConfigItem("Sort Order", "Current: ${DataManager.habitSortOrder}", options = listOf("Time", "Streak"), selectedIndex = listOf("Time", "Streak").indexOf(DataManager.habitSortOrder), onOptionSelected = { index ->
            val orders = listOf("Time", "Streak")
            DataManager.habitSortOrder = orders[index]
            loadSettings()
        }))
        
        settings.add(ConfigItem("Interaction", isHeader = true))
        settings.add(ConfigItem("Vacation Mode", "Freeze streaks during breaks", isToggle = true, isChecked = DataManager.habitVacationMode) {
            DataManager.habitVacationMode = !DataManager.habitVacationMode
        })
        
        settings.add(ConfigItem("Completion Sound", "Play sound on habit finished", isToggle = true, isChecked = DataManager.habitCompletionSound) {
            DataManager.habitCompletionSound = !DataManager.habitCompletionSound
        })
        
        settings.add(ConfigItem("Haptic Feedback", "Vibrate on habit finished", isToggle = true, isChecked = DataManager.habitCompletionHaptics) {
            DataManager.habitCompletionHaptics = !DataManager.habitCompletionHaptics
        })
        
        settings.add(ConfigItem("Bulk Action Mode", "Fast multi-update mode", isToggle = true, isChecked = DataManager.habitBulkMode) {
            DataManager.habitBulkMode = !DataManager.habitBulkMode
        })
        
        settings.add(ConfigItem("System Rules", isHeader = true))
        val hourOptions = (0..23).map { formatHour(it) }
        settings.add(ConfigItem("Day Reset Hour", "Current: ${formatHour(DataManager.habitDayResetHour)}", options = hourOptions, selectedIndex = DataManager.habitDayResetHour, onOptionSelected = { index ->
            DataManager.habitDayResetHour = index
            loadSettings()
        }))
        
        settings.add(ConfigItem("Grace Period", "Allowed misses: ${DataManager.habitGraceDaysAllowed} days", options = listOf("0", "1", "2", "3"), selectedIndex = listOf(0, 1, 2, 3).indexOf(DataManager.habitGraceDaysAllowed), onOptionSelected = { index ->
            val options = listOf(0, 1, 2, 3)
            DataManager.habitGraceDaysAllowed = options[index]
            loadSettings()
        }))

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00 AM"
            hour < 12 -> "$hour:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }
}