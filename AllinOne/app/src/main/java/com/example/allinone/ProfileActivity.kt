package com.example.allinone

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    
    private lateinit var identitySection: ProfileIdentitySection
    private lateinit var impactSection: ProfileImpactSummarySection
    private lateinit var securitySection: ProfileSecurityHubSection
    private lateinit var dataSection: ProfileDataGovernanceSection

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                DataManager.userProfileImageUri = it.toString()
                DataManager.saveData(this)
                viewModel.refresh()
                Toast.makeText(this, "Profile Picture Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var backupPassword: CharArray? = null

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val json = DataManager.exportData(this@ProfileActivity, backupPassword)
                    contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    backupPassword = null
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

        initSections()
        setupLogic()
    }

    private fun initSections() {
        identitySection = ProfileIdentitySection(
            findViewById(R.id.profile_root),
            onAvatarClicked = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                imagePickerLauncher.launch(intent)
            },
            onNameClicked = { showEditProfileDialog() }
        )

        impactSection = ProfileImpactSummarySection(findViewById(R.id.profile_root))

        securitySection = ProfileSecurityHubSection(
            findViewById(R.id.profile_root),
            onAppLockToggled = { isChecked ->
                if (isChecked && DataManager.appLockPin == null) {
                    startActivity(Intent(this, LockActivity::class.java).apply { 
                        putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) 
                    })
                } else {
                    DataManager.isAppLockEnabled = isChecked
                    DataManager.saveData(this)
                    viewModel.refresh()
                }
            },
            onBiometricToggled = { isChecked ->
                DataManager.isBiometricLockEnabled = isChecked
                DataManager.saveData(this)
                viewModel.refresh()
            },
            onScreenshotToggled = { isChecked ->
                DataManager.isScreenshotProtectionEnabled = isChecked
                DataManager.saveData(this)
                SecurityManager.setScreenshotProtection(this, isChecked)
                viewModel.refresh()
            },
            onOledToggled = { isChecked ->
                DataManager.isOledThemeEnabled = isChecked
                DataManager.saveData(this)
                viewModel.refresh()
                Toast.makeText(this, "Restart app to apply theme fully", Toast.LENGTH_SHORT).show()
            }
        )

        dataSection = ProfileDataGovernanceSection(findViewById(R.id.profile_root)) {
            UIUtils.showPasswordDialog(this, "ENCRYPT BACKUP") { password ->
                backupPassword = password
                exportLauncher.launch("allinone_backup_${System.currentTimeMillis()}.json")
            }
        }
    }

    private fun setupLogic() {
        val scrollView = findViewById<View>(R.id.profile_scroll_view)
        setupKeyboardHandling(scrollView, null)

        val btnBack = findViewById<View>(R.id.btn_back)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val layoutParams = btnBack.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + (16 * resources.displayMetrics.density).toInt()
            btnBack.layoutParams = layoutParams
            v.setPadding(v.paddingLeft, 0, v.paddingRight, imeInsets.bottom.coerceAtLeast(systemBars.bottom))
            insets
        }

        btnBack.setOnClickListener { finish() }
        findViewById<View>(R.id.btn_edit_profile_top).setOnClickListener { showEditProfileDialog() }

        identitySection.setup()
        securitySection.setup(
            DataManager.isAppLockEnabled,
            DataManager.isBiometricLockEnabled,
            DataManager.isScreenshotProtectionEnabled,
            DataManager.isOledThemeEnabled
        )
        dataSection.setup()
        
        updateUI()
    }

    private fun updateUI() {
        identitySection.update(viewModel.userName, viewModel.userBio, viewModel.userProfileImageUri, viewModel.userAvatarRes)
        impactSection.update()
        setupHeaderBackground()
    }

    private fun setupHeaderBackground() {
        val today = DataManager.getTrackingDateString()
        val currentTime = System.currentTimeMillis()
        val isMoodExpired = DataManager.lastMoodTimestamp != 0L && (currentTime - DataManager.lastMoodTimestamp) > 3600000
        val effectiveMood = if (isMoodExpired) null else DataManager.dailyMoods[today]

        val moodColor = UIUtils.getMoodColor(effectiveMood, UIUtils.getAccentColor(this))
        val headerAura = findViewById<View>(R.id.header_aura)

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(153, Color.red(moodColor), Color.green(moodColor), Color.blue(moodColor)),
                Color.BLACK
            )
        )
        headerAura.background = gradient
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_identity)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val etName = dialog.findViewById<EditText>(R.id.et_edit_name)
        val etBio = dialog.findViewById<EditText>(R.id.et_edit_bio)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_identity)

        etName.setText(viewModel.userName)
        etBio.setText(viewModel.userBio)
        
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newBio = etBio.text.toString().trim()
            if (newName.isNotEmpty()) {
                DataManager.userName = newName
                DataManager.userBio = newBio
                DataManager.saveData(this)
                viewModel.refresh()
                updateUI()
                dialog.dismiss()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        updateUI()
    }
}
