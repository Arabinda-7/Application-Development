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

            // 3. Theme Mode (Night/Light)
            val nightMode = when(DataManager.appThemeMode) {
                "LIGHT" -> Configuration.UI_MODE_NIGHT_NO
                "DARK", "OLED" -> Configuration.UI_MODE_NIGHT_YES
                else -> config.uiMode and Configuration.UI_MODE_NIGHT_MASK
            }
            config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        }
        
        return context.createConfigurationContext(config)
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
}