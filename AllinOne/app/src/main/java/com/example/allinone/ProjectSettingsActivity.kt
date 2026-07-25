package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProjectSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_project)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "PROJECT SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Structure", isHeader = true))
        settings.add(ConfigItem("Roadmaps Section", "Show/Hide Project Roadmaps", isToggle = true, isChecked = DataManager.projectRoadmapsEnabled) {
            if (DataManager.projectRoadmapsEnabled && !DataManager.projectIdeasEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
                loadSettings()
            } else {
                DataManager.projectRoadmapsEnabled = !DataManager.projectRoadmapsEnabled
            }
        })
        
        settings.add(ConfigItem("Ideas Section", "Show/Hide Idea brainstorming", isToggle = true, isChecked = DataManager.projectIdeasEnabled) {
            if (DataManager.projectIdeasEnabled && !DataManager.projectRoadmapsEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
                loadSettings()
            } else {
                DataManager.projectIdeasEnabled = !DataManager.projectIdeasEnabled
            }
        })

        settings.add(ConfigItem("Manage Templates", "Edit roadmap pre-sets") { 
            showManageTemplatesDialog()
        })
        
        settings.add(ConfigItem("Automation", isHeader = true))
        settings.add(ConfigItem("Auto-Save Ideas", "Save content as you type", isToggle = true, isChecked = DataManager.projectAutoSaveIdeas) {
            DataManager.projectAutoSaveIdeas = !DataManager.projectAutoSaveIdeas
        })

        settings.add(ConfigItem("Auto Archive", "Hide completed projects", isToggle = true, isChecked = DataManager.projectAutoArchive) {
            DataManager.projectAutoArchive = !DataManager.projectAutoArchive
        })

        settings.add(ConfigItem("Synergy Sync", "Unified task & project tracking", isToggle = true, isChecked = DataManager.projectSynergySync) {
            DataManager.projectSynergySync = !DataManager.projectSynergySync
        })

        settings.add(ConfigItem("Performance", isHeader = true))
        settings.add(ConfigItem("Deadline Notifications", "Alerts for upcoming milestones", isToggle = true, isChecked = DataManager.projectDeadlineAlerts) {
            DataManager.projectDeadlineAlerts = !DataManager.projectDeadlineAlerts
        })
        
        settings.add(ConfigItem("Productivity Analytics", "Track completion velocity", isToggle = true, isChecked = DataManager.projectAnalyticsEnabled) {
            DataManager.projectAnalyticsEnabled = !DataManager.projectAnalyticsEnabled
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showManageTemplatesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_project)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val btnDeleteMode = dialog.findViewById<ImageButton>(R.id.btn_toggle_delete_mode)

        title.text = "Project Templates"
        etNew.hint = "Template Name..."

        var isDeleteMode = false

        fun refresh() {
            container.removeAllViews()
            btnDeleteMode?.imageTintList = android.content.res.ColorStateList.valueOf(if (isDeleteMode) Color.RED else Color.WHITE)

            DataManager.projectTemplates.keys.forEach { templateName ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage_project, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = templateName

                val btnRemove = itemView.findViewById<View>(R.id.btn_remove_category)
                btnRemove.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    if (isDeleteMode) {
                        isDeleteMode = false
                        refresh()
                    } else {
                        Toast.makeText(this, "Steps: ${DataManager.projectTemplates[templateName]?.joinToString(", ")}", Toast.LENGTH_LONG).show()
                    }
                }

                btnRemove.setOnClickListener {
                    if (DataManager.projectTemplates.size > 1) {
                        DataManager.projectTemplates.remove(templateName)
                        DataManager.saveData(this)
                        refresh()
                    } else {
                        Toast.makeText(this, "At least one template required", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(itemView)
            }
        }

        btnDeleteMode.setOnClickListener {
            isDeleteMode = !isDeleteMode
            refresh()
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.projectTemplates.containsKey(name)) {
                showCreateTemplateStepsDialog(name) {
                    refresh()
                    etNew.text.clear()
                }
            }
        }

        refresh()
        showDialogSafe(dialog)
    }


    private fun showCreateTemplateStepsDialog(templateName: String, onComplete: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_project)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etStep = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAddStep = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)

        val btnSave = TextView(this).apply {
            text = "SAVE TEMPLATE"
            setTextColor(Color.parseColor("#1A73E8"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 40, 0, 40)
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        title.text = "Add Steps for: $templateName"
        etStep.hint = "Step name (e.g. Design)..."

        val steps = mutableListOf<String>()

        fun refreshSteps() {
            container.removeAllViews()
            steps.forEach { step ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage_project, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = step
                itemView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
                    steps.remove(step)
                    refreshSteps()
                }
                container.addView(itemView)
            }
            container.addView(btnSave)
        }

        btnAddStep.setOnClickListener {
            val stepName = etStep.text.toString().trim()
            if (stepName.isNotEmpty()) {
                steps.add(stepName)
                etStep.text.clear()
                refreshSteps()
            }
        }

        btnSave.setOnClickListener {
            if (steps.isNotEmpty()) {
                DataManager.projectTemplates[templateName] = steps
                DataManager.saveData(this)
                onComplete()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Add at least one step", Toast.LENGTH_SHORT).show()
            }
        }

        refreshSteps()
        showDialogSafe(dialog)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}