package com.example.allinone

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerformanceDashboardScreen(
    onBack: () -> Unit,
    onDateSelected: (String) -> Unit,
    selectedDate: String,
    currentMonth: Calendar,
    onMonthChanged: (Calendar) -> Unit,
    onShowPicker: () -> Unit,
    performanceData: DayHistory,
    trendData: List<Pair<Int, Int>>,
    currentMood: String? = null
) {
    val moodColorTarget = remember(currentMood) {
        when (currentMood) {
            "🔥" -> Color(0xFFFFB800)
            "⚡" -> Color(0xFF2EC4B6)
            "🧘" -> Color(0xFF673AB7)
            "💼" -> Color(0xFF1A73E8)
            "😴" -> Color(0xFF9E9E9E)
            "🧠" -> Color(0xFF3F51B5)
            else -> Color(0xFF1A73E8)
        }
    }

    val animatedMoodColor by animateColorAsState(
        targetValue = moodColorTarget,
        animationSpec = tween(durationMillis = 500),
        label = "MoodColorAnimation"
    )

    var isPerformanceExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Aura Header Background matching Home Screen (localized to top content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedMoodColor.copy(alpha = 0.6f), Color.Black)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val sdfMonth = SimpleDateFormat("MMMM", Locale.getDefault())
                    val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(currentMonth) {
                                var totalDrag = 0f
                                var hasSwiped = false
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        totalDrag = 0f
                                        hasSwiped = false
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        if (!hasSwiped) {
                                            totalDrag += dragAmount
                                            if (totalDrag > 100) {
                                                val newCal = currentMonth.clone() as Calendar
                                                newCal.add(Calendar.MONTH, -1)
                                                onMonthChanged(newCal)
                                                hasSwiped = true
                                            } else if (totalDrag < -100) {
                                                val newCal = currentMonth.clone() as Calendar
                                                newCal.add(Calendar.MONTH, 1)
                                                onMonthChanged(newCal)
                                                hasSwiped = true
                                            }
                                        }
                                    }
                                )
                            },
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = sdfMonth.format(currentMonth.time).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sdfYear.format(currentMonth.time),
                            fontSize = 20.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onShowPicker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Calendar Header
            item {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF808080),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Calendar Grid
            item {
                val calendar = currentMonth.clone() as Calendar
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val gridMonthStr = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(calendar.time)

                Column(modifier = Modifier.fillMaxWidth()) {
                    var day = 1
                    for (row in 0..5) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (col in 0..6) {
                                val currentIdx = row * 7 + col
                                if (currentIdx < firstDayOfWeek || day > daysInMonth) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val dateStr = gridMonthStr + day.toString().padStart(2, '0')
                                    CalendarDayItem(
                                        day = day,
                                        isSelected = dateStr == selectedDate,
                                        isToday = dateStr == todayStr,
                                        themeColor = animatedMoodColor,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { onDateSelected(dateStr) }
                                    )
                                    day++
                                }
                            }
                        }
                        if (day > daysInMonth) break
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Performance Card
            item {
                val formattedDate = try {
                    val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(selectedDate)
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date!!).uppercase()
                } catch (e: Exception) { "JULY 16, 2026" }

                DashboardCard(
                    title = "PERFORMANCE FOR $formattedDate",
                    modifier = Modifier.clickable { isPerformanceExpanded = !isPerformanceExpanded }
                ) {
                    PerformanceSummary(
                        data = performanceData,
                        isExpanded = isPerformanceExpanded,
                        themeColor = animatedMoodColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Trend Card
            item {
                DashboardCard(title = "7-DAY COMPLETION TREND") {
                    TrendChart(trendData, animatedMoodColor)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CalendarDayItem(day: Int, isSelected: Boolean, isToday: Boolean, themeColor: Color, modifier: Modifier = Modifier) {
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
                if (isSelected) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Main circle outline
                        drawCircle(
                            color = themeColor,
                            radius = size.minDimension / 2.2f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        // Thick progress arch at the top
                        drawArc(
                            color = themeColor,
                            startAngle = -120f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                            size = size * 0.8f,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = day.toString(), color = Color.White, fontSize = 14.sp)
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(themeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = day.toString(), color = Color.LightGray, fontSize = 14.sp)
                    }
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
fun DashboardCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.1.em
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun PerformanceSummary(data: DayHistory, isExpanded: Boolean, themeColor: Color) {
    val totalItems = data.totalHabits + data.totalWorkouts
    val totalCompleted = data.habitsCompleted + data.workoutsCompleted
    val overallPercent = if (totalItems > 0) (totalCompleted * 100) / totalItems else 0

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

                if (data.totalHabits > 0) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (totalItems > 0) {
                    ProgressRow(
                        icon = "Σ",
                        label = "Total Performance ($totalCompleted/$totalItems)",
                        progress = totalCompleted.toFloat() / totalItems,
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
        animationSpec = tween(durationMillis = 600),
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
fun TrendChart(data: List<Pair<Int, Int>>, themeColor: Color) {
    val days = listOf("F", "S", "S", "M", "T", "W", "T")
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = themeColor, label = "Habits")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFF29D9C3), label = "Workouts")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { pair ->
                DoubleBar(
                    habitProgress = pair.first.toFloat() / 100f,
                    workoutProgress = pair.second.toFloat() / 100f,
                    themeColor = themeColor
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.width(34.dp), // Matched to double bar + spacer width
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DoubleBar(habitProgress: Float, workoutProgress: Float, themeColor: Color) {
    val fullHeight = 90.dp
    Row(verticalAlignment = Alignment.Bottom) {
        // Habit Bar Column
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(fullHeight)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fullHeight * habitProgress.coerceIn(0.1f, 1f))
                    .background(
                        brush = Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.4f), themeColor)),
                        shape = CircleShape
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(6.dp))
        
        // Workout Bar Column
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(fullHeight) // Made equal to Habit bar height
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fullHeight * workoutProgress.coerceIn(0.1f, 1f))
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF29D9C3).copy(alpha = 0.4f), Color(0xFF29D9C3))),
                        shape = CircleShape
                    )
            )
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
