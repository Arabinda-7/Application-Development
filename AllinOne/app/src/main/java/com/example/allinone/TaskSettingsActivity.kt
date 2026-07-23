package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "TASK SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Customization", isHeader = true))
        settings.add(ConfigItem("Manage Categories", "Customize your task tags") { 
            showManageCategoriesDialog()
        })

        settings.add(ConfigItem("Manage Sections", "Enable or disable task sections") {
            showManageSectionsDialog()
        })
        
        settings.add(ConfigItem("Logic & View", isHeader = true))
        settings.add(ConfigItem("Sort Order", "Current: ${DataManager.taskSortOrder}", options = listOf("Priority", "Newest", "Alphabetical"), selectedIndex = listOf("Priority", "Newest", "Alphabetical").indexOf(DataManager.taskSortOrder), onOptionSelected = { index ->
            val orders = listOf("Priority", "Newest", "Alphabetical")
            DataManager.taskSortOrder = orders[index]
            loadSettings()
        }))
        
        settings.add(ConfigItem(
            "Default Section", 
            "Current: ${DataManager.taskDefaultSection}", 
            options = DataManager.taskVisibleSections,
            selectedIndex = DataManager.taskVisibleSections.indexOf(DataManager.taskDefaultSection).takeIf { it != -1 },
            onOptionSelected = { index ->
                val selectedSection = DataManager.taskVisibleSections[index]
                DataManager.taskDefaultSection = selectedSection
                
                // Reorder: Move selected to first position
                val sections = DataManager.taskVisibleSections.toMutableList()
                sections.remove(selectedSection)
                sections.add(0, selectedSection)
                
                DataManager.taskVisibleSections.clear()
                DataManager.taskVisibleSections.addAll(sections)
                
                loadSettings()
            }
        ))
        
        settings.add(ConfigItem("Auto-Archive Tasks", "Cleanup old completed items", isToggle = true, isChecked = DataManager.taskAutoArchive) {
            DataManager.taskAutoArchive = !DataManager.taskAutoArchive
        })
        
        settings.add(ConfigItem("Show Hidden Tasks", "Reveal private roadmap items", isToggle = true, isChecked = DataManager.taskShowHidden) {
            DataManager.taskShowHidden = !DataManager.taskShowHidden
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showManageSectionsDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_sections)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.container_section_switches)
        val btnSave = dialog.findViewById<android.view.View>(R.id.btn_save_sections)
        
        val options = listOf("Tasks", "List")
        val tempSelection = DataManager.taskVisibleSections.toMutableList()

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
                            android.widget.Toast.makeText(this@TaskSettingsActivity, "At least one section must be visible", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            container.addView(switch)
        }

        btnSave.setOnClickListener {
            // Maintain order while updating visibility
            val newSections = tempSelection.toList()
            DataManager.taskVisibleSections.clear()
            DataManager.taskVisibleSections.addAll(newSections)
            
            // Ensure default is still valid
            if (!DataManager.taskVisibleSections.contains(DataManager.taskDefaultSection)) {
                DataManager.taskDefaultSection = DataManager.taskVisibleSections.firstOrNull() ?: "Tasks"
            }
            
            DataManager.saveData(this)
            loadSettings() // Refresh default section options
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showManageCategoriesDialog() {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_manage_categories, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = view.findViewById<LinearLayout>(R.id.categories_container)
        val etNewCategory = view.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = view.findViewById<ImageButton>(R.id.btn_add_category)
        val btnDeleteMode = view.findViewById<ImageButton>(R.id.btn_toggle_delete_mode)

        var isDeleteMode = false

        fun render() {
            container.removeAllViews()
            btnDeleteMode.imageTintList = android.content.res.ColorStateList.valueOf(if (isDeleteMode) Color.RED else Color.WHITE)

            DataManager.taskCustomCategories.forEach { category ->
                val catView = layoutInflater.inflate(R.layout.item_category_manage, container, false)
                catView.findViewById<TextView>(R.id.tv_category_name).text = category
                
                val btnRemove = catView.findViewById<View>(R.id.btn_remove_category)
                btnRemove.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

                btnRemove.setOnClickListener {
                    if (DataManager.taskCustomCategories.size > 1) {
                        DataManager.taskCustomCategories.remove(category)
                        DataManager.saveData(this)
                        render()
                    } else {
                        Toast.makeText(this, "At least one category required", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(catView)
            }
        }

        btnDeleteMode.setOnClickListener {
            isDeleteMode = !isDeleteMode
            render()
        }

        btnAdd.setOnClickListener {
            val name = etNewCategory.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.taskCustomCategories.contains(name)) {
                DataManager.taskCustomCategories.add(name)
                DataManager.saveData(this)
                etNewCategory.text.clear()
                render()
            }
        }

        render()
        showDialogSafe(dialog)
    }

}