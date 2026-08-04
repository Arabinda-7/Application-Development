package com.example.allinone.ui.components.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StabilityGauge(stabilityIndex: Float, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            CircularProgressIndicator(
                progress = { stabilityIndex / 100f },
                modifier = Modifier.fillMaxSize(),
                color = themeColor,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${stabilityIndex.toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "STABILITY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (stabilityIndex > 85) "Your routine is highly stable!" 
                   else if (stabilityIndex > 60) "Moderate consistency. Keep it up." 
                   else "Routine is volatile. Focus on small wins.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StabilityChaosGauge(score: Float, themeColor: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            
            drawArc(
                brush = Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green), center),
                startAngle = 135f,
                sweepAngle = 270f * score,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(score * 100).toInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (score > 0.8f) "STABLE" else if (score > 0.5f) "BALANCED" else "CHAOTIC",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
        }
    }
}

@Composable
fun ResilienceGauge(score: Float, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxSize(),
                color = themeColor,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${score.toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "RECOVERY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (score > 85) "Exceptional resilience! Breaks don't stop you." 
                   else if (score > 60) "Good recovery pace. Keep momentum high." 
                   else "Difficult to restart. Focus on 'Never Miss Twice'.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecoveryStatusDashboard(recoveryStatus: Map<String, Float>, themeColor: Color) {
    Column {
        recoveryStatus.entries.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { (muscle, status) ->
                    RecoveryItem(muscle, status, themeColor, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecoveryItem(muscle: String, status: Float, themeColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = muscle.uppercase(), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${(status * 100).toInt()}%", 
                    fontSize = 10.sp, 
                    color = themeColor, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(status)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(listOf(themeColor.copy(alpha = 0.7f), themeColor)),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun MilestoneProgressCard(current: Int, next: Int, progress: Float, themeColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$current DAY STREAK",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "NEXT: $next",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = themeColor,
            trackColor = Color.White.copy(alpha = 0.05f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${(progress * 100).toInt()}% to your next milestone!",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
