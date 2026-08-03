package com.example.allinone.ui.performance.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.allinone.DataManager
import com.example.allinone.getHeatmapData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerformanceCalendarSection(
    currentMonth: Calendar,
    selectedDate: String,
    themeColor: Color,
    onDateSelected: (String) -> Unit,
    onMonthChanged: (Calendar) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 1000) { 2000 } 
    
    LaunchedEffect(currentMonth) {
        val today = Calendar.getInstance()
        val monthDiff = (currentMonth.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * 12 + 
                        (currentMonth.get(Calendar.MONTH) - today.get(Calendar.MONTH))
        val targetPage = 1000 + monthDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val diff = pagerState.currentPage - 1000
        val targetMonth = Calendar.getInstance()
        targetMonth.add(Calendar.MONTH, diff)
        
        if (targetMonth.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH) || 
            targetMonth.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
            onMonthChanged(targetMonth)
        }
    }

    DashboardCard(title = "MONTHLY HISTORY") {
        Column {
            // Month Navigation Labels (Arrows removed as requested)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SWIPE TO CHANGE MONTH",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(240.dp)
            ) { page ->
                val monthToRender = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, page - 1000)
                }
                
                CalendarGrid(
                    calendar = monthToRender,
                    selectedDate = selectedDate,
                    themeColor = themeColor,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    calendar: Calendar,
    selectedDate: String,
    themeColor: Color,
    onDateSelected: (String) -> Unit
) {
    val tempCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    val heatmapData = remember(calendar) { DataManager.getHeatmapData(calendar, "ALL") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        items(firstDayOfWeek) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        items(daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            val dayCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            val dateKey = sdf.format(dayCal.time)
            val progress = if (dayIndex < heatmapData.size) heatmapData[dayIndex] else 0

            CalendarDayItem(
                day = day,
                progress = progress,
                isSelected = dateKey == selectedDate,
                isToday = dateKey == todayStr,
                themeColor = themeColor,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDateSelected(dateKey) }
            )
        }
    }
}

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
                        val strokeWidth = 1.dp.toPx()
                        val inset = strokeWidth / 2
                        drawRoundRect(
                            color = themeColor,
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            style = Stroke(width = strokeWidth)
                        )
                        
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
