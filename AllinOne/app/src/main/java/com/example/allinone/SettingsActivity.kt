package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.activity.result.contract.ActivityResultContracts
import java.io.FileOutputStream

class SettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var layoutProfileHub: View
    private var currentPath: String = "HUB"

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val json = DataManager.exportData(this@SettingsActivity)
                    contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    Toast.makeText(this@SettingsActivity, "Backup Saved Successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Failed to Save Backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                val content = contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (content != null) {
                    showConfirmationDialog("RESTORE DATA", "Overwrite all current app data?", "RESTORE NOW") {
                        lifecycleScope.launch {
                            if (DataManager.importData(this@SettingsActivity, content)) {
                                Toast.makeText(this@SettingsActivity, "Data Restored Successfully", Toast.LENGTH_LONG).show()
                                showHub()
                            } else {
                                Toast.makeText(this@SettingsActivity, "Import Failed: File content is incompatible with this version", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to Import: Selected file is empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to Read File: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Restore navigation path if activity was recreated
        currentPath = savedInstanceState?.getString("current_path") ?: "HUB"

        setupKeyboardHandling(findViewById(R.id.settings_root_layout), findViewById(R.id.settings_content_container))

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        layoutProfileHub = findViewById(R.id.layout_profile_hub)
        settingsList.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            handleBackNavigation()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        if (currentPath == "HUB") {
            showHub()
        } else {
            showSectionSettings(currentPath)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_path", currentPath)
    }

    override fun onResume() {
        super.onResume()
        if (currentPath == "HUB") {
            updateMiniProfileUI()
        }
    }

    private fun handleBackNavigation() {
        when (currentPath) {
            "HUB" -> finish()
            "APPEARANCE_ICONS", "APPEARANCE_COLORS", "APPEARANCE_ADD_FEATURE", "APPEARANCE_COLOR", "APPEARANCE_ICON" -> showSectionSettings("APPEARANCE")
            "HABITS_ICONS" -> showSectionSettings("HABITS")
            "WORKOUTS_ICONS" -> showSectionSettings("WORKOUTS")
            "APPEARANCE", "OTHERS", "HELP", "SECURITY" -> showHub()
            else -> showHub()
        }
    }

    private fun showHub() {
        currentPath = "HUB"
        tvTitle.text = "APP SETTINGS"
        
        // Show Profile Hub in main settings
        layoutProfileHub.visibility = View.VISIBLE
        updateMiniProfileUI()

        val menuItems = listOf(
            SettingsHubItem("Features", isHeader = true),
            SettingsHubItem("Habit Tracker", "Manage your daily rituals and streaks", R.drawable.ic_habit_tracker, "HABITS"),
            SettingsHubItem("Workout Routine", "Configure exercises and rest timers", R.drawable.ic_workout_routine, "WORKOUTS"),
            SettingsHubItem("To-Do List", "Organize tasks and prioritization", R.drawable.ic_task, "TASKS"),
            SettingsHubItem("Notes", "Manage categories and writing templates", R.drawable.ic_notes, "NOTES"),
            SettingsHubItem("Finance", "Setup currency and budget goals", R.drawable.ic_finance, "FINANCE"),
            SettingsHubItem("Projects", "Advanced roadmap and project settings", R.drawable.ic_project, "PROJECTS"),
            
            SettingsHubItem("UI & Appearance", isHeader = true),
            SettingsHubItem("Appearance Settings", "Manage section icons and colors", R.drawable.ic_habit_tracker, "APPEARANCE"),
            SettingsHubItem("Others", "Additional app configurations", R.drawable.baseline_tune_24, "OTHERS"),
            
            SettingsHubItem("Security & Support", isHeader = true),
            SettingsHubItem("Lock & Security", "App PIN lock and privacy settings", R.drawable.baseline_settings_24, "SECURITY"),
            SettingsHubItem("Help & Guide", "Support and feature documentation", R.drawable.baseline_settings_24, "HELP")
        )

        settingsList.adapter = SettingsHubAdapter(menuItems) { section ->
            when (section) {
                "HABITS" -> startActivity(Intent(this, HabitSettingsActivity::class.java))
                "WORKOUTS" -> startActivity(Intent(this, WorkoutSettingsActivity::class.java))
                "TASKS" -> startActivity(Intent(this, TaskSettingsActivity::class.java))
                "NOTES" -> startActivity(Intent(this, NoteSettingsActivity::class.java))
                "FINANCE" -> startActivity(Intent(this, FinanceSettingsActivity::class.java))
                "PROJECTS" -> startActivity(Intent(this, ProjectSettingsActivity::class.java))
                else -> showSectionSettings(section)
            }
        }
    }

    private fun updateMiniProfileUI() {
        findViewById<TextView>(R.id.tv_mini_name).text = UIUtils.formatTitleCase(DataManager.userName)
        val ivProfile = findViewById<ImageView>(R.id.iv_profile_pic)
        
        if (DataManager.userProfileImageUri != null) {
            ivProfile.setImageURI(Uri.parse(DataManager.userProfileImageUri))
        } else {
            ivProfile.setImageResource(DataManager.userAvatarRes)
        }
        
        // Handle Profile Click
        findViewById<View>(R.id.card_profile_entry).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Feature: Avatar Options
        findViewById<View>(R.id.iv_profile_pic).setOnClickListener {
            showAvatarOptionsDialog()
        }

        val streak = DataManager.getCurrentStreak()
        val projects = DataManager.projects.count { it.category == "Project" }
        findViewById<TextView>(R.id.tv_mini_stat_streak).text = "$streak Day Streak"
        findViewById<TextView>(R.id.tv_mini_stat_projects).text = "$projects Projects"
    }

    private fun showHomeVisibilityDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_sections)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        val container = dialog.findViewById<LinearLayout>(R.id.container_section_switches)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_sections)
        val title = dialog.findViewById<TextView>(R.id.tv_manage_title)
        
        title.text = "Home Page Sections"
        container.removeAllViews()

        val sections = listOf(
            "Habits" to DataManager.showHabitSection,
            "Workouts" to DataManager.showWorkoutSection,
            "Tasks" to DataManager.showTaskSection,
            "Notes" to DataManager.showNoteSection,
            "Projects" to DataManager.showProjectSection,
            "Finance" to DataManager.showFinanceSection
        )

        val switches = mutableMapOf<String, SwitchCompat>()

        sections.forEach { (name, isChecked) ->
            val sw = SwitchCompat(this).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 16f
                this.isChecked = isChecked
                setPadding(0, 32, 0, 32)
            }
            switches[name] = sw
            container.addView(sw)
        }

        btnSave.setOnClickListener {
            DataManager.showHabitSection = switches["Habits"]?.isChecked ?: true
            DataManager.showWorkoutSection = switches["Workouts"]?.isChecked ?: true
            DataManager.showTaskSection = switches["Tasks"]?.isChecked ?: true
            DataManager.showNoteSection = switches["Notes"]?.isChecked ?: true
            DataManager.showProjectSection = switches["Projects"]?.isChecked ?: true
            DataManager.showFinanceSection = switches["Finance"]?.isChecked ?: true
            
            DataManager.saveData(this)
            dialog.dismiss()
            Toast.makeText(this, "Home visibility updated", Toast.LENGTH_SHORT).show()
        }
        showDialogSafe(dialog)
    }

    private fun showSectionSettings(section: String) {
        currentPath = section
        tvTitle.text = section.removePrefix("APPEARANCE_").replace("_", " ").uppercase()
        
        // Hide Profile Hub in sub-sections
        layoutProfileHub.visibility = View.GONE

        val settings = mutableListOf<ConfigItem>()
        
        when(section) {
            "SECURITY" -> {
                settings.add(ConfigItem("Access Control", isHeader = true))
                settings.add(ConfigItem("App Access Lock", "Require PIN to open the app", isToggle = true, isChecked = DataManager.isAppLockEnabled) {
                    if (!DataManager.isAppLockEnabled) {
                        if (DataManager.appLockPin == null) {
                            val intent = Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) }
                            startActivity(intent)
                        } else {
                            DataManager.isAppLockEnabled = true
                        }
                    } else {
                        DataManager.isAppLockEnabled = false
                    }
                    DataManager.saveData(this)
                    showSectionSettings("SECURITY")
                })
                
                if (DataManager.isAppLockEnabled && DataManager.appLockPin != null) {
                    settings.add(ConfigItem("Change PIN", "Update your security code") {
                        val intent = Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_CHANGE) }
                        startActivity(intent)
                    })

                    if (DataManager.appLockQuestion == null) {
                        settings.add(ConfigItem("Set Security Question", "Add PIN recovery option") {
                            val intent = Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_VERIFY_FOR_RECOVERY) }
                            startActivity(intent)
                        })
                    } else {
                        settings.add(ConfigItem("Change Security Question", "Update recovery question/answer") {
                            val intent = Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_VERIFY_FOR_RECOVERY) }
                            startActivity(intent)
                        })
                    }
                }

                settings.add(ConfigItem("Display Strategy", isHeader = true))
                settings.add(ConfigItem("OLED Mode", "Pure black theme for OLED screens", isToggle = true, isChecked = DataManager.isOledThemeEnabled) {
                    DataManager.isOledThemeEnabled = !DataManager.isOledThemeEnabled
                })
            }
            "OTHERS" -> {
                settings.add(ConfigItem("Customization", isHeader = true))
                settings.add(ConfigItem("Home Page Sections", "Customize dashboard visibility") {
                    showHomeVisibilityDialog()
                })
                settings.add(ConfigItem("Startup Loading Time", "Control app initialization speed (Current: ${DataManager.startupLoadingTime / 1000.0}s)") {
                    showLoadingTimeSliderDialog()
                })

                settings.add(ConfigItem("Data & Maintenance", isHeader = true))
                settings.add(ConfigItem("Export Backup", "Save all data to a local JSON file") { exportBackup() })
                settings.add(ConfigItem("Import Backup", "Restore data from a JSON file") { importBackup() })
                settings.add(ConfigItem("System Deep Clean", "Clear old history and cache") {
                    showConfirmationDialog(
                        "SYSTEM DEEP CLEAN",
                        "This will permanently delete project change history and clear temporary cache. Are you sure you want to proceed?",
                        "CLEAN NOW"
                    ) {
                        DataManager.notes.forEach { it.changeHistory.clear() }
                        DataManager.projects.forEach { it.changeHistory.clear() }
                        DataManager.saveData(this)
                        Toast.makeText(this, "System Deep Clean Complete!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            "APPEARANCE" -> {
                settings.add(ConfigItem("Styling & Themes", isHeader = true))
                settings.add(ConfigItem("Section Icons", "Manage section and item icons") { showSectionSettings("APPEARANCE_ICONS") })
                settings.add(ConfigItem("Section Colors", "Manage section theme colors") { showSectionSettings("APPEARANCE_COLORS") })
                settings.add(ConfigItem("Add Feature", "Custom section features") { showSectionSettings("APPEARANCE_ADD_FEATURE") })
                settings.add(ConfigItem("Color Management", "Custom section colors") { showSectionSettings("APPEARANCE_COLOR") })
                settings.add(ConfigItem("Icon Management", "Custom section icons") { showUpcomingFeatureDialog("Custom Icon Management") })

                settings.add(ConfigItem("Global Scaling", isHeader = true))
                settings.add(ConfigItem("Follow System Settings", "Sync display and font size with phone", isToggle = true, isChecked = DataManager.isSystemAppearanceEnabled) {
                    DataManager.isSystemAppearanceEnabled = !DataManager.isSystemAppearanceEnabled
                    DataManager.saveData(this)
                    recreate()
                })
                
                if (!DataManager.isSystemAppearanceEnabled) {
                    settings.add(ConfigItem("Current Focus Size", "Circle scale for mood logging (Current: ${DataManager.homeFocusSize})", options = listOf("S", "M", "L"), selectedIndex = listOf("S", "M", "L").indexOf(DataManager.homeFocusSize), onOptionSelected = { index ->
                        val sizes = listOf("S", "M", "L")
                        DataManager.homeFocusSize = sizes[index]
                        DataManager.saveData(this)
                        recreate()
                    }))
                    settings.add(ConfigItem("Global Display Size", "Icons and margins for all sub-sections (Current: ${DataManager.displaySize})", options = listOf("XS", "S", "L"), selectedIndex = listOf("XS", "S", "L").indexOf(DataManager.displaySize), onOptionSelected = { index ->
                        val sizes = listOf("XS", "S", "L")
                        DataManager.displaySize = sizes[index]
                        DataManager.saveData(this)
                        recreate()
                    }))
                    settings.add(ConfigItem("Home Page Display Size", "Dedicated scale for the main dashboard (Current: ${DataManager.homeDisplaySize})", options = listOf("XS", "S", "L"), selectedIndex = listOf("XS", "S", "L").indexOf(DataManager.homeDisplaySize), onOptionSelected = { index ->
                        val sizes = listOf("XS", "S", "L")
                        DataManager.homeDisplaySize = sizes[index]
                        DataManager.saveData(this)
                        recreate()
                    }))
                    settings.add(ConfigItem("Text Font Size", "Scaling for titles and content (Current: ${DataManager.fontSize})", options = listOf("XS", "S", "L"), selectedIndex = listOf("XS", "S", "L").indexOf(DataManager.fontSize), onOptionSelected = { index ->
                        val sizes = listOf("XS", "S", "L")
                        DataManager.fontSize = sizes[index]
                        DataManager.saveData(this)
                        recreate()
                    }))
                }

                settings.add(ConfigItem("Advanced Look & Feel", isHeader = true))
                settings.add(ConfigItem("Theme Mode", "Override system theme (Current: ${DataManager.appThemeMode})", options = listOf("LIGHT", "DARK", "OLED"), selectedIndex = listOf("LIGHT", "DARK", "OLED").indexOf(DataManager.appThemeMode), onOptionSelected = { index ->
                    val modes = listOf("LIGHT", "DARK", "OLED")
                    DataManager.appThemeMode = modes[index]
                    DataManager.saveData(this)
                    recreate()
                }))

                settings.add(ConfigItem("Accent Color", "Custom highlights app-wide") { showColorPickerDialog("APP_ACCENT") })
                settings.add(ConfigItem("Border Radius", "Curvature for cards and buttons (Current: ${DataManager.appBorderRadius}dp)") { showBorderRadiusSliderDialog() })

                settings.add(ConfigItem("Card Style", "Surface appearance (Current: ${DataManager.appCardStyle})", options = listOf("GLASS", "ELEVATED", "FLAT"), selectedIndex = listOf("GLASS", "ELEVATED", "FLAT").indexOf(DataManager.appCardStyle), onOptionSelected = { index ->
                    val styles = listOf("GLASS", "ELEVATED", "FLAT")
                    DataManager.appCardStyle = styles[index]
                    DataManager.saveData(this)
                    recreate()
                }))

                settings.add(ConfigItem("Font Family", "Change typography style (Current: ${DataManager.appFontFamily})", options = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE"), selectedIndex = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE").indexOf(DataManager.appFontFamily), onOptionSelected = { index ->
                    val fonts = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE")
                    DataManager.appFontFamily = fonts[index]
                    DataManager.saveData(this)
                    recreate()
                }))

                settings.add(ConfigItem("Show Shadows", "Toggle UI depth and elevation", isToggle = true, isChecked = DataManager.appShowShadows) {
                    DataManager.appShowShadows = !DataManager.appShowShadows
                })
            }
            "APPEARANCE_ADD_FEATURE" -> {
                settings.add(ConfigItem("Section Creation Colors", isHeader = true))
                settings.add(ConfigItem("Habit Add & Save Color", "Theme for creating habits") { showColorPickerDialog("ADD_HABIT") })
                settings.add(ConfigItem("Workout Add & Save Color", "Theme for creating workouts") { showColorPickerDialog("ADD_WORKOUT") })
                settings.add(ConfigItem("Task Add & Save Color", "Theme for creating tasks") { showColorPickerDialog("ADD_TASK") })
                settings.add(ConfigItem("Project Add & Save Color", "Theme for creating projects") { showColorPickerDialog("ADD_PROJECT") })
                settings.add(ConfigItem("Note Add & Save Color", "Theme for creating notes") { showColorPickerDialog("ADD_NOTE") })
                settings.add(ConfigItem("Finance Add & Save Color", "Theme for creating transactions") { showColorPickerDialog("ADD_FINANCE") })
            }
            "APPEARANCE_COLOR" -> {
                settings.add(ConfigItem("ADD NEW CUSTOM COLOR", "Create a color to use app-wide") { showAddCustomColorDialog() })
                if (DataManager.userCustomColors.isNotEmpty()) {
                    settings.add(ConfigItem("--- YOUR CUSTOM COLORS ---", "") {})
                    DataManager.userCustomColors.forEach { color ->
                        val hex = String.format("#%06X", (0xFFFFFF and color))
                        settings.add(ConfigItem("Custom Color $hex", "Click to preview") {
                            Toast.makeText(this, "Hex: $hex", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
            "APPEARANCE_ICONS" -> {
                settings.add(ConfigItem("Global Actions", isHeader = true))
                settings.add(ConfigItem("RESET ALL ICONS", "Restore defaults") {
                    showConfirmationDialog("RESET ICONS", "Reset all icons?", "RESET") {
                        DataManager.resetAppearanceIcons(); DataManager.saveData(this); showSectionSettings("APPEARANCE_ICONS")
                    }
                })
                settings.add(ConfigItem("Section Icons", isHeader = true))
                settings.add(ConfigItem("Habit Icon", "Change default habit icon") { showIconPickerDialog("HABIT") })
                settings.add(ConfigItem("Workout Icon", "Change default workout icon") { showIconPickerDialog("WORKOUT") })
                settings.add(ConfigItem("Task Icon", "Change default task icon") { showIconPickerDialog("TASK") })
                settings.add(ConfigItem("Project Icon", "Change default project icon") { showIconPickerDialog("PROJECT") })
                settings.add(ConfigItem("Note Icon", "Change default note icon") { showIconPickerDialog("NOTE") })
                settings.add(ConfigItem("Finance Icon", "Change default finance icon") { showIconPickerDialog("FINANCE") })
            }
            "APPEARANCE_COLORS" -> {
                settings.add(ConfigItem("Global Actions", isHeader = true))
                settings.add(ConfigItem("RESET ALL COLORS", "Restore defaults") {
                    showConfirmationDialog("RESET COLORS", "Reset all colors?", "RESET") {
                        DataManager.resetAppearanceColors(); DataManager.saveData(this); showSectionSettings("APPEARANCE_COLORS")
                    }
                })
                settings.add(ConfigItem("Section Themes", isHeader = true))
                settings.add(ConfigItem("Habit Section Color", "Change theme color for Habits") { showColorPickerDialog("HABIT") })
                settings.add(ConfigItem("Workout Section Color", "Change theme color for Workouts") { showColorPickerDialog("WORKOUT") })
                settings.add(ConfigItem("Task Section Color", "Change theme color for Tasks") { showColorPickerDialog("TASK") })
                settings.add(ConfigItem("Project Section Color", "Change theme color for Projects") { showColorPickerDialog("PROJECT") })
                settings.add(ConfigItem("Note Section Color", "Change theme color for Notes") { showColorPickerDialog("NOTE") })
                settings.add(ConfigItem("Finance Section Color", "Change theme color for Finance") { showColorPickerDialog("FINANCE") })
            }
            "HELP", "HELP_GUIDE" -> {
                settings.add(ConfigItem("Master Guides", isHeader = true))
                HelpData.getMasterGuides().forEach { article ->
                    settings.add(ConfigItem(article.title, article.summary) { showMasterGuideDetail(article) })
                }
                
                settings.add(ConfigItem("Section Walkthroughs", isHeader = true))
                val guides = listOf("HABITS", "WORKOUTS", "TASKS", "PROJECTS", "NOTES", "FINANCE", "OTHERS")
                guides.forEach { guide ->
                    val pageCount = HelpData.getGuideForSection(guide).size
                    val summary = "Interactive Tour • $pageCount Pages"
                    settings.add(ConfigItem("${guide.substring(0,1)}${guide.substring(1).lowercase()} Guide", summary) { showHelpDetail(guide) })
                }
            }
            "HABITS_ICONS", "WORKOUTS_ICONS" -> {
                settings.add(ConfigItem("Empty Section", "Coming soon") {})
            }
        }
        
        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun exportBackup() {
        exportLauncher.launch("allinone_backup_${System.currentTimeMillis()}.json")
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00 AM"
            hour < 12 -> "$hour:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }

    private fun importBackup() {
        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
    }

    private fun showUpcomingFeatureDialog(featureName: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnClose = dialog.findViewById<TextView>(R.id.btn_save_budget)

        title.text = "UPCOMING FEATURE"
        etInput.visibility = View.GONE
        subtext.text = "The \"$featureName\" system is currently under development for the next executive update. Stay tuned!"
        btnClose.text = "UNDERSTOOD"
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAvatarOptionsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        
        title.text = "SELECT AVATAR STYLE"
        
        val icons = listOf(R.drawable.boy_avatar_profile, R.drawable.girl_avatar_profile)
        
        container.removeAllViews()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        icons.forEach { iconRes ->
            val iv = ImageView(this).apply {
                val s = (80 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(s, s).apply { setMargins(24, 24, 24, 24) }
                setImageResource(iconRes)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    DataManager.userAvatarRes = iconRes
                    DataManager.saveData(this@SettingsActivity)
                    updateMiniProfileUI()
                    dialog.dismiss()
                    Toast.makeText(this@SettingsActivity, "Avatar Style Updated", Toast.LENGTH_SHORT).show()
                }
            }
            row.addView(iv)
        }
        container.addView(row)
        dialog.show()
    }

    private fun showMasterGuideDetail(article: HelpArticle) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_help_detail)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        dialog.findViewById<TextView>(R.id.tv_help_title).text = article.title.uppercase()
        dialog.findViewById<TextView>(R.id.tv_help_content).text = article.content
        dialog.findViewById<View>(R.id.btn_close_help).setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }

    private fun showHelpDetail(section: String) {
        val features = HelpData.getGuideForSection(section)
        if (features.isEmpty()) {
            Toast.makeText(this, "Guide coming soon", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_help_guide)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_help_title)
        val viewPager = dialog.findViewById<ViewPager2>(R.id.vp_help_features)
        val tabLayout = dialog.findViewById<TabLayout>(R.id.tl_indicator)
        val btnGotIt = dialog.findViewById<View>(R.id.btn_got_it)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_help)

        tvTitle.text = "${section.uppercase()} GUIDE"
        
        viewPager.adapter = HelpGuideAdapter(features)
        
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        btnGotIt.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }

    private fun showConfirmationDialog(title: String, message: String, pos: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        val btnPos = dialog.findViewById<TextView>(R.id.btn_confirm_positive)
        btnPos.text = pos; btnPos.setOnClickListener { onConfirm(); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showColorPickerDialog(section: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_settings_color_picker)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        val grid = dialog.findViewById<GridLayout>(R.id.color_grid)
        val title = dialog.findViewById<TextView>(R.id.tv_picker_title)
        title.text = "SELECT COLOR: $section"
        val colors = listOf(Color.parseColor("#FF7A59"), Color.parseColor("#FFB800"), Color.parseColor("#2EC4B6"), Color.parseColor("#1A73E8"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"), Color.parseColor("#673AB7"), Color.parseColor("#4CAF50"))
        colors.forEach { color ->
            val v = View(this).apply {
                val s = (48 * resources.displayMetrics.density).toInt()
                layoutParams = GridLayout.LayoutParams().apply { width = s; height = s; setMargins(12, 12, 12, 12) }
                background = ContextCompat.getDrawable(this@SettingsActivity, R.drawable.circle_selected_bg)
                backgroundTintList = ColorStateList.valueOf(color)
                setOnClickListener {
                    when (section) {
                        "HABIT" -> DataManager.globalHabitColor = color
                        "WORKOUT" -> DataManager.globalWorkoutColor = color
                        "TASK" -> DataManager.globalTaskColor = color
                        "PROJECT" -> DataManager.globalProjectColor = color
                        "NOTE" -> DataManager.globalNoteColor = color
                        "FINANCE" -> DataManager.globalFinanceColor = color
                        "ADD_HABIT" -> DataManager.habitAddThemeColor = color
                        "ADD_WORKOUT" -> DataManager.workoutAddThemeColor = color
                        "ADD_TASK" -> DataManager.taskAddThemeColor = color
                        "ADD_PROJECT" -> DataManager.projectAddThemeColor = color
                        "ADD_NOTE" -> DataManager.noteAddThemeColor = color
                        "ADD_FINANCE" -> DataManager.financeAddThemeColor = color
                        "APP_ACCENT" -> DataManager.appAccentColor = color
                    }
                    DataManager.saveData(this@SettingsActivity); dialog.dismiss(); showSectionSettings(currentPath)
                }
            }
            grid.addView(v)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    private fun showBorderRadiusSliderDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings_slider)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        
        val title = dialog.findViewById<TextView>(R.id.tv_slider_title)
        val slider = dialog.findViewById<SeekBar>(R.id.settings_slider)
        val valueText = dialog.findViewById<TextView>(R.id.tv_slider_value)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_slider)

        title.text = "BORDER RADIUS"
        slider.max = 32
        slider.progress = DataManager.appBorderRadius
        valueText.text = "${slider.progress}dp"

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { valueText.text = "${p}dp" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            DataManager.appBorderRadius = slider.progress
            DataManager.saveData(this)
            dialog.dismiss()
            recreate()
        }
        dialog.show()
    }

    private fun showLoadingTimeSliderDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings_slider)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        
        val title = dialog.findViewById<TextView>(R.id.tv_slider_title)
        val slider = dialog.findViewById<SeekBar>(R.id.settings_slider)
        val valueText = dialog.findViewById<TextView>(R.id.tv_slider_value)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_slider)

        title.text = "STARTUP LOADING TIME"
        slider.max = 40 // (5000 - 1000) / 100
        slider.progress = (DataManager.startupLoadingTime - 1000) / 100
        valueText.text = "${DataManager.startupLoadingTime / 1000.0}s"

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { 
                val time = 1000 + (p * 100)
                valueText.text = "${time / 1000.0}s" 
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            DataManager.startupLoadingTime = 1000 + (slider.progress * 100)
            DataManager.saveData(this)
            dialog.dismiss()
            showSectionSettings("OTHERS")
            Toast.makeText(this, "Loading time updated to ${DataManager.startupLoadingTime / 1000.0}s", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun showAddCustomColorDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_add_custom_color)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        val et = dialog.findViewById<EditText>(R.id.et_hex_code)
        dialog.findViewById<View>(R.id.btn_add_hex).setOnClickListener {
            try { val c = Color.parseColor(et.text.toString()); DataManager.userCustomColors.add(c); DataManager.saveData(this); showSectionSettings("APPEARANCE_COLOR"); dialog.dismiss() } catch(e: Exception) {}
        }
        dialog.show()
    }

    private fun showIconPickerDialog(section: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val icons = listOf(R.drawable.ic_habit_tracker, R.drawable.ic_workout_routine, R.drawable.ic_task, R.drawable.ic_notes, R.drawable.ic_project, R.drawable.ic_finance)
        val rv = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@SettingsActivity, 4)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p: ViewGroup, t: Int) = object : RecyclerView.ViewHolder(ImageView(p.context).apply {
                    val s = (56 * resources.displayMetrics.density).toInt()
                    layoutParams = ViewGroup.LayoutParams(s, s); setPadding(12, 12, 12, 12)
                }) {}
                override fun onBindViewHolder(h: RecyclerView.ViewHolder, p: Int) {
                    val i = icons[p]; val iv = h.itemView as ImageView; iv.setImageResource(i); iv.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    iv.setOnClickListener {
                        when(section) {
                            "HABIT" -> DataManager.globalHabitIcon = i
                            "WORKOUT" -> DataManager.globalWorkoutIcon = i
                            "TASK" -> DataManager.globalTaskIcon = i
                            "PROJECT" -> DataManager.globalProjectIcon = i
                            "NOTE" -> DataManager.globalNoteIcon = i
                            "FINANCE" -> DataManager.globalFinanceIcon = i
                        }
                        DataManager.saveData(this@SettingsActivity); dialog.dismiss(); showSectionSettings(currentPath)
                    }
                }
                override fun getItemCount() = icons.size
            }
        }
        container.removeAllViews(); container.addView(rv); dialog.show()
    }



    data class SettingsHubItem(
        val title: String,
        val description: String = "",
        val iconRes: Int = 0,
        val sectionKey: String = "",
        val isHeader: Boolean = false
    )

    inner class HelpGuideAdapter(private val features: List<HelpFeature>) : RecyclerView.Adapter<HelpGuideAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_feature_title)
            val desc: TextView = v.findViewById(R.id.tv_feature_description)
            val img: ImageView = v.findViewById(R.id.iv_feature_screenshot)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_help_feature, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val feature = features[position]
            holder.title.text = feature.title
            holder.desc.text = feature.description
            
            if (feature.imageFileName != null) {
                val file = java.io.File(holder.itemView.context.filesDir, feature.imageFileName)
                if (file.exists()) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        holder.img.setPadding(0, 0, 0, 0)
                        holder.img.setImageBitmap(bitmap)
                        holder.img.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        holder.img.setPadding(24, 24, 24, 24)
                        holder.img.setImageResource(feature.imageRes)
                        holder.img.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                } else {
                    holder.img.setPadding(24, 24, 24, 24)
                    holder.img.setImageResource(feature.imageRes)
                    holder.img.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            } else {
                holder.img.setPadding(24, 24, 24, 24)
                holder.img.setImageResource(feature.imageRes)
                holder.img.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }

        override fun getItemCount() = features.size
    }

    inner class SettingsHubAdapter(private val items: List<SettingsHubItem>, private val onSelect: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        
        private val TYPE_ITEM = 0
        private val TYPE_HEADER = 1

        override fun getItemViewType(position: Int): Int {
            return if (items[position].isHeader) TYPE_HEADER else TYPE_ITEM
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
            return if (t == TYPE_HEADER) {
                HeaderViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_settings_header, p, false))
            } else {
                ItemViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_settings_hub, p, false))
            }
        }

        override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
            val i = items[pos]
            if (h is ItemViewHolder) {
                h.title.text = i.title
                h.description.text = i.description
                h.icon.setImageResource(i.iconRes)
                h.itemView.setOnClickListener { onSelect(i.sectionKey) }
            } else if (h is HeaderViewHolder) {
                h.title.text = i.title.uppercase()
            }
        }

        override fun getItemCount() = items.size

        inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_item_title)
            val description: TextView = v.findViewById(R.id.tv_item_description)
            val icon: ImageView = v.findViewById(R.id.iv_item_icon)
        }

        inner class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_header_title)
        }
    }
}