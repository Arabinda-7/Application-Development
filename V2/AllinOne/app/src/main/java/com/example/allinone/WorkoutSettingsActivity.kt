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

class WorkoutSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_workout)

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
        
        settings.add(ConfigItem("Configuration", isHeader = true))
        settings.add(ConfigItem("Manage Muscles", "Add or remove body part tags") { 
            showManageMuscleGroupsDialog()
        })

        settings.add(ConfigItem("Primary Filter Type", "Current: ${if (DataManager.workoutFilterType == "TIME") "Time of Day" else "Muscle Groups"}", options = listOf("Time of Day", "Muscle Groups"), selectedIndex = if (DataManager.workoutFilterType == "TIME") 0 else 1, onOptionSelected = { index ->
            DataManager.workoutFilterType = if (index == 0) "TIME" else "MUSCLE"
            loadSettings()
        }))

        settings.add(ConfigItem("Workout Weight Unit", "Current: ${DataManager.workoutWeightUnit}", options = listOf("Kg", "Lb"), selectedIndex = if (DataManager.workoutWeightUnit == "Kg") 0 else 1, onOptionSelected = { index ->
            DataManager.workoutWeightUnit = if (index == 0) "Kg" else "Lb"
            loadSettings()
        }))
        
        settings.add(ConfigItem("Performance", isHeader = true))
        settings.add(ConfigItem("Auto-Rest Timer", "Trigger timer after set", isToggle = true, isChecked = DataManager.workoutAutoRestTimer) {
            DataManager.workoutAutoRestTimer = !DataManager.workoutAutoRestTimer
        })
        
        settings.add(ConfigItem("Default Tracking Mode", "Current: ${DataManager.workoutDefaultMode}", options = listOf("Reps", "Sets", "Timer"), selectedIndex = listOf("Reps", "Sets", "Timer").indexOf(DataManager.workoutDefaultMode), onOptionSelected = { index ->
            val modes = listOf("Reps", "Sets", "Timer")
            DataManager.workoutDefaultMode = modes[index]
            loadSettings()
        }))
        
        settings.add(ConfigItem("Rest Duration", "Current: ${DataManager.workoutRestDuration}s", options = listOf("30s", "60s", "90s", "120s", "180s"), selectedIndex = listOf(30, 60, 90, 120, 180).indexOf(DataManager.workoutRestDuration), onOptionSelected = { index ->
            val durations = listOf(30, 60, 90, 120, 180)
            DataManager.workoutRestDuration = durations[index]
            loadSettings()
        }))

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showManageMuscleGroupsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_workout)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)
        val inputContainer = dialog.findViewById<View>(R.id.container_add_category)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        // Apply theme to dialog components
        accentLine.setBackgroundColor(accentColor)
        (btnAdd.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = 1000f // Circular
        }

        // Backgrounds with dynamic radius
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        
        inputContainer.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#2A2A2A"))
            cornerRadius = radius * 0.6f
        }

        fun refresh() {
            container.removeAllViews()
            DataManager.workoutMuscleGroups.forEach { g ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_workout, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = g
                
                // Apply dynamic radius to item
                iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#2A2A2A"))
                    cornerRadius = radius * 0.5f
                }

                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { 
                    DataManager.workoutMuscleGroups.remove(g)
                    DataManager.saveData(this)
                    refresh() 
                }
                container.addView(iv)
            }
        }
        
        btnAdd.setOnClickListener {
            val n = et.text.toString().trim()
            if (n.isNotEmpty()) { 
                DataManager.workoutMuscleGroups.add(n)
                DataManager.saveData(this)
                et.text.clear()
                refresh() 
            }
        }
        refresh()
        showDialogSafe(dialog)
    }
}