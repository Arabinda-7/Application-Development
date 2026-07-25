package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onTimerStart: (Workout, Int) -> Unit,
    private val onDataChanged: () -> Unit
) {
    val workoutAdapter: WorkoutAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        workoutAdapter = WorkoutAdapter(DataManager.workouts, {
            DataManager.saveData(context)
            onDataChanged()
        }, { workout, position -> onTimerStart(workout, position) })
        recyclerView.adapter = workoutAdapter
    }

    fun applyFilter(filter: String, dayIndex: Int, dateString: String) {
        workoutAdapter.filter(filter, dayIndex, dateString)
    }
}
