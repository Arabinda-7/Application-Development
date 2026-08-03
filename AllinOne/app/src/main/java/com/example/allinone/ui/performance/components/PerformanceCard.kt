package com.example.allinone.ui.performance.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.DayHistory
import com.example.allinone.ui.performance.state.PerformanceFilterType

/**
 * PerformanceCard: Renders momentum cards, summary statistics, and trend container cards.
 */
@Composable
fun PerformanceCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF121216),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun PerformanceSummaryContainer(
    performanceData: DayHistory,
    animatedMoodColor: Color,
    primaryFilter: PerformanceFilterType,
    isWorkoutContext: Boolean,
    onShowPicker: () -> Unit
) {
    PerformanceCard(title = "DAILY PROGRESS SUMMARY") {
        PerformanceSummary(
            data = performanceData,
            isExpanded = true,
            themeColor = animatedMoodColor,
            isWorkoutContext = isWorkoutContext || primaryFilter == PerformanceFilterType.WORKOUTS
        )
    }
}
