package com.example.allinone

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryActivity : AppCompatActivity() {

    private var currentCalendar = Calendar.getInstance()
    private lateinit var tvMonthYear: TextView
    private lateinit var grid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performance_history)

        tvMonthYear = findViewById(R.id.tv_month_year)
        grid = findViewById(R.id.history_grid)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_prev_month).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            setupGrid()
        }

        findViewById<View>(R.id.btn_next_month).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            setupGrid()
        }

        setupGrid()
    }

    private fun setupGrid() {
        grid.removeAllViews()

        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = sdfMonth.format(currentCalendar.time).uppercase()

        val displayMonth = currentCalendar.get(Calendar.MONTH)
        val displayYear = currentCalendar.get(Calendar.YEAR)

        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfDate.format(Date())

        // 1. Spacers for empty days
        for (i in 0 until firstDayOfWeek) {
            grid.addView(createSpacerView())
        }

        // 2. Actual day views
        for (day in 1..daysInMonth) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(displayYear, displayMonth, day)
            val dateKey = sdfDate.format(dayCalendar.time)

            // Calculate progress for this day
            val progress = if (dateKey == todayStr) {
                DataManager.getTotalDailyProgress()
            } else {
                val dayData = DataManager.history[dateKey]
                if (dayData != null) {
                    val total = dayData.totalHabits + dayData.totalWorkouts
                    if (total > 0) {
                        ((dayData.habitsCompleted + dayData.workoutsCompleted) * 100) / total
                    } else 0
                } else 0
            }

            grid.addView(createDayView(day.toString(), progress))
        }
    }

    private fun createSpacerView(): View {
        return View(this).apply {
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 140
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams = params
        }
    }

    private fun createDayView(day: String, progressPercent: Int): View {
        val frameLayout = FrameLayout(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 140
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params

        // Performance Indicator Circle
        if (progressPercent > 0) {
            val circle = View(this)
            // Size mapping: 60px to 100px based on progress
            val size = (60 * (progressPercent / 100f) + 40).toInt()
            val circleParams = FrameLayout.LayoutParams(size, size)
            circleParams.gravity = Gravity.CENTER
            circle.layoutParams = circleParams
            
            circle.background = ContextCompat.getDrawable(this, R.drawable.circle_selected_bg)
            circle.backgroundTintList = ContextCompat.getColorStateList(this, R.color.card_blue)
            // Use alpha for visual intensity
            circle.alpha = (progressPercent / 100f).coerceAtLeast(0.15f)
            
            frameLayout.addView(circle)
        }

        // Day Number
        val textView = TextView(this)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        return frameLayout
    }
}
