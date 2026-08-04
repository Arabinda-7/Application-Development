package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.allinone.domain.repository.HabitSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HabitSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private val viewModel: HabitTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_habit)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "HABIT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.habitSettings.collect { settings ->
                    loadSettings(settings)
                }
            }
        }
    }

    private fun loadSettings(currentSettings: HabitSettings) {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Display Preferences", isHeader = true))
        settings.add(ConfigItem("Default Startup Tab", "Current: ${currentSettings.defaultTab}", options = listOf("TODAY", "WEEK", "ALL"), selectedIndex = listOf("TODAY", "WEEK", "ALL").indexOf(currentSettings.defaultTab), onOptionSelected = { index ->
            val tabs = listOf("TODAY", "WEEK", "ALL")
            viewModel.updateSettings(currentSettings.copy(defaultTab = tabs[index]))
        }))
        
        settings.add(ConfigItem("Sort Order", "Current: ${currentSettings.sortOrder}", options = listOf("Time", "Streak"), selectedIndex = listOf("Time", "Streak").indexOf(currentSettings.sortOrder), onOptionSelected = { index ->
            val orders = listOf("Time", "Streak")
            viewModel.updateSettings(currentSettings.copy(sortOrder = orders[index]))
        }))
        
        settings.add(ConfigItem("Interaction", isHeader = true))
        settings.add(ConfigItem("Vacation Mode", "Freeze streaks during breaks", isToggle = true, isChecked = currentSettings.vacationMode) {
            viewModel.updateSettings(currentSettings.copy(vacationMode = !currentSettings.vacationMode))
        })
        
        settings.add(ConfigItem("Completion Sound", "Play sound on habit finished", isToggle = true, isChecked = currentSettings.completionSound) {
            viewModel.updateSettings(currentSettings.copy(completionSound = !currentSettings.completionSound))
        })
        
        settings.add(ConfigItem("Haptic Feedback", "Vibrate on habit finished", isToggle = true, isChecked = currentSettings.completionHaptics) {
            viewModel.updateSettings(currentSettings.copy(completionHaptics = !currentSettings.completionHaptics))
        })
        
        settings.add(ConfigItem("Bulk Action Mode", "Fast multi-update mode", isToggle = true, isChecked = currentSettings.bulkMode) {
            viewModel.updateSettings(currentSettings.copy(bulkMode = !currentSettings.bulkMode))
        })
        
        settings.add(ConfigItem("System Rules", isHeader = true))
        val hourOptions = (0..23).map { formatHour(it) }
        settings.add(ConfigItem("Day Reset Hour", "Current: ${formatHour(currentSettings.dayResetHour)}", options = hourOptions, selectedIndex = currentSettings.dayResetHour, onOptionSelected = { index ->
            viewModel.updateSettings(currentSettings.copy(dayResetHour = index))
        }))
        
        settings.add(ConfigItem("Grace Period", "Allowed misses: ${currentSettings.graceDaysAllowed} days", options = listOf("0", "1", "2", "3"), selectedIndex = listOf(0, 1, 2, 3).indexOf(currentSettings.graceDaysAllowed), onOptionSelected = { index ->
            val options = listOf(0, 1, 2, 3)
            viewModel.updateSettings(currentSettings.copy(graceDaysAllowed = options[index]))
        }))

        settingsList.adapter = ConfigAdapter(settings) { /* No-op, saved immediately via VM */ }
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