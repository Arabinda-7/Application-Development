package com.example.allinone

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.example.allinone.assistant.model.ChatMessage

@Composable
fun ChatBubble(message: ChatMessage) {
    val style = LocalAppStyle.current
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) style.accentColor else Color.Black
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            border = if (!message.isUser) androidx.compose.foundation.BorderStroke(1.dp, style.accentColor.copy(alpha = 0.3f)) else null,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(message.text))
                    }
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = renderMarkdown(message.text),
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.timestamp),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun renderMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun InsightCard(insight: AssistantBrain.Insight) {
    val themeColor = when (insight.type) {
        "FINANCE" -> Color(0xFFFF5252)
        "PRODUCTIVITY" -> Color(0xFF29D9C3)
        "MINDSET" -> Color(0xFFBB86FC)
        "MANAGEMENT" -> Color(0xFFFBBC05)
        "RISK" -> Color(0xFFEA4335)
        "ONBOARDING" -> Color(0xFF4285F4)
        else -> Color(0xFF4285F4)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(themeColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = insight.type,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = insight.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.description,
                fontSize = 15.sp,
                color = Color.LightGray,
                lineHeight = 22.sp
            )
        }
    }
}
