package com.example.allinone

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppStyle(
    val borderRadius: Dp = 16.dp,
    val accentColor: Color = Color(0xFF1A73E8),
    val surfaceColor: Color = Color(0xFF1A1A1A),
    val backgroundColor: Color = Color.Black,
    val isOled: Boolean = false,
    val showShadows: Boolean = true,
    val isDynamicColorEnabled: Boolean = false,
    val fontFamily: FontFamily = FontFamily.Default,
    val cardStyle: String = "GLASS"
) {
    companion object {
        fun fromSettings(): AppStyle {
            val isOled = DataManager.appThemeMode == "OLED"
            val isLight = DataManager.appThemeMode == "LIGHT"
            
            return AppStyle(
                borderRadius = DataManager.appBorderRadius.dp,
                accentColor = if (DataManager.appAccentColor != -1) Color(DataManager.appAccentColor) else Color(0xFF1A73E8),
                surfaceColor = when {
                    isOled -> Color.Black.copy(alpha = 0.05f)
                    isLight -> if (DataManager.appCardStyle == "GLASS") Color.White.copy(alpha = 0.7f) else Color(0xFFF5F5F5)
                    else -> if (DataManager.appCardStyle == "GLASS") Color.White.copy(alpha = 0.02f) else Color(0xFF1A1A1A)
                },
                backgroundColor = when {
                    isLight -> Color.White
                    isOled -> Color.Black
                    else -> Color.Black
                },
                isOled = isOled,
                showShadows = DataManager.appShowShadows,
                isDynamicColorEnabled = DataManager.isDynamicColorEnabled,
                cardStyle = DataManager.appCardStyle
            )
        }
    }
}

val LocalAppStyle = staticCompositionLocalOf { AppStyle() }
