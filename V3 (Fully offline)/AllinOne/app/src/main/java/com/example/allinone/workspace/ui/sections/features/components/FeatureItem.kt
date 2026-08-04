package com.example.allinone.workspace.ui.sections.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FeatureItem: Detail info badges, status selector chips, and complexity/effort selectors.
 */
@Composable
fun DetailInfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FeatureStatusChipRow(
    status: String,
    projectColor: Color,
    onStatusSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Backlog", "Planning", "Development", "Testing", "Shipped").forEach { s ->
            val isSel = status == s
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) projectColor else projectColor.copy(alpha = 0.1f))
                    .clickable { onStatusSelected(s) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(s.uppercase(), color = if (isSel) Color.Black else projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
