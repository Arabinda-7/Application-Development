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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.allinone.security.SecurityManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.allinone.core.utils.UIUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    
    private lateinit var identitySection: ProfileIdentitySection
    private lateinit var impactSection: ProfileImpactSummarySection
    private lateinit var securitySection: ProfileSecurityHubSection
    private lateinit var dataSection: ProfileDataGovernanceSection

    private var lastClickTime: Long = 0
    private fun safeClick(action: () -> Unit) {
        if (System.currentTimeMillis() - lastClickTime < 500) return
        lastClickTime = System.currentTimeMillis()
        action()
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                val currentProfile = viewModel.userProfile.value
                viewModel.updateProfile(currentProfile.copy(profileImageUri = it.toString()))
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
        observeViewModel()
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
            onNameClicked = { showEditProfileBottomSheet() }
        )

        impactSection = ProfileImpactSummarySection(findViewById(R.id.profile_root))

        securitySection = ProfileSecurityHubSection(
            findViewById(R.id.profile_root),
            onAppLockToggled = { isChecked ->
                val settings = viewModel.userSettings.value
                if (isChecked && settings.appLockPin == null) {
                    startActivity(Intent(this, LockActivity::class.java).apply { 
                        putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SETUP) 
                    })
                } else {
                    viewModel.updateSettings(settings.copy(isAppLockEnabled = isChecked))
                }
            },
            onBiometricToggled = { isChecked ->
                viewModel.updateSettings(viewModel.userSettings.value.copy(isBiometricLockEnabled = isChecked))
            },
            onScreenshotToggled = { isChecked ->
                viewModel.updateSettings(viewModel.userSettings.value.copy(isScreenshotProtectionEnabled = isChecked))
                SecurityManager.setScreenshotProtection(this, isChecked)
            },
            onOledToggled = { isChecked ->
                viewModel.updateSettings(viewModel.userSettings.value.copy(appThemeMode = if (isChecked) "OLED" else "DARK"))
                recreate()
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

        val btnBack = findViewById<View>(R.id.btn_back)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, imeInsets.bottom.coerceAtLeast(systemBars.bottom))
            insets
        }

        btnBack.setOnClickListener { finish() }
        findViewById<View>(R.id.btn_edit_profile_top).setOnClickListener { showEditProfileBottomSheet() }

        setupQuickActions()

        identitySection.setup()
        dataSection.setup()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { profile ->
                        updateUI()
                    }
                }
                launch {
                    viewModel.userSettings.collect { settings ->
                        securitySection.setup(
                            settings.isAppLockEnabled,
                            settings.isBiometricLockEnabled,
                            settings.isScreenshotProtectionEnabled,
                            settings.appThemeMode == "OLED"
                        )
                        updateUI()
                    }
                }
            }
        }
    }

    private fun setupQuickActions() {
        val actionShare = findViewById<View>(R.id.action_share)
        actionShare.findViewById<ImageView>(R.id.iv_action_icon).setImageResource(R.drawable.icons8_share_100_apng)
        actionShare.findViewById<TextView>(R.id.tv_action_label).text = "SHARE"
        actionShare.setOnClickListener {
            safeClick {
                val profile = viewModel.userProfile.value
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Hey, check out my profile on All in One app! I'm ${profile.name}.")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }

        val actionSettings = findViewById<View>(R.id.action_settings)
        actionSettings.findViewById<ImageView>(R.id.iv_action_icon).setImageResource(R.drawable.baseline_settings_24)
        actionSettings.findViewById<TextView>(R.id.tv_action_label).text = "SETTINGS"
        actionSettings.setOnClickListener {
            safeClick {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        val actionHelp = findViewById<View>(R.id.action_help)
        actionHelp.findViewById<ImageView>(R.id.iv_action_icon).setImageResource(R.drawable.icons8_info_100)
        actionHelp.findViewById<TextView>(R.id.tv_action_label).text = "HELP"
        actionHelp.setOnClickListener {
            safeClick {
                startActivity(Intent(this, SettingsActivity::class.java).apply {
                    putExtra(SettingsActivity.EXTRA_SECTION, "HELP")
                })
            }
        }
    }

    private fun updateUI() {
        val profile = viewModel.userProfile.value
        val settings = viewModel.userSettings.value
        
        val today = DataManager.getTrackingDateString()
        val currentTime = System.currentTimeMillis()
        val isMoodExpired = profile.lastMoodTimestamp != 0L && (currentTime - profile.lastMoodTimestamp) > 3600000
        val effectiveMood = if (isMoodExpired) null else profile.dailyMoods[today]
        val moodColor = UIUtils.getMoodColor(effectiveMood, UIUtils.getAccentColor(this))

        identitySection.update(profile.name, profile.bio, profile.profileImageUri, profile.avatarRes)
        impactSection.update(moodColor)
        
        setupHeaderBackground(moodColor, settings.appThemeMode == "OLED")
    }

    private fun setupHeaderBackground(moodColor: Int, isOled: Boolean) {
        val headerAura = findViewById<View>(R.id.header_aura)

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(153, Color.red(moodColor), Color.green(moodColor), Color.blue(moodColor)),
                Color.BLACK
            )
        )
        headerAura.background = gradient

        val cardColor = if (isOled) Color.BLACK else Color.parseColor("#1A1A1A")
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_impact_summary).setCardBackgroundColor(cardColor)
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_security_hub).setCardBackgroundColor(cardColor)

        identitySection.applyTint(moodColor)
        impactSection.applyTint(moodColor)
        securitySection.applyTint(moodColor)
        dataSection.applyTint(moodColor)
        
        tintQuickActions(moodColor)
    }

    private fun tintQuickActions(color: Int) {
        val colorStateList = android.content.res.ColorStateList.valueOf(color)
        findViewById<View>(R.id.action_share).findViewById<ImageView>(R.id.iv_action_icon).imageTintList = colorStateList
        findViewById<View>(R.id.action_settings).findViewById<ImageView>(R.id.iv_action_icon).imageTintList = colorStateList
        findViewById<View>(R.id.action_help).findViewById<ImageView>(R.id.iv_action_icon).imageTintList = colorStateList
    }

    private fun showEditProfileBottomSheet() {
        val profile = viewModel.userProfile.value
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile_bottom, null)
        bottomSheet.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.et_edit_name)
        val etBio = view.findViewById<EditText>(R.id.et_edit_bio)
        val ivEditAvatar = view.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.iv_edit_avatar)
        val btnChangePhoto = view.findViewById<View>(R.id.btn_change_photo)
        val containerAvatars = view.findViewById<android.widget.LinearLayout>(R.id.container_avatars)
        val btnSave = view.findViewById<View>(R.id.btn_save_profile)

        var tempImageUri: String? = profile.profileImageUri
        var tempAvatarRes: Int = profile.avatarRes

        fun updateSheetPreview() {
            if (tempImageUri != null) {
                ivEditAvatar.setImageURI(Uri.parse(tempImageUri))
            } else {
                ivEditAvatar.setImageResource(tempAvatarRes)
            }
        }

        updateSheetPreview()
        etName.setText(profile.name)
        etBio.setText(profile.bio)

        btnChangePhoto.setOnClickListener {
            val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            imagePickerLauncher.launch(pickerIntent)
            bottomSheet.dismiss()
        }

        val avatars = listOf(R.drawable.boy_avatar_profile, R.drawable.girl_avatar_profile)
        avatars.forEach { res ->
            val iv = com.google.android.material.imageview.ShapeableImageView(this).apply {
                val s = (48 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(s, s).apply { setMargins(16, 16, 16, 16) }
                setImageResource(res)
                shapeAppearanceModel = ivEditAvatar.shapeAppearanceModel
                strokeWidth = if (tempAvatarRes == res && tempImageUri == null) 4f else 0f
                strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#2EC4B6"))
                setOnClickListener {
                    tempAvatarRes = res
                    tempImageUri = null
                    updateSheetPreview()
                    // Update all strokes
                    for (i in 0 until containerAvatars.childCount) {
                        (containerAvatars.getChildAt(i) as? com.google.android.material.imageview.ShapeableImageView)?.strokeWidth = 0f
                    }
                    strokeWidth = 4f
                }
            }
            containerAvatars.addView(iv)
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newBio = etBio.text.toString().trim()
            if (newName.isNotEmpty()) {
                viewModel.updateProfile(profile.copy(
                    name = newName, 
                    bio = newBio,
                    profileImageUri = tempImageUri,
                    avatarRes = tempAvatarRes
                ))
                bottomSheet.dismiss()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheet.show()
    }

    override fun finish() {
        super.finish()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}
