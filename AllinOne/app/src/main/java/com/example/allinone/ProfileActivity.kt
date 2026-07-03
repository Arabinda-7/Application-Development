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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

class ProfileActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        setupIdentity()
        setupImpactSummary()
        setupSecurityHub()
        setupDataGovernance()
    }

    private fun setupIdentity() {
        findViewById<TextView>(R.id.tv_user_name).text = DataManager.userName
        findViewById<TextView>(R.id.tv_user_tier).text = DataManager.userBio.uppercase()
        findViewById<ImageView>(R.id.iv_profile_avatar).setImageResource(DataManager.userAvatarRes)

        // Feature: Edit Profile - FIXED CLICK LISTENER
        findViewById<View>(R.id.container_avatar).setOnClickListener {
            showEditProfileDialog()
        }

        findViewById<ImageView>(R.id.iv_profile_avatar).setOnLongClickListener {
            // Toggle between two profile icons
            val current = DataManager.userAvatarRes
            val next = if (current == R.drawable.icons8_profile_100) {
                R.drawable.icons8_profile_100_2
            } else {
                R.drawable.icons8_profile_100
            }
            
            DataManager.userAvatarRes = next
            DataManager.saveData(this)
            setupIdentity()
            Toast.makeText(this, "Profile Style Toggled", Toast.LENGTH_SHORT).show()
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
        val btnSave = dialog.findViewById<View>(R.id.btn_save_identity)

        etName.setText(DataManager.userName)
        
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                DataManager.userName = newName
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
        val totalProjects = DataManager.notes.count { it.category == "Project" && it.status == "Completed" }
        updateStat(R.id.stat_projects, R.drawable.ic_project, totalProjects.toString(), "DONE")
    }

    private fun updateStat(containerId: Int, icon: Int, value: String, label: String) {
        val container = findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.iv_stat_icon).setImageResource(icon)
        container.findViewById<TextView>(R.id.tv_stat_value).text = value
        container.findViewById<TextView>(R.id.tv_stat_label).text = label
    }

    private fun setupSecurityHub() {
        // Biometric Lock
        setupToggle(
            R.id.item_biometric_lock,
            R.drawable.baseline_settings_24,
            "Biometric Lock",
            DataManager.isAppLockEnabled
        ) { isChecked ->
            DataManager.isAppLockEnabled = isChecked
            DataManager.saveData(this)
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
}