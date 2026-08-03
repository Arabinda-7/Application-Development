package com.example.allinone.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.ui.home.*

@Composable
fun GrowthDisciplineSection(
    showHabit: Boolean,
    showWorkout: Boolean,
    habitProgress: Int,
    workoutProgress: Int,
    habitColor: Int,
    workoutColor: Int,
    habitIcon: Int,
    workoutIcon: Int,
    auraAlpha: Float,
    onHabitClick: () -> Unit,
    onWorkoutClick: () -> Unit,
    onHabitColorClick: () -> Unit,
    onWorkoutColorClick: () -> Unit
) {
    if (!showHabit && !showWorkout) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Growth & Discipline",
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        DashboardPair(
            item1 = if (showHabit) {
                {
                    HabitCard(
                        progress = habitProgress,
                        color = Color(if (habitColor == -1) 0xFFFF7A59 else habitColor.toLong()),
                        icon = habitIcon,
                        onClick = onHabitClick,
                        onColorClick = onHabitColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null,
            item2 = if (showWorkout) {
                {
                    WorkoutCard(
                        progress = workoutProgress,
                        color = Color(if (workoutColor == -1) 0xFFFFB800 else workoutColor.toLong()),
                        icon = workoutIcon,
                        onClick = onWorkoutClick,
                        onColorClick = onWorkoutColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null
        )
    }
}
