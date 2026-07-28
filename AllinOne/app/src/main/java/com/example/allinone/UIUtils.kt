package com.example.allinone

import android.content.Context
import android.content.res.Configuration
import java.util.*

object UIUtils {
    fun wrapContext(context: Context): Context {
        val config = Configuration(context.resources.configuration)

        if (!DataManager.isSystemAppearanceEnabled) {
            // 1. Scale layout components (Icons, Buttons, Margins)
            val displayScale = when(DataManager.displaySize) {
                "XS" -> 0.85f
                "L" -> 1.15f
                else -> 1.0f
            }
            val defaultMetrics = context.resources.displayMetrics
            config.densityDpi = (defaultMetrics.densityDpi * displayScale).toInt()
            
            // 2. Scale Text independently
            val fontScale = when(DataManager.fontSize) {
                "XS" -> 0.85f
                "L" -> 1.25f
                else -> 1.0f
            }
            config.fontScale = fontScale
        }

        // 3. Theme Mode (Night/Light)
        val nightMode = when(DataManager.appThemeMode) {
            "LIGHT" -> Configuration.UI_MODE_NIGHT_NO
            "DARK", "OLED" -> Configuration.UI_MODE_NIGHT_YES
            else -> config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        
        return context.createConfigurationContext(config)
    }

    fun getIsolatedMoodDensity(state: DashboardState): Float {
        // We use system density as the raw baseline, ignoring any app-level context scaling
        val systemDensity = android.content.res.Resources.getSystem().displayMetrics.density
        
        if (state.isSystemAppearanceEnabled) return systemDensity

        val gScale = when (state.globalDisplaySize) {
            "XS" -> 0.85f
            "L" -> 1.15f
            else -> 1.0f
        }
        val hScale = when (state.homeDisplaySize) {
            "XS" -> 0.85f
            "L" -> 1.15f
            else -> 1.0f
        }
        val fScale = when (state.homeFocusSize) {
            "S" -> 0.85f
            "L" -> 1.25f
            else -> 1.0f
        }

        return systemDensity * gScale * hScale * fScale
    }

    fun getAccentColor(context: Context): Int {
        return if (DataManager.appAccentColor != -1) {
            DataManager.appAccentColor
        } else {
            androidx.core.content.ContextCompat.getColor(context, R.color.primary_blue)
        }
    }

    fun getCardBackgroundColor(context: Context): Int {
        if (!DataManager.isSystemAppearanceEnabled && DataManager.appThemeMode == "OLED") return android.graphics.Color.BLACK
        return if (isNightMode(context)) {
            android.graphics.Color.parseColor("#1A1A1A")
        } else {
            android.graphics.Color.parseColor("#F5F5F5")
        }
    }

    fun getMoodColor(mood: String?, defaultColor: Int): Int {
        return when (mood) {
            "🔥" -> android.graphics.Color.parseColor("#FFB800")
            "⚡" -> android.graphics.Color.parseColor("#2EC4B6")
            "🧘" -> android.graphics.Color.parseColor("#673AB7")
            "💼" -> defaultColor
            "😴" -> android.graphics.Color.parseColor("#9E9E9E")
            "🧠" -> android.graphics.Color.parseColor("#3F51B5")
            else -> defaultColor
        }
    }

    fun getMoodMessage(mood: String?): String {
        return when (mood) {
            "🔥" -> "Unstoppable mode active."
            "⚡" -> "High energy detected."
            "🧘" -> "Mindful progress only."
            "💼" -> "Execution mode: ON."
            "😴" -> "Rest well. Momentum stays."
            "🧠" -> "Deep focus engaged."
            else -> ""
        }
    }

    private fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    
    fun formatTitleCase(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input.lowercase().split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun darkenColor(color: Int, factor: Float): Int {
        val a = android.graphics.Color.alpha(color)
        val r = Math.round(android.graphics.Color.red(color) * factor)
        val g = Math.round(android.graphics.Color.green(color) * factor)
        val b = Math.round(android.graphics.Color.blue(color) * factor)
        return android.graphics.Color.argb(
            a,
            Math.min(r, 255),
            Math.min(g, 255),
            Math.min(b, 255)
        )
    }

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(android.graphics.Color.alpha(color) * factor)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }

    fun showPasswordDialog(context: Context, title: String, onConfirm: (CharArray) -> Unit) {
        val dialog = android.app.Dialog(context)
        dialog.setContentView(R.layout.dialog_backup_password)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        val tvTitle = dialog.findViewById<android.widget.TextView>(R.id.tv_password_title)
        val etPassword = dialog.findViewById<android.widget.EditText>(R.id.et_backup_password)
        val btnSave = dialog.findViewById<android.view.View>(R.id.btn_save_password)
        val btnCancel = dialog.findViewById<android.view.View>(R.id.btn_cancel_password)

        tvTitle.text = title

        btnSave.setOnClickListener {
            val pass = etPassword.text.toString()
            if (pass.isNotEmpty()) {
                onConfirm(pass.toCharArray())
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(context, "Please enter a password", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
