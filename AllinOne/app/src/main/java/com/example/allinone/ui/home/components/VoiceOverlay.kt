package com.example.allinone.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.VoiceAuraGlow
import com.example.allinone.GoogleVoiceBars
import com.example.allinone.assistant.model.ChatMessage
import kotlinx.coroutines.delay

@Composable
fun VoiceOverlay(
    isVisible: Boolean,
    isListening: Boolean,
    partialText: String,
    messages: List<ChatMessage>,
    onDismiss: () -> Unit,
    onMicClick: () -> Unit
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = false
                    ) { },
                color = Color.Black,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
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
                                .heightIn(max = 400.dp)
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { msg ->
                                val isLatest = messages.last() == msg
                                VoiceChatBubble(msg, style.accentColor, isLatest)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    Text(
                        text = when {
                            isListening -> "I'm listening..."
                            else -> "How can I help?"
                        },
                        color = style.accentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    if (partialText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = partialText,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (isListening) {
                        GoogleVoiceBars(isListening = true)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        VoiceAuraGlow(
                            isListening = isListening,
                            isThinking = false,
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

@Composable
fun VoiceChatBubble(msg: ChatMessage, accentColor: Color, isLatest: Boolean) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bubbleColor = Color.Black
    val borderColor = if (msg.isUser) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f)
    
    // Typewriter animation logic
    var visibleText by remember(msg.timestamp) { 
        mutableStateOf(if (msg.isUser || !isLatest) msg.text else "") 
    }
    
    LaunchedEffect(msg.text, isLatest) {
        if (!msg.isUser && isLatest && visibleText.length < msg.text.length) {
            msg.text.forEachIndexed { index, _ ->
                if (visibleText.length <= index) {
                    visibleText = msg.text.substring(0, index + 1)
                    delay(30) // Faster speed (30ms per character)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = visibleText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (msg.isUser) Color.White else Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}
