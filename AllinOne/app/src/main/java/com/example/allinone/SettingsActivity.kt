package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserSettings
import com.example.allinone.security.SecurityManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    
    private lateinit var hubSection: SettingsHubSection
    private lateinit var appearanceHandler: SettingsAppearanceHandler
    private lateinit var helpHandler: SettingsHelpHandler
    private lateinit var backupHandler: SettingsBackupHandler
    private lateinit var securityHandler: SettingsSecurityHandler
    private lateinit var aiHandler: SettingsAiHandler
    @Inject lateinit var voiceManager: VoiceInteractionManager

    private val aiIntroLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val settings = viewModel.settings.value
            viewModel.updateSettings(settings.copy(isAiAssistantEnabled = true))
            showSectionSettings("AI_ASSISTANT")
        }
    }

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
        observeViewModel()
        
        showHub()
    }

    private fun showHub() {
        findViewById<TextView>(R.id.tv_title).text = "APP SETTINGS"
        hubSection.showHub()
        updateMiniProfile()
    }

    private fun updateMiniProfile() {
        val profile = profileViewModel.userProfile.value
        hubSection.updateMiniProfileUI(profile.name, profile.profileImageUri, profile.avatarRes)
    }

    private fun initHandlers() {
        hubSection = SettingsHubSection(this, findViewById(R.id.settings_list), findViewById(R.id.layout_profile_hub), { showSectionSettings(it) }, { showAvatarOptionsDialog() })
        appearanceHandler = SettingsAppearanceHandler(this) { recreate() }
        helpHandler = SettingsHelpHandler(this)
        backupHandler = SettingsBackupHandler(this, exportLauncher, importLauncher, lifecycleScope)
        securityHandler = SettingsSecurityHandler(this, viewModel) { showSectionSettings("SECURITY") }
        aiHandler = SettingsAiHandler(this, viewModel, aiIntroLauncher) { showSectionSettings("AI_ASSISTANT") }
    }

    private fun setupLogic() {
        findViewById<View>(R.id.btn_back).setOnClickListener { handleBackNavigation() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackNavigation() }
        })
        setupKeyboardHandling(findViewById(R.id.settings_root_layout), findViewById(R.id.settings_content_container))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.settings.collect { settings ->
                        // Refresh logic if needed
                    }
                }
                launch {
                    profileViewModel.userProfile.collect { profile ->
                        updateMiniProfile()
                    }
                }
            }
        }
    }

    private fun handleBackNavigation() {
        if (findViewById<View>(R.id.layout_profile_hub).visibility == View.VISIBLE) {
            finish()
        } else {
            showHub()
        }
    }

    private fun showSectionSettings(section: String) {
        findViewById<TextView>(R.id.tv_title).text = section.replace("_", " ").uppercase()
        findViewById<View>(R.id.layout_profile_hub).visibility = View.GONE

        val settings = viewModel.settings.value
        val configItems = mutableListOf<ConfigItem>()
        
        when(section) {
            "AI_ASSISTANT" -> configItems.addAll(aiHandler.getConfigItems(settings))
            "SECURITY" -> configItems.addAll(securityHandler.getConfigItems(settings))
            "OTHERS" -> {
                configItems.add(ConfigItem("Offline Integrity", "No Internet permission requested", isHeader = true))
                configItems.add(ConfigItem("Home Page Sections", "Customize dashboard visibility") { showHomePageSectionsDialog(settings) })
                configItems.add(ConfigItem("Export Backup", "Save to JSON") { backupHandler.exportBackup() })
                configItems.add(ConfigItem("Import Backup", "Restore from JSON") { backupHandler.importBackup() })
            }
            "APPEARANCE" -> {
                configItems.add(ConfigItem("Global Scaling", isHeader = true))
                configItems.add(ConfigItem("Follow System Settings", "Sync display and font size with phone", isToggle = true, isChecked = settings.isSystemAppearanceEnabled) {
                    viewModel.updateSettings(settings.copy(isSystemAppearanceEnabled = !settings.isSystemAppearanceEnabled))
                    recreate()
                })
                
                val scaleOptions = listOf("XS", "S", "M", "L", "XL")
                configItems.add(ConfigItem("Current Focus Size", "Circle scale for mood logging (Current: ${settings.homeFocusSize})", options = scaleOptions, selectedIndex = scaleOptions.indexOf(settings.homeFocusSize), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(homeFocusSize = scaleOptions[i]))
                    showSectionSettings("APPEARANCE")
                }))
                configItems.add(ConfigItem("Global Display Size", "Icons and margins for all sub-sections (Current: ${settings.displaySize})", options = scaleOptions, selectedIndex = scaleOptions.indexOf(settings.displaySize), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(displaySize = scaleOptions[i]))
                    showSectionSettings("APPEARANCE")
                }))
                configItems.add(ConfigItem("Home Page Display Size", "Dedicated scale for the main dashboard (Current: ${settings.homeDisplaySize})", options = scaleOptions, selectedIndex = scaleOptions.indexOf(settings.homeDisplaySize), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(homeDisplaySize = scaleOptions[i]))
                    showSectionSettings("APPEARANCE")
                }))
                configItems.add(ConfigItem("Text Font Size", "Scaling for titles and content (Current: ${settings.fontSize})", options = scaleOptions, selectedIndex = scaleOptions.indexOf(settings.fontSize), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(fontSize = scaleOptions[i]))
                    showSectionSettings("APPEARANCE")
                }))

                configItems.add(ConfigItem("Advanced Look & Feel", isHeader = true))
                configItems.add(ConfigItem("Theme Mode", "Override system theme (Current: ${settings.appThemeMode})", options = listOf("LIGHT", "DARK", "OLED"), selectedIndex = listOf("LIGHT", "DARK", "OLED").indexOf(settings.appThemeMode), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(appThemeMode = listOf("LIGHT", "DARK", "OLED")[i]))
                    recreate()
                }))
                configItems.add(ConfigItem("Accent Color", "Custom highlights app-wide") {
                    appearanceHandler.showColorPickerDialog("APP_ACCENT") { showSectionSettings("APPEARANCE") }
                })
                configItems.add(ConfigItem("Border Radius", "Curvature for cards and buttons (Current: ${settings.appBorderRadius}dp)") {
                    appearanceHandler.showBorderRadiusSliderDialog()
                })
                
                val cardStyles = listOf("DEFAULT", "FLAT", "GLASS", "NEOMORPHIC")
                configItems.add(ConfigItem("Card Style", "Surface appearance (Current: ${settings.appCardStyle})", options = cardStyles, selectedIndex = cardStyles.indexOf(settings.appCardStyle), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(appCardStyle = cardStyles[i]))
                    showSectionSettings("APPEARANCE")
                }))
                
                val fontFamilies = listOf("DEFAULT", "SERIF", "MONOSPACE", "CONDENSED")
                configItems.add(ConfigItem("Font Family", "Change typography style (Current: ${settings.appFontFamily})", options = fontFamilies, selectedIndex = fontFamilies.indexOf(settings.appFontFamily), onOptionSelected = { i ->
                    viewModel.updateSettings(settings.copy(appFontFamily = fontFamilies[i]))
                    showSectionSettings("APPEARANCE")
                }))
                
                configItems.add(ConfigItem("Show Shadows", "Toggle UI depth and elevation", isToggle = true, isChecked = settings.appShowShadows) {
                    viewModel.updateSettings(settings.copy(appShowShadows = !settings.appShowShadows))
                    showSectionSettings("APPEARANCE")
                })
            }
        }
        findViewById<RecyclerView>(R.id.settings_list).adapter = ConfigAdapter(configItems) { /* Callback */ }
    }

    private fun showHomePageSectionsDialog(currentSettings: UserSettings) {
        val d = Dialog(this)
        d.setContentView(R.layout.dialog_manage_sections)
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        d.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = d.findViewById<LinearLayout>(R.id.container_section_switches)
        val sections = mutableMapOf(
            "Habits" to currentSettings.showHabitSection,
            "Workouts" to currentSettings.showWorkoutSection,
            "Tasks" to currentSettings.showTaskSection,
            "Notes" to currentSettings.showNoteSection,
            "Projects" to currentSettings.showProjectSection,
            "Finance" to currentSettings.showFinanceSection
        )

        sections.forEach { (name, isEnabled) ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_manage_section_row, container, false)
            row.findViewById<TextView>(R.id.tv_section_name).text = name
            val sw = row.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.sw_section_toggle)
            sw.isChecked = isEnabled
            sw.setOnCheckedChangeListener { _, isChecked -> sections[name] = isChecked }
            container.addView(row)
        }

        d.findViewById<View>(R.id.btn_save_sections).setOnClickListener {
            viewModel.updateSettings(currentSettings.copy(
                showHabitSection = sections["Habits"] ?: true,
                showWorkoutSection = sections["Workouts"] ?: true,
                showTaskSection = sections["Tasks"] ?: true,
                showNoteSection = sections["Notes"] ?: true,
                showProjectSection = sections["Projects"] ?: true,
                showFinanceSection = sections["Finance"] ?: true
            ))
            d.dismiss()
        }
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
                setImageResource(res); setOnClickListener { 
                    val currentProfile = profileViewModel.userProfile.value
                    profileViewModel.updateProfile(currentProfile.copy(avatarRes = res))
                    d.dismiss() 
                }
            })
        }
        container.removeAllViews(); container.addView(row); d.show()
    }

    override fun onResume() {
        super.onResume()
        updateMiniProfile()
    }

    override fun onDestroy() {
        // voiceManager.destroy() // It's a singleton, don't destroy it here if shared
        super.onDestroy()
    }
}
