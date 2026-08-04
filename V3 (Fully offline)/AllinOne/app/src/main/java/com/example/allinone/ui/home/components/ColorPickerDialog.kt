package com.example.allinone.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.allinone.LocalAppStyle

@Composable
fun ColorPickerDialog(
    section: String,
    onColorSelected: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val style = LocalAppStyle.current
    val colors = listOf(
        0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 
        0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { 
            TextButton(onClick = onDismiss) { 
                Text("CLOSE", color = style.accentColor) 
            } 
        },
        title = { Text("Choose Theme Color", color = Color.White) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .clickable { 
                                onColorSelected(section, colorInt.toInt())
                                onDismiss()
                            }
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }
        }
    )
}
