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
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_sections_note)
        
        val container = dialog.findViewById<LinearLayout>(R.id.container_section_switches)
        val btnSave = dialog.findViewById<android.view.View>(R.id.btn_save_sections)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        (btnSave.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = radius * 0.5f
        }
        
        val options = listOf("Notes", "Questions", "Daily", "Stories")
        val tempSelection = DataManager.noteVisibleSections.toMutableList()

        options.forEach { option ->
            val switch = androidx.appcompat.widget.SwitchCompat(this).apply {
                text = option
                setTextColor(android.graphics.Color.WHITE)
                textSize = 16f
                isChecked = tempSelection.contains(option)
                setPadding(0, 24, 0, 24)
                
                // Theme the switch
                val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
                val thumbColors = intArrayOf(android.graphics.Color.GRAY, accentColor)
                val trackColors = intArrayOf(android.graphics.Color.parseColor("#33FFFFFF"), UIUtils.adjustAlpha(accentColor, 0.3f))
                
                androidx.core.graphics.drawable.DrawableCompat.setTintList(thumbDrawable, android.content.res.ColorStateList(states, thumbColors))
                androidx.core.graphics.drawable.DrawableCompat.setTintList(trackDrawable, android.content.res.ColorStateList(states, trackColors))

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
            val newSections = tempSelection.toList()
            DataManager.noteVisibleSections.clear()
            DataManager.noteVisibleSections.addAll(newSections)
            
            if (!DataManager.noteVisibleSections.contains(DataManager.noteDefaultCategory)) {
                DataManager.noteDefaultCategory = DataManager.noteVisibleSections.firstOrNull() ?: "Notes"
            }
            
            DataManager.saveData(this)
            loadSettings()
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showNoteTemplatesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_note)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        tvTitle.text = "NOTE TEMPLATES"
        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }

        DataManager.noteTemplates.keys.forEach { name ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = name
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            
            // Item Background
            iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#2A2A2A"))
                cornerRadius = radius * 0.5f
            }

            iv.setOnClickListener { 
                showSingleTemplateEditor(name)
                dialog.dismiss() 
            }
            container.addView(iv)
        }
        showDialogSafe(dialog)
    }

    private fun showSingleTemplateEditor(name: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget_note)
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_settings_title)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        tvTitle.text = "EDIT: ${name.uppercase()}"
        et.setHint("Template content...")
        et.setText(DataManager.noteTemplates[name])
        et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        et.setSingleLine(false)
        et.maxLines = 10
        
        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        
        et.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#1AFFFFFF"))
            cornerRadius = radius * 0.7f
            setStroke(1, android.graphics.Color.parseColor("#33FFFFFF"))
        }

        (btnSave.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = radius * 0.5f
        }
        
        btnSave.setOnClickListener {
            DataManager.noteTemplates[name] = et.text.toString()
            DataManager.saveData(this)
            dialog.dismiss()
            showNoteTemplatesDialog()
        }
        showDialogSafe(dialog)
    }

    private fun showNoteBulkMoveDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_note)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        tvTitle.text = "MOVE ALL NOTES TO..."
        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }

        DataManager.noteVisibleSections.forEach { category ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = category
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            
            // Item Background
            iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#2A2A2A"))
                cornerRadius = radius * 0.5f
            }

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