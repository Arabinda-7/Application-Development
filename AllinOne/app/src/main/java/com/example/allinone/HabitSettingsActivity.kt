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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_header)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "HABIT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.habitDefaultTab}") {
            val tabs = listOf("TODAY", "WEEK", "ALL")
            DataManager.habitDefaultTab = tabs[(tabs.indexOf(DataManager.habitDefaultTab) + 1) % tabs.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Sort Order", "Current: ${DataManager.habitSortOrder}") {
            val orders = listOf("Time", "Streak")
            DataManager.habitSortOrder = orders[(orders.indexOf(DataManager.habitSortOrder).coerceAtLeast(0) + 1) % orders.size]
            loadSettings()
        })
        
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
        
        settings.add(ConfigItem("Day Reset Hour", "Current: ${formatHour(DataManager.habitDayResetHour)}") {
            DataManager.habitDayResetHour = (DataManager.habitDayResetHour + 1) % 24
            loadSettings()
        })
        
        settings.add(ConfigItem("Grace Period", "Allowed misses: ${DataManager.habitGraceDaysAllowed} days") {
            val options = listOf(0, 1, 2, 3)
            DataManager.habitGraceDaysAllowed = options[(options.indexOf(DataManager.habitGraceDaysAllowed) + 1) % options.size]
            loadSettings()
        })

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