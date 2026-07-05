package com.example.allinone

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryActivity : AppCompatActivity() {

    private var currentCalendar = Calendar.getInstance()
    private lateinit var tvMonthYear: TextView
    private lateinit var grid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performance_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.performance_root_layout)) { v, insets ->
            val topPadding = (8 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, topPadding, v.paddingRight, v.paddingBottom)
            insets
        }

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

        // Circular Progress Bar
        val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val s = (36 * resources.displayMetrics.density).toInt() // Slightly larger for main history
        val pbParams = FrameLayout.LayoutParams(s, s)
        pbParams.gravity = Gravity.CENTER
        progressBar.layoutParams = pbParams
        progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.circular_history_progress)
        progressBar.max = 100
        progressBar.progress = progressPercent
        progressBar.scaleX = -1f // Flip horizontally for anti-clockwise fill
        frameLayout.addView(progressBar)

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
