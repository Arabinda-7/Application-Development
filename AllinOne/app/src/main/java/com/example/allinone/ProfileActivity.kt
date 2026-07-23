package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                // Persist permission to access this URI across reboots
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                
                DataManager.userProfileImageUri = it.toString()
                DataManager.saveData(this)
                setupIdentity()
                Toast.makeText(this, "Profile Picture Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val json = DataManager.exportData(this@ProfileActivity)
                    contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    Toast.makeText(this@ProfileActivity, "Backup Saved Successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Failed to Save Backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val scrollView = findViewById<View>(R.id.profile_scroll_view)
        setupKeyboardHandling(scrollView, null)

        // Manual Status Bar Padding for content (Back Button and Title)
        val btnBack = findViewById<View>(R.id.btn_back)

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply status bar padding only to top controls, not the background aura
            // btn_back is the anchor for Identity Hub title and Edit button
            val layoutParams = btnBack.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + (16 * resources.displayMetrics.density).toInt()
            btnBack.layoutParams = layoutParams

            // Handle bottom padding for keyboard + navigation bar
            v.setPadding(v.paddingLeft, 0, v.paddingRight, imeInsets.bottom.coerceAtLeast(systemBars.bottom))
            
            insets
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_edit_profile_top).setOnClickListener { showEditProfileDialog() }

        setupIdentity()
        setupHeaderBackground()
        setupImpactSummary()
        setupSecurityHub()
        setupDataGovernance()
    }

    private fun setupHeaderBackground() {
        val today = DataManager.getTrackingDateString()
        val currentTime = System.currentTimeMillis()
        val isMoodExpired = DataManager.lastMoodTimestamp != 0L && (currentTime - DataManager.lastMoodTimestamp) > 3600000
        val effectiveMood = if (isMoodExpired) null else DataManager.dailyMoods[today]

        val moodColor = UIUtils.getMoodColor(effectiveMood, UIUtils.getAccentColor(this))
        val headerAura = findViewById<View>(R.id.header_aura)

        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                android.graphics.Color.argb(
                    153, // 60% alpha matching Compose alpha = 0.6f
                    android.graphics.Color.red(moodColor),
                    android.graphics.Color.green(moodColor),
                    android.graphics.Color.blue(moodColor)
                ),
                android.graphics.Color.BLACK
            )
        )
        headerAura.background = gradient
    }

    private fun setupIdentity() {
        findViewById<TextView>(R.id.tv_user_name).text = UIUtils.formatTitleCase(DataManager.userName)
        findViewById<TextView>(R.id.tv_user_tier).text = DataManager.userBio.uppercase()
        
        val ivProfile = findViewById<ImageView>(R.id.iv_profile_avatar)
        
        if (DataManager.userProfileImageUri != null) {
            ivProfile.setImageURI(Uri.parse(DataManager.userProfileImageUri))
        } else {
            ivProfile.setImageResource(DataManager.userAvatarRes)
        }

        // Feature: Edit Profile - FIXED CLICK LISTENER
        findViewById<View>(R.id.container_avatar).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        findViewById<ImageView>(R.id.iv_profile_avatar).setOnLongClickListener {
            // Toggle between two profile icons and CLEAR custom image
            val current = DataManager.userAvatarRes
            val next = if (current == R.drawable.boy_avatar_profile) {
                R.drawable.girl_avatar_profile
            } else {
                R.drawable.boy_avatar_profile
            }
            
            DataManager.userProfileImageUri = null
            DataManager.userAvatarRes = next
            DataManager.saveData(this)
            setupIdentity()
            Toast.makeText(this, "Switched to Default Avatar", Toast.LENGTH_SHORT).show()
            true
        }
        
        // Also allow clicking the name to edit
        findViewById<View>(R.id.tv_user_name).setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_identity)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val etName = dialog.findViewById<EditText>(R.id.et_edit_name)
        val etBio = dialog.findViewById<EditText>(R.id.et_edit_bio)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_identity)

        etName.setText(DataManager.userName)
        etBio.setText(DataManager.userBio)
        
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newBio = etBio.text.toString().trim()
            if (newName.isNotEmpty()) {
                DataManager.userName = newName
                DataManager.userBio = newBio
                DataManager.saveData(this)
                setupIdentity()
                dialog.dismiss()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun setupImpactSummary() {
        // Habits Stat
        val totalHabits = DataManager.getTotalHabitsFinished()
        updateStat(R.id.stat_habits, R.drawable.ic_habit_tracker, totalHabits.toString(), "HABITS")

        // Savings Stat
        val totalSavings = DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
        updateStat(R.id.stat_savings, R.drawable.ic_finance, "${DataManager.financeCurrency}${totalSavings.toInt()}", "SAVED")

        // Projects Stat
        val totalProjects = DataManager.projects.count { it.category == "Project" && it.status == "Completed" }
        updateStat(R.id.stat_projects, R.drawable.ic_project, totalProjects.toString(), "DONE")
    }

    private fun updateStat(containerId: Int, icon: Int, value: String, label: String) {
        val container = findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.iv_stat_icon).setImageResource(icon)
        container.findViewById<TextView>(R.id.tv_stat_value).text = value
        container.findViewById<TextView>(R.id.tv_stat_label).text = label
    }

    private fun setupSecurityHub() {
        // App Lock
        setupToggle(
            R.id.item_biometric_lock,
            R.drawable.baseline_settings_24,
            "App Access Lock",
            DataManager.isAppLockEnabled
        ) { isChecked ->
            if (isChecked && DataManager.appLockPin == null) {
                // If turning ON but no PIN, go to setup
                val intent = Intent(this, LockActivity::class.java).apply { 
                    putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) 
                }
                startActivity(intent)
            } else {
                DataManager.isAppLockEnabled = isChecked
                DataManager.saveData(this)
            }
        }

        // OLED Mode
        setupToggle(
            R.id.item_oled_mode,
            R.drawable.ic_habit_tracker,
            "OLED Theme",
            DataManager.isOledThemeEnabled
        ) { isChecked ->
            DataManager.isOledThemeEnabled = isChecked
            DataManager.saveData(this)
            Toast.makeText(this, "Restart app to apply theme fully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToggle(containerId: Int, icon: Int, title: String, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
        val container = findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.iv_setting_icon).setImageResource(icon)
        container.findViewById<TextView>(R.id.tv_setting_title).text = title
        val sw = container.findViewById<SwitchCompat>(R.id.sw_profile_toggle)
        sw.isChecked = isChecked
        
        container.setOnClickListener {
            val newState = !sw.isChecked
            sw.isChecked = newState
            onToggle(newState)
        }
    }

    private fun setupDataGovernance() {
        val exportItem = findViewById<View>(R.id.item_export_data)
        exportItem.findViewById<ImageView>(R.id.iv_item_icon).setImageResource(R.drawable.baseline_tune_24)
        exportItem.findViewById<TextView>(R.id.tv_item_title).text = "Export Data Backup"
        exportItem.findViewById<TextView>(R.id.tv_item_description).text = "Generate a JSON recovery file"
        
        exportItem.setOnClickListener {
            exportLauncher.launch("allinone_backup_${System.currentTimeMillis()}.json")
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}