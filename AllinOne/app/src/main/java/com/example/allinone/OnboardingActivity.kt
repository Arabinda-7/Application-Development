package com.example.allinone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnboardingActivity : AppCompatActivity() {

    private var selectedAvatarRes: Int = R.drawable.boy_avatar_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding_root_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        val etName = findViewById<EditText>(R.id.et_onboarding_name)
        val ivAvatar1 = findViewById<ImageView>(R.id.iv_avatar_1)
        val ivAvatar2 = findViewById<ImageView>(R.id.iv_avatar_2)
        val btnGetStarted = findViewById<TextView>(R.id.btn_get_started)

        fun updateAvatarSelection(resId: Int) {
            selectedAvatarRes = resId
            
            val activeAlpha = 1.0f
            val inactiveAlpha = 0.3f
            
            ivAvatar1.alpha = if (resId == R.drawable.boy_avatar_profile) activeAlpha else inactiveAlpha
            ivAvatar2.alpha = if (resId == R.drawable.girl_avatar_profile) activeAlpha else inactiveAlpha
        }

        ivAvatar1.setOnClickListener { updateAvatarSelection(R.drawable.boy_avatar_profile) }
        ivAvatar2.setOnClickListener { updateAvatarSelection(R.drawable.girl_avatar_profile) }

        btnGetStarted.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                // Save User Choices
                DataManager.userName = name
                DataManager.userAvatarRes = selectedAvatarRes
                DataManager.isOnboardingCompleted = true
                DataManager.saveData(this)

                // Navigate to Main Dashboard
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please enter your name to continue", Toast.LENGTH_SHORT).show()
                etName.requestFocus()
            }
        }
    }
}
