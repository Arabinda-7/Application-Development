package com.example.allinone

import android.widget.ProgressBar
import android.widget.TextView

class HabitProgressSection(
    private val progressBar: ProgressBar,
    private val progressText: TextView
) {
    fun update(progress: Int) {
        progressBar.progress = progress
        progressText.text = "$progress%"
    }
}
