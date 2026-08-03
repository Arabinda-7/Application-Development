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
fun PunchCardChart(densityData: Map<Int, Map<String, Int>>, themeColor: Color) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val times = listOf("Morning", "Afternoon", "Evening", "Anytime")
    
    val maxCount = densityData.values.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(60.dp))
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
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

                for (j in 0 until numPoints) {
                    val angle = j * angleStep - (PI / 2).toFloat()
                    val x = center.x + radius * cos(angle)
                    val y = center.y + radius * sin(angle)
                    drawLine(Color.White.copy(alpha = 0.08f), center, Offset(x, y), strokeWidth = 1.dp.toPx())
                }
            }
            
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
fun WeeklyCyclicalRadarChart(cyclicalData: Map<Int, Float>, themeColor: Color) {
    val labels = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    val distribution = labels.indices.associate { labels[it] to (cyclicalData[it]?.toInt() ?: 0) }
    MuscleRadarChart(muscleDistribution = distribution, themeColor = themeColor)
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

                val chronicPath = Path()
                chronic.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = height - (height * (v / maxVal)).coerceIn(0f, height)
                    if (i == 0) chronicPath.moveTo(x, y) else chronicPath.lineTo(x, y)
                }
                drawPath(chronicPath, Color.White.copy(alpha = 0.2f), style = Stroke(width = 2.dp.toPx()))

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
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}
