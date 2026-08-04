package com.example.allinone.ui.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.VoiceAuraGlow
import com.example.allinone.GoogleVoiceBars

@Composable
fun VoiceOverlay(
    isVisible: Boolean,
    isListening: Boolean,
    partialText: String,
    onDismiss: () -> Unit,
    onMicClick: () -> Unit
) {
    val style = LocalAppStyle.current

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable { onDismiss() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) {},
                color = style.surfaceColor.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isListening) "I'm listening..." else if (isVisible) "Initializing..." else "Press to speak",
                        color = style.accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = partialText.ifEmpty { "What can I help you with?" },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (isListening) {
                        GoogleVoiceBars(isListening = true)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp)
                    ) {
                        VoiceAuraGlow(
                            isListening = isListening,
                            isThinking = false, // Home overlay is usually just listening
                            accentColor = style.accentColor
                        )
                        
                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color.Red else style.accentColor)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
