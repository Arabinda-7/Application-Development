package com.example.allinone

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.domain.repository.NoteSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private val viewModel: NoteSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings_note)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "NOTE SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    loadSettings(settings)
                }
            }
        }
    }

    private fun loadSettings(currentSettings: NoteSettings) {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Structure", isHeader = true))
        settings.add(ConfigItem("Manage Sections", "Enable or disable note categories") {
            showManageSectionsDialog(currentSettings)
        })
        
        settings.add(ConfigItem(
            "Default Startup Tab", 
            "Current: ${currentSettings.defaultCategory}", 
            options = currentSettings.visibleSections,
            selectedIndex = currentSettings.visibleSections.indexOf(currentSettings.defaultCategory).takeIf { it != -1 },
            onOptionSelected = { index ->
                val selectedTab = currentSettings.visibleSections[index]
                val sections = currentSettings.visibleSections.toMutableList()
                sections.remove(selectedTab)
                sections.add(0, selectedTab)
                
                viewModel.updateSettings(currentSettings.copy(
                    defaultCategory = selectedTab,
                    visibleSections = sections
                ))
            }
        ))

        settings.add(ConfigItem("Organization", isHeader = true))
        settings.add(ConfigItem("Custom Templates", "Edit note pre-fill text") { 
            showNoteTemplatesDialog(currentSettings)
        })
        
        settings.add(ConfigItem("Bulk Category Move", "Move all notes at once") { 
            showNoteBulkMoveDialog(currentSettings)
        })

        settings.add(ConfigItem("Maintenance", isHeader = true))
        settings.add(ConfigItem("Show Hidden Notes", "Reveal your private logs", isToggle = true, isChecked = currentSettings.showHidden) {
            viewModel.updateSettings(currentSettings.copy(showHidden = !currentSettings.showHidden))
        })
        
        val cleanupOptions = listOf(0, 7, 30, 90)
        settings.add(ConfigItem(
            "Auto-Cleanup", 
            "Days: ${if (currentSettings.autoCleanupDays > 0) currentSettings.autoCleanupDays else "Off"}", 
            options = listOf("Off", "7 Days", "30 Days", "90 Days"), 
            selectedIndex = cleanupOptions.indexOf(currentSettings.autoCleanupDays).coerceAtLeast(0), 
            onOptionSelected = { index ->
                viewModel.updateSettings(currentSettings.copy(autoCleanupDays = cleanupOptions[index]))
            }
        ))

        settingsList.adapter = ConfigAdapter(settings) { /* No-op, we save via viewModel */ }
    }

    private fun showManageSectionsDialog(currentSettings: NoteSettings) {
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
        val tempSelection = currentSettings.visibleSections.toMutableList()

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
            val newDefault = if (tempSelection.contains(currentSettings.defaultCategory)) currentSettings.defaultCategory else tempSelection.first()
            viewModel.updateSettings(currentSettings.copy(
                visibleSections = tempSelection.toList(),
                defaultCategory = newDefault
            ))
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showNoteTemplatesDialog(currentSettings: NoteSettings) {
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

        currentSettings.noteTemplates.keys.forEach { name ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = name
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            
            // Item Background
            iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#2A2A2A"))
                cornerRadius = radius * 0.5f
            }

            iv.setOnClickListener { 
                showSingleTemplateEditor(name, currentSettings)
                dialog.dismiss() 
            }
            container.addView(iv)
        }
        showDialogSafe(dialog)
    }

    private fun showSingleTemplateEditor(name: String, currentSettings: NoteSettings) {
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
        et.setText(currentSettings.noteTemplates[name])
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
            val newTemplates = currentSettings.noteTemplates.toMutableMap()
            newTemplates[name] = et.text.toString()
            viewModel.updateSettings(currentSettings.copy(noteTemplates = newTemplates))
            dialog.dismiss()
            showNoteTemplatesDialog(currentSettings)
        }
        showDialogSafe(dialog)
    }

    private fun showNoteBulkMoveDialog(currentSettings: NoteSettings) {
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

        currentSettings.visibleSections.forEach { category ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_note, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = category
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            
            // Item Background
            iv.findViewById<View>(R.id.item_container).background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#2A2A2A"))
                cornerRadius = radius * 0.5f
            }

            iv.setOnClickListener {
                viewModel.bulkMoveNotes(category)
                Toast.makeText(this, "Moving notes to $category...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            container.addView(iv)
        }
        showDialogSafe(dialog)
    }
}
