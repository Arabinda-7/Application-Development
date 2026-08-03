package com.example.allinone

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import com.example.allinone.domain.repository.WorkoutSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private val viewModel: WorkoutViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_workout)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "WORKOUT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workoutSettings.collect { settings ->
                    loadSettings(settings)
                }
            }
        }
    }

    private fun loadSettings(currentSettings: WorkoutSettings) {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Configuration", isHeader = true))
        settings.add(ConfigItem("Manage Muscles", "Add or remove body part tags") { 
            showManageMuscleGroupsDialog(currentSettings)
        })

        settings.add(ConfigItem("Primary Filter Type", "Current: ${if (currentSettings.filterType == "TIME") "Time of Day" else "Muscle Groups"}", options = listOf("Time of Day", "Muscle Groups"), selectedIndex = if (currentSettings.filterType == "TIME") 0 else 1, onOptionSelected = { index ->
            viewModel.updateSettings(currentSettings.copy(filterType = if (index == 0) "TIME" else "MUSCLE"))
        }))

        settings.add(ConfigItem("Workout Weight Unit", "Current: ${currentSettings.weightUnit}", options = listOf("Kg", "Lb"), selectedIndex = if (currentSettings.weightUnit == "Kg") 0 else 1, onOptionSelected = { index ->
            viewModel.updateSettings(currentSettings.copy(weightUnit = if (index == 0) "Kg" else "Lb"))
        }))
        
        settings.add(ConfigItem("Performance", isHeader = true))
        settings.add(ConfigItem("Auto-Rest Timer", "Trigger timer after set", isToggle = true, isChecked = currentSettings.autoRestTimer) {
            viewModel.updateSettings(currentSettings.copy(autoRestTimer = !currentSettings.autoRestTimer))
        })
        
        settings.add(ConfigItem("Default Tracking Mode", "Current: ${currentSettings.defaultMode}", options = listOf("Reps", "Sets", "Timer"), selectedIndex = listOf("Reps", "Sets", "Timer").indexOf(currentSettings.defaultMode), onOptionSelected = { index ->
            val modes = listOf("Reps", "Sets", "Timer")
            viewModel.updateSettings(currentSettings.copy(defaultMode = modes[index]))
        }))
        
        settings.add(ConfigItem("Rest Duration", "Current: ${currentSettings.restDuration}s", options = listOf("30s", "60s", "90s", "120s", "180s"), selectedIndex = listOf(30, 60, 90, 120, 180).indexOf(currentSettings.restDuration), onOptionSelected = { index ->
            val durations = listOf(30, 60, 90, 120, 180)
            viewModel.updateSettings(currentSettings.copy(restDuration = durations[index]))
        }))

        settingsList.adapter = ConfigAdapter(settings) { /* No-op */ }
    }

    private fun showManageMuscleGroupsDialog(currentSettings: WorkoutSettings) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_workout)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)
        val inputContainer = dialog.findViewById<View>(R.id.container_add_category)

        val appSettings = settingsViewModel.settings.value
        val accentColor = if (appSettings.appAccentColor != -1) appSettings.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = appSettings.appBorderRadius.toFloat() * resources.displayMetrics.density

        // Apply theme to dialog components
        accentLine.setBackgroundColor(accentColor)
        (btnAdd.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = 1000f // Circular
        }

        // Backgrounds with dynamic radius
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (appSettings.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        
        inputContainer.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#2A2A2A"))
            cornerRadius = radius * 0.6f
        }

        fun refresh(muscles: List<String>) {
            container.removeAllViews()
            muscles.forEach { g ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_workout, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = g
                
                // Apply dynamic radius to item
                iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#2A2A2A"))
                    cornerRadius = radius * 0.5f
                }

                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { 
                    val newList = muscles.toMutableList().apply { remove(g) }
                    viewModel.updateSettings(currentSettings.copy(muscleGroups = newList))
                    refresh(newList)
                }
                container.addView(iv)
            }
        }
        
        btnAdd.setOnClickListener {
            val n = et.text.toString().trim()
            if (n.isNotEmpty()) { 
                val newList = currentSettings.muscleGroups.toMutableList().apply { add(n) }
                viewModel.updateSettings(currentSettings.copy(muscleGroups = newList))
                et.text.clear()
                refresh(newList) 
            }
        }
        refresh(currentSettings.muscleGroups)
        showDialogSafe(dialog)
    }
}