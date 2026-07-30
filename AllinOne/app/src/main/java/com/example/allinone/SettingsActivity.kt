package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsViewModel by viewModels()
    
    private lateinit var hubSection: SettingsHubSection
    private lateinit var appearanceHandler: SettingsAppearanceHandler
    private lateinit var helpHandler: SettingsHelpHandler
    private lateinit var backupHandler: SettingsBackupHandler

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            backupHandler.getExportedJson { json ->
                lifecycleScope.launch {
                    try {
                        contentResolver.openOutputStream(it)?.use { it.write(json.toByteArray()) }
                        Toast.makeText(this@SettingsActivity, "Backup Saved", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) { Toast.makeText(this@SettingsActivity, "Export Failed", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                val content = contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
                if (content != null) backupHandler.handleImport(content) { hubSection.showHub() }
            } catch (e: Exception) { Toast.makeText(this, "Read Failed", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<RecyclerView>(R.id.settings_list).layoutManager = LinearLayoutManager(this)

        initHandlers()
        setupLogic()

        if (viewModel.currentPath == "HUB") showHub() else showSectionSettings(viewModel.currentPath)
    }

    private fun showHub() {
        viewModel.currentPath = "HUB"
        findViewById<TextView>(R.id.tv_title).text = "APP SETTINGS"
        hubSection.updateMiniProfileUI()
        hubSection.showHub()
    }

    private fun initHandlers() {
        hubSection = SettingsHubSection(this, findViewById(R.id.settings_list), findViewById(R.id.layout_profile_hub), { showSectionSettings(it) }, { showAvatarOptionsDialog() })
        appearanceHandler = SettingsAppearanceHandler(this) { recreate() }
        helpHandler = SettingsHelpHandler(this)
        backupHandler = SettingsBackupHandler(this, exportLauncher, importLauncher, lifecycleScope)
    }

    private fun setupLogic() {
        findViewById<View>(R.id.btn_back).setOnClickListener { handleBackNavigation() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackNavigation() }
        })
        setupKeyboardHandling(findViewById(R.id.settings_root_layout), findViewById(R.id.settings_content_container))
    }

    private fun handleBackNavigation() {
        when (viewModel.currentPath) {
            "HUB" -> finish()
            "APPEARANCE_ICONS", "APPEARANCE_COLORS", "APPEARANCE_ADD_FEATURE", "APPEARANCE_COLOR", "APPEARANCE_ICON" -> showSectionSettings("APPEARANCE")
            else -> showHub()
        }
    }

    private fun showSectionSettings(section: String) {
        viewModel.currentPath = section
        findViewById<TextView>(R.id.tv_title).text = section.replace("_", " ").uppercase()
        findViewById<View>(R.id.layout_profile_hub).visibility = View.GONE

        val settings = mutableListOf<ConfigItem>()
        when(section) {
            "SECURITY" -> {
                settings.add(ConfigItem("App Access Lock", "Require PIN", isToggle = true, isChecked = DataManager.isAppLockEnabled) {
                    if (!DataManager.isAppLockEnabled && DataManager.appLockPin == null) startActivity(Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) })
                    else { DataManager.isAppLockEnabled = !DataManager.isAppLockEnabled; DataManager.saveData(this); showSectionSettings("SECURITY") }
                })
                if (DataManager.isAppLockEnabled && DataManager.appLockPin != null) {
                    settings.add(ConfigItem("Change PIN", "Update security code") { startActivity(Intent(this, LockActivity::class.java).apply { putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_CHANGE) }) })
                    settings.add(ConfigItem("Biometric Unlock", "Use Fingerprint/Face", isToggle = true, isChecked = DataManager.isBiometricLockEnabled) {
                        DataManager.isBiometricLockEnabled = !DataManager.isBiometricLockEnabled
                        DataManager.saveData(this)
                        showSectionSettings("SECURITY")
                    })
                }
                settings.add(ConfigItem("Screen Protection", "Block screenshots & recording", isToggle = true, isChecked = DataManager.isScreenshotProtectionEnabled) {
                    DataManager.isScreenshotProtectionEnabled = !DataManager.isScreenshotProtectionEnabled
                    DataManager.saveData(this)
                    SecurityManager.setScreenshotProtection(this, DataManager.isScreenshotProtectionEnabled)
                    showSectionSettings("SECURITY")
                })
            }
            "OTHERS" -> {
                settings.add(ConfigItem("Offline Integrity", "No Internet permission requested", isHeader = true))
                settings.add(ConfigItem("Strictly Offline", "App data is saved locally on your device") { })
                settings.add(ConfigItem("Home Page Sections", "Customize dashboard visibility") { showHomePageSectionsDialog() })
                settings.add(ConfigItem("Startup Loading Time", "Current: ${DataManager.startupLoadingTime/1000.0}s") { showLoadingTimeSliderDialog() })
                settings.add(ConfigItem("Export Backup", "Save to JSON") { backupHandler.exportBackup() })
                settings.add(ConfigItem("Import Backup", "Restore from JSON") { backupHandler.importBackup() })
            }
            "APPEARANCE" -> {
                settings.add(ConfigItem("Global Scaling", isHeader = true))
                settings.add(ConfigItem("Follow System Settings", "Sync display and font size with phone", isToggle = true, isChecked = DataManager.isSystemAppearanceEnabled) {
                    DataManager.isSystemAppearanceEnabled = !DataManager.isSystemAppearanceEnabled
                    DataManager.saveData(this)
                    recreate()
                })
                settings.add(ConfigItem("Current Focus Size", "Circle scale for mood logging (Current: ${DataManager.homeFocusSize})", options = listOf("S", "M", "L", "XL"), selectedIndex = listOf("S", "M", "L", "XL").indexOf(DataManager.homeFocusSize), onOptionSelected = { i ->
                    DataManager.homeFocusSize = listOf("S", "M", "L", "XL")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))
                settings.add(ConfigItem("Global Display Size", "Icons and margins for all sub-sections (Current: ${DataManager.displaySize})", options = listOf("S", "M", "L", "XL"), selectedIndex = listOf("S", "M", "L", "XL").indexOf(DataManager.displaySize), onOptionSelected = { i ->
                    DataManager.displaySize = listOf("S", "M", "L", "XL")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))
                settings.add(ConfigItem("Home Page Display Size", "Dedicated scale for the main dashboard (Current: ${DataManager.homeDisplaySize})", options = listOf("S", "M", "L", "XL"), selectedIndex = listOf("S", "M", "L", "XL").indexOf(DataManager.homeDisplaySize), onOptionSelected = { i ->
                    DataManager.homeDisplaySize = listOf("S", "M", "L", "XL")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))
                settings.add(ConfigItem("Text Font Size", "Scaling for titles and content (Current: ${DataManager.fontSize})", options = listOf("XS", "S", "M", "L", "XL"), selectedIndex = listOf("XS", "S", "M", "L", "XL").indexOf(DataManager.fontSize), onOptionSelected = { i ->
                    DataManager.fontSize = listOf("XS", "S", "M", "L", "XL")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))

                settings.add(ConfigItem("Advanced Look & Feel", isHeader = true))
                settings.add(ConfigItem("Dynamic Coloring", "Use wallpaper colors (Android 12+)", isToggle = true, isChecked = DataManager.isDynamicColorEnabled) {
                    DataManager.isDynamicColorEnabled = !DataManager.isDynamicColorEnabled
                    DataManager.saveData(this)
                    recreate()
                })
                settings.add(ConfigItem("Theme Mode", "Override system theme (Current: ${DataManager.appThemeMode})", options = listOf("LIGHT", "DARK", "OLED"), selectedIndex = listOf("LIGHT", "DARK", "OLED").indexOf(DataManager.appThemeMode), onOptionSelected = { i ->
                    DataManager.appThemeMode = listOf("LIGHT", "DARK", "OLED")[i]
                    DataManager.saveData(this)
                    recreate()
                }))
                settings.add(ConfigItem("Accent Color", "Custom highlights app-wide") { appearanceHandler.showColorPickerDialog("APP_ACCENT") { showSectionSettings("APPEARANCE") } })
                settings.add(ConfigItem("Border Radius", "Curvature for cards and buttons (Current: ${DataManager.appBorderRadius}dp)") { appearanceHandler.showBorderRadiusSliderDialog() })
                settings.add(ConfigItem("Card Style", "Surface appearance (Current: ${DataManager.appCardStyle})", options = listOf("GLASS", "MATERIAL", "FLAT"), selectedIndex = listOf("GLASS", "MATERIAL", "FLAT").indexOf(DataManager.appCardStyle), onOptionSelected = { i ->
                    DataManager.appCardStyle = listOf("GLASS", "MATERIAL", "FLAT")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))
                settings.add(ConfigItem("Font Family", "Change typography style (Current: ${DataManager.appFontFamily})", options = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE"), selectedIndex = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE").indexOf(DataManager.appFontFamily), onOptionSelected = { i ->
                    DataManager.appFontFamily = listOf("DEFAULT", "SERIF", "SANS_SERIF", "MONOSPACE")[i]
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                }))
                settings.add(ConfigItem("Show Shadows", "Toggle UI depth and elevation", isToggle = true, isChecked = DataManager.appShowShadows) {
                    DataManager.appShowShadows = !DataManager.appShowShadows
                    DataManager.saveData(this)
                    showSectionSettings("APPEARANCE")
                })

                settings.add(ConfigItem("Legacy Settings", isHeader = true))
                settings.add(ConfigItem("Section Icons", "Manage icons") { showSectionSettings("APPEARANCE_ICONS") })
                settings.add(ConfigItem("Section Colors", "Manage colors") { showSectionSettings("APPEARANCE_COLORS") })
            }
            "APPEARANCE_ICONS" -> {
                listOf("HABIT", "WORKOUT", "TASK", "PROJECT", "NOTE", "FINANCE").forEach { settings.add(ConfigItem("$it Icon", "Change default") { appearanceHandler.showIconPickerDialog(it, section) { showSectionSettings("APPEARANCE_ICONS") } }) }
            }
            "APPEARANCE_COLORS" -> {
                listOf("HABIT", "WORKOUT", "TASK", "PROJECT", "NOTE", "FINANCE").forEach { settings.add(ConfigItem("$it Theme", "Change color") { appearanceHandler.showColorPickerDialog(it) { showSectionSettings("APPEARANCE_COLORS") } }) }
            }
            "HELP" -> {
                HelpData.getMasterGuides().forEach { a -> settings.add(ConfigItem(a.title, a.summary) { helpHandler.showMasterGuideDetail(a) }) }
                listOf("HABITS", "WORKOUTS", "TASKS", "PROJECTS", "NOTES", "FINANCE").forEach { g -> settings.add(ConfigItem("$g Guide", "Walkthrough") { helpHandler.showHelpDetail(g) }) }
            }
        }
        findViewById<RecyclerView>(R.id.settings_list).adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showHomePageSectionsDialog() {
        val d = Dialog(this)
        d.setContentView(R.layout.dialog_manage_sections)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        d.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = d.findViewById<LinearLayout>(R.id.container_section_switches)
        val sections = listOf(
            "Habits" to { DataManager.showHabitSection } to { v: Boolean -> DataManager.showHabitSection = v },
            "Workouts" to { DataManager.showWorkoutSection } to { v: Boolean -> DataManager.showWorkoutSection = v },
            "Tasks" to { DataManager.showTaskSection } to { v: Boolean -> DataManager.showTaskSection = v },
            "Notes" to { DataManager.showNoteSection } to { v: Boolean -> DataManager.showNoteSection = v },
            "Projects" to { DataManager.showProjectSection } to { v: Boolean -> DataManager.showProjectSection = v },
            "Finance" to { DataManager.showFinanceSection } to { v: Boolean -> DataManager.showFinanceSection = v }
        )

        sections.forEach { pair ->
            val name = pair.first.first
            val getter = pair.first.second
            val setter = pair.second

            val row = LayoutInflater.from(this).inflate(R.layout.item_manage_section_row, container, false)
            row.findViewById<TextView>(R.id.tv_section_name).text = name
            val sw = row.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.sw_section_toggle)
            sw.isChecked = getter()
            sw.setOnCheckedChangeListener { _, isChecked -> setter(isChecked) }
            container.addView(row)
        }

        d.findViewById<View>(R.id.btn_save_sections).setOnClickListener {
            DataManager.saveData(this)
            d.dismiss()
        }
        d.show()
    }

    private fun showLoadingTimeSliderDialog() {
        val d = Dialog(this); d.setContentView(R.layout.dialog_settings_slider_loading)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val slider = d.findViewById<SeekBar>(R.id.settings_slider)
        val valueText = d.findViewById<TextView>(R.id.tv_slider_value)
        d.findViewById<TextView>(R.id.tv_slider_title).text = "STARTUP LOADING TIME"
        slider.max = 40; slider.progress = (DataManager.startupLoadingTime - 1000) / 100
        valueText.text = "${DataManager.startupLoadingTime / 1000.0}s"
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { valueText.text = "${(1000 + p * 100) / 1000.0}s" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        d.findViewById<View>(R.id.btn_save_slider).setOnClickListener { DataManager.startupLoadingTime = 1000 + (slider.progress * 100); DataManager.saveData(this); d.dismiss(); showSectionSettings("OTHERS") }
        d.show()
    }

    private fun showAvatarOptionsDialog() {
        val d = Dialog(this); d.setContentView(R.layout.dialog_manage_cat_settings)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val container = d.findViewById<LinearLayout>(R.id.categories_container)
        d.findViewById<TextView>(R.id.tv_categories_title).text = "SELECT AVATAR"
        
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER }
        listOf(R.drawable.boy_avatar_profile, R.drawable.girl_avatar_profile).forEach { res ->
            row.addView(ImageView(this).apply {
                val s = (80 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(s, s).apply { setMargins(24, 24, 24, 24) }
                setImageResource(res); setOnClickListener { DataManager.userAvatarRes = res; DataManager.saveData(this@SettingsActivity); hubSection.updateMiniProfileUI(); d.dismiss() }
            })
        }
        container.removeAllViews(); container.addView(row); d.show()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentPath == "HUB") hubSection.updateMiniProfileUI()
    }
}
