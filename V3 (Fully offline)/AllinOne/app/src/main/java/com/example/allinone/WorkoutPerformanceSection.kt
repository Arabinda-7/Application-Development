package com.example.allinone

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.example.allinone.ui.components.analytics.*
import java.text.SimpleDateFormat
import java.util.*

class WorkoutPerformanceSection(
    private val rootView: View
) {
    fun update(dateKey: String) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val calendar = Calendar.getInstance()
        try { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateKey)?.let { calendar.time = it } } catch (e: Exception) {}
        
        val workoutsForDate = DataManager.workouts.filter { workout ->
            val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) - 1)
            
            (workout.repeatType != "SPECIFIC_DAYS" || workout.repeatDays.contains(dayIndex)) &&
            workout.timestamp <= calendar.timeInMillis + 86400000
        }

        val completedCount = workoutsForDate.count { 
            if (dateKey == today) it.isCompleted else it.completedDates.contains(dateKey)
        }
        val totalCount = workoutsForDate.size
        val overallPercent = if (totalCount > 0) {
            val totalProgress = workoutsForDate.sumOf { 
                if (dateKey == today) {
                    if (it.isCompleted) 100 else (it.progress * 100) / it.target.coerceAtLeast(1)
                } else {
                    it.dailyProgress[dateKey] ?: if (it.completedDates.contains(dateKey)) 100 else 0
                }
            }
            totalProgress / totalCount
        } else 0

        val sdfInput = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val formattedDate = try {
            sdfOutput.format(sdfInput.parse(dateKey) ?: Date())
        } catch (e: Exception) { "" }

        rootView.findViewById<TextView>(R.id.tv_performance_title)?.text = "PERFORMANCE FOR ${formattedDate.uppercase()}"
        rootView.findViewById<TextView>(R.id.tv_overall_percentage)?.text = "$overallPercent%"
        
        // Apply Theme
        val workoutColorInt = if (DataManager.globalWorkoutColor != -1) DataManager.globalWorkoutColor else android.graphics.Color.parseColor("#FFFFB800")
        val colorStateList = android.content.res.ColorStateList.valueOf(workoutColorInt)
        
        rootView.findViewById<TextView>(R.id.tv_overall_percentage)?.setTextColor(workoutColorInt)
        rootView.findViewById<ImageView>(R.id.performance_card_arrow)?.imageTintList = colorStateList
        
        rootView.findViewById<TextView>(R.id.tv_workouts_stat_label)?.text = "Workouts ($completedCount/$totalCount)"
        rootView.findViewById<TextView>(R.id.tv_workouts_stat_percent)?.text = "$overallPercent%"
        rootView.findViewById<ProgressBar>(R.id.pb_workouts_history)?.progress = overallPercent
        rootView.findViewById<ProgressBar>(R.id.pb_workouts_history)?.progressTintList = colorStateList

        // Update Compose-based Analytics
        val workoutColor = Color(workoutColorInt)
        
        rootView.findViewById<ComposeView>(R.id.compose_volume_trend)?.setContent {
            val volumeData = DataManager.getMonthlyVolumeData(calendar)
            VolumeProgressionChart(volumeData, workoutColor)
        }

        rootView.findViewById<ComposeView>(R.id.compose_diversity_chart)?.setContent {
            val diversityData = DataManager.getWorkoutDiversityData()
            WorkoutDiversityChart(diversityData, workoutColor)
        }

        rootView.findViewById<ComposeView>(R.id.compose_intensity_grid)?.setContent {
            val intensityData = DataManager.getIntensityDistribution(calendar)
            IntensityHeatmap(intensityData, workoutColor)
        }

        rootView.findViewById<ComposeView>(R.id.compose_muscle_focus)?.setContent {
            val muscleFocusData = DataManager.getDailyMuscleFocus(calendar)
            MuscleFocusGrid(muscleFocusData, workoutColor)
        }
    }
}
