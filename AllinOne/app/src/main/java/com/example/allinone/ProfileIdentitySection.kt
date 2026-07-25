package com.example.allinone

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ProfileIdentitySection(
    private val rootView: View,
    private val onAvatarClicked: () -> Unit,
    private val onNameClicked: () -> Unit
) {
    private val tvName: TextView = rootView.findViewById(R.id.tv_user_name)
    private val tvTier: TextView = rootView.findViewById(R.id.tv_user_tier)
    private val ivProfile: ImageView = rootView.findViewById(R.id.iv_profile_avatar)
    private val containerAvatar: View = rootView.findViewById(R.id.container_avatar)

    fun setup() {
        containerAvatar.setOnClickListener { onAvatarClicked() }
        tvName.setOnClickListener { onNameClicked() }
    }

    fun update(name: String, bio: String, imageUri: String?, avatarRes: Int) {
        tvName.text = UIUtils.formatTitleCase(name)
        tvTier.text = bio.uppercase()
        
        if (imageUri != null) {
            ivProfile.setImageURI(Uri.parse(imageUri))
        } else {
            ivProfile.setImageResource(avatarRes)
        }
    }
}
