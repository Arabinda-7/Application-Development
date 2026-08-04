package com.example.allinone.feature.workout.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.allinone.BaseActivity
import com.example.allinone.R
import com.example.allinone.feature.workout.viewmodel.AddWorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * AddWorkoutActivity (Modern Architecture): UI controller bound to AddWorkoutViewModel.
 */
@AndroidEntryPoint
class AddWorkoutActivity : BaseActivity() {

    private val viewModel: AddWorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        val workoutId = intent.getLongExtra("WORKOUT_ID", -1L)
        viewModel.initialize(workoutId)
    }

    fun onSaveClicked(
        name: String,
        targetInput: String,
        targetSetsInput: String,
        repsPerSetInput: String,
        targetTimerInput: String
    ) {
        viewModel.saveWorkout(
            name = name,
            targetInput = targetInput,
            targetSetsInput = targetSetsInput,
            repsPerSetInput = repsPerSetInput,
            targetTimerInput = targetTimerInput,
            onSuccess = {
                Toast.makeText(this, "Workout saved", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
