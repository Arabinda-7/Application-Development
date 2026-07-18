package com.example.allinone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProjectSettingsActivity : BaseActivity() {

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
        
        tvTitle.text = "PROJECT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Manage Templates", "Edit roadmap pre-sets") { 
            showUpcomingFeatureDialog("Manage Templates")
        })
        
        settings.add(ConfigItem("Manage Tags", "Customize UI, Logic, Bug tags") { 
            showUpcomingFeatureDialog("Manage Tags")
        })
        
        settings.add(ConfigItem("Roadmaps Section", "Show/Hide Project Roadmaps", isToggle = true, isChecked = DataManager.projectRoadmapsEnabled) {
            if (DataManager.projectRoadmapsEnabled && !DataManager.projectIdeasEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
                // The toggle logic in ConfigAdapter will revert it anyway if we don't handle it carefully, 
                // but let's just use simple toggle for now and fix if it desyncs.
            } else {
                DataManager.projectRoadmapsEnabled = !DataManager.projectRoadmapsEnabled
            }
        })
        
        settings.add(ConfigItem("Ideas Section", "Show/Hide Idea brainstorming", isToggle = true, isChecked = DataManager.projectIdeasEnabled) {
            if (DataManager.projectIdeasEnabled && !DataManager.projectRoadmapsEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
            } else {
                DataManager.projectIdeasEnabled = !DataManager.projectIdeasEnabled
            }
        })
        
        settings.add(ConfigItem("Dual Exist", "Show projects in both tabs", isToggle = true, isChecked = DataManager.projectDualExistEnabled) {
            DataManager.projectDualExistEnabled = !DataManager.projectDualExistEnabled
        })
        
        settings.add(ConfigItem("Deadline Notifications", "Alerts for upcoming milestones", isToggle = true, isChecked = DataManager.projectDeadlineAlerts) {
            DataManager.projectDeadlineAlerts = !DataManager.projectDeadlineAlerts
        })
        
        settings.add(ConfigItem("Productivity Analytics", "Track completion velocity", isToggle = true, isChecked = DataManager.projectAnalyticsEnabled) {
            DataManager.projectAnalyticsEnabled = !DataManager.projectAnalyticsEnabled
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showUpcomingFeatureDialog(name: String) {
        Toast.makeText(this, "$name: Feature in transition", Toast.LENGTH_SHORT).show()
    }
}