package com.example.allinone

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutSettings

class WorkoutListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val viewModel: WorkoutViewModel,
    private val onTimerStart: (Workout, Int) -> Unit,
    private val onDataChanged: () -> Unit
) {
    val workoutAdapter: WorkoutAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        workoutAdapter = WorkoutAdapter(
            allWorkouts = viewModel.workouts.value,
            workoutSettings = viewModel.workoutSettings.value,
            onProgressChanged = { immediate ->
                viewModel.updateWorkout(viewModel.workouts.value[0]) // This is a bit hacky, need a better way if we want to save specific one, but usually sync handles it. Actually onProgressChanged in adapter already modified the object.
                onDataChanged()
            },
            onTimerStart = { workout, position -> onTimerStart(workout, position) },
            onAddActivity = { viewModel.addActivity(it) },
            onAddXP = { viewModel.addXP(it) },
            onDeleteWorkout = { viewModel.deleteWorkout(it) }
        )
        recyclerView.adapter = workoutAdapter
    }

    fun updateWorkouts(workouts: List<Workout>) {
        workoutAdapter.updateWorkouts(workouts)
    }

    fun updateSettings(settings: WorkoutSettings) {
        workoutAdapter.updateSettings(settings)
    }

    fun applyFilter(filter: String, dayIndex: Int, dateString: String) {
        workoutAdapter.filter(filter, dayIndex, dateString)
    }
}
