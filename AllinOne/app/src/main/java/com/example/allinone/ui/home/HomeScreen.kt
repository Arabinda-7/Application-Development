package com.example.allinone.ui.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.*
import com.example.allinone.ui.home.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    state: DashboardState,
    onNavigateToHabits: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWorkspace: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPerformanceHistory: () -> Unit = {},
    onQuickAddTodo: () -> Unit = {},
    onQuickAddExpense: () -> Unit = {},
    onQuickAddNote: () -> Unit = {},
    onColorSelected: (String, Int) -> Unit = { _, _ -> },
    onMoodSelected: (String) -> Unit = {},
    onSearchRequested: (String) -> Unit = {}
) {
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSpeedDial by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showVoiceComingSoon by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "Aura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    val style = LocalAppStyle.current

    val moodTheme = remember(state.currentMood, style.accentColor) {
        val colorInt = UIUtils.getMoodColor(state.currentMood, style.accentColor.toArgb())
        Color(colorInt) to UIUtils.getMoodMessage(state.currentMood)
    }

    val smartGreeting = remember(state.overallProgress, state.userName, state.currentMood) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timePrefix = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
        
        if (state.currentMood != null && moodTheme.second.isNotEmpty()) {
            moodTheme.second
        } else {
            val icon = when (hour) {
                in 5..11 -> "☕"
                in 12..16 -> "🚀"
                in 17..20 -> "🧘"
                else -> "🌙"
            }
            val milestone = when {
                state.overallProgress >= 100 -> "Elite Momentum! 🏆"
                state.overallProgress >= 70 -> "Crushing it! 🔥"
                state.overallProgress >= 30 -> "Great start! ⚡"
                else -> icon
            }
            "$timePrefix, $milestone"
        }
    }

    val transition = updateTransition(targetState = showSpeedDial, label = "SpeedDial")
    val dialRotation by transition.animateFloat(label = "Rotation") { if (it) 45f else 0f }

    val todayDateString = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val showRedDot = state.todayAgenda.isNotEmpty() && (DataManager.lastViewedNotificationDate != todayDateString || DataManager.hasNewTodayNotifications)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                QuickActionItem(
                    label = "Task",
                    icon = Icons.Default.Add,
                    color = Color(0xFF2EC4B6),
                    isVisible = showSpeedDial,
                    offsetY = 0.dp,
                    offsetX = (-85).dp,
                    onClick = { onQuickAddTodo(); showSpeedDial = false }
                )

                QuickActionItem(
                    label = "Cash",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFE91E63),
                    isVisible = showSpeedDial,
                    offsetY = (-60).dp,
                    offsetX = (-60).dp,
                    onClick = { onQuickAddExpense(); showSpeedDial = false }
                )

                QuickActionItem(
                    label = "Note",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF3A86F0),
                    isVisible = showSpeedDial,
                    offsetY = (-85).dp,
                    offsetX = 0.dp,
                    onClick = { onQuickAddNote(); showSpeedDial = false }
                )

                val context = LocalContext.current
                val config = LocalConfiguration.current
                val standardDensity = remember(context, config) { 
                    Density(
                        density = context.resources.displayMetrics.density,
                        fontScale = config.fontScale
                    )
                }

                CompositionLocalProvider(LocalDensity provides standardDensity) {
                    FloatingActionButton(
                        onClick = { showSpeedDial = !showSpeedDial },
                        containerColor = style.accentColor,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Quick Action",
                            modifier = Modifier.size(26.dp).graphicsLayer(rotationZ = dialRotation)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                }
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader(
                state = state,
                isSearchVisible = isSearchVisible,
                onSearchToggle = { isSearchVisible = !isSearchVisible },
                onNotificationsClick = { 
                    showNotificationsDialog = true
                    DataManager.lastViewedNotificationDate = todayDateString
                    DataManager.hasNewTodayNotifications = false
                    DataManager.saveData(context)
                },
                onSettingsClick = onNavigateToSettings,
                onProfileClick = onNavigateToProfile,
                onMoodSelected = onMoodSelected,
                onSearchRequested = onSearchRequested,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                moodTheme = moodTheme,
                smartGreeting = smartGreeting,
                showRedDot = showRedDot
            )

            Spacer(modifier = Modifier.height(16.dp))

            val showExecutiveCard = (state.showPerformanceSection && (state.showHabitSection || state.showWorkoutSection)) || state.showFinanceSection
            if (showExecutiveCard) {
                Card(
                    shape = RoundedCornerShape(style.borderRadius),
                    colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.showPerformanceSection && (state.showHabitSection || state.showWorkoutSection)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Daily Performance", 
                                        color = style.accentColor, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Black, 
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.clickable { onNavigateToPerformanceHistory() }
                                    )
                                    Text("${state.overallProgress}% Completed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            
                            if (state.showFinanceSection) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Safe Spend", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(String.format(Locale.getDefault(), "₹%.0f", state.safeSpendAmount), color = Color(0xFF2EC4B6), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        
                        if (state.showPerformanceSection && (state.showHabitSection || state.showWorkoutSection)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { state.overallProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = style.accentColor,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            if (state.recentActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pulse Activity", modifier = Modifier.padding(horizontal = 20.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recentActions) { action ->
                        Surface(color = style.surfaceColor, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(40.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(style.accentColor.copy(alpha = 0.5f), CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(action, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            if (state.showHabitSection || state.showWorkoutSection) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Growth & Discipline",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                DashboardPair(
                    item1 = if (state.showHabitSection) {
                        {
                            HabitCard(
                                progress = state.habitProgress,
                                color = Color(if (state.habitColor == -1) 0xFFFF7A59 else state.habitColor.toLong()),
                                icon = state.habitIcon,
                                onClick = { onNavigateToHabits() },
                                onColorClick = { showColorPicker = "HABIT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showWorkoutSection) {
                        {
                            WorkoutCard(
                                progress = state.workoutProgress,
                                color = Color(if (state.workoutColor == -1) 0xFFFFB800 else state.workoutColor.toLong()),
                                icon = state.workoutIcon,
                                onClick = { onNavigateToWorkout() },
                                onColorClick = { showColorPicker = "WORKOUT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )
            }

            if (state.currentMood != null && (state.showHabitSection || state.showWorkoutSection)) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = style.accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(style.borderRadius),
                    border = BorderStroke(0.5.dp, style.accentColor.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.growthAdvice,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (state.showTaskSection || state.showNoteSection || state.showProjectSection || state.showFinanceSection) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Management & Notes",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                DashboardPair(
                    item1 = if (state.showTaskSection) {
                        {
                            TaskCard(
                                color = Color(if (state.taskColor == -1) 0xFF2EC4B6 else state.taskColor.toLong()),
                                icon = state.taskIcon,
                                onClick = { onNavigateToTodos() },
                                onColorClick = { showColorPicker = "TASK" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showNoteSection) {
                        {
                            NoteCard(
                                color = Color(if (state.noteColor == -1) 0xFF3A86F0 else state.noteColor.toLong()),
                                icon = state.noteIcon,
                                onClick = { onNavigateToNotes() },
                                onColorClick = { showColorPicker = "NOTE" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )

                if ((state.showTaskSection || state.showNoteSection) && (state.showProjectSection || state.showFinanceSection)) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                DashboardPair(
                    item1 = if (state.showProjectSection) {
                        {
                            ProjectCard(
                                color = Color(if (state.projectColor == -1) 0xFF1A73E8 else state.projectColor.toLong()),
                                icon = state.projectIcon,
                                onClick = { onNavigateToProjects() },
                                onColorClick = { showColorPicker = "PROJECT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showFinanceSection) {
                        {
                            FinanceCard(
                                amount = state.safeSpendAmount,
                                color = Color(if (state.financeColor == -1) 0xFFE91E63 else state.financeColor.toLong()),
                                icon = state.financeIcon,
                                onClick = { onNavigateToFinance() },
                                onColorClick = { showColorPicker = "FINANCE" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )
            }

            if (state.currentMood != null && (state.showTaskSection || state.showNoteSection || state.showProjectSection || state.showFinanceSection)) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF2EC4B6).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF2EC4B6).copy(alpha = 0.2f)),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFF2EC4B6), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.managementAdvice, 
                            color = Color.White.copy(alpha = 0.8f), 
                            fontSize = 11.sp, 
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showColorPicker != null) {
        AlertDialog(
            onDismissRequest = { showColorPicker = null },
            title = { Text("Choose Theme Color", color = Color.White) },
            containerColor = Color(0xFF1A1A1A),
            text = {
                val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colors.forEach { colorInt ->
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(colorInt)).clickable { onColorSelected(showColorPicker!!, colorInt.toInt()); showColorPicker = null }.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorPicker = null }) { Text("CLOSE", color = Color(0xFF1A73E8)) } }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Today's Agenda", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (state.todayAgenda.isEmpty()) {
                        Text("Your agenda is clear for today!", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    } else {
                        state.todayAgenda.forEach { (section, items) ->
                            Text(
                                text = section,
                                color = style.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            
                            var lastColorInt: Int? = null
                            
                            items.forEach { item ->
                                val rawItemColor = if (item.color != -1) item.color else style.accentColor.toArgb()
                                // Logic: If same as previous color, use a slight variation or a fixed fallback to ensure distinction
                                val finalItemColorInt = if (lastColorInt != null && lastColorInt == rawItemColor) {
                                    // Fallback: If it's a conflict, use a secondary color or darken it
                                    UIUtils.darkenColor(rawItemColor, 0.7f)
                                } else {
                                    rawItemColor
                                }
                                lastColorInt = finalItemColorInt
                                val itemColor = Color(finalItemColorInt)

                                Surface(
                                    onClick = {
                                        when (item.navigationTarget) {
                                            "TASK_ACTIVITY" -> onNavigateToTodos()
                                            "PROJECT_ACTIVITY" -> {
                                                val intent = Intent(context, ViewProjectActivity::class.java).apply {
                                                    val index = item.id.toIntOrNull() ?: item.parentId?.toIntOrNull()
                                                    putExtra("PROJECT_INDEX", index ?: -1)
                                                }
                                                context.startActivity(intent)
                                            }
                                            "WORKSPACE" -> onNavigateToWorkspace()
                                            "NOTE_ACTIVITY" -> onNavigateToNotes()
                                        }
                                        showNotificationsDialog = false
                                    },
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().padding(end = 80.dp)
                                            ) {
                                                val icon = when(item.category) {
                                                    "TASKS" -> Icons.Default.CheckCircle
                                                    "PROJECTS" -> Icons.Default.DateRange
                                                    "SUBFEATURES" -> Icons.Default.Info
                                                    else -> Icons.Default.Notifications
                                                }
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = itemColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item.title,
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            
                                            val timeStr = if (item.time != 0L) {
                                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.time))
                                            } else ""
                                            
                                            Row(modifier = Modifier.padding(start = 28.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                if (timeStr.isNotEmpty()) {
                                                    Text(
                                                        text = timeStr,
                                                        color = itemColor.copy(alpha = 0.8f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                if (item.details.isNotEmpty()) {
                                                    Text(
                                                        text = item.details,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.align(Alignment.TopEnd),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val tagText = buildString {
                                                append(item.category)
                                                if (item.priority.isNotEmpty()) {
                                                    append(" | ")
                                                    append(item.priority.uppercase())
                                                }
                                            }
                                            
                                            if (tagText.isNotEmpty()) {
                                                Surface(
                                                    color = itemColor.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, itemColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = tagText,
                                                        color = itemColor,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { showNotificationsDialog = false }) { 
                    Text("DISMISS", color = Color(0xFF1A73E8), fontWeight = FontWeight.Bold) 
                } 
            }
        )
    }

    if (showVoiceComingSoon) {
        AlertDialog(
            onDismissRequest = { showVoiceComingSoon = false },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Mic, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Voice Assistant Coming Soon", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("We are currently training the AI to recognize your specific productivity commands.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = { showVoiceComingSoon = false }) { Text("UNDERSTOOD", color = Color(0xFF1A73E8)) } }
        )
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, isVisible: Boolean, offsetY: Dp, offsetX: Dp, onClick: () -> Unit) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val standardDensity = remember(context, config) { 
        Density(
            density = context.resources.displayMetrics.density,
            fontScale = config.fontScale
        )
    }

    val animatedAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, label = "Alpha")
    val animatedScale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, label = "Scale")
    
    if (animatedAlpha > 0f) {
        CompositionLocalProvider(LocalDensity provides standardDensity) {
            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .graphicsLayer(alpha = animatedAlpha, scaleX = animatedScale, scaleY = animatedScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardPair(item1: (@Composable () -> Unit)?, item2: (@Composable () -> Unit)?) {
    if (item1 == null && item2 == null) return
    
    Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        if (item1 != null) {
            Box(modifier = Modifier.weight(1f)) { item1() }
        }
        
        if (item1 != null && item2 != null) {
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        if (item2 != null) {
            Box(modifier = Modifier.weight(1f)) { item2() }
        }
    }
}
