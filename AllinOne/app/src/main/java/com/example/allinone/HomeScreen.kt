package com.example.allinone

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onColorSelected: (String, Int) -> Unit = { _, _ -> },
    onMoodSelected: (String) -> Unit = {}
) {
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSpeedDial by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showSpeedDial) {
                    SpeedDialItem(label = "New Task", icon = Icons.Default.Add, color = Color(0xFF2EC4B6), onClick = { onNavigateToTodos(); showSpeedDial = false })
                    Spacer(modifier = Modifier.height(12.dp))
                    SpeedDialItem(label = "Add Expense", icon = Icons.Default.ShoppingCart, color = Color(0xFFE91E63), onClick = { onNavigateToFinance(); showSpeedDial = false })
                    Spacer(modifier = Modifier.height(12.dp))
                    SpeedDialItem(label = "Quick Note", icon = Icons.Default.Edit, color = Color(0xFF3A86F0), onClick = { onNavigateToNotes(); showSpeedDial = false })
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(
                    onClick = { showSpeedDial = !showSpeedDial },
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (showSpeedDial) Icons.Default.Close else Icons.Default.Add, contentDescription = "Quick Action")
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
            // Header with Dynamic Greeting & Search Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A73E8).copy(alpha = 0.7f), Color.Black)
                        )
                    )
                    .padding(top = 32.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${state.userName}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.dateString,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isSearchVisible = !isSearchVisible },
                            modifier = Modifier.background(Color(0xFF1A1A1A).copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Toggle Search",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.background(Color(0xFF1A1A1A).copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Universal Search Bar (Animated)
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects, tasks, or notes...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
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
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Executive Summary Card (Includes Safe-Spend)
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DAILY PERFORMANCE", color = Color(0xFF1A73E8), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text("${state.overallProgress}% Today's Target", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Pulse Activity Feed
            if (state.recentActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("PULSE ACTIVITY", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recentActions) { action ->
                        Surface(
                            color = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF1A73E8), CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(action, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Sentiment Tracker
            Spacer(modifier = Modifier.height(24.dp))
            Text("CURRENT MOOD", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val moods = listOf("🔥", "⚡", "🧘", "💼", "😴", "🧠")
                items(moods) { mood ->
                    val isSelected = state.currentMood == mood
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF1A73E8) else Color(0xFF1A1A1A))
                            .clickable { onMoodSelected(mood) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mood, fontSize = 22.sp)
                    }
                }
            }

            // Insight Pill
            Spacer(modifier = Modifier.height(24.dp))
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

            // Section Group: Growth
            Spacer(modifier = Modifier.height(32.dp))
            Text("GROWTH & DISCIPLINE", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            DashboardPair(
                item1 = {
                    DashboardCard(
                        title = "Habits",
                        subtitle = "${state.habitProgress}% Done",
                        iconRes = state.habitIcon,
                        backgroundColor = if (state.habitColor != -1) Color(state.habitColor) else Color(0xFFFF7A59),
                        onClick = onNavigateToHabits,
                        onLongClick = { showColorPicker = "HABIT" }
                    )
                },
                item2 = {
                    DashboardCard(
                        title = "Workouts",
                        subtitle = "${state.workoutProgress}% Done",
                        iconRes = state.workoutIcon,
                        backgroundColor = if (state.workoutColor != -1) Color(state.workoutColor) else Color(0xFFFFB800),
                        onClick = onNavigateToWorkout,
                        onLongClick = { showColorPicker = "WORKOUT" }
                    )
                }
            )

            // Section Group: Management
            Spacer(modifier = Modifier.height(32.dp))
            Text("MANAGEMENT & LOGIC", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            DashboardPair(
                item1 = {
                    DashboardCard(
                        title = "Tasks",
                        subtitle = "Smart To-Dos",
                        iconRes = state.taskIcon,
                        backgroundColor = if (state.taskColor != -1) Color(state.taskColor) else Color(0xFF2EC4B6),
                        onClick = onNavigateToTodos,
                        onLongClick = { showColorPicker = "TASK" }
                    )
                },
                item2 = {
                    DashboardCard(
                        title = "Notes",
                        subtitle = "Secure Vault",
                        iconRes = state.noteIcon,
                        backgroundColor = if (state.noteColor != -1) Color(state.noteColor) else Color(0xFF3A86F0),
                        onClick = onNavigateToNotes,
                        onLongClick = { showColorPicker = "NOTE" }
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            DashboardPair(
                item1 = {
                    DashboardCard(
                        title = "Projects",
                        subtitle = "Roadmap Boards",
                        iconRes = state.projectIcon,
                        backgroundColor = if (state.projectColor != -1) Color(state.projectColor) else Color(0xFF1A73E8),
                        onClick = onNavigateToProjects,
                        onLongClick = { showColorPicker = "PROJECT" }
                    )
                },
                item2 = {
                    DashboardCard(
                        title = "Finance",
                        subtitle = "Debt & Budgets",
                        iconRes = state.financeIcon,
                        backgroundColor = if (state.financeColor != -1) Color(state.financeColor) else Color(0xFFE91E63),
                        onClick = onNavigateToFinance,
                        onLongClick = { showColorPicker = "FINANCE" }
                    )
                }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Color Picker Dialog
    if (showColorPicker != null) {
        AlertDialog(
            onDismissRequest = { showColorPicker = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Custom Theme Color", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                val colors = listOf(
                    Color(0xFFFF7A59), Color(0xFFFFB800), Color(0xFF2EC4B6), Color(0xFF3A86F0),
                    Color(0xFF1A73E8), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
                    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(colors[index], CircleShape)
                                .clickable {
                                    onColorSelected(showColorPicker!!, colors[index].toArgb())
                                    showColorPicker = null
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = null }) {
                    Text("CLOSE", color = Color(0xFF1A73E8), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun DashboardPair(item1: @Composable () -> Unit, item2: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) { item1() }
        Box(modifier = Modifier.weight(1f)) { item2() }
    }
}

@Composable
fun SpeedDialItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }) {
        Surface(color = Color(0xFF1A1A1A), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 12.dp)) {
            Text(label, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        SmallFloatingActionButton(onClick = onClick, containerColor = color, contentColor = Color.White, shape = CircleShape) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DashboardCard(
    title: String,
    iconRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    subtitle: String? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.9f)),
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
