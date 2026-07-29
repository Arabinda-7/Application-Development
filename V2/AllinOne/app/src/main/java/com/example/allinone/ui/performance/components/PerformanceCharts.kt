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
fun TrendChart(data: List<Pair<Int, Int>>, themeColor: Color, isWorkoutContext: Boolean = false) {
    val days = listOf("F", "S", "S", "M", "T", "W", "T")
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isWorkoutContext) {
                LegendItem(color = themeColor, label = "Habits")
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (isWorkoutContext || data.any { it.second > 0 }) {
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
                    habitProgress = pair.first.toFloat() / 100f,
                    workoutProgress = pair.second.toFloat() / 100f,
                    themeColor = themeColor,
                    isWorkoutContext = isWorkoutContext
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.width(34.dp), // Matched to double bar + spacer width
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DoubleBar(habitProgress: Float, workoutProgress: Float, themeColor: Color, isWorkoutContext: Boolean = false) {
    val fullHeight = 90.dp
    Row(verticalAlignment = Alignment.Bottom) {
        // Habit Bar Column
        if (!isWorkoutContext) {
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
                        .height(fullHeight * habitProgress.coerceIn(0.1f, 1f))
                        .background(
                            brush = Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.4f), themeColor)),
                            shape = CircleShape
                        )
                )
            }
        }

        if (!isWorkoutContext && workoutProgress > 0) {
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Workout Bar Column
        if (isWorkoutContext || workoutProgress > 0) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(fullHeight) // Made equal to Habit bar height
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fullHeight * workoutProgress.coerceIn(0.1f, 1f))
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
