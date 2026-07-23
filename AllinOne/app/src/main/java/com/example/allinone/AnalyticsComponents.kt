package com.example.allinone

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConsistencyHeatmap(data: List<Int>, themeColor: Color) {
    Column {
        // Grid of 7 columns (days of week)
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
fun PunchCardChart(densityData: Map<Int, Map<String, Int>>, themeColor: Color) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val times = listOf("Morning", "Afternoon", "Evening", "Anytime")
    
    val maxCount = densityData.values.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(60.dp)) // Label space
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        times.forEach { time ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = time,
                    modifier = Modifier.width(60.dp),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                for (day in 0..6) {
                    val count = densityData[day]?.get(time) ?: 0
                    val sizeFactor = (count.toFloat() / maxCount).coerceIn(0.1f, 1f)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size((20 * sizeFactor).dp)
                                .background(
                                    color = if (count > 0) themeColor.copy(alpha = 0.6f + (sizeFactor * 0.4f)) 
                                            else Color.White.copy(alpha = 0.05f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CorrelationInsightCard(correlations: List<Triple<String, String, Double>>, themeColor: Color) {
    Column {
        if (correlations.isEmpty()) {
            Text(
                text = "Tracking more habits for 14+ days will unlock correlation insights.",
                fontSize = 12.sp,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        } else {
            correlations.take(3).forEach { (h1, h2, score) ->
                val percentage = (score * 100).toInt()
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(themeColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SUCCESS TRIGGER",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Completing '$h1' is highly correlated with '$h2' ($percentage%)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MuscleRadarChart(muscleDistribution: Map<String, Int>, themeColor: Color) {
    val labels = muscleDistribution.keys.toList()
    val values = muscleDistribution.values.toList()
    val numPoints = labels.size
    val maxVal = values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2.5f
                val angleStep = (2 * PI / numPoints).toFloat()

                // Draw Background Polygons
                for (i in 1..4) {
                    val r = radius * (i / 4f)
                    val path = Path()
                    for (j in 0 until numPoints) {
                        val angle = j * angleStep - (PI / 2).toFloat()
                        val x = center.x + r * cos(angle)
                        val y = center.y + r * sin(angle)
                        if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, Color.White.copy(alpha = 0.05f), style = Stroke(width = 1.dp.toPx()))
                }

                // Draw Data Polygon with Gradient
                if (numPoints > 0) {
                    val dataPath = Path()
                    for (j in 0 until numPoints) {
                        val value = muscleDistribution[labels[j]] ?: 0
                        val r = radius * (value.toFloat() / maxVal).coerceIn(0.15f, 1f)
                        val angle = j * angleStep - (PI / 2).toFloat()
                        val x = center.x + r * cos(angle)
                        val y = center.y + r * sin(angle)
                        if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                    }
                    dataPath.close()
                    
                    drawPath(
                        path = dataPath,
                        brush = Brush.radialGradient(
                            colors = listOf(themeColor.copy(alpha = 0.6f), themeColor.copy(alpha = 0.1f)),
                            center = center,
                            radius = radius
                        ),
                        style = Fill
                    )
                    drawPath(dataPath, themeColor, style = Stroke(width = 2.dp.toPx()))
                }

                // Draw Axes
                for (j in 0 until numPoints) {
                    val angle = j * angleStep - (PI / 2).toFloat()
                    val x = center.x + radius * cos(angle)
                    val y = center.y + radius * sin(angle)
                    drawLine(Color.White.copy(alpha = 0.08f), center, Offset(x, y), strokeWidth = 1.dp.toPx())
                }
            }
            
            // Labels (Overlay)
            labels.forEachIndexed { index, label ->
                val angleStep = (2 * PI / numPoints).toFloat()
                val angle = index * angleStep - (PI / 2).toFloat()
                val xOffset = 115 * cos(angle)
                val yOffset = 115 * sin(angle)
                
                Box(modifier = Modifier.offset(xOffset.dp, yOffset.dp)) {
                    Text(
                        text = label.uppercase(),
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
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
