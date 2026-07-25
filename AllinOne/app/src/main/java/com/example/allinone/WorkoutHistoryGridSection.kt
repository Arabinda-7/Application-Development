package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryGridSection(
    private val context: Context,
    private val grid: GridLayout,
    private val monthTextView: TextView,
    private val onDateSelected: (String) -> Unit
) {
    private var currentlySelectedDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun setup(calendar: Calendar) {
        grid.removeAllViews()

        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthTextView.text = sdfMonth.format(calendar.time)
        
        val displayMonth = calendar.get(Calendar.MONTH)
        val displayYear = calendar.get(Calendar.YEAR)
        
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        for (i in 0 until firstDayOfWeek) {
            grid.addView(createSpacerView())
        }

        val heatmapData = DataManager.getHeatmapData(calendar, "WORKOUTS")

        for (day in 1..daysInMonth) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(displayYear, displayMonth, day)
            val dateKey = sdfDate.format(dayCalendar.time)
            
            val progress = heatmapData[day - 1] ?: 0
            grid.addView(createDayView(day.toString(), progress, dateKey))
        }
    }

    private fun createSpacerView(): View {
        val view = View(context)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 100
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        return view
    }

    private fun createDayView(day: String, progressPercent: Int, dateKey: String): View {
        val frameLayout = FrameLayout(context)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = (48 * context.resources.displayMetrics.density).toInt()
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params

        if (dateKey == currentlySelectedDate) {
            frameLayout.setBackgroundResource(R.drawable.history_calendar_selected_bg)
        }

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        val s = (36 * context.resources.displayMetrics.density).toInt()
        val pbParams = FrameLayout.LayoutParams(s, s)
        pbParams.gravity = Gravity.CENTER
        progressBar.layoutParams = pbParams
        progressBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.circular_history_progress)
        progressBar.max = 100
        progressBar.progress = progressPercent
        progressBar.scaleX = -1f

        val workoutColor = if (DataManager.globalWorkoutColor != -1) DataManager.globalWorkoutColor else Color.parseColor("#FFFFB800")
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(workoutColor)

        frameLayout.addView(progressBar)

        val textView = TextView(context)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.textSize = 13f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        frameLayout.setOnClickListener {
            currentlySelectedDate = dateKey
            onDateSelected(dateKey)
        }

        return frameLayout
    }

    fun setSelectedDate(date: String) {
        currentlySelectedDate = date
    }
}
