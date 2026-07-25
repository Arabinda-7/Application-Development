package com.example.allinone.ui.performance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.*
import com.example.allinone.ui.performance.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerformanceDashboardScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    title: String? = null,
    onDateSelected: (String) -> Unit,
    selectedDate: String,
    currentMonth: Calendar,
    onMonthChanged: (Calendar) -> Unit,
    onShowPicker: () -> Unit,
    performanceData: DayHistory,
    trendData: List<Pair<Int, Int>>,
    currentMood: String? = null,
    overrideColor: Color? = null,
    isWorkoutContext: Boolean = false,
    showPerformanceCard: Boolean = true,
    showTrendCard: Boolean = true,
    showBackgroundAura: Boolean = true,
    habits: List<Habit> = emptyList(),
    selectedHabitName: String? = null,
    onHabitSelected: (String?) -> Unit = {},
    onSaveNote: (String, String) -> Unit = { _, _ -> }
) {
    val heatmapData = remember(currentMonth, isWorkoutContext, selectedHabitName) {
        if (isWorkoutContext) DataManager.getVolumeWeightedHeatmap(currentMonth)
        else if (selectedHabitName != null) com.example.allinone.data.HabitDataManager.getHabitSpecificHeatmap(selectedHabitName, currentMonth)
        else DataManager.getHeatmapData(currentMonth) 
    }
    val densityData = remember { DataManager.getTemporalDensityData() }
    val correlations = remember { DataManager.getHabitCorrelationMatrix() }
    
    val streaks = remember(selectedHabitName) {
        if (selectedHabitName != null) DataManager.getHabitStreaks(selectedHabitName)
        else null
    }

    val cyclicalData = remember(selectedHabitName) { DataManager.getWeeklyCyclicalData(selectedHabitName) }
    val stabilityIndex = remember(selectedHabitName) { DataManager.getStabilityIndex(selectedHabitName) }
    val resilienceScore = remember(selectedHabitName) { DataManager.getResilienceScore(selectedHabitName) }
    val momentumHistory = remember(selectedHabitName) { DataManager.getMonthlyMomentumHistory(selectedHabitName) }
    val milestoneProgress = remember(selectedHabitName) { DataManager.getStreakMilestoneProgress(selectedHabitName) }

    val muscleDistribution = remember { DataManager.getMuscleDistributionData() }
    val recoveryStatus = remember { DataManager.getMuscleRecoveryStatus() }
    val acwrData = remember { DataManager.getACWRData() }
    val workoutStability = remember { DataManager.getTrainingStabilityScore() }
    
    val volumeData = remember(currentMonth) { DataManager.getMonthlyVolumeData(currentMonth) }
    val diversityData = remember { DataManager.getWorkoutDiversityData() }
    val intensityData = remember(currentMonth) { DataManager.getIntensityDistribution(currentMonth) }
    val muscleFocusData = remember(currentMonth) { DataManager.getDailyMuscleFocus(currentMonth) }

    val moodColorTarget = remember(currentMood, overrideColor) {
        if (overrideColor != null) return@remember overrideColor
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
        modifier = modifier
            .fillMaxSize()
            .then(if (showBackgroundAura) Modifier.background(Color.Black) else Modifier)
    ) {
        // Fixed Background Aura
        if (showBackgroundAura) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(animatedMoodColor.copy(alpha = 0.6f), Color.Black)
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onBack != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onBack.invoke() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onShowPicker() },
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

                        if (title != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "MOMENTUM LOG",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = title.uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        // Habit Selector
                        if (!isWorkoutContext && habits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(end = 24.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedHabitName == null,
                                        onClick = { onHabitSelected(null) },
                                        label = { Text("OVERALL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = animatedMoodColor,
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            labelColor = Color.White
                                        ),
                                        border = null,
                                        shape = CircleShape
                                    )
                                }
                                items(habits) { habit ->
                                    FilterChip(
                                        selected = selectedHabitName == habit.name,
                                        onClick = { onHabitSelected(habit.name) },
                                        label = { Text(habit.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (habit.color != -1) Color(habit.color) else animatedMoodColor,
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            labelColor = Color.White
                                        ),
                                        border = null,
                                        shape = CircleShape
                                    )
                                }
                            }
                        }

                        val sdfMonth = SimpleDateFormat("MMMM", Locale.getDefault())
                        val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sdfMonth.format(currentMonth.time)} ${sdfYear.format(currentMonth.time)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = animatedMoodColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calendar Header
            item {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    var day = 1
                    for (row in 0..5) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (col in 0..6) {
                                val currentIdx = row * 7 + col
                                if (currentIdx < firstDayOfWeek || day > daysInMonth) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val dateStr = gridMonthStr + day.toString().padStart(2, '0')
                                    val dayProgress = heatmapData[day - 1] ?: 0
                                    CalendarDayItem(
                                        day = day,
                                        progress = dayProgress,
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
            if (showPerformanceCard) {
                item {
                    val formattedDate = try {
                        val sdfIn = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        val sdfOut = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        val date = sdfIn.parse(selectedDate)
                        if (date != null) sdfOut.format(date).uppercase() else selectedDate
                    } catch (e: Exception) { selectedDate }

                    DashboardCard(
                        title = "PERFORMANCE FOR $formattedDate",
                        modifier = Modifier.padding(horizontal = 24.dp).clickable { isPerformanceExpanded = !isPerformanceExpanded }
                    ) {
                        PerformanceSummary(
                            data = performanceData,
                            isExpanded = isPerformanceExpanded,
                            themeColor = animatedMoodColor,
                            isWorkoutContext = isWorkoutContext,
                            currentStreak = streaks?.first,
                            longestStreak = streaks?.second
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Trend Card
            if (showTrendCard) {
                item {
                    DashboardCard(
                        title = "7-DAY COMPLETION TREND",
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        TrendChart(trendData, animatedMoodColor, isWorkoutContext)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Daily Reflection Card
            item {
                var noteText by remember(selectedDate, performanceData.notes) { mutableStateOf(performanceData.notes ?: "") }
                DashboardCard(
                    title = "DAILY REFLECTION",
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Column {
                        TextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("How was your day? Any obstacles?", fontSize = 12.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = animatedMoodColor,
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        if (noteText != (performanceData.notes ?: "")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { onSaveNote(selectedDate, noteText) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("SAVE REFLECTION", color = animatedMoodColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Advanced Analytics / Insights
            item {
                if (isWorkoutContext) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        DashboardCard(
                            title = "PHYSIOLOGICAL READINESS (ACWR)",
                            description = "Acute:Chronic Workload Ratio. Compares your recent fatigue (7d) against long-term fitness (28d)."
                        ) {
                            ACWRChart(acwrData, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardCard(title = "MUSCLE BALANCE", modifier = Modifier.weight(1.2f)) {
                                MuscleRadarChart(muscleDistribution, animatedMoodColor)
                            }
                            DashboardCard(title = "TRAINING STABILITY", modifier = Modifier.weight(0.8f)) {
                                StabilityChaosGauge(workoutStability, animatedMoodColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "MUSCLE READINESS") {
                            RecoveryStatusDashboard(recoveryStatus, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "DAILY VOLUME PROGRESSION") {
                            VolumeProgressionChart(volumeData, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardCard(title = "WORKOUT DIVERSITY", modifier = Modifier.weight(1f)) {
                                WorkoutDiversityChart(diversityData, animatedMoodColor)
                            }
                            DashboardCard(title = "INTENSITY HEATMAP", modifier = Modifier.weight(1f)) {
                                IntensityHeatmap(intensityData, animatedMoodColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "MUSCLE FOCUS HEATMAP") {
                            MuscleFocusGrid(muscleFocusData, animatedMoodColor)
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        DashboardCard(title = "STREAK MILESTONES") {
                            MilestoneProgressCard(
                                current = milestoneProgress.first,
                                next = milestoneProgress.second,
                                progress = milestoneProgress.third,
                                themeColor = animatedMoodColor
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "MONTHLY MOMENTUM HISTORY") {
                            MonthlyMomentumChart(momentumHistory, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "WEEKLY CYCLICALITY") {
                            WeeklyCyclicalRadarChart(cyclicalData, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardCard(title = "ROUTINE STABILITY", modifier = Modifier.weight(1f)) {
                                StabilityGauge(stabilityIndex, animatedMoodColor)
                            }
                            DashboardCard(title = "RESILIENCE (RECOVERY)", modifier = Modifier.weight(1f)) {
                                ResilienceGauge(resilienceScore, animatedMoodColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "POWER HOURS (TEMPORAL SUCCESS)") {
                            PunchCardChart(densityData, animatedMoodColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        DashboardCard(title = "BEHAVIORAL CORRELATIONS") {
                            CorrelationInsightCard(correlations, animatedMoodColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Consistency Heatmap (Moved to bottom)
            item {
                val momentumDescription = if (isWorkoutContext) {
                    "Volume-weighted frequency map. Darker shades indicate higher intensity or total volume per session."
                } else {
                    "Daily completion heat-map across all tracked habits. Darker shades indicate higher success rates."
                }
                DashboardCard(
                    title = if (isWorkoutContext) "VOLUME INTENSITY" else "MONTHLY MOMENTUM",
                    description = momentumDescription,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    ConsistencyHeatmap(heatmapData.values.toList(), animatedMoodColor)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
