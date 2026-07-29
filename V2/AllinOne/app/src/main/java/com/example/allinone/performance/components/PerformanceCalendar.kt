package com.example.allinone.performance.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun CalendarDayItem(
    day: Int, 
    progress: Int,
    isSelected: Boolean, 
    isToday: Boolean, 
    themeColor: Color, 
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f).padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background based on progress
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = if (progress == 0) Color.White.copy(alpha = 0.05f) 
                                    else themeColor.copy(alpha = (progress / 100f).coerceIn(0.15f, 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                if (isSelected) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Square selection border
                        val strokeWidth = 1.dp.toPx()
                        val inset = strokeWidth / 2
                        drawRoundRect(
                            color = themeColor,
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Small indicator for selection
                        drawArc(
                            color = themeColor,
                            startAngle = -120f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                            size = size * 0.7f,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = day.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                } else {
                    Text(text = day.toString(), color = Color.LightGray, fontSize = 14.sp)
                }
            }
            if (isToday) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(themeColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String, 
    modifier: Modifier = Modifier, 
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.1.em
            )
            
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
