package com.example.allinone.core.utils

import android.app.Activity
import android.content.Context
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.Gravity
import com.example.allinone.R

object WorkoutUiHelper {

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    fun showTimerRollerDialog(activity: Activity, et: EditText, onConfirm: () -> Unit = {}) {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(24.dpToPx(activity), 16.dpToPx(activity), 24.dpToPx(activity), 16.dpToPx(activity))
        }

        val npMin = NumberPicker(activity).apply {
            minValue = 0; maxValue = 59; value = 0
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val npSec = NumberPicker(activity).apply {
            minValue = 0; maxValue = 59; value = 30
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        layout.addView(npMin)
        layout.addView(npSec)

        android.app.AlertDialog.Builder(activity)
            .setTitle("Set Timer (Min:Sec)")
            .setView(layout)
            .setPositiveButton("SET") { _, _ ->
                val totalSec = (npMin.value * 60) + npSec.value
                et.setText(totalSec.toString())
                onConfirm()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    fun showSingleRollerDialog(activity: Activity, title: String, et: EditText, min: Int, max: Int, onConfirm: () -> Unit = {}) {
        val np = NumberPicker(activity).apply {
            minValue = min; maxValue = max
            value = et.text.toString().toIntOrNull() ?: min
            setPadding(24.dpToPx(activity), 16.dpToPx(activity), 24.dpToPx(activity), 16.dpToPx(activity))
        }

        android.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(np)
            .setPositiveButton("SET") { _, _ ->
                et.setText(np.value.toString())
                onConfirm()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    fun showDividedRollerDialog(activity: Activity, etSets: EditText, etReps: EditText, onConfirm: () -> Unit = {}) {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(24.dpToPx(activity), 16.dpToPx(activity), 24.dpToPx(activity), 16.dpToPx(activity))
        }

        val npSets = NumberPicker(activity).apply {
            minValue = 1; maxValue = 20
            value = etSets.text.toString().toIntOrNull() ?: 3
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val npReps = NumberPicker(activity).apply {
            minValue = 1; maxValue = 100
            value = etReps.text.toString().toIntOrNull() ?: 10
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        layout.addView(npSets)
        layout.addView(npReps)

        android.app.AlertDialog.Builder(activity)
            .setTitle("Sets & Reps")
            .setView(layout)
            .setPositiveButton("SET") { _, _ ->
                etSets.setText(npSets.value.toString())
                etReps.setText(npReps.value.toString())
                onConfirm()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
