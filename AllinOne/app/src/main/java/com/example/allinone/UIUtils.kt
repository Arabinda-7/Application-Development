package com.example.allinone

import android.content.Context
import android.content.res.Configuration
import java.util.*

object UIUtils {
    fun wrapContext(context: Context): Context {
        if (DataManager.isSystemAppearanceEnabled) return context

        val displayScale = when(DataManager.displaySize) {
            "XS" -> 0.85f
            "L" -> 1.15f
            else -> 1.0f
        }
        
        val fontScale = when(DataManager.fontSize) {
            "XS" -> 0.85f
            "L" -> 1.25f // Slightly larger font jump for accessibility
            else -> 1.0f
        }
        
        val config = Configuration(context.resources.configuration)
        
        // 1. Scale layout components (Icons, Buttons, Margins)
        val defaultMetrics = context.resources.displayMetrics
        config.densityDpi = (defaultMetrics.densityDpi * displayScale).toInt()
        
        // 2. Scale Text independently
        config.fontScale = fontScale
        
        return context.createConfigurationContext(config)
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