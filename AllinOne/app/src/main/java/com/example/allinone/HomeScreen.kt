package com.example.allinone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onSendMessage: (String) -> Unit
) {
    var aiText by remember { mutableStateOf("") }

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
                        icon = Icons.Default.Check,
                        backgroundColor = Color(0xFFFF7A59),
                        onClick = onNavigateToHabits
                    )
                }
                item {
                    DashboardCard(
                        title = "Workout Routine",
                        subtitle = "${state.workoutProgress}%",
                        icon = Icons.Default.FitnessCenter,
                        backgroundColor = Color(0xFFFFB800),
                        onClick = onNavigateToWorkout
                    )
                }
                item {
                    DashboardCard(
                        title = "Tasks",
                        icon = Icons.AutoMirrored.Filled.List,
                        backgroundColor = Color(0xFF2EC4B6),
                        onClick = onNavigateToTodos
                    )
                }
                item {
                    DashboardCard(
                        title = "Notes",
                        icon = Icons.Default.EditNote,
                        backgroundColor = Color(0xFF3A86F0),
                        onClick = onNavigateToNotes
                    )
                }
                item {
                    DashboardCard(
                        title = "Project",
                        icon = Icons.Default.RocketLaunch,
                        backgroundColor = Color(0xFF1A73E8),
                        onClick = onNavigateToProjects
                    )
                }
                item {
                    DashboardCard(
                        title = "Finance",
                        icon = Icons.Default.AccountBalance,
                        backgroundColor = Color(0xFF1A73E8),
                        onClick = onNavigateToFinance
                    )
                }
            }
        }

        // AI Command Input
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = aiText,
                    onValueChange = { aiText = it },
                    placeholder = { Text("Ask AI (e.g. 'Spent $12 on lunch')", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (aiText.isNotEmpty()) {
                            onSendMessage(aiText)
                            aiText = ""
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF1A73E8), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
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
