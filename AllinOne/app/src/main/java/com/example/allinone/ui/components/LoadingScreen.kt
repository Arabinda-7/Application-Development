package com.example.allinone.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.DataManager
import com.example.allinone.R
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LoadingStep(
    val icon: Int,
    val name: String,
    val color: Color
)

@Composable
fun LoadingScreen(onFinished: () -> Unit = {}) {
    val loadingTime = DataManager.startupLoadingTime
    val progress = remember { Animatable(0f) }
    
    val steps = remember {
        mutableListOf<LoadingStep>()
            .apply {
                if (DataManager.showHabitSection) {
                    val color = if (DataManager.globalHabitColor != -1) Color(DataManager.globalHabitColor) else Color(0xFFFF7A59)
                    add(LoadingStep(R.drawable.icons8_yoga_100, "Loading Habits", color))
                }
                if (DataManager.showWorkoutSection) {
                    val color = if (DataManager.globalWorkoutColor != -1) Color(DataManager.globalWorkoutColor) else Color(0xFFFFB800)
                    add(LoadingStep(R.drawable.icons8_fitness_100, "Loading Workouts", color))
                }
                if (DataManager.showTaskSection) {
                    val color = if (DataManager.globalTaskColor != -1) Color(DataManager.globalTaskColor) else Color(0xFF2EC4B6)
                    add(LoadingStep(R.drawable.icons8_done_100, "Loading Tasks", color))
                }
                if (DataManager.showNoteSection) {
                    val color = if (DataManager.globalNoteColor != -1) Color(DataManager.globalNoteColor) else Color(0xFF3A86F0)
                    add(LoadingStep(R.drawable.icons8_bookmark_100, "Loading Notes", color))
                }
                if (DataManager.showProjectSection) {
                    val color = if (DataManager.globalProjectColor != -1) Color(DataManager.globalProjectColor) else Color(0xFF1A73E8)
                    add(LoadingStep(R.drawable.icons8_opened_folder_100, "Loading Projects", color))
                }
                if (DataManager.showFinanceSection) {
                    val color = if (DataManager.globalFinanceColor != -1) Color(DataManager.globalFinanceColor) else Color(0xFFE91E63)
                    add(LoadingStep(R.drawable.icons8_savings_100, "Loading Finance", color))
                }
                
                // Fallback if everything is disabled
                if (isEmpty()) {
                    add(LoadingStep(R.drawable.ic_rocket_launch, "Preparing", Color(0xFF1A73E8)))
                }
            }
    }
    
    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]
    
    val animatedColor by animateColorAsState(
        targetValue = currentStep.color,
        animationSpec = tween(500),
        label = "StepColor"
    )

    LaunchedEffect(Unit) {
        // Handle Progress
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = loadingTime,
                    easing = LinearEasing
                )
            )
            onFinished()
        }
        
        // Handle Step Rotation
        launch {
            val stepDuration = if (steps.size > 1) loadingTime / steps.size else loadingTime
            while (progress.value < 1f) {
                delay(stepDuration.toLong())
                if (currentStepIndex < steps.size - 1) {
                    currentStepIndex++
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Progress with Dynamic Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Progress Arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = animatedColor,
                        startAngle = -90f,
                        sweepAngle = 360 * progress.value,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Animated Icons
                Crossfade(
                    targetState = currentStep,
                    animationSpec = tween(500),
                    label = "StepCrossfade"
                ) { step ->
                    Icon(
                        painter = painterResource(id = step.icon),
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "All In One",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Dynamic Step Name
            Crossfade(
                targetState = currentStep.name,
                animationSpec = tween(500),
                label = "NameCrossfade"
            ) { name ->
                Text(
                    text = "$name...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Percentage
            Text(
                text = "${(progress.value * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}
