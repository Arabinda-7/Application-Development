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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

import androidx.activity.result.contract.ActivityResultContracts
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private var currentPath: String = "HUB"

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            try {
                val json = DataManager.exportData()
                contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(this, "Backup Saved Successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to Save Backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                val content = contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (content != null) {
                    showConfirmationDialog("RESTORE DATA", "Overwrite all current app data?", "RESTORE NOW") {
                        if (DataManager.importData(this, content)) {
                            Toast.makeText(this, "Data Restored Successfully", Toast.LENGTH_LONG).show()
                            showHub()
                        } else {
                            Toast.makeText(this, "Import Failed: File content is incompatible with this version", Toast.LENGTH_LONG).show()
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

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        settingsList.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            handleBackNavigation()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        showHub()
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
        
        // Show Profile Header in Hub
        findViewById<View>(R.id.settings_top_header).visibility = View.VISIBLE
        updateMiniProfileUI()

        val menuItems = listOf(
            SettingsHubItem("Habit Tracker", "Manage your daily rituals and streaks", R.drawable.ic_habit_tracker, "HABITS"),
            SettingsHubItem("Workout Routine", "Configure exercises and rest timers", R.drawable.ic_workout_routine, "WORKOUTS"),
            SettingsHubItem("To-Do List", "Organize tasks and prioritization", R.drawable.ic_todo_list, "TASKS"),
            SettingsHubItem("Notes", "Manage categories and writing templates", R.drawable.ic_notes, "NOTES"),
            SettingsHubItem("Finance", "Setup currency and budget goals", R.drawable.ic_finance, "FINANCE"),
            SettingsHubItem("Projects", "Advanced roadmap and project settings", R.drawable.ic_project, "PROJECTS"),
            SettingsHubItem("App Security", "Biometric lock and privacy settings", R.drawable.baseline_settings_24, "SECURITY"),
            SettingsHubItem("Appearance Settings", "Manage section icons and colors", R.drawable.ic_habit_tracker, "APPEARANCE"),
            SettingsHubItem("Others", "Additional app configurations", R.drawable.baseline_tune_24, "OTHERS"),
            SettingsHubItem("Help & Guide", "Support and feature documentation", R.drawable.baseline_settings_24, "HELP")
        )

        settingsList.adapter = SettingsHubAdapter(menuItems) { section ->
            showSectionSettings(section)
        }
    }

    private fun updateMiniProfileUI() {
        findViewById<TextView>(R.id.tv_mini_name).text = DataManager.userName
        findViewById<ImageView>(R.id.iv_profile_pic).setImageResource(DataManager.userAvatarRes)
        
        // Handle Profile Click
        findViewById<View>(R.id.card_profile_entry).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Feature: Avatar Options
        findViewById<View>(R.id.iv_profile_pic).setOnClickListener {
            showAvatarOptionsDialog()
        }

        val streak = DataManager.getCurrentStreak()
        val projects = DataManager.notes.count { it.category == "Project" }
        findViewById<TextView>(R.id.tv_mini_stat_streak).text = "$streak Day Streak"
        findViewById<TextView>(R.id.tv_mini_stat_projects).text = "$projects Projects"
    }

    private fun showSectionSettings(section: String) {
        currentPath = section
        tvTitle.text = section.removePrefix("APPEARANCE_").replace("_", " ").uppercase()
        
        // Hide Profile Header in sub-sections
        findViewById<View>(R.id.settings_top_header).visibility = View.GONE

        val settings = mutableListOf<ConfigItem>()
        
        when(section) {
            "HABITS" -> {
                settings.add(ConfigItem("Icons", "No features added yet") { showSectionSettings("HABITS_ICONS") })
                settings.add(ConfigItem("Theme Color", "Customize creating habits theme") { showColorPickerDialog("ADD_HABIT") })
                settings.add(ConfigItem("Behavioral Insights", "Peak performance analytics") { showBehavioralInsightsDialog() })
                settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.habitDefaultTab}") {
                    val tabs = listOf("TODAY", "WEEK", "ALL")
                    DataManager.habitDefaultTab = tabs[(tabs.indexOf(DataManager.habitDefaultTab) + 1) % tabs.size]
                    showSectionSettings("HABITS")
                })
                settings.add(ConfigItem("Sort Order", "Current: ${DataManager.habitSortOrder}") {
                    val orders = listOf("Time", "Streak")
                    DataManager.habitSortOrder = orders[(orders.indexOf(DataManager.habitSortOrder) + 1) % orders.size]
                    showSectionSettings("HABITS")
                })
                settings.add(ConfigItem("Vacation Mode", "Freeze streaks during breaks", isToggle = true, isChecked = DataManager.habitVacationMode) {
                    DataManager.habitVacationMode = !DataManager.habitVacationMode
                })
                settings.add(ConfigItem("Completion Sound", "Play sound on habit finished", isToggle = true, isChecked = DataManager.habitCompletionSound) {
                    DataManager.habitCompletionSound = !DataManager.habitCompletionSound
                })
                settings.add(ConfigItem("Haptic Feedback", "Vibrate on habit finished", isToggle = true, isChecked = DataManager.habitCompletionHaptics) {
                    DataManager.habitCompletionHaptics = !DataManager.habitCompletionHaptics
                })
                settings.add(ConfigItem("Bulk Action Mode", "Fast multi-update mode", isToggle = true, isChecked = DataManager.habitBulkMode) {
                    DataManager.habitBulkMode = !DataManager.habitBulkMode
                })
                settings.add(ConfigItem("Day Reset Hour", "Current: ${formatHour(DataManager.habitDayResetHour)}") {
                    DataManager.habitDayResetHour = (DataManager.habitDayResetHour + 1) % 24
                    showSectionSettings("HABITS")
                })
                settings.add(ConfigItem("Grace Period", "Allowed misses: ${DataManager.habitGraceDaysAllowed} days") {
                    val options = listOf(0, 1, 2, 3)
                    DataManager.habitGraceDaysAllowed = options[(options.indexOf(DataManager.habitGraceDaysAllowed) + 1) % options.size]
                    showSectionSettings("HABITS")
                })
            }
            "WORKOUTS" -> {
                settings.add(ConfigItem("Icons", "No features added yet") { showSectionSettings("WORKOUTS_ICONS") })
                settings.add(ConfigItem("Theme Color", "Customize creating workouts theme") { showColorPickerDialog("ADD_WORKOUT") })
                settings.add(ConfigItem("Manage Muscle Groups", "Add or remove body part tags") { showManageMuscleGroupsDialog() })
                settings.add(ConfigItem("Workout Readiness", "Check your energy levels") { showWorkoutReadinessDialog() })
                settings.add(ConfigItem("Auto-Rest Timer", "Trigger timer after set", isToggle = true, isChecked = DataManager.workoutAutoRestTimer) {
                    DataManager.workoutAutoRestTimer = !DataManager.workoutAutoRestTimer
                })
                settings.add(ConfigItem("Workout Weight Unit", "Current: ${DataManager.workoutWeightUnit}") {
                    DataManager.workoutWeightUnit = if (DataManager.workoutWeightUnit == "Kg") "Lbs" else "Kg"
                    showSectionSettings("WORKOUTS")
                })
                settings.add(ConfigItem("Default Tracking Mode", "Current: ${DataManager.workoutDefaultMode}") {
                    val modes = listOf("Reps", "Timer", "Sets")
                    DataManager.workoutDefaultMode = modes[(modes.indexOf(DataManager.workoutDefaultMode) + 1) % modes.size]
                    showSectionSettings("WORKOUTS")
                })
                settings.add(ConfigItem("Rest Duration", "Current: ${DataManager.workoutRestDuration}s") {
                    val durations = listOf(30, 60, 90, 120)
                    DataManager.workoutRestDuration = durations[(durations.indexOf(DataManager.workoutRestDuration) + 1) % durations.size]
                    showSectionSettings("WORKOUTS")
                })
            }
            "TASKS" -> {
                settings.add(ConfigItem("Manage Categories", "Customize your task tags") { showManageTaskCategoriesDialog() })
                settings.add(ConfigItem("Task Analytics", "View completion totals") { showTaskAnalyticsDialog() })
                settings.add(ConfigItem("Sorting Logic", "Current: ${DataManager.taskSortOrder}") {
                    val orders = listOf("Priority", "Newest", "Alphabetical")
                    DataManager.taskSortOrder = orders[(orders.indexOf(DataManager.taskSortOrder) + 1) % orders.size]
                    showSectionSettings("TASKS")
                })
                settings.add(ConfigItem("Default Section", "Current: ${DataManager.taskDefaultSection}") {
                    val sections = listOf("Tasks", "List")
                    DataManager.taskDefaultSection = sections[(sections.indexOf(DataManager.taskDefaultSection) + 1) % sections.size]
                    showSectionSettings("TASKS")
                })
                settings.add(ConfigItem("Auto-Archive Tasks", "Cleanup old completed items", isToggle = true, isChecked = DataManager.taskAutoArchive) {
                    DataManager.taskAutoArchive = !DataManager.taskAutoArchive
                })
                settings.add(ConfigItem("Show Hidden Tasks", "Reveal private roadmap items", isToggle = true, isChecked = DataManager.taskShowHidden) {
                    DataManager.taskShowHidden = !DataManager.taskShowHidden
                })
            }
            "NOTES" -> {
                settings.add(ConfigItem("Custom Templates", "Edit note pre-fill text") { showNoteTemplatesDialog() })
                settings.add(ConfigItem("Bulk Category Move", "Move all notes at once") { showNoteBulkMoveDialog() })
                settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.noteDefaultCategory}") {
                    val cats = DataManager.noteVisibleSections
                    DataManager.noteDefaultCategory = cats[(cats.indexOf(DataManager.noteDefaultCategory) + 1) % cats.size]
                    showSectionSettings("NOTES")
                })
                settings.add(ConfigItem("Show Hidden Notes", "Reveal your private logs", isToggle = true, isChecked = DataManager.noteShowHidden) {
                    DataManager.noteShowHidden = !DataManager.noteShowHidden
                })
                settings.add(ConfigItem("Auto-Cleanup", "Days: ${if (DataManager.noteAutoCleanupDays > 0) DataManager.noteAutoCleanupDays else "Off"}") {
                    val options = listOf(0, 7, 30, 90)
                    DataManager.noteAutoCleanupDays = options[(options.indexOf(DataManager.noteAutoCleanupDays) + 1) % options.size]
                    showSectionSettings("NOTES")
                })
            }
            "FINANCE" -> {
                settings.add(ConfigItem("Primary Currency", "Current: ${DataManager.financeCurrency}") {
                    val symbols = listOf("₹", "$", "€", "£", "¥")
                    DataManager.financeCurrency = symbols[(symbols.indexOf(DataManager.financeCurrency) + 1) % symbols.size]
                    showSectionSettings("FINANCE")
                })
                settings.add(ConfigItem("Theme Color", "Customize creating finance theme") { showColorPickerDialog("ADD_FINANCE") })
                settings.add(ConfigItem("Manage Categories", "Edit spending labels") { showManageFinanceCategoriesDialog() })
                settings.add(ConfigItem("Monthly Budget", "Current: ${DataManager.financeCurrency}${DataManager.monthlyBudget}") { showSetBudgetDialog() })
                settings.add(ConfigItem("Savings Goal", "Current: ${DataManager.financeCurrency}${DataManager.monthlySavingsGoal}") { showSetSavingsGoalDialog() })
                settings.add(ConfigItem("Ledger System", "Enable person-based debt tracking", isToggle = true, isChecked = DataManager.isFinanceLedgerEnabled) {
                    DataManager.isFinanceLedgerEnabled = !DataManager.isFinanceLedgerEnabled
                })
            }
            "PROJECTS" -> {
                settings.add(ConfigItem("Manage Templates", "Edit roadmap pre-sets") { showProjectTemplatesDialog() })
                settings.add(ConfigItem("Manage Tags", "Customize UI, Logic, Bug tags") { showManageTagsDialog() })
                settings.add(ConfigItem("Theme Color", "Customize creating projects theme") { showColorPickerDialog("ADD_PROJECT") })
                settings.add(ConfigItem("Roadmaps Section", "Show/Hide Project Roadmaps", isToggle = true, isChecked = DataManager.projectRoadmapsEnabled) {
                    if (DataManager.projectRoadmapsEnabled && !DataManager.projectIdeasEnabled) {
                        Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
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
            }
            "SECURITY" -> {
                settings.add(ConfigItem("Biometric Lock", "Require Fingerprint/Face ID to enter", isToggle = true, isChecked = DataManager.isAppLockEnabled) {
                    DataManager.isAppLockEnabled = !DataManager.isAppLockEnabled
                })
                settings.add(ConfigItem("OLED Mode", "Pure black theme for OLED screens", isToggle = true, isChecked = DataManager.isOledThemeEnabled) {
                    DataManager.isOledThemeEnabled = !DataManager.isOledThemeEnabled
                })
            }
            "OTHERS" -> {
                settings.add(ConfigItem("Export Backup", "Save all data to a local JSON file") { exportBackup() })
                settings.add(ConfigItem("Import Backup", "Restore data from a JSON file") { importBackup() })
                settings.add(ConfigItem("System Deep Clean", "Clear old history and cache") {
                    showConfirmationDialog(
                        "SYSTEM DEEP CLEAN",
                        "This will permanently delete project change history and clear temporary cache. Are you sure you want to proceed?",
                        "CLEAN NOW"
                    ) {
                        DataManager.notes.forEach { it.changeHistory.clear() }
                        DataManager.saveData(this)
                        Toast.makeText(this, "System Deep Clean Complete!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            "APPEARANCE" -> {
                val menuItems = listOf(
                    SettingsHubItem("Section Icons", "Manage section and item icons", R.drawable.ic_habit_tracker, "APPEARANCE_ICONS"),
                    SettingsHubItem("Section Colors", "Manage section theme colors", R.drawable.ic_project, "APPEARANCE_COLORS"),
                    SettingsHubItem("Add Feature", "Custom section features", R.drawable.ic_habit_tracker, "APPEARANCE_ADD_FEATURE"),
                    SettingsHubItem("Color Management", "Custom section colors", R.drawable.ic_project, "APPEARANCE_COLOR"),
                    SettingsHubItem("Icon Management", "Custom section icons", R.drawable.ic_habit_tracker, "APPEARANCE_ICON")
                )
                settingsList.adapter = SettingsHubAdapter(menuItems) { section ->
                    showSectionSettings(section)
                }
                return
            }
            "APPEARANCE_ADD_FEATURE" -> {
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
                settings.add(ConfigItem("RESET ALL ICONS", "Restore defaults") {
                    showConfirmationDialog("RESET ICONS", "Reset all icons?", "RESET") {
                        DataManager.resetAppearanceIcons(); DataManager.saveData(this); showSectionSettings("APPEARANCE_ICONS")
                    }
                })
                settings.add(ConfigItem("Habit Icon", "Change default habit icon") { showIconPickerDialog("HABIT") })
                settings.add(ConfigItem("Workout Icon", "Change default workout icon") { showIconPickerDialog("WORKOUT") })
                settings.add(ConfigItem("Task Icon", "Change default task icon") { showIconPickerDialog("TASK") })
                settings.add(ConfigItem("Project Icon", "Change default project icon") { showIconPickerDialog("PROJECT") })
                settings.add(ConfigItem("Note Icon", "Change default note icon") { showIconPickerDialog("NOTE") })
                settings.add(ConfigItem("Finance Icon", "Change default finance icon") { showIconPickerDialog("FINANCE") })
            }
            "APPEARANCE_COLORS" -> {
                settings.add(ConfigItem("RESET ALL COLORS", "Restore defaults") {
                    showConfirmationDialog("RESET COLORS", "Reset all colors?", "RESET") {
                        DataManager.resetAppearanceColors(); DataManager.saveData(this); showSectionSettings("APPEARANCE_COLORS")
                    }
                })
                settings.add(ConfigItem("Habit Section Color", "Change theme color for Habits") { showColorPickerDialog("HABIT") })
                settings.add(ConfigItem("Workout Section Color", "Change theme color for Workouts") { showColorPickerDialog("WORKOUT") })
                settings.add(ConfigItem("Task Section Color", "Change theme color for Tasks") { showColorPickerDialog("TASK") })
                settings.add(ConfigItem("Project Section Color", "Change theme color for Projects") { showColorPickerDialog("PROJECT") })
                settings.add(ConfigItem("Note Section Color", "Change theme color for Notes") { showColorPickerDialog("NOTE") })
                settings.add(ConfigItem("Finance Section Color", "Change theme color for Finance") { showColorPickerDialog("FINANCE") })
            }
            "HELP", "HELP_GUIDE" -> {
                val guides = listOf("HABITS", "WORKOUTS", "TASKS", "PROJECTS", "NOTES", "FINANCE", "OTHERS")
                guides.forEach { guide ->
                    settings.add(ConfigItem("${guide.substring(0,1)}${guide.substring(1).lowercase()} Guide", "Detailed instructions for $guide") { showHelpDetail(guide) })
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

    private fun showAvatarOptionsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        
        title.text = "SELECT AVATAR STYLE"
        
        val icons = listOf(R.drawable.icons8_profile_100, R.drawable.icons8_profile_100_2)
        
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

    private fun showHelpDetail(section: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_help_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_help_title)
        val tvContent = dialog.findViewById<TextView>(R.id.tv_help_content)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_help)
        tvTitle.text = "$section GUIDE"
        val contentHtml = when(section) {
            "HABITS" -> "<b>1. BUILDING RITUALS:</b> Create recurring daily goals.<br><br><b>2. STREAK SYSTEM:</b> Grow your discipline.<br><br><b>3. VACATION MODE:</b> Pause progress while away."
            "WORKOUTS" -> "<b>1. ROUTINE MANAGEMENT:</b> Add exercises.<br><br><b>2. MUSCLE GROUPS:</b> Tag specific body parts.<br><br><b>3. REST TIMER:</b> Automated alerts."
            "TASKS" -> "<b>1. SMART LISTS:</b> Categorize by Priority.<br><br><b>2. AUTO-ARCHIVE:</b> Keep list clean."
            "PROJECTS" -> "<b>1. ROADMAP BOARDS:</b> Break goals into sub-features.<br><br><b>2. SEQUENCING:</b> Reorder with Number Roller."
            "NOTES" -> "<b>1. SMART TEMPLATES:</b> Pre-filled logs.<br><br><b>2. PRIVACY:</b> Hide sensitive data."
            "FINANCE" -> "<b>1. BUDGETING:</b> Set limits.<br><br><b>2. INDEPENDENT LEDGERS:</b> Person-based tracking."
            "OTHERS" -> "<b>1. DATA GOVERNANCE:</b> Export/Import backups.<br><br><b>2. UI ARCHITECTURE:</b> Toggle sections."
            else -> "Feature guide coming soon."
        }
        tvContent.text = android.text.Html.fromHtml(contentHtml, android.text.Html.FROM_HTML_MODE_LEGACY)
        btnClose.setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    private fun showConfirmationDialog(title: String, message: String, pos: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        val btnPos = dialog.findViewById<TextView>(R.id.btn_confirm_positive)
        btnPos.text = pos; btnPos.setOnClickListener { onConfirm(); dialog.dismiss() }
        dialog.findViewById<View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showColorPickerDialog(section: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_settings_color_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val grid = dialog.findViewById<GridLayout>(R.id.color_grid)
        val title = dialog.findViewById<TextView>(R.id.tv_picker_title)
        title.text = "SELECT COLOR: $section"
        val colors = listOf(Color.parseColor("#FF7A59"), Color.parseColor("#FFB800"), Color.parseColor("#2EC4B6"), Color.parseColor("#1A73E8"), Color.parseColor("#E91E63"))
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
                    }
                    DataManager.saveData(this@SettingsActivity); dialog.dismiss(); showSectionSettings(currentPath)
                }
            }
            grid.addView(v)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    private fun showAddCustomColorDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_add_custom_color)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val et = dialog.findViewById<EditText>(R.id.et_hex_code)
        dialog.findViewById<View>(R.id.btn_add_hex).setOnClickListener {
            try { val c = Color.parseColor(et.text.toString()); DataManager.userCustomColors.add(c); DataManager.saveData(this); showSectionSettings("APPEARANCE_COLOR"); dialog.dismiss() } catch(e: Exception) {}
        }
        dialog.show()
    }

    private fun showIconPickerDialog(section: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val icons = listOf(R.drawable.ic_habit_tracker, R.drawable.ic_workout_routine, R.drawable.ic_todo_list, R.drawable.ic_notes, R.drawable.ic_project, R.drawable.ic_finance)
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

    private fun showBehavioralInsightsDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val sub = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        dialog.findViewById<TextView>(R.id.tv_dialog_title).text = "BEHAVIORAL INSIGHTS"
        dialog.findViewById<View>(R.id.et_budget_amount).visibility = View.GONE
        val stats = DataManager.getHabitPerformanceByFrequency()
        val sb = StringBuilder(); stats.forEach { (f, r) -> if (r >= 0) sb.append("$f: $r%\n") }
        sub.text = if (sb.isEmpty()) "Keep tracking to see insights!" else sb.toString()
        dialog.findViewById<TextView>(R.id.btn_save_budget).text = "CLOSE"
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    private fun showManageMuscleGroupsDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        fun refresh() {
            container.removeAllViews()
            DataManager.workoutMuscleGroups.forEach { g ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = g
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { DataManager.workoutMuscleGroups.remove(g); DataManager.saveData(this); refresh() }
                container.addView(iv)
            }
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim(); if (n.isNotEmpty()) { DataManager.workoutMuscleGroups.add(n); DataManager.saveData(this); et.text.clear(); refresh() }
        }
        refresh(); dialog.show()
    }

    private fun showWorkoutReadinessDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.id.tv_title) // Reusing simple view
        Toast.makeText(this, "Survey Coming Soon", Toast.LENGTH_SHORT).show(); dialog.dismiss()
    }

    private fun showManageTaskCategoriesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        fun refresh() {
            container.removeAllViews()
            DataManager.taskCustomCategories.forEach { c ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = c
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { DataManager.taskCustomCategories.remove(c); DataManager.saveData(this); refresh() }
                container.addView(iv)
            }
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim(); if (n.isNotEmpty()) { DataManager.taskCustomCategories.add(n); DataManager.saveData(this); et.text.clear(); refresh() }
        }
        refresh(); dialog.show()
    }

    private fun showTaskAnalyticsDialog() {
        Toast.makeText(this, "Rate: ${DataManager.getGlobalCompletionRate()}%", Toast.LENGTH_SHORT).show()
    }

    private fun showNoteTemplatesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        DataManager.noteTemplates.keys.forEach { name ->
            val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
            iv.findViewById<TextView>(R.id.tv_category_name).text = name
            iv.findViewById<View>(R.id.btn_remove_category).visibility = View.GONE
            iv.setOnClickListener { showSingleTemplateEditor(name); dialog.dismiss() }
            container.addView(iv)
        }
        dialog.show()
    }

    private fun showSingleTemplateEditor(name: String) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setText(DataManager.noteTemplates[name]); et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener { DataManager.noteTemplates[name] = et.text.toString(); DataManager.saveData(this); dialog.dismiss() }
        dialog.show()
    }

    private fun showNoteBulkMoveDialog() {
        Toast.makeText(this, "Select category to move all notes", Toast.LENGTH_SHORT).show()
    }

    private fun showManageFinanceCategoriesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        fun refresh() {
            container.removeAllViews()
            DataManager.financeCustomCategories.forEach { c ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = c
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { DataManager.financeCustomCategories.remove(c); DataManager.saveData(this); refresh() }
                container.addView(iv)
            }
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim(); if (n.isNotEmpty()) { DataManager.financeCustomCategories.add(n); DataManager.saveData(this); et.text.clear(); refresh() }
        }
        refresh(); dialog.show()
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setText(DataManager.monthlyBudget.toString())
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.monthlyBudget = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this); dialog.dismiss(); showSectionSettings("FINANCE")
        }
        dialog.show()
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setText(DataManager.monthlySavingsGoal.toString())
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.monthlySavingsGoal = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this); dialog.dismiss(); showSectionSettings("FINANCE")
        }
        dialog.show()
    }

    private fun showProjectTemplatesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        fun refresh() {
            container.removeAllViews()
            DataManager.projectTemplates.keys.forEach { t ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = t
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { DataManager.projectTemplates.remove(t); DataManager.saveData(this); refresh() }
                container.addView(iv)
            }
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim(); if (n.isNotEmpty()) showCreateTemplateStepsDialog(n) { refresh(); et.text.clear() }
        }
        refresh(); dialog.show()
    }

    private fun showCreateTemplateStepsDialog(n: String, c: () -> Unit) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category); et.hint = "Step Name"
        val steps = mutableListOf<String>()
        fun refresh() {
            container.removeAllViews()
            steps.forEach { s ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = s
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { steps.remove(s); refresh() }
                container.addView(iv)
            }
            val btn = Button(this); btn.text = "SAVE"; btn.setOnClickListener { DataManager.projectTemplates[n] = steps; DataManager.saveData(this); c(); dialog.dismiss() }
            container.addView(btn)
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener { val s = et.text.toString().trim(); if (s.isNotEmpty()) { steps.add(s); et.text.clear(); refresh() } }
        refresh(); dialog.show()
    }

    private fun showManageTagsDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        fun refresh() {
            container.removeAllViews()
            DataManager.projectCustomTags.forEach { t ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = t
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { DataManager.projectCustomTags.remove(t); DataManager.saveData(this); refresh() }
                container.addView(iv)
            }
        }
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim().uppercase(); if (n.isNotEmpty()) { DataManager.projectCustomTags.add(n); DataManager.saveData(this); et.text.clear(); refresh() }
        }
        refresh(); dialog.show()
    }

    data class SettingsHubItem(val title: String, val description: String, val iconRes: Int, val sectionKey: String)
    data class ConfigItem(val title: String, val summary: String = "", val isToggle: Boolean = false, var isChecked: Boolean = false, val action: () -> Unit)

    inner class SettingsHubAdapter(private val items: List<SettingsHubItem>, private val onSelect: (String) -> Unit) : RecyclerView.Adapter<SettingsHubAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_settings_hub, p, false))
        override fun onBindViewHolder(h: ViewHolder, pos: Int) {
            val i = items[pos]; h.title.text = i.title; h.description.text = i.description; h.icon.setImageResource(i.iconRes)
            h.itemView.setOnClickListener { onSelect(i.sectionKey) }
        }
        override fun getItemCount() = items.size
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_item_title)
            val description: TextView = v.findViewById(R.id.tv_item_description)
            val icon: ImageView = v.findViewById(R.id.iv_item_icon)
        }
    }

    inner class ConfigAdapter(private val items: List<ConfigItem>, private val onAnyChange: () -> Unit) : RecyclerView.Adapter<ConfigAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p: ViewGroup, v: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_config_row, p, false))
        override fun onBindViewHolder(h: ViewHolder, pos: Int) {
            val i = items[pos]; h.title.text = i.title; h.summary.text = i.summary
            h.switch.visibility = if (i.isToggle) View.VISIBLE else View.GONE
            h.switch.isChecked = i.isChecked; h.itemView.findViewById<View>(R.id.iv_chevron).visibility = if (i.isToggle) View.GONE else View.VISIBLE
            h.itemView.setOnClickListener {
                i.action()
                if (i.isToggle) { i.isChecked = !i.isChecked; h.switch.isChecked = i.isChecked }
                onAnyChange()
            }
        }
        override fun getItemCount() = items.size
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_config_title)
            val summary: TextView = v.findViewById(R.id.tv_config_summary)
            val switch: SwitchCompat = v.findViewById(R.id.sw_config_toggle)
        }
    }
}