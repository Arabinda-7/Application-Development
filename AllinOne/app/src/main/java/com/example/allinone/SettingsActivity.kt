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
            lifecycleScope.launch {
                try {
                    val json = DataManager.exportData(this@SettingsActivity)
                    contentResolver.openOutputStream(it)?.use { it.write(json.toByteArray()) }
                    Toast.makeText(this@SettingsActivity, "Backup Saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this@SettingsActivity, "Export Failed", Toast.LENGTH_SHORT).show() }
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

        if (viewModel.currentPath == "HUB") hubSection.setup() else showSectionSettings(viewModel.currentPath)
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
            else -> hubSection.showHub().also { viewModel.currentPath = "HUB" }
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
                }
                settings.add(ConfigItem("OLED Mode", "Pure black theme", isToggle = true, isChecked = DataManager.isOledThemeEnabled) { DataManager.isOledThemeEnabled = !DataManager.isOledThemeEnabled; DataManager.saveData(this) })
            }
            "OTHERS" -> {
                settings.add(ConfigItem("Startup Loading Time", "Current: ${DataManager.startupLoadingTime/1000.0}s") { showLoadingTimeSliderDialog() })
                settings.add(ConfigItem("Export Backup", "Save to JSON") { backupHandler.exportBackup() })
                settings.add(ConfigItem("Import Backup", "Restore from JSON") { backupHandler.importBackup() })
            }
            "APPEARANCE" -> {
                settings.add(ConfigItem("Section Icons", "Manage icons") { showSectionSettings("APPEARANCE_ICONS") })
                settings.add(ConfigItem("Section Colors", "Manage colors") { showSectionSettings("APPEARANCE_COLORS") })
                settings.add(ConfigItem("Accent Color", "Custom highlights") { appearanceHandler.showColorPickerDialog("APP_ACCENT") { showSectionSettings("APPEARANCE") } })
                settings.add(ConfigItem("Border Radius", "Curvature: ${DataManager.appBorderRadius}dp") { appearanceHandler.showBorderRadiusSliderDialog() })
                settings.add(ConfigItem("Theme Mode", "LIGHT/DARK/OLED", options = listOf("LIGHT", "DARK", "OLED"), selectedIndex = listOf("LIGHT", "DARK", "OLED").indexOf(DataManager.appThemeMode), onOptionSelected = { i -> DataManager.appThemeMode = listOf("LIGHT", "DARK", "OLED")[i]; DataManager.saveData(this); recreate() }))
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
        val d = Dialog(this); d.setContentView(R.layout.dialog_manage_categories_appearance)
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
