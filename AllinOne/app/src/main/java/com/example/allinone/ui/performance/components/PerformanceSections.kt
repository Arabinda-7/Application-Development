package com.example.allinone.ui.performance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.allinone.*

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

@Composable
fun PerformanceHabitAnalyticsSection(
    stabilityScore: Float,
    resilienceScore: Float,
    momentumData: List<Pair<String, Int>>,
    milestone: Triple<Int, Int, Float>,
    temporalDensity: Map<Int, Map<String, Int>>,
    correlations: List<Triple<String, String, Double>>,
    animatedMoodColor: Color
) {
    val analyticsPages = mutableListOf<@Composable () -> Unit>()
    
    analyticsPages.add {
        PerformanceCard(title = "STABILITY & RESILIENCE") {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    StabilityGauge(stabilityScore, animatedMoodColor)
                }
                Box(modifier = Modifier.weight(1f)) {
                    ResilienceGauge(resilienceScore, animatedMoodColor)
                }
            }
        }
    }
    
    analyticsPages.add {
        PerformanceCard(title = "MONTHLY MOMENTUM") {
            MonthlyMomentumChart(momentumData, animatedMoodColor)
        }
    }
    
    analyticsPages.add {
        PerformanceCard(title = "STREAK MILESTONES") {
            MilestoneProgressCard(milestone.first, milestone.second, milestone.third, animatedMoodColor)
        }
    }
    
    if (temporalDensity.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "TEMPORAL DENSITY") {
                PunchCardChart(temporalDensity, animatedMoodColor)
            }
        }
    }
    
    if (correlations.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "CORRELATION INSIGHTS") {
                CorrelationInsightCard(correlations, animatedMoodColor)
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { analyticsPages.size })
    
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { page ->
            analyticsPages[page]()
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Simple Page Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(analyticsPages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) animatedMoodColor else Color.White.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun PerformanceWorkoutAnalyticsSection(
    volumeData: List<Float>,
    diversityData: Map<String, Int>,
    intensityData: List<Int>,
    muscleFocus: Map<Int, List<String>>,
    muscleDistribution: Map<String, Int>,
    recoveryStatus: Map<String, Float>,
    acwrData: Pair<List<Float>, List<Float>>,
    animatedMoodColor: Color
) {
    val analyticsPages = mutableListOf<@Composable () -> Unit>()

    analyticsPages.add {
        PerformanceCard(title = "VOLUME PROGRESSION") {
            VolumeProgressionChart(volumeData, animatedMoodColor)
        }
    }
    
    analyticsPages.add {
        PerformanceCard(title = "WORKOUT DIVERSITY") {
            WorkoutDiversityChart(diversityData, animatedMoodColor)
        }
    }
    
    analyticsPages.add {
        PerformanceCard(title = "INTENSITY HEATMAP") {
            IntensityHeatmap(intensityData, animatedMoodColor)
        }
    }
    
    if (muscleFocus.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "MUSCLE FOCUS GRID") {
                MuscleFocusGrid(muscleFocus, animatedMoodColor)
            }
        }
    }
    
    if (muscleDistribution.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "MUSCLE DISTRIBUTION") {
                MuscleRadarChart(muscleDistribution, animatedMoodColor)
            }
        }
    }
    
    if (recoveryStatus.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "RECOVERY STATUS") {
                RecoveryStatusDashboard(recoveryStatus, animatedMoodColor)
            }
        }
    }
    
    if (acwrData.first.isNotEmpty()) {
        analyticsPages.add {
            PerformanceCard(title = "TRAINING LOAD (ACWR)") {
                ACWRChart(acwrData, animatedMoodColor)
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { analyticsPages.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { page ->
            analyticsPages[page]()
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simple Page Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(analyticsPages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) animatedMoodColor else Color.White.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}
