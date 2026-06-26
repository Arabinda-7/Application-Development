package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView
    private var currentPath: String = "HUB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        settingsList.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.btn_back).setOnClickListener { 
            if (currentPath == "HUB") {
                finish()
            } else {
                showHub()
            }
        }

        showHub()
    }

    private fun showHub() {
        currentPath = "HUB"
        tvTitle.text = "APP SETTINGS"
        
        val menuItems = listOf(
            SettingsHubItem("Habit Tracker", "Manage your daily rituals and streaks", R.drawable.ic_habit_tracker, "HABITS"),
            SettingsHubItem("Workout Routine", "Configure exercises and rest timers", R.drawable.ic_workout_routine, "WORKOUTS"),
            SettingsHubItem("To-Do List", "Organize tasks and prioritization", R.drawable.ic_todo_list, "TASKS"),
            SettingsHubItem("Notes", "Manage categories and writing templates", R.drawable.ic_notes, "NOTES"),
            SettingsHubItem("Finance", "Setup currency and budget goals", R.drawable.ic_finance, "FINANCE"),
            SettingsHubItem("Projects", "Advanced roadmap and project settings", R.drawable.ic_project, "PROJECTS"),
            SettingsHubItem("App Security", "Biometric lock and privacy settings", R.drawable.baseline_settings_24, "SECURITY")
        )

        settingsList.adapter = SettingsHubAdapter(menuItems) { section ->
            showSectionSettings(section)
        }
    }

    private fun showSectionSettings(section: String) {
        currentPath = section
        tvTitle.text = section.replace("_", " ") + " SETTINGS"
        
        val settings = mutableListOf<ConfigItem>()
        
        when(section) {
            "HABITS" -> {
                settings.add(ConfigItem("Behavioral Insights", "Peak performance analytics") {
                    showBehavioralInsightsDialog()
                })
                settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.habitDefaultTab}") {
                    DataManager.habitDefaultTab = if (DataManager.habitDefaultTab == "TODAY") "HISTORY" else "TODAY"
                    showSectionSettings("HABITS")
                })
                settings.add(ConfigItem("Sort Order", "Current: ${DataManager.habitSortOrder}") {
                    DataManager.habitSortOrder = if (DataManager.habitSortOrder == "Time") "Streak" else "Time"
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
                settings.add(ConfigItem("Day Reset Hour", "Current: ${DataManager.habitDayResetHour}:00 AM") {
                    val options = listOf(0, 1, 2, 3, 4)
                    DataManager.habitDayResetHour = options[(options.indexOf(DataManager.habitDayResetHour) + 1) % options.size]
                    showSectionSettings("HABITS")
                })
                settings.add(ConfigItem("Grace Period", "Allowed misses: ${DataManager.habitGraceDaysAllowed} days") {
                    val options = listOf(0, 1, 2, 3)
                    DataManager.habitGraceDaysAllowed = options[(options.indexOf(DataManager.habitGraceDaysAllowed) + 1) % options.size]
                    showSectionSettings("HABITS")
                })
            }
            "WORKOUTS" -> {
                settings.add(ConfigItem("Manage Muscle Groups", "Add or remove body part tags") {
                    showManageMuscleGroupsDialog()
                })
                settings.add(ConfigItem("Workout Readiness", "Check your energy levels") {
                    showWorkoutReadinessDialog()
                })
                settings.add(ConfigItem("Auto-Rest Timer", "Trigger timer after set", isToggle = true, isChecked = DataManager.workoutAutoRestTimer) {
                    DataManager.workoutAutoRestTimer = !DataManager.workoutAutoRestTimer
                })
                settings.add(ConfigItem("Workout Weight Unit", "Current: ${DataManager.workoutWeightUnit}") {
                    DataManager.workoutWeightUnit = if (DataManager.workoutWeightUnit == "Kg") "Lb" else "Kg"
                    showSectionSettings("WORKOUTS")
                })
                settings.add(ConfigItem("Default Tracking Mode", "Current: ${DataManager.workoutDefaultMode}") {
                    val modes = listOf("Reps", "Sets", "Timer")
                    DataManager.workoutDefaultMode = modes[(modes.indexOf(DataManager.workoutDefaultMode) + 1) % modes.size]
                    showSectionSettings("WORKOUTS")
                })
                settings.add(ConfigItem("Rest Duration", "Current: ${DataManager.workoutRestDuration}s") {
                    val durations = listOf(30, 60, 90, 120, 180)
                    DataManager.workoutRestDuration = durations[(durations.indexOf(DataManager.workoutRestDuration) + 1) % durations.size]
                    showSectionSettings("WORKOUTS")
                })
            }
            "TASKS" -> {
                settings.add(ConfigItem("Manage Categories", "Customize your task tags") {
                    showManageTaskCategoriesDialog()
                })
                settings.add(ConfigItem("Task Analytics", "View completion totals") {
                    showTaskAnalyticsDialog()
                })
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
                settings.add(ConfigItem("Custom Templates", "Edit note pre-fill text") {
                    showNoteTemplatesDialog()
                })
                settings.add(ConfigItem("Bulk Category Move", "Move all notes at once") {
                    showNoteBulkMoveDialog()
                })
                settings.add(ConfigItem("Default Startup Tab", "Current: ${DataManager.noteDefaultCategory}") {
                    val categories = listOf("Notes", "Questions", "Daily", "Stories")
                    DataManager.noteDefaultCategory = categories[(categories.indexOf(DataManager.noteDefaultCategory) + 1) % categories.size]
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
                settings.add(ConfigItem("Manage Categories", "Add or remove expense tags") {
                    showManageFinanceCategoriesDialog()
                })
                settings.add(ConfigItem("Monthly Budget", "Current Goal: ${DataManager.financeCurrency}${DataManager.monthlyBudget}") {
                    showSetBudgetDialog()
                })
                settings.add(ConfigItem("Savings Goal", "Current Goal: ${DataManager.financeCurrency}${DataManager.monthlySavingsGoal}") {
                    showSetSavingsGoalDialog()
                })
            }
            "PROJECTS" -> {
                settings.add(ConfigItem("Manage Templates", "Edit roadmap pre-sets") {
                    showProjectTemplatesDialog()
                })
                settings.add(ConfigItem("Auto-Archive Projects", "Hide 100% completed boards", isToggle = true, isChecked = DataManager.projectAutoArchive) {
                    DataManager.projectAutoArchive = !DataManager.projectAutoArchive
                })
                settings.add(ConfigItem("Synergy Sync", "Bridge tasks with daily goals", isToggle = true, isChecked = DataManager.projectSynergySync) {
                    DataManager.projectSynergySync = !DataManager.projectSynergySync
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
                settings.add(ConfigItem("Export Backup", "Save all data to a JSON file") {
                    exportBackup()
                })
                settings.add(ConfigItem("Import Backup", "Restore data from a JSON file") {
                    importBackup()
                })
                settings.add(ConfigItem("Focus Mode", "Pause all alerts during quiet hours", isToggle = true, isChecked = false) {
                    // Logic for global DND
                    Toast.makeText(this, "Focus Mode Scheduled", Toast.LENGTH_SHORT).show()
                })
                settings.add(ConfigItem("System Deep Clean", "Clear old history and cache") {
                    showConfirmationDialog(
                        title = "SYSTEM DEEP CLEAN",
                        message = "This will permanently delete project change history and clear temporary cache. Are you sure you want to proceed?",
                        positiveButtonText = "CLEAN NOW",
                        onConfirm = {
                            DataManager.notes.forEach { it.changeHistory.clear() }
                            DataManager.saveData(this)
                            Toast.makeText(this, "System Deep Clean Complete!", Toast.LENGTH_SHORT).show()
                        }
                    )
                })
            }
        }
        
        settingsList.adapter = ConfigAdapter(settings) {
            DataManager.saveData(this)
        }
    }

    private fun exportBackup() {
        val json = DataManager.exportData()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "All-in-One Backup")
            putExtra(Intent.EXTRA_TEXT, json)
        }
        startActivity(Intent.createChooser(intent, "Save Backup"))
    }

    private fun importBackup() {
        // Implementation for importing JSON string via clipboard or file picker
        // For simplicity, showing a Toast for now
        Toast.makeText(this, "Import via JSON file: Coming soon in next update!", Toast.LENGTH_SHORT).show()
    }

    // --- ADVANCED DIALOGS MIGRATED FROM SECTIONS ---

    private fun showBehavioralInsightsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnAction = dialog.findViewById<TextView>(R.id.btn_save_budget)

        title.text = "BEHAVIORAL INSIGHTS"
        etInput.visibility = View.GONE
        
        val stats = DataManager.getHabitPerformanceByFrequency()
        val peak = stats.maxByOrNull { it.value }
        
        if (peak == null || peak.value <= 0) {
            subtext.text = "Not enough data yet. Keep tracking your habits to see your peak performance times!"
        } else {
            val sb = StringBuilder()
            sb.append("Your Peak Performance Time: ${peak.key.uppercase()}\n\n")
            stats.forEach { (freq, score) ->
                if (score >= 0) {
                    sb.append("$freq Habits: $score% Completion\n")
                }
            }
            subtext.text = sb.toString()
        }
        
        btnAction.text = "CLOSE"
        btnAction.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showManageMuscleGroupsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)

        title.text = "Manage Muscle Groups"

        fun refresh() {
            container.removeAllViews()
            DataManager.workoutMuscleGroups.forEach { group ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
                itemView.findViewById<TextView>(R.id.tv_header_title).text = group
                itemView.findViewById<View>(R.id.iv_header_chevron).visibility = View.GONE
                itemView.setOnLongClickListener {
                    DataManager.workoutMuscleGroups.remove(group)
                    DataManager.saveData(this)
                    refresh()
                    true
                }
                container.addView(itemView)
            }
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.workoutMuscleGroups.contains(name)) {
                DataManager.workoutMuscleGroups.add(name)
                DataManager.saveData(this)
                refresh()
                etNew.text.clear()
            }
        }
        refresh()
        dialog.show()
    }

    private fun showWorkoutReadinessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnAction = dialog.findViewById<TextView>(R.id.btn_save_budget)

        title.text = "READINESS CHECK"
        etInput.visibility = View.GONE
        subtext.text = "How are you feeling today?\n\n1. Did you sleep 7+ hours?\n2. Do you have high energy?"
        btnAction.text = "START SURVEY"

        var step = 1
        var score = 0
        btnAction.setOnClickListener {
            if (step == 1) {
                AlertDialog.Builder(this).setTitle("Sleep").setMessage("Did you sleep well?")
                    .setPositiveButton("Yes") { _, _ -> score += 50; step = 2; subtext.text = "Step 2: Check your energy levels." }
                    .setNegativeButton("No") { _, _ -> step = 2; subtext.text = "Step 2: Check your energy levels." }.show()
            } else if (step == 2) {
                AlertDialog.Builder(this).setTitle("Energy").setMessage("Ready for heavy lifting?")
                    .setPositiveButton("Yes") { _, _ -> 
                        score += 50; step = 3; title.text = "YOUR SCORE: $score%"; 
                        subtext.text = if (score >= 100) "You are fully ready! Crush it!" else "Ready to train!"; 
                        btnAction.text = "CLOSE" 
                    }
                    .setNegativeButton("No") { _, _ -> 
                        step = 3; title.text = "YOUR SCORE: $score%"; 
                        subtext.text = "Take it easy today."; 
                        btnAction.text = "CLOSE" 
                    }.show()
            } else {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showManageTaskCategoriesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)

        fun refresh() {
            container.removeAllViews()
            DataManager.taskCustomCategories.forEach { cat ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = cat
                itemView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
                    if (DataManager.taskCustomCategories.size > 1) {
                        DataManager.taskCustomCategories.remove(cat)
                        DataManager.saveData(this); refresh()
                    }
                }
                container.addView(itemView)
            }
        }
        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.taskCustomCategories.contains(name)) {
                DataManager.taskCustomCategories.add(name); DataManager.saveData(this); etNew.text.clear(); refresh()
            }
        }
        refresh()
        dialog.show()
    }

    private fun showTaskAnalyticsDialog() {
        val total = DataManager.tasks.size
        val completed = DataManager.tasks.count { it.isCompleted }
        val message = "Total Tasks: $total\nCompleted: $completed\nRate: ${if (total > 0) (completed * 100) / total else 0}%"

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_analytics_simple)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.findViewById<TextView>(R.id.tv_analytics_content).text = message
        dialog.findViewById<View>(R.id.btn_close_analytics).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showNoteTemplatesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        title.text = "Edit Note Templates"

        listOf("Daily", "Questions", "Stories").forEach { cat ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
            itemView.findViewById<TextView>(R.id.tv_header_title).text = cat
            itemView.findViewById<View>(R.id.iv_header_chevron).visibility = View.GONE
            itemView.setOnClickListener { 
                showSingleTemplateEditor(cat)
                dialog.dismiss() 
            }
            container.addView(itemView)
        }
        dialog.show()
    }

    private fun showSingleTemplateEditor(cat: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        dialog.findViewById<TextView>(R.id.tv_dialog_title).text = "$cat Template"
        dialog.findViewById<TextView>(R.id.tv_dialog_subtext).text = "Enter pre-fill text"
        et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        et.setText(DataManager.noteTemplates[cat] ?: "")
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.noteTemplates[cat] = et.text.toString()
            DataManager.saveData(this); dialog.dismiss()
        }
        dialog.show()
    }

    private fun showNoteBulkMoveDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        dialog.findViewById<TextView>(R.id.tv_categories_title).text = "Bulk Move Category"

        val categories = listOf("Notes", "Questions", "Daily", "Stories")
        categories.forEach { source ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
            itemView.findViewById<TextView>(R.id.tv_header_title).text = "Move FROM $source"
            itemView.setOnClickListener {
                showTargetSelectionForBulkMove(source)
                dialog.dismiss()
            }
            container.addView(itemView)
        }
        dialog.show()
    }

    private fun showTargetSelectionForBulkMove(source: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE
        dialog.findViewById<TextView>(R.id.tv_categories_title).text = "Move $source TO..."

        listOf("Notes", "Questions", "Daily", "Stories").filter { it != source }.forEach { target ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
            itemView.findViewById<TextView>(R.id.tv_header_title).text = "Move to $target"
            itemView.setOnClickListener {
                DataManager.notes.forEach { if (it.category == source) it.category = target }
                DataManager.saveData(this); dialog.dismiss()
                Toast.makeText(this, "Notes moved from $source to $target", Toast.LENGTH_SHORT).show()
            }
            container.addView(itemView)
        }
        dialog.show()
    }

    private fun showManageFinanceCategoriesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        title.text = "Manage Finance Categories"

        fun refresh() {
            container.removeAllViews()
            DataManager.financeCustomCategories.forEach { cat ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = cat
                itemView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
                    if (DataManager.financeCustomCategories.size > 1) {
                        DataManager.financeCustomCategories.remove(cat)
                        DataManager.saveData(this); refresh()
                    }
                }
                container.addView(itemView)
            }
        }
        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.financeCustomCategories.contains(name)) {
                DataManager.financeCustomCategories.add(name); DataManager.saveData(this); etNew.text.clear(); refresh()
            }
        }
        refresh()
        dialog.show()
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etInput = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        title.text = "SET MONTHLY BUDGET"
        etInput.setText(DataManager.monthlyBudget.toString())

        btnSave.setOnClickListener {
            val amount = etInput.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.monthlyBudget = amount
            DataManager.saveData(this)
            showSectionSettings("FINANCE")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etInput = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        title.text = "SET SAVINGS GOAL"
        subtext.text = "Enter your monthly target"
        etInput.setText(DataManager.monthlySavingsGoal.toString())

        btnSave.setOnClickListener {
            val amount = etInput.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.monthlySavingsGoal = amount
            DataManager.saveData(this)
            showSectionSettings("FINANCE")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showProjectTemplatesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)

        title.text = "Project Templates"
        etNew.hint = "Template Name..."

        fun refresh() {
            container.removeAllViews()
            DataManager.projectTemplates.keys.forEach { templateName ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = templateName
                
                // Show steps on tap
                itemView.setOnClickListener {
                    Toast.makeText(this, "Steps: ${DataManager.projectTemplates[templateName]?.joinToString(", ")}", Toast.LENGTH_LONG).show()
                }

                itemView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
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
        dialog.show()
    }

    private fun showCreateTemplateStepsDialog(templateName: String, onComplete: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etStep = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAddStep = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        
        // Add a "SAVE TEMPLATE" button at the bottom
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
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        title.text = "Add Steps for: $templateName"
        etStep.hint = "Step name (e.g. Design)..."
        
        val steps = mutableListOf<String>()

        fun refreshSteps() {
            container.removeAllViews()
            steps.forEach { step ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
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
        dialog.show()
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String = "PROCEED",
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_confirm_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_confirm_message)
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_confirm_negative)
        val btnPositive = dialog.findViewById<TextView>(R.id.btn_confirm_positive)

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = positiveButtonText

        btnNegative.setOnClickListener { dialog.dismiss() }
        btnPositive.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }
        dialog.show()
    }

    data class SettingsHubItem(val title: String, val description: String, val iconRes: Int, val sectionKey: String)
    data class ConfigItem(val title: String, val summary: String, val isToggle: Boolean = false, var isChecked: Boolean = false, val action: () -> Unit)

    class SettingsHubAdapter(private val items: List<SettingsHubItem>, private val onSelect: (String) -> Unit) :
        RecyclerView.Adapter<SettingsHubAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings_hub, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.description.text = item.description
            holder.icon.setImageResource(item.iconRes)
            holder.itemView.setOnClickListener { onSelect(item.sectionKey) }
        }

        override fun getItemCount() = items.size
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_settings_name)
            val description: TextView = view.findViewById(R.id.tv_settings_description)
            val icon: ImageView = view.findViewById(R.id.iv_settings_icon)
        }
    }

    class ConfigAdapter(private val items: List<ConfigItem>, private val onAnyChange: () -> Unit) :
        RecyclerView.Adapter<ConfigAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_config_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.summary.text = item.summary
            
            if (item.isToggle) {
                holder.switch.visibility = View.VISIBLE
                holder.switch.isChecked = item.isChecked
            } else {
                holder.switch.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { 
                item.action()
                if (item.isToggle) {
                    item.isChecked = !item.isChecked
                    holder.switch.isChecked = item.isChecked
                }
                onAnyChange()
            }
        }

        override fun getItemCount() = items.size
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_config_title)
            val summary: TextView = view.findViewById(R.id.tv_config_summary)
            val switch: SwitchCompat = view.findViewById(R.id.sw_config_toggle)
        }
    }
}
