package com.example.allinone.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle

@Composable
fun AdviceBanner(
    text: String,
    icon: ImageVector? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    emoji: String? = null
) {
    val style = LocalAppStyle.current
    val finalBg = backgroundColor ?: style.accentColor.copy(alpha = 0.15f)
    val finalBorder = borderColor ?: style.accentColor.copy(alpha = 0.3f)

    Surface(
        color = finalBg,
        shape = RoundedCornerShape(style.borderRadius),
        border = BorderStroke(0.5.dp, finalBorder),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 14.sp)
            } else if (icon != null) {
                Icon(icon, null, tint = finalBorder.copy(alpha = 1.0f), modifier = Modifier.size(14.dp))
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
