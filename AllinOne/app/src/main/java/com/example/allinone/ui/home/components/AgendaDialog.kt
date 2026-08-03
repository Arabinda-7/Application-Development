package com.example.allinone.ui.home.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.allinone.LocalAppStyle
import com.example.allinone.ViewProjectActivity
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.domain.model.AgendaItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaDialog(
    agenda: Map<String, List<AgendaItem>>,
    onDismiss: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToWorkspace: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val style = LocalAppStyle.current
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val locale = config.locales[0]

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { 
            TextButton(onClick = onDismiss) { 
                Text("DISMISS", color = style.accentColor, fontWeight = FontWeight.Bold) 
            } 
        },
        title = { Text("Today's Agenda", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (agenda.isEmpty()) {
                    Text("Your agenda is clear for today!", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                } else {
                    agenda.forEach { (section, items) ->
                        Text(
                            text = section,
                            color = style.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        
                        var lastColorInt: Int? = null
                        
                        items.forEach { item ->
                            val rawItemColor = if (item.color != -1) item.color else style.accentColor.toArgb()
                            val finalItemColorInt = if (lastColorInt != null && lastColorInt == rawItemColor) {
                                UIUtils.darkenColor(rawItemColor, 0.7f)
                            } else {
                                rawItemColor
                            }
                            lastColorInt = finalItemColorInt
                            val itemColor = Color(finalItemColorInt)

                            Surface(
                                onClick = {
                                    when (item.navigationTarget) {
                                        "TASK_ACTIVITY" -> onNavigateToTodos()
                                        "PROJECT_ACTIVITY" -> {
                                            val intent = Intent(context, ViewProjectActivity::class.java).apply {
                                                val idStr = item.parentId ?: item.id
                                                val id = idStr.toLongOrNull() ?: -1L
                                                putExtra("PROJECT_ID", id)
                                            }
                                            context.startActivity(intent)
                                        }
                                        "WORKSPACE" -> onNavigateToWorkspace()
                                        "NOTE_ACTIVITY" -> onNavigateToNotes()
                                    }
                                    onDismiss()
                                },
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().padding(end = 80.dp)
                                        ) {
                                            val icon = when(item.category) {
                                                "TASKS" -> Icons.Default.CheckCircle
                                                "PROJECTS" -> Icons.Default.DateRange
                                                "SUBFEATURES" -> Icons.Default.Info
                                                else -> Icons.Default.Notifications
                                            }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = itemColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        
                                        val timeStr = if (item.time != 0L) {
                                            SimpleDateFormat("hh:mm a", locale).format(Date(item.time))
                                        } else ""
                                        
                                        Row(modifier = Modifier.padding(start = 28.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                            if (timeStr.isNotEmpty()) {
                                                Text(
                                                    text = timeStr,
                                                    color = itemColor.copy(alpha = 0.8f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            if (item.details.isNotEmpty()) {
                                                Text(
                                                    text = item.details,
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.align(Alignment.TopEnd),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val tagText = buildString {
                                            append(item.category)
                                            if (item.priority.isNotEmpty()) {
                                                append(" | ")
                                                append(item.priority.uppercase())
                                            }
                                        }
                                        
                                        if (tagText.isNotEmpty()) {
                                            Surface(
                                                color = itemColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, itemColor.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = tagText,
                                                    color = itemColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
