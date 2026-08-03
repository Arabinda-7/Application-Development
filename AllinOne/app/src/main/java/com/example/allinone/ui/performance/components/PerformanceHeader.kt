package com.example.allinone.ui.performance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.data.model.Habit
import com.example.allinone.ui.performance.state.PerformanceFilterType

/**
 * PerformanceHeader: Renders the top navigation bar, title text, 
 * filter selector chips, and habit selection chips.
 */
@Composable
fun PerformanceHeader(
    title: String?,
    onBack: (() -> Unit)?,
    showFilterSelector: Boolean,
    primaryFilter: PerformanceFilterType,
    animatedMoodColor: Color,
    habits: List<Habit>,
    selectedHabitName: String?,
    onFilterSelected: (PerformanceFilterType) -> Unit,
    onHabitSelected: (String?) -> Unit
) {
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

            if (showFilterSelector) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PerformanceFilterType.values().forEach { type ->
                        FilterChip(
                            selected = primaryFilter == type,
                            onClick = { onFilterSelected(type) },
                            label = { Text(type.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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
                }
            }

            if (primaryFilter == PerformanceFilterType.HABITS && habits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
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
                    items(habits.size) { index ->
                        val habit = habits[index]
                        FilterChip(
                            selected = selectedHabitName == habit.name,
                            onClick = { onHabitSelected(habit.name) },
                            label = { Text(habit.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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
                }
            }
        }
    }
}
