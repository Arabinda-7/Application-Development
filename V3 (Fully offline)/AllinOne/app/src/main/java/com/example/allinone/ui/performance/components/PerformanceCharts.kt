package com.example.allinone.ui.performance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrendChart(
    data: List<Pair<Int, Int>>, 
    themeColor: Color, 
    filterType: com.example.allinone.ui.performance.state.PerformanceFilterType = com.example.allinone.ui.performance.state.PerformanceFilterType.OVERALL
) {
    val days = listOf("F", "S", "S", "M", "T", "W", "T")
    val showHabits = filterType == com.example.allinone.ui.performance.state.PerformanceFilterType.OVERALL || filterType == com.example.allinone.ui.performance.state.PerformanceFilterType.HABITS
    val showWorkouts = filterType == com.example.allinone.ui.performance.state.PerformanceFilterType.OVERALL || filterType == com.example.allinone.ui.performance.state.PerformanceFilterType.WORKOUTS
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHabits) {
                LegendItem(color = themeColor, label = "Habits")
            }
            if (showHabits && showWorkouts) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (showWorkouts) {
                LegendItem(color = Color(0xFF29D9C3), label = "Workouts")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { pair ->
                DoubleBar(
                    habitProgress = if (showHabits) pair.first.toFloat() / 100f else 0f,
                    workoutProgress = if (showWorkouts) pair.second.toFloat() / 100f else 0f,
                    themeColor = themeColor,
                    showHabits = showHabits,
                    showWorkouts = showWorkouts
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.width(if (showHabits && showWorkouts) 34.dp else 24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DoubleBar(
    habitProgress: Float, 
    workoutProgress: Float, 
    themeColor: Color, 
    showHabits: Boolean = true,
    showWorkouts: Boolean = true
) {
    val fullHeight = 90.dp
    Row(verticalAlignment = Alignment.Bottom) {
        // Habit Bar Column
        if (showHabits) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(fullHeight)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fullHeight * habitProgress.coerceIn(0.05f, 1f))
                        .background(
                            brush = Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.4f), themeColor)),
                            shape = CircleShape
                        )
                )
            }
        }

        if (showHabits && showWorkouts) {
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Workout Bar Column
        if (showWorkouts) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(fullHeight)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fullHeight * workoutProgress.coerceIn(0.05f, 1f))
                        .background(
                            brush = Brush.verticalGradient(listOf(Color(0xFF29D9C3).copy(alpha = 0.4f), Color(0xFF29D9C3))),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}
