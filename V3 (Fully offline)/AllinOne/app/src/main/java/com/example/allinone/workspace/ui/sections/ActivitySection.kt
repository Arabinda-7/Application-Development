package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.ActivityLogEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityLogSection(logs: List<ActivityLogEntity>) {
    val sortedLogs = remember(logs) { logs.sortedByDescending { it.timestamp } }
    if (sortedLogs.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No activity recorded yet.", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) } }
    else { 
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) { 
            itemsIndexed(sortedLogs, key = { _, log -> log.logId }) { index, log -> 
                Box(modifier = Modifier.animateItem()) {
                    ActivityLogItemUI(log = log, isLast = index == sortedLogs.size - 1) 
                }
            } 
        } 
    }
}

@Composable
fun ActivityLogItemUI(log: ActivityLogEntity, isLast: Boolean) {
    val style = LocalAppStyle.current
    val timeStr = remember(log.timestamp) { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(log.timestamp)) }
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.width(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(style.accentColor)); if (!isLast) { Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.1f))) } }
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 24.dp).weight(1f)) { Text(text = log.action.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp); if (log.description.isNotEmpty()) { Text(text = log.description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }; Text(text = timeStr, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp)) }
    }
}
