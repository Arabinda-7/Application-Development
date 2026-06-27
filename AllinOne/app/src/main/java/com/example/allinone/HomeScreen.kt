package com.example.allinone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    onColorSelected: (String, Int) -> Unit = { _, _ -> }
) {
    var showColorPicker by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Header
        Surface(
            color = Color(0xFF1A73E8),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "All in One",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.dateString,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Grid
        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Habit Tracker",
                        subtitle = "${state.habitProgress}%",
                        iconRes = state.habitIcon,
                        backgroundColor = if (state.habitColor != -1) Color(state.habitColor) else Color(0xFFFF7A59),
                        onClick = onNavigateToHabits,
                        onLongClick = { showColorPicker = "HABIT" }
                    )
                }
                item {
                    DashboardCard(
                        title = "Workout Routine",
                        subtitle = "${state.workoutProgress}%",
                        iconRes = state.workoutIcon,
                        backgroundColor = if (state.workoutColor != -1) Color(state.workoutColor) else Color(0xFFFFB800),
                        onClick = onNavigateToWorkout,
                        onLongClick = { showColorPicker = "WORKOUT" }
                    )
                }
                item {
                    DashboardCard(
                        title = "Tasks",
                        iconRes = state.taskIcon,
                        backgroundColor = if (state.taskColor != -1) Color(state.taskColor) else Color(0xFF2EC4B6),
                        onClick = onNavigateToTodos,
                        onLongClick = { showColorPicker = "TASK" }
                    )
                }
                item {
                    DashboardCard(
                        title = "Notes",
                        iconRes = state.noteIcon,
                        backgroundColor = if (state.noteColor != -1) Color(state.noteColor) else Color(0xFF3A86F0),
                        onClick = onNavigateToNotes,
                        onLongClick = { showColorPicker = "NOTE" }
                    )
                }
                item {
                    DashboardCard(
                        title = "Project",
                        iconRes = state.projectIcon,
                        backgroundColor = if (state.projectColor != -1) Color(state.projectColor) else Color(0xFF1A73E8),
                        onClick = onNavigateToProjects,
                        onLongClick = { showColorPicker = "PROJECT" }
                    )
                }
                item {
                    DashboardCard(
                        title = "Finance",
                        iconRes = state.financeIcon,
                        backgroundColor = if (state.financeColor != -1) Color(state.financeColor) else Color(0xFF1A73E8),
                        onClick = onNavigateToFinance,
                        onLongClick = { showColorPicker = "FINANCE" }
                    )
                }
            }
        }
    }

    if (showColorPicker != null) {
        AlertDialog(
            onDismissRequest = { showColorPicker = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Pick Color", color = Color.White) },
            text = {
                val colors = listOf(
                    Color(0xFFFF7A59), Color(0xFFFFB800), Color(0xFF2EC4B6), Color(0xFF3A86F0),
                    Color(0xFF1A73E8), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
                    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
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
                    Text("CLOSE", color = Color(0xFF1A73E8))
                }
            }
        )
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
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
