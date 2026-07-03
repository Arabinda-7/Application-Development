package com.example.allinone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private var selectedAvatarRes: Int = R.drawable.icons8_profile_100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val etName = findViewById<EditText>(R.id.et_onboarding_name)
        val ivAvatar1 = findViewById<ImageView>(R.id.iv_avatar_1)
        val ivAvatar2 = findViewById<ImageView>(R.id.iv_avatar_2)
        val btnGetStarted = findViewById<TextView>(R.id.btn_get_started)

        fun updateAvatarSelection(resId: Int) {
            selectedAvatarRes = resId
            val activeColor = Color.parseColor("#33FFFFFF")
            val inactiveColor = Color.parseColor("#11FFFFFF")
            
            ivAvatar1.backgroundTintList = ColorStateList.valueOf(if (resId == R.drawable.icons8_profile_100) activeColor else inactiveColor)
            ivAvatar2.backgroundTintList = ColorStateList.valueOf(if (resId == R.drawable.icons8_profile_100_2) activeColor else inactiveColor)
        }

        ivAvatar1.setOnClickListener { updateAvatarSelection(R.drawable.icons8_profile_100) }
        ivAvatar2.setOnClickListener { updateAvatarSelection(R.drawable.icons8_profile_100_2) }

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
