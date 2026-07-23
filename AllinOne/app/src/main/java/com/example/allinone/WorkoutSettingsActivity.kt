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
        
        settings.add(ConfigItem("Configuration", isHeader = true))
        settings.add(ConfigItem("Manage Muscle Groups", "Add or remove body part tags") { 
            showManageMuscleGroupsDialog()
        })

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
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        
        fun refresh() {
            container.removeAllViews()
            DataManager.workoutMuscleGroups.forEach { g ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = g
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { 
                    DataManager.workoutMuscleGroups.remove(g)
                    DataManager.saveData(this)
                    refresh() 
                }
                container.addView(iv)
            }
        }
        
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
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