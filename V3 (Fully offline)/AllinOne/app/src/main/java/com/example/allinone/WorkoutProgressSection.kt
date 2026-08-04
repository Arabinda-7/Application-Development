package com.example.allinone

import android.widget.ProgressBar
import android.widget.TextView

class WorkoutProgressSection(
    private val progressBar: ProgressBar,
    private val progressText: TextView
) {
    fun update() {
        val progress = DataManager.getWorkoutProgress()
        progressBar.progress = progress
        progressText.text = "$progress%"
    }
}
