package com.example.allinone.ui.assistant

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.assistant.model.ChatMessage

/**
 * Top-level AssistantScreen used by AssistantSessionDetailActivity and other assistant views.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    chatMessages: List<ChatMessage>,
    commandInput: String,
    isListening: Boolean,
    isThinking: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onCommandChange: (String) -> Unit,
    onSendCommand: () -> Unit,
    onMicClick: () -> Unit,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFeedClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {},
    sessionTitle: String = "Conversation"
) {
    val style = LocalAppStyle.current

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sessionTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "AI SESSION DETAIL",
                            fontSize = 10.sp,
                            color = style.accentColor,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMuteToggle) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = if (isMuted) Color.Gray else style.accentColor
                        )
                    }
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            // Chat Messages List
            val listState = rememberLazyListState()
            LaunchedEffect(chatMessages.size) {
                if (chatMessages.isNotEmpty()) {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages in this session.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { message ->
                            ChatBubble(message)
                        }
                    }
                }
            }

            // Listening / Thinking Indicator
            AnimatedVisibility(
                visible = isListening || isThinking,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = style.accentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "Listening..." else "Thinking...",
                        color = style.accentColor,
                        fontSize = 12.sp
                    )
                }
            }

            // Bottom Command Input Bar
            Surface(
                color = Color.Black,
                border = androidx.compose.foundation.BorderStroke(1.dp, style.accentColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isListening) style.accentColor else style.surfaceColor,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = commandInput,
                        onValueChange = onCommandChange,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, style.accentColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                        placeholder = { Text("Type a command...", color = Color.Gray, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF141414),
                            unfocusedContainerColor = Color(0xFF141414),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSendCommand,
                        enabled = commandInput.isNotBlank(),
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (commandInput.isNotBlank()) style.accentColor else Color.DarkGray,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (commandInput.isNotBlank()) Color.Black else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
