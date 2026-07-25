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
    val fontFamily: FontFamily = FontFamily.Default,
    val cardStyle: String = "GLASS"
)

val LocalAppStyle = staticCompositionLocalOf { AppStyle() }
