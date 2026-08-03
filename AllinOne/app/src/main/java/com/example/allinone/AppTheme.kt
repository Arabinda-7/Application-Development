package com.example.allinone

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.allinone.ui.home.DashboardState

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
        fun fromDashboardState(state: DashboardState): AppStyle {
            val isOled = state.appThemeMode == "OLED"
            val isLight = state.appThemeMode == "LIGHT"
            
            return AppStyle(
                borderRadius = state.appBorderRadius.dp,
                accentColor = if (state.appAccentColor != -1) Color(state.appAccentColor) else Color(0xFF1A73E8),
                surfaceColor = when {
                    isOled -> Color.Black.copy(alpha = 0.05f)
                    isLight -> if (state.appCardStyle == "GLASS") Color.White.copy(alpha = 0.7f) else Color(0xFFF5F5F5)
                    else -> if (state.appCardStyle == "GLASS") Color.White.copy(alpha = 0.02f) else Color(0xFF1A1A1A)
                },
                backgroundColor = when {
                    isLight -> Color.White
                    isOled -> Color.Black
                    else -> Color.Black
                },
                isOled = isOled,
                showShadows = state.appShowShadows,
                isDynamicColorEnabled = state.isDynamicColorEnabled,
                fontFamily = when(state.appFontFamily) {
                    "MONOSPACE" -> FontFamily.Monospace
                    "SERIF" -> FontFamily.Serif
                    else -> FontFamily.Default
                },
                cardStyle = state.appCardStyle
            )
        }

        fun fromSettings(): AppStyle {
            // This is a placeholder for components that still call this.
            // Ideally should be passed from DashboardState.
            return AppStyle()
        }
    }
}

val LocalAppStyle = staticCompositionLocalOf { AppStyle() }
