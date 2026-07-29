package com.example.allinone

import android.widget.ProgressBar
import android.widget.TextView

class HabitProgressSection(
    private val progressBar: ProgressBar,
    private val progressText: TextView
) {
    fun update() {
        val progress = DataManager.getHabitProgress()
        progressBar.progress = progress
        progressText.text = "$progress%"
    }
}
