package com.example.allinone

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun VoiceAuraGlow(
    isListening: Boolean,
    isThinking: Boolean,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraGlow")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    if (isListening || isThinking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotation)
                .blur(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0f),
                                accentColor.copy(alpha = pulseAlpha),
                                accentColor.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun GoogleVoiceBars(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceBars")
    
    val barColors = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFFEA4335), // Red
        Color(0xFFFBBC05), // Yellow
        Color(0xFF34A853)  // Green
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        barColors.forEachIndexed { index, color ->
            val heightMultiplier by if (isListening) {
                infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + (index * 100), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "BarHeight$index"
                )
            } else {
                remember { mutableStateOf(0.2f) }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(4.dp)
                    .fillMaxHeight(heightMultiplier)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
