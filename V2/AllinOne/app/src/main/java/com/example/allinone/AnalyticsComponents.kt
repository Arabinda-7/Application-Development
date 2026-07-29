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
import androidx.compose.ui.draw.clip
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
fun StabilityGauge(stabilityIndex: Float, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            CircularProgressIndicator(
                progress = { stabilityIndex / 100f },
                modifier = Modifier.fillMaxSize(),
                color = themeColor,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeWidth = 8.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
fun WeeklyCyclicalRadarChart(cyclicalData: Map<Int, Float>, themeColor: Color) {
    val labels = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val distribution = labels.indices.associate { labels[it] to (cyclicalData[it]?.toInt() ?: 0) }
    MuscleRadarChart(muscleDistribution = distribution, themeColor = themeColor)
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
fun ACWRChart(data: Pair<List<Float>, List<Float>>, themeColor: Color) {
    val acute = data.first
    val chronic = data.second
    val maxVal = (acute + chronic).maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LegendItem(color = themeColor, label = "Fatigue (Acute)")
            Spacer(modifier = Modifier.width(12.dp))
            LegendItem(color = Color.White.copy(alpha = 0.3f), label = "Fitness (Chronic)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (acute.size - 1).coerceAtLeast(1)

                // Sweet Spot Corridor (0.8 - 1.3 of Chronic)
                val corridorPath = Path()
                chronic.forEachIndexed { i, c ->
                    val x = i * stepX
                    val y = height - (height * (c * 1.3f / maxVal)).coerceIn(0f, height)
                    if (i == 0) corridorPath.moveTo(x, y) else corridorPath.lineTo(x, y)
                }
                for (i in chronic.indices.reversed()) {
                    val x = i * stepX
                    val y = height - (height * (chronic[i] * 0.8f / maxVal)).coerceIn(0f, height)
                    corridorPath.lineTo(x, y)
                }
                corridorPath.close()
                drawPath(corridorPath, Color(0xFF4CAF50).copy(alpha = 0.1f))

                // Chronic Line
                val chronicPath = Path()
                chronic.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = height - (height * (v / maxVal)).coerceIn(0f, height)
                    if (i == 0) chronicPath.moveTo(x, y) else chronicPath.lineTo(x, y)
                }
                drawPath(chronicPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 2.dp.toPx()))

                // Acute Area
                val acutePath = Path()
                acute.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = height - (height * (v / maxVal)).coerceIn(0f, height)
                    if (i == 0) acutePath.moveTo(x, y) else acutePath.lineTo(x, y)
                }
                acutePath.lineTo(width, height)
                acutePath.lineTo(0f, height)
                acutePath.close()
                drawPath(acutePath, Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.3f), Color.Transparent)))
                
                val acuteLinePath = Path()
                acute.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = height - (height * (v / maxVal)).coerceIn(0f, height)
                    if (i == 0) acuteLinePath.moveTo(x, y) else acuteLinePath.lineTo(x, y)
                }
                drawPath(acuteLinePath, themeColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The green corridor represents your optimal 'Sweet Spot' for growth without overtraining.",
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
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
                style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            drawArc(
                brush = Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green), center),
                startAngle = 135f,
                sweepAngle = 270f * score,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
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
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
fun MonthlyMomentumChart(data: List<Pair<String, Int>>, themeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (month, percent) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(percent / 100f)
                            .background(
                                brush = Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.4f), themeColor)),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = month, fontSize = 10.sp, color = Color.Gray)
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
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${(progress * 100).toInt()}% to your next milestone!",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun VolumeProgressionChart(data: List<Float>, themeColor: Color) {
    val maxVolume = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.BottomCenter) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { volume ->
                    val heightFactor = (volume / maxVolume).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFactor)
                            .background(
                                brush = Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.4f), themeColor)),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DAILY WORKLOAD PROGRESSION (LATEST 30 DAYS)",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun WorkoutDiversityChart(data: Map<String, Int>, themeColor: Color) {
    val total = data.values.sum().coerceAtLeast(1)
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { (mode, count) ->
            val percentage = (count.toFloat() / total)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mode.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(themeColor, CircleShape)
                    )
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

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}
