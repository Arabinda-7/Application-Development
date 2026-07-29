package com.example.allinone.performance.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.DayHistory
import com.example.allinone.WorkoutProgressEntry

@Composable
fun PerformanceSummary(data: DayHistory, isExpanded: Boolean, themeColor: Color, isWorkoutContext: Boolean = false) {
    val effectiveTotalItems = if (isWorkoutContext) data.totalWorkouts else data.totalHabits + data.totalWorkouts
    val effectiveTotalCompleted = if (isWorkoutContext) data.workoutsCompleted else data.habitsCompleted + data.workoutsCompleted
    
    val overallPercent = if (effectiveTotalItems > 0) (effectiveTotalCompleted * 100) / effectiveTotalItems else 0

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$overallPercent%",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Details",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(text = "Overall Completion", fontSize = 12.sp, color = Color.Gray)
        
        androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))

                if (data.totalHabits > 0 && !isWorkoutContext) {
                    ProgressRow(
                        icon = "[H]",
                        label = "Habits (${data.habitsCompleted}/${data.totalHabits})",
                        progress = data.habitsCompleted.toFloat() / data.totalHabits,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (data.totalWorkouts > 0) {
                    ProgressRow(
                        icon = "[W]",
                        label = "Workouts (${data.workoutsCompleted}/${data.totalWorkouts})",
                        progress = data.workoutsCompleted.toFloat() / data.totalWorkouts,
                        color = Color(0xFF29D9C3)
                    )
                    
                    if (!data.workoutDetails.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "WORKOUT DETAILS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(data.workoutDetails) { entry ->
                                WorkoutProgressCircleItem(entry)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (effectiveTotalItems > 0) {
                    ProgressRow(
                        icon = "Σ",
                        label = "Total Performance ($effectiveTotalCompleted/$effectiveTotalItems)",
                        progress = effectiveTotalCompleted.toFloat() / effectiveTotalItems,
                        color = themeColor
                    )
                } else {
                    Text(
                        text = "No items scheduled for this day.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRow(icon: String, label: String, progress: Float, color: Color) {
    val percentage = (progress * 100).toInt()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            Text(text = "$percentage%", fontSize = 12.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomLinearProgressIndicator(progress = progress, color = color)
    }
}

@Composable
fun CustomLinearProgressIndicator(progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween<Float>(durationMillis = 600),
        label = "ProgressAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.8f), color)),
                    shape = RoundedCornerShape(3.dp)
                )
        )
    }
}

@Composable
fun WorkoutProgressCircleItem(entry: WorkoutProgressEntry) {
    val progress = if (entry.target > 0) entry.progress.toFloat() / entry.target else 0f
    val themeColor = if (entry.color != -1) Color(entry.color) else Color(0xFF29D9C3)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = themeColor,
                strokeWidth = 3.dp,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeCap = StrokeCap.Round
            )
            if (entry.isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = entry.name.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = "${entry.progress}/${entry.target}${entry.unit}",
            fontSize = 7.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
