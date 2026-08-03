package com.example.allinone.ui.performance.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.allinone.ConsistencyHeatmap
import com.example.allinone.PunchCardChart

/**
 * PerformanceSections: Houses composite analytical sections (Heatmaps, Punch cards, Radar charts).
 */
@Composable
fun PerformanceHeatmapSection(
    heatmapData: List<Int>,
    animatedMoodColor: Color
) {
    if (heatmapData.isNotEmpty()) {
        PerformanceCard(title = "CONSISTENCY HEATMAP") {
            ConsistencyHeatmap(heatmapData, animatedMoodColor)
        }
    }
}
