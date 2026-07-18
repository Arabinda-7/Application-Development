package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskSettingsActivity : BaseActivity() {

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
        
        tvTitle.text = "TASK SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Manage Categories", "Customize your task tags") { 
            showUpcomingFeatureDialog("Manage Categories")
        })
        
        settings.add(ConfigItem("Sort Order", "Current: ${DataManager.taskSortOrder}") {
            val orders = listOf("Priority", "Newest", "Alphabetical")
            DataManager.taskSortOrder = orders[(orders.indexOf(DataManager.taskSortOrder) + 1) % orders.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Default Section", "Current: ${DataManager.taskDefaultSection}") {
            val sections = listOf("Tasks", "List")
            DataManager.taskDefaultSection = sections[(sections.indexOf(DataManager.taskDefaultSection) + 1) % sections.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Auto-Archive Tasks", "Cleanup old completed items", isToggle = true, isChecked = DataManager.taskAutoArchive) {
            DataManager.taskAutoArchive = !DataManager.taskAutoArchive
        })
        
        settings.add(ConfigItem("Show Hidden Tasks", "Reveal private roadmap items", isToggle = true, isChecked = DataManager.taskShowHidden) {
            DataManager.taskShowHidden = !DataManager.taskShowHidden
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showUpcomingFeatureDialog(name: String) {
        android.widget.Toast.makeText(this, "$name: Feature in transition", android.widget.Toast.LENGTH_SHORT).show()
    }
}