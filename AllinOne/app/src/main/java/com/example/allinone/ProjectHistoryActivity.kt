package com.example.allinone

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProjectHistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_history)

        val projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        if (projectIndex == -1 || projectIndex >= DataManager.projects.size) {
            finish()
            return
        }

        val project = DataManager.projects[projectIndex]
        setupUI(project)
        setupKeyboardHandling(findViewById(R.id.project_history_root))
    }

    private fun setupUI(project: Note) {
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        val tvProjectName = findViewById<TextView>(R.id.tv_project_name)
        tvProjectName.text = project.title

        val projectColor = if (project.color != -1) project.color else Color.parseColor("#1A73E8")
        findViewById<View>(R.id.history_aura_background).backgroundTintList = ColorStateList.valueOf(projectColor)

        // Populate Stats
        findViewById<TextView>(R.id.tv_stat_progress).text = "${project.progress}%"
        val completedFeatures = project.subFeatures.count { it.isCompleted }
        findViewById<TextView>(R.id.tv_stat_features).text = "$completedFeatures/${project.subFeatures.size}"
        findViewById<TextView>(R.id.tv_stat_actions).text = project.changeHistory.size.toString()

        // Setup History List
        val recyclerView = findViewById<RecyclerView>(R.id.history_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProjectHistoryAdapter(project.changeHistory.reversed())
    }
}
