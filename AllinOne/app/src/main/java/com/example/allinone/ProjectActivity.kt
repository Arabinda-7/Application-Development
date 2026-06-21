package com.example.allinone

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup

class ProjectActivity : AppCompatActivity() {

    private val projects = DataManager.projects
    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        val projectList = findViewById<RecyclerView>(R.id.project_list)
        projectList.layoutManager = LinearLayoutManager(this)
        projectAdapter = ProjectAdapter(projects) {
            DataManager.saveData(this)
        }
        projectList.adapter = projectAdapter

        val filterChips = findViewById<ChipGroup>(R.id.project_filter_chips)
        filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chip_working_projects -> "WORKING"
                R.id.chip_upcoming_projects -> "UPCOMING"
                else -> "ALL"
            }
            projectAdapter.filter(filter)
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_create_new_project).setOnClickListener {
            showAddProjectDialog(null)
        }
    }

    fun showAddProjectDialog(existingProject: Project? = null) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_project)

        val nameInput = dialog.findViewById<EditText>(R.id.project_name_input)
        val descInput = dialog.findViewById<EditText>(R.id.project_desc_input)
        val statusGroup = dialog.findViewById<RadioGroup>(R.id.status_radio_group)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_project)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_project)

        if (existingProject != null) {
            nameInput.setText(existingProject.name)
            descInput.setText(existingProject.description)
            if (existingProject.status == "Working") {
                statusGroup.check(R.id.radio_working)
            } else {
                statusGroup.check(R.id.radio_upcoming)
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString()
            if (name.isNotEmpty()) {
                val status = if (statusGroup.checkedRadioButtonId == R.id.radio_working) "Working" else "Upcoming"
                val desc = descInput.text.toString()

                if (existingProject == null) {
                    projects.add(Project(name, desc, status))
                } else {
                    existingProject.name = name
                    existingProject.description = desc
                    existingProject.status = status
                }

                projectAdapter.filter("ALL") // Reset filter to show the new/edited item
                DataManager.saveData(this)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
