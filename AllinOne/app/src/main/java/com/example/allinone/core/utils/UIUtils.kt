package com.example.allinone.core.utils

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.allinone.ui.home.DashboardState
import com.example.allinone.R
import com.example.allinone.security.SecurityManager
import java.util.*

object UIUtils {
    fun isDrawableResource(context: Context, resId: Int): Boolean {
        if (resId <= 0) return false
        return try {
            val value = TypedValue()
            context.resources.getValue(resId, value, true)
            value.type != TypedValue.TYPE_DIMENSION && 
            (value.string?.toString()?.contains("drawable") == true || 
             value.string?.toString()?.endsWith(".xml") == true || 
             value.string?.toString()?.endsWith(".png") == true)
        } catch (e: Exception) {
            false
        }
    }

    fun safeSetImageResource(imageView: ImageView, @DrawableRes resId: Int, @DrawableRes fallbackResId: Int) {
        if (resId == -1) {
            imageView.setImageResource(fallbackResId)
            return
        }
        if (isDrawableResource(imageView.context, resId)) {
            imageView.setImageResource(resId)
        } else {
            imageView.setImageResource(fallbackResId)
        }
    }

    fun wrapContext(context: Context): Context {
        val prefs = SecurityManager.getEncryptedPrefs(context)
        val config = Configuration(context.resources.configuration)

        val isSystemAppearanceEnabled = prefs.getBoolean("is_system_appearance_enabled", true)
        if (!isSystemAppearanceEnabled) {
            val displayScale = when(prefs.getString("app_display_size", "S")) {
                "XS" -> 0.85f
                "L" -> 1.15f
                else -> 1.0f
            }
            val defaultMetrics = context.resources.displayMetrics
            config.densityDpi = (defaultMetrics.densityDpi * displayScale).toInt()
            
            val fontScale = when(prefs.getString("app_font_size", "S")) {
                "XS" -> 0.85f
                "L" -> 1.25f
                else -> 1.0f
            }
            config.fontScale = fontScale
        }

        val appThemeMode = prefs.getString("app_theme_mode", "DARK") ?: "DARK"
        val nightMode = when(appThemeMode) {
            "LIGHT" -> Configuration.UI_MODE_NIGHT_NO
            "DARK", "OLED" -> Configuration.UI_MODE_NIGHT_YES
            else -> config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        
        return context.createConfigurationContext(config)
    }

    fun getIsolatedMoodDensity(state: DashboardState): Float {
        val systemDensity = android.content.res.Resources.getSystem().displayMetrics.density
        if (state.isSystemAppearanceEnabled) return systemDensity

        val gScale = when (state.globalDisplaySize) { "XS" -> 0.85f; "L" -> 1.15f; else -> 1.0f }
        val hScale = when (state.homeDisplaySize) { "XS" -> 0.85f; "L" -> 1.15f; else -> 1.0f }
        val fScale = when (state.homeFocusSize) { "S" -> 0.85f; "L" -> 1.25f; else -> 1.0f }

        return systemDensity * gScale * hScale * fScale
    }

    fun getAccentColor(context: Context): Int {
        val prefs = SecurityManager.getEncryptedPrefs(context)
        val color = prefs.getInt("app_accent_color", -1)
        return if (color != -1) color else ContextCompat.getColor(context, R.color.primary_blue)
    }

    fun getCardBackgroundColor(context: Context): Int {
        val prefs = SecurityManager.getEncryptedPrefs(context)
        if (!prefs.getBoolean("is_system_appearance_enabled", true) && 
            prefs.getString("app_theme_mode", "DARK") == "OLED") return Color.BLACK
        
        return if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            Color.parseColor("#1A1A1A")
        } else {
            Color.parseColor("#F5F5F5")
        }
    }

    fun getMoodColor(mood: String?, defaultColor: Int): Int {
        return when (mood) {
            "🔥" -> Color.parseColor("#FFB800")
            "⚡" -> Color.parseColor("#2EC4B6")
            "🧘" -> Color.parseColor("#673AB7")
            "💼" -> Color.parseColor("#FF5722")
            "😴" -> Color.parseColor("#9E9E9E")
            "🧠" -> Color.parseColor("#3F51B5")
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

    fun performSuccessHaptic(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        vibrator.vibrate(
            android.os.VibrationEffect.startComposition()
                .addPrimitive(android.os.VibrationEffect.Composition.PRIMITIVE_CLICK)
                .addPrimitive(android.os.VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 50)
                .compose()
        )
    }

    fun performErrorHaptic(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        vibrator.vibrate(
            android.os.VibrationEffect.startComposition()
                .addPrimitive(android.os.VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.8f)
                .addPrimitive(android.os.VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 100)
                .compose()
        )
    }

    fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.round(Color.red(color) * factor)
        val g = Math.round(Color.green(color) * factor)
        val b = Math.round(Color.blue(color) * factor)
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255))
    }

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun showPasswordDialog(context: Context, title: String, onConfirm: (CharArray) -> Unit) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_backup_password)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 20
        }
        dialog.findViewById<TextView>(R.id.tv_password_title).text = title
        val etPassword = dialog.findViewById<EditText>(R.id.et_backup_password)
        dialog.findViewById<View>(R.id.btn_save_password).setOnClickListener {
            val pass = etPassword.text.toString()
            if (pass.isNotEmpty()) { onConfirm(pass.toCharArray()); dialog.dismiss() }
            else { Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show() }
        }
        dialog.findViewById<View>(R.id.btn_cancel_password).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
