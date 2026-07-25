package com.example.allinone

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ProfileImpactSummarySection(
    private val rootView: View
) {
    fun update() {
        // Habits Stat
        val totalHabits = DataManager.getTotalHabitsFinished()
        updateStat(R.id.stat_habits, R.drawable.ic_habit_tracker, totalHabits.toString(), "HABITS")

        // Savings Stat
        val totalSavings = DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
        updateStat(R.id.stat_savings, R.drawable.ic_finance, "${DataManager.financeCurrency}${totalSavings.toInt()}", "SAVED")

        // Projects Stat
        val totalProjects = DataManager.projects.count { it.category == "Project" && it.status == "Completed" }
        updateStat(R.id.stat_projects, R.drawable.ic_project, totalProjects.toString(), "DONE")
    }

    private fun updateStat(containerId: Int, icon: Int, value: String, label: String) {
        val container = rootView.findViewById<View>(containerId)
        container.findViewById<ImageView>(R.id.iv_stat_icon).setImageResource(icon)
        container.findViewById<TextView>(R.id.tv_stat_value).text = value
        container.findViewById<TextView>(R.id.tv_stat_label).text = label
    }
}
