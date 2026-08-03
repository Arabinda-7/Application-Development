package com.example.allinone.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.GoogleVoiceBars
import com.example.allinone.LocalAppStyle
import com.example.allinone.VoiceAuraGlow
import com.example.allinone.assistant.model.ChatMessage

@Composable
fun VoiceAssistantOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isListening: Boolean = false,
    isThinking: Boolean = false,
    messages: List<ChatMessage> = emptyList(),
    onMicClick: () -> Unit = {}
) {
    if (isVisible) {
        BackHandler { onDismiss() }
    }

    val style = LocalAppStyle.current
    val scrollState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            VoiceAuraGlow(
                isListening = isListening,
                isThinking = isThinking,
                accentColor = style.accentColor
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (style.isOled) 0.6f else 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {},
                    color = Color.Black,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.5f)),
                    shadowElevation = if (style.showShadows) 8.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp, 4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        if (messages.isNotEmpty()) {
                            LazyColumn(
                                state = scrollState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 480.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(messages) { msg ->
                                    val isLast = messages.last() == msg
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isUser) Color.White else Color.White.copy(alpha = if (isLast) 0.8f else 0.5f),
                                        fontSize = if (msg.isUser) 18.sp else 16.sp,
                                        fontWeight = if (msg.isUser) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        Text(
                            text = when {
                                isThinking -> "Thinking..."
                                isListening -> "I'm listening..."
                                else -> "How can I help?"
                            },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        GoogleVoiceBars(isListening = isListening || isThinking)
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onMicClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening) Color(0xFFEA4335) else if (isThinking) Color(0xFFFBBC05) else Color(0xFF4285F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThinking) Icons.Default.AutoAwesome else Icons.Default.Mic,
                                    contentDescription = "Action",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
