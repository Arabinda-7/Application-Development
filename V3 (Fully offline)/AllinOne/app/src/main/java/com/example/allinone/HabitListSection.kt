package com.example.allinone

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.example.allinone.data.model.Habit
import com.example.allinone.domain.repository.HabitSettings

class HabitListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val createButton: MaterialCardView,
    private val viewModel: HabitTrackerViewModel,
    private val onDataChanged: () -> Unit
) {
    private val habitAdapter: HabitAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        habitAdapter = HabitAdapter(
            allHabits = viewModel.habits.value,
            habitSettings = viewModel.habitSettings.value,
            onProgressChanged = { habit, progress, completed ->
                viewModel.toggleHabitCompletion(habit, progress, completed)
                onDataChanged()
            },
            onTimerStart = { _, _ -> },
            onAddActivity = { viewModel.updateActivity(it) },
            onAddXP = { viewModel.addXP(it) }
        )
        recyclerView.adapter = habitAdapter
        
        createButton.setOnClickListener {
            context.startActivity(Intent(context, AddHabitActivity::class.java))
        }
    }

    fun updateHabits(habits: List<Habit>) {
        habitAdapter.updateHabits(habits)
    }

    fun updateSettings(settings: HabitSettings) {
        habitAdapter.updateSettings(settings)
    }

    fun applyFilter(filter: String, dayIndex: Int, dateString: String) {
        habitAdapter.filter(filter, dayIndex, dateString)
    }

    fun setShowCompleted(show: Boolean) {
        habitAdapter.setShowCompleted(show)
    }
}
