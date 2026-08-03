package com.example.allinone.ui.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConsistencyHeatmap(data: List<Int>, themeColor: Color) {
    Column {
        val rows = (data.size + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val index = r * 7 + c
                        if (index < data.size) {
                            val percentage = data[index]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (percentage == 0) Color.White.copy(alpha = 0.05f) 
                                                else themeColor.copy(alpha = (percentage / 100f).coerceIn(0.25f, 1f)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntensityHeatmap(data: List<Int>, themeColor: Color) {
    ConsistencyHeatmap(data = data, themeColor = themeColor)
}

@Composable
fun MuscleFocusGrid(data: Map<Int, List<String>>, themeColor: Color) {
    val muscles = listOf("Chest", "Back", "Legs", "Shoulders", "Arms")
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        muscles.forEach { muscle ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = muscle.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.width(60.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (day in 0 until 31) {
                        val isTrained = data[day]?.contains(muscle) ?: false
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (isTrained) themeColor else Color.White.copy(alpha = 0.05f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
