package com.example.allinone.ui.assistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.AssistantBrain
import com.example.allinone.LocalAppStyle
import com.example.allinone.assistant.model.ChatMessage

@Composable
fun InsightCard(insight: AssistantBrain.Insight) {
    val style = LocalAppStyle.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.3f)),
        modifier = Modifier.width(280.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(insight.title, fontWeight = FontWeight.Bold, color = style.accentColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(insight.description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val style = LocalAppStyle.current
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val color = if (msg.isUser) style.accentColor else style.surfaceColor
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                msg.text,
                modifier = Modifier.padding(12.dp),
                color = if (msg.isUser) Color.Black else Color.White,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    isListening: Boolean,
    onListenToggle: () -> Unit,
    isMuted: Boolean,
    onMuteToggle: () -> Unit
) {
    val style = LocalAppStyle.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onListenToggle,
            modifier = Modifier.background(if (isListening) Color.Red else style.surfaceColor, CircleShape)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = null,
                tint = if (isListening) Color.White else style.accentColor
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a command...", color = Color.Gray) },
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = style.surfaceColor,
                focusedContainerColor = style.surfaceColor,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = style.accentColor
            ),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { onSend(input) },
            enabled = input.isNotBlank(),
            modifier = Modifier.background(if (input.isNotBlank()) style.accentColor else Color.DarkGray, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = if (input.isNotBlank()) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val style = LocalAppStyle.current
    Row(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = style.accentColor, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Assistant is thinking...", color = Color.Gray, fontSize = 12.sp)
    }
}
