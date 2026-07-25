package com.example.allinone

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NoteSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_note)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "NOTE SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Structure", isHeader = true))
        settings.add(ConfigItem("Manage Sections", "Enable or disable note categories") {
            showManageSectionsDialog()
        })
        
        settings.add(ConfigItem(
            "Default Startup Tab", 
            "Current: ${DataManager.noteDefaultCategory}", 
            options = DataManager.noteVisibleSections,
            selectedIndex = DataManager.noteVisibleSections.indexOf(DataManager.noteDefaultCategory).takeIf { it != -1 },
            onOptionSelected = { index ->
                val selectedTab = DataManager.noteVisibleSections[index]
                DataManager.noteDefaultCategory = selectedTab
                
                // Reorder: Move selected to first position
                val sections = DataManager.noteVisibleSections.toMutableList()
                sections.remove(selectedTab)
                sections.add(0, selectedTab)
                
                DataManager.noteVisibleSections.clear()
                DataManager.noteVisibleSections.addAll(sections)
                
                loadSettings()
            }
        ))

        settings.add(ConfigItem("Organization", isHeader = true))
        settings.add(ConfigItem("Custom Templates", "Edit note pre-fill text") { 
            showNoteTemplatesDialog()
        })
        
        settings.add(ConfigItem("Bulk Category Move", "Move all notes at once") { 
            showNoteBulkMoveDialog()
        })

        settings.add(ConfigItem("Maintenance", isHeader = true))
        settings.add(ConfigItem("Show Hidden Notes", "Reveal your private logs", isToggle = true, isChecked = DataManager.noteShowHidden) {
            DataManager.noteShowHidden = !DataManager.noteShowHidden
        })
        
        settings.add(ConfigItem("Auto-Cleanup", "Days: ${if (DataManager.noteAutoCleanupDays > 0) DataManager.noteAutoCleanupDays else "Off"}", options = listOf("Off", "7 Days", "30 Days", "90 Days"), selectedIndex = listOf(0, 7, 30, 90).indexOf(DataManager.noteAutoCleanupDays), onOptionSelected = { index ->
            val options = listOf(0, 7, 30, 90)
            DataManager.noteAutoCleanupDays = options[index]
            loadSettings()
        }))

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showManageSectionsDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_sections_note)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.container_section_switches)
        val btnSave = dialog.findViewById<android.view.View>(R.id.btn_save_sections)
        
        val options = listOf("Notes", "Questions", "Daily", "Stories")
        val tempSelection = DataManager.noteVisibleSections.toMutableList()

        options.forEach { option ->
            val switch = androidx.appcompat.widget.SwitchCompat(this).apply {
                text = option
                setTextColor(android.graphics.Color.WHITE)
                textSize = 16f
                isChecked = tempSelection.contains(option)
                setPadding(0, 24, 0, 24)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (!tempSelection.contains(option)) tempSelection.add(option)
                    } else {
                        if (tempSelection.size > 1) {
                            tempSelection.remove(option)
                        } else {
                            this.isChecked = true
                            android.widget.Toast.makeText(this@NoteSettingsActivity, "At least one section must be visible", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            container.addView(switch)
        }

        btnSave.setOnClickListener {
            // Maintain selection while updating visibility
            val newSections = tempSelection.toList()
            DataManager.noteVisibleSections.clear()
            DataManager.noteVisibleSections.addAll(newSections)
            
            // Ensure default is still valid
            if (!DataManager.noteVisibleSections.contains(DataManager.noteDefaultCategory)) {
                DataManager.noteDefaultCategory = DataManager.noteVisibleSections.firstOrNull() ?: "Notes"
            }
            
            DataManager.saveData(this)
            loadSettings() // Refresh default tab options
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showNoteTemplatesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories_note)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        dialog.findViewById<TextView>(R.id.tv_categories_title).text = "Note Templates"

        DataManager.noteTemplates.keys.forEach { name ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = name
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            iv.setOnClickListener { 
                showSingleTemplateEditor(name)
                dialog.dismiss() 
            }
            container.addView(iv)
        }
        showDialogSafe(dialog)
    }

    private fun showSingleTemplateEditor(name: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        dialog.findViewById<TextView>(R.id.tv_settings_title).text = "Edit: $name"
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setHint("Template content...")
        et.setText(DataManager.noteTemplates[name])
        et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        et.setSingleLine(false)
        et.maxLines = 5
        
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.noteTemplates[name] = et.text.toString()
            DataManager.saveData(this)
            dialog.dismiss()
            showNoteTemplatesDialog()
        }
        showDialogSafe(dialog)
    }

    private fun showNoteBulkMoveDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories_note)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        dialog.findViewById<TextView>(R.id.tv_categories_title).text = "Move All Notes To..."

        DataManager.noteVisibleSections.forEach { category ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = category
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            iv.setOnClickListener {
                val count = DataManager.notes.size
                DataManager.notes.forEach { it.category = category }
                DataManager.saveData(this)
                Toast.makeText(this, "Moved $count notes to $category", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            container.addView(iv)
        }
        showDialogSafe(dialog)
    }
}