package com.example.allinone

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class HabitPerformanceSection(
    private val rootView: View
) {
    fun update(dateKey: String) {
        val habits = DataManager.habits
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val historyData = if (dateKey == today) {
            val hCount = habits.size
            val hComp = habits.count { it.isCompleted }
            DayHistory(hComp, hCount, 0, 0)
        } else {
            val snapshot = DataManager.history[dateKey]
            if (snapshot != null) {
                DayHistory(snapshot.habitsCompleted, snapshot.totalHabits, 0, 0)
            } else {
                val cal = Calendar.getInstance()
                try { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateKey)?.let { cal.time = it } } catch (e: Exception) {}
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1)
                val activeHabits = habits.filter {
                    it.timestamp <= cal.timeInMillis + 86400000 && (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(dayIndex))
                }
                DayHistory(activeHabits.count { it.completedDates.contains(dateKey) }, activeHabits.size, 0, 0)
            }
        }

        val sdfInput = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val formattedDate = try {
            sdfOutput.format(sdfInput.parse(dateKey) ?: Date())
        } catch (e: Exception) { "" }

        rootView.findViewById<TextView>(R.id.tv_performance_title)?.text = "PERFORMANCE FOR ${formattedDate.uppercase()}"

        val totalItems = historyData.totalHabits
        val totalCompleted = historyData.habitsCompleted
        val overallPercent = if (totalItems > 0) (totalCompleted * 100) / totalItems else 0

        rootView.findViewById<TextView>(R.id.tv_overall_percentage)?.text = "$overallPercent%"

        rootView.findViewById<TextView>(R.id.tv_habits_stat_label)?.text = "[H] Habits (${historyData.habitsCompleted}/${historyData.totalHabits})"
        val hPercent = if (historyData.totalHabits > 0) (historyData.habitsCompleted * 100) / historyData.totalHabits else 0
        rootView.findViewById<TextView>(R.id.tv_habits_stat_percent)?.text = "$hPercent%"
        rootView.findViewById<ProgressBar>(R.id.pb_habits_history)?.progress = hPercent

        rootView.findViewById<View>(R.id.tv_workouts_stat_label)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.tv_workouts_stat_percent)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.pb_workouts_history)?.visibility = View.GONE

        rootView.findViewById<TextView>(R.id.tv_total_stat_label)?.text = "Σ Total Performance ($totalCompleted/$totalItems)"
        rootView.findViewById<TextView>(R.id.tv_total_stat_percent)?.text = "$overallPercent%"
        rootView.findViewById<ProgressBar>(R.id.pb_total_history)?.progress = overallPercent

        // New Analytics
        updateAdvancedAnalytics()
    }

    private fun updateAdvancedAnalytics() {
        // We might want to know which habit is selected if any, 
        // but for now the XML history usually shows overall. 
        // If we had a selector we'd pass the name here.
        val selectedHabitName: String? = null 

        val milestone = DataManager.getStreakMilestoneProgress(selectedHabitName)
        rootView.findViewById<TextView>(R.id.tv_next_milestone)?.text = "NEXT: ${milestone.second} DAYS"
        rootView.findViewById<TextView>(R.id.tv_current_streak_value)?.text = "${milestone.first} DAY STREAK"
        rootView.findViewById<ProgressBar>(R.id.pb_streak_milestone)?.progress = (milestone.third * 100).toInt()
        rootView.findViewById<TextView>(R.id.tv_milestone_hint)?.text = "${(milestone.third * 100).toInt()}% to your next milestone!"

        val stability = DataManager.getStabilityIndex(selectedHabitName)
        rootView.findViewById<TextView>(R.id.tv_stability_score)?.text = "${stability.toInt()}%"

        val resilience = DataManager.getResilienceScore(selectedHabitName)
        rootView.findViewById<TextView>(R.id.tv_resilience_score)?.text = "${resilience.toInt()}%"

        val momentum = DataManager.getMonthlyMomentumHistory(selectedHabitName)
        val container = rootView.findViewById<android.widget.LinearLayout>(R.id.monthly_momentum_container)
        container?.let { populateMomentumChart(it, momentum) }
    }

    private fun populateMomentumChart(container: android.widget.LinearLayout, data: List<Pair<String, Int>>) {
        container.removeAllViews()
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")
        
        data.forEach { (month, percent) ->
            val column = android.widget.LinearLayout(rootView.context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }

            val bar = View(rootView.context).apply {
                val h = (80 * percent / 100).let { if (it < 4) 4 else it } // min height
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    (20 * rootView.context.resources.displayMetrics.density).toInt(),
                    (h * rootView.context.resources.displayMetrics.density).toInt()
                )
                background = createBarBackground(habitColor)
            }

            val label = TextView(rootView.context).apply {
                text = month
                textSize = 8f
                setTextColor(android.graphics.Color.parseColor("#80FFFFFF"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, (4 * rootView.context.resources.displayMetrics.density).toInt(), 0, 0)
            }

            column.addView(bar)
            column.addView(label)
            container.addView(column)
        }
    }

    private fun createBarBackground(color: Int): android.graphics.drawable.Drawable {
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        shape.cornerRadius = 4f * rootView.context.resources.displayMetrics.density
        shape.setColor(color)
        shape.alpha = 200
        return shape
    }
}
