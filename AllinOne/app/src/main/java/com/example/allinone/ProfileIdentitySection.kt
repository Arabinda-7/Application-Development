package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.allinone.core.utils.UIUtils

class ProfileIdentitySection(
    private val rootView: View,
    private val onAvatarClicked: () -> Unit,
    private val onNameClicked: () -> Unit
) {
    private val tvName: TextView = rootView.findViewById(R.id.tv_user_name)
    private val tvTier: TextView = rootView.findViewById(R.id.tv_user_tier)
    private val ivProfile: ImageView = rootView.findViewById(R.id.iv_profile_avatar)
    private val containerAvatar: View = rootView.findViewById(R.id.container_avatar)
    private val strengthProgress: android.widget.ProgressBar = rootView.findViewById(R.id.profile_strength_progress)

    fun setup() {
        containerAvatar.setOnClickListener { onAvatarClicked() }
        tvName.setOnClickListener { onNameClicked() }
    }

    fun applyTint(color: Int) {
        tvTier.setTextColor(color)
        val badgeBg = tvTier.background as? android.graphics.drawable.GradientDrawable
        badgeBg?.setStroke(UIUtils.dpToPx(rootView.context, 1), color)
        badgeBg?.setColor(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)))
        
        strengthProgress.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    fun update(name: String, bio: String, imageUri: String?, avatarRes: Int) {
        tvName.text = UIUtils.formatTitleCase(name)
        tvTier.text = bio.uppercase()
        
        if (imageUri != null) {
            ivProfile.setImageURI(Uri.parse(imageUri))
        } else {
            UIUtils.safeSetImageResource(ivProfile, avatarRes, R.drawable.ic_launcher_foreground)
        }

        updateStrength(name, bio, imageUri != null)
    }

    private fun updateStrength(name: String, bio: String, hasImage: Boolean) {
        var score = 0
        if (name.isNotEmpty() && name != "Arabi") score += 25
        if (bio.isNotEmpty() && bio != "PROFESSIONAL TIER") score += 25
        if (hasImage) score += 25
        if (DataManager.isAppLockEnabled) score += 25
        
        strengthProgress.progress = score
    }
}
