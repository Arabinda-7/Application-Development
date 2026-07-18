package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NoteSettingsActivity : BaseActivity() {

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
        
        tvTitle.text = "NOTE SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Custom Templates", "Edit note pre-fill text") { 
            showUpcomingFeatureDialog("Custom Templates")
        })
        
        settings.add(ConfigItem("Bulk Category Move", "Move all notes at once") { 
            showUpcomingFeatureDialog("Bulk Move")
        })
        
        settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.noteDefaultCategory}") {
            val cats = DataManager.noteVisibleSections
            DataManager.noteDefaultCategory = cats[(cats.indexOf(DataManager.noteDefaultCategory) + 1) % cats.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Show Hidden Notes", "Reveal your private logs", isToggle = true, isChecked = DataManager.noteShowHidden) {
            DataManager.noteShowHidden = !DataManager.noteShowHidden
        })
        
        settings.add(ConfigItem("Auto-Cleanup", "Days: ${if (DataManager.noteAutoCleanupDays > 0) DataManager.noteAutoCleanupDays else "Off"}") {
            val options = listOf(0, 7, 30, 90)
            DataManager.noteAutoCleanupDays = options[(options.indexOf(DataManager.noteAutoCleanupDays) + 1) % options.size]
            loadSettings()
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showUpcomingFeatureDialog(name: String) {
        android.widget.Toast.makeText(this, "$name: Feature in transition", android.widget.Toast.LENGTH_SHORT).show()
    }
}