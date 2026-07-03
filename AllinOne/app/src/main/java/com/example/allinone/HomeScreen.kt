package com.example.allinone

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // New Feature States
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showVoiceComingSoon by remember { mutableStateOf(false) }

    // Keyboard Controller
    val keyboardController = LocalSoftwareKeyboardController.current

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    // Animation for Speed Dial
    val transition = updateTransition(targetState = showSpeedDial, label = "SpeedDial")
    val dialRotation by transition.animateFloat(label = "Rotation") { if (it) 45f else 0f }

    Scaffold(
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                // 1. New Task Shortcut (Moves LEFT)
                QuickActionItem(
                    label = "Task",
                    icon = Icons.Default.Add,
                    color = Color(0xFF2EC4B6),
                    isVisible = showSpeedDial,
                    offsetY = 0.dp,
                    offsetX = (-100).dp,
                    onClick = { onQuickAddTodo(); showSpeedDial = false }
                )

                // 2. Add Expense Shortcut (Moves DIAGONAL)
                QuickActionItem(
                    label = "Cash",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFE91E63),
                    isVisible = showSpeedDial,
                    offsetY = (-70).dp,
                    offsetX = (-70).dp,
                    onClick = { onQuickAddExpense(); showSpeedDial = false }
                )

                // 3. Quick Note Shortcut (Moves UP)
                QuickActionItem(
                    label = "Note",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF3A86F0),
                    isVisible = showSpeedDial,
                    offsetY = (-100).dp,
                    offsetX = 0.dp,
                    onClick = { onQuickAddNote(); showSpeedDial = false }
                )

                FloatingActionButton(
                    onClick = { showSpeedDial = !showSpeedDial },
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Quick Action",
                        modifier = Modifier.graphicsLayer(rotationZ = dialRotation)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEXT: ${state.nextMilestone}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. Aura Header with Controls ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A73E8).copy(alpha = 0.6f), Color.Black)
                        )
                    )
                    .padding(top = 24.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A1A1A))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    .clickable { onNavigateToProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = state.userAvatarRes),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF2EC4B6), CircleShape).border(2.dp, Color.Black, CircleShape))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                IconButton(onClick = { showNotificationsDialog = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Notifications, "Alerts", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Box(modifier = Modifier.size(7.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A1A1A))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                    .clickable { onNavigateToSettings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "$greeting,", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = state.userName, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 2. Universal Command Bar (Search/Voice) ---
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ask or search anything...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A73E8),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            cursorColor = Color(0xFF1A73E8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.White.copy(alpha = 0.5f)) },
                        trailingIcon = { 
                            IconButton(onClick = { 
                                if (searchQuery.isNotEmpty()) {
                                    onSearchRequested(searchQuery)
                                    keyboardController?.hide()
                                } else {
                                    showVoiceComingSoon = true 
                                }
                            }) {
                                Icon(
                                    imageVector = if (searchQuery.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send, 
                                    "Action", 
                                    tint = Color(0xFF1A73E8)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchRequested(searchQuery)
                                keyboardController?.hide()
                            }
                        }),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Sentiment Tracker (Mood Log) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CURRENT FOCUS", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(state.dateString, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val moods = listOf("🔥", "⚡", "🧘", "💼", "😴", "🧠")
                items(moods) { mood ->
                    val isSelected = state.currentMood == mood
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) Color(0xFF1A73E8) else Color(0xFF1A1A1A))
                            .clickable { onMoodSelected(mood) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mood, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Executive Summary Card (Safe Spend & Performance) ---
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "DAILY PERFORMANCE", 
                                color = Color(0xFF1A73E8), 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 1.sp,
                                modifier = Modifier.clickable { onNavigateToPerformanceHistory() }
                            )
                            Text("${state.overallProgress}% Completed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SAFE SPEND", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.getDefault(), "₹%.0f", state.safeSpendAmount), color = Color(0xFF2EC4B6), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { state.overallProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFF1A73E8),
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // --- 5. Pulse Activity Feed ---
            if (state.recentActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("PULSE ACTIVITY", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recentActions) { action ->
                        Surface(color = Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(40.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF1A73E8), CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(action, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 6. Pro-Insight Pill ---
            Surface(
                color = Color(0xFF1A73E8).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(state.proTip, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            // --- 7-12. Diversified Growth & Management Sections ---
            Spacer(modifier = Modifier.height(32.dp))
            Text("GROWTH & DISCIPLINE", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            DashboardPair(
                item1 = { HabitCard(progress = state.habitProgress, color = Color(if (state.habitColor == -1) 0xFFFF7A59 else state.habitColor.toLong()), icon = state.habitIcon, onClick = onNavigateToHabits, onColorClick = { showColorPicker = "HABIT" }) },
                item2 = { WorkoutCard(progress = state.workoutProgress, color = Color(if (state.workoutColor == -1) 0xFFFFB800 else state.workoutColor.toLong()), icon = state.workoutIcon, onClick = onNavigateToWorkout, onColorClick = { showColorPicker = "WORKOUT" }) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("MANAGEMENT & NOTES", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            DashboardPair(
                item1 = { TaskCard(color = Color(if (state.taskColor == -1) 0xFF2EC4B6 else state.taskColor.toLong()), icon = state.taskIcon, onClick = onNavigateToTodos, onColorClick = { showColorPicker = "TASK" }) },
                item2 = { NoteCard(color = Color(if (state.noteColor == -1) 0xFF3A86F0 else state.noteColor.toLong()), icon = state.noteIcon, onClick = onNavigateToNotes, onColorClick = { showColorPicker = "NOTE" }) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            DashboardPair(
                item1 = { ProjectCard(color = Color(if (state.projectColor == -1) 0xFF1A73E8 else state.projectColor.toLong()), icon = state.projectIcon, onClick = onNavigateToProjects, onColorClick = { showColorPicker = "PROJECT" }) },
                item2 = { FinanceCard(amount = state.safeSpendAmount, color = Color(if (state.financeColor == -1) 0xFFE91E63 else state.financeColor.toLong()), icon = state.financeIcon, onClick = onNavigateToFinance, onColorClick = { showColorPicker = "FINANCE" }) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- Overlay Dialogs ---
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
            title = { Text("Recent Alerts", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Column {
                    NotificationItem("Habit Streak", "5 days and counting! Keep it up.")
                    NotificationItem("Finance", "You reached 70% of your budget.")
                    NotificationItem("Projects", "Milestone 'UI Logic' is due tomorrow.")
                }
            },
            confirmButton = { TextButton(onClick = { showNotificationsDialog = false }) { Text("DONE", color = Color(0xFF1A73E8)) } }
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
                    Text("Voice Assistant Coming Soon", color = Color.White, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("We are currently training the AI to recognize your specific productivity commands.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = { showVoiceComingSoon = false }) { Text("UNDERSTOOD", color = Color(0xFF1A73E8)) } }
        )
    }
}

// --- Specialized Section Cards ---

@Composable
fun HabitCard(progress: Int, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().height(160.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("HABITS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("Daily Rituals", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = -1f),
                    color = color,
                    strokeWidth = 8.dp,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    strokeCap = StrokeCap.Round
                )
                Text("$progress%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun WorkoutCard(progress: Int, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().height(160.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WORKOUT", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text("ACTIVE MODE", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$progress%", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = color, trackColor = Color.White.copy(alpha = 0.05f), strokeCap = StrokeCap.Round)
        }
    }
}

@Composable
fun TaskCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().height(140.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("TASKS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}

@Composable
fun NoteCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().height(140.dp).graphicsLayer(rotationZ = -2f).border(1.dp, color.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("IDEAS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Canvas", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
        }
    }
}

@Composable
fun ProjectCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().height(130.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PROJECTS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { index -> Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(if (index < 2) color else Color.White.copy(alpha = 0.05f))) }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
        }
    }
}

@Composable
fun FinanceCard(amount: Double, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().height(130.dp).border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("VAULT", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(String.format(Locale.getDefault(), "₹%.0f", amount), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, isVisible: Boolean, offsetY: Dp, offsetX: Dp, onClick: () -> Unit) {
    val animatedAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, label = "Alpha")
    val animatedScale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, label = "Scale")
    if (animatedAlpha > 0f) {
        Box(modifier = Modifier.offset(x = offsetX, y = offsetY).graphicsLayer(alpha = animatedAlpha, scaleX = animatedScale, scaleY = animatedScale).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NotificationItem(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(body, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
    }
}

@Composable
fun DashboardPair(item1: @Composable () -> Unit, item2: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) { item1() }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f)) { item2() }
    }
}
