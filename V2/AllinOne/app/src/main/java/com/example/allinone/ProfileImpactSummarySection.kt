package com.example.allinone

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ProfileImpactSummarySection(
    private val rootView: View
) {
    fun update(accentColor: Int? = null) {
        // Habits Stat
        val totalHabits = DataManager.getTotalHabitsFinished()

        // Savings Stat
        val totalSavings = synchronized(DataManager.transactions) {
            DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
        }

        // Projects Stat
        val totalProjects = synchronized(DataManager.projects) {
            DataManager.projects.count { it.category == "Project" && it.status == "Completed" }
        }

        val hasData = totalHabits > 0 || totalSavings > 0 || totalProjects > 0
        
        val statsGrid = rootView.findViewById<View>(R.id.layout_stats_grid)
        val noDataView = rootView.findViewById<View>(R.id.layout_no_impact_data)

        if (hasData) {
            statsGrid.visibility = View.VISIBLE
            noDataView.visibility = View.GONE
            
            updateStat(R.id.stat_habits, R.drawable.ic_habit_tracker, totalHabits.toString(), "HABITS", accentColor)
            updateStat(R.id.stat_savings, R.drawable.ic_finance, "${DataManager.financeCurrency}${totalSavings.toInt()}", "SAVED", accentColor)
            updateStat(R.id.stat_projects, R.drawable.ic_project, totalProjects.toString(), "DONE", accentColor)
        } else {
            statsGrid.visibility = View.GONE
            noDataView.visibility = View.VISIBLE
        }
    }

    fun applyTint(color: Int) {
        update(color)
    }

    private fun updateStat(containerId: Int, icon: Int, value: String, label: String, accentColor: Int?) {
        val container = rootView.findViewById<View>(containerId)
        val ivIcon = container.findViewById<ImageView>(R.id.iv_stat_icon)
        ivIcon.setImageResource(icon)
        accentColor?.let { ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(it) }
        container.findViewById<TextView>(R.id.tv_stat_value).text = value
        container.findViewById<TextView>(R.id.tv_stat_label).text = label
    }
}
