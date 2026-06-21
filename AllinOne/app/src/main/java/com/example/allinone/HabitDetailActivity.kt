package com.example.allinone

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class HabitDetailActivity : AppCompatActivity() {

    private var habit: Habit? = null
    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_detail)

        val habitId = intent.getLongExtra("HABIT_ID", -1L)
        habit = DataManager.habits.find { it.timestamp == habitId }

        if (habit == null) {
            finish()
            return
        }

        setupUI()
        setupCalendarNavigation()
        setupCalendar()
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.tv_habit_title).text = habit?.name
        findViewById<TextView>(R.id.tv_frequency_chip).text = habit?.frequency?.uppercase()
        findViewById<TextView>(R.id.tv_repeat_chip).text = if (habit?.repeatDays?.size == 7) "EVERYDAY" else "SPECIFIC DAYS"
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        updateStats()
    }

    private fun updateStats() {
        val totalCompleted = habit?.completedDates?.size ?: 0
        findViewById<TextView>(R.id.tv_finished_count).text = totalCompleted.toString()
        findViewById<TextView>(R.id.tv_streak_count).text = calculateStreak().toString()
        val creationDate = Date(habit?.timestamp ?: System.currentTimeMillis())
        val daysSinceCreation = ((System.currentTimeMillis() - creationDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        val rate = if (daysSinceCreation > 0) (totalCompleted * 100) / daysSinceCreation else 0
        findViewById<TextView>(R.id.tv_rate_percent).text = "$rate%"
        findViewById<TextView>(R.id.tv_rate_fraction).text = "$totalCompleted/$daysSinceCreation habits"
    }

    private fun setupCalendarNavigation() {
        findViewById<View>(R.id.btn_prev_month).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }
        findViewById<View>(R.id.btn_next_month).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            setupCalendar()
        }
    }

    private fun calculateStreak(): Int {
        val completedDates = habit?.completedDates ?: return 0
        if (completedDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0
        val today = sdf.format(calendar.time)
        val yesterday = calendar.let { val cal = it.clone() as Calendar; cal.add(Calendar.DAY_OF_YEAR, -1); sdf.format(cal.time) }
        if (!completedDates.contains(today) && !completedDates.contains(yesterday)) return 0
        calendar.time = Date()
        if (!completedDates.contains(today)) calendar.add(Calendar.DAY_OF_YEAR, -1)
        while (completedDates.contains(sdf.format(calendar.time))) { streak++; calendar.add(Calendar.DAY_OF_YEAR, -1) }
        return streak
    }

    private fun setupCalendar() {
        val calendarGrid = findViewById<GridLayout>(R.id.calendar_grid)
        val tvMonth = findViewById<TextView>(R.id.tv_calendar_month)
        
        // Remove old views but keep the 7 day headers
        val childCount = calendarGrid.childCount
        if (childCount > 7) {
            calendarGrid.removeViews(7, childCount - 7)
        }
        
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = sdfMonth.format(currentCalendar.time)
        
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentYear = currentCalendar.get(Calendar.YEAR)
        
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        
        for (i in 0 until firstDayOfWeek) {
            calendarGrid.addView(createSpacerView())
        }
        
        for (day in 1..daysInMonth) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(currentYear, currentMonth, day)
            val isCompleted = habit?.completedDates?.contains(sdfDate.format(dayCalendar.time)) == true
            calendarGrid.addView(createDayView(day.toString(), if (isCompleted) 100 else 0))
        }
    }

    private fun createSpacerView(): View {
        val view = View(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 100
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        return view
    }

    private fun createDayView(day: String, progressPercent: Int): View {
        val frameLayout = FrameLayout(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 100
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params
        
        if (progressPercent > 0) {
            val circle = View(this)
            val size = (80 * (progressPercent / 100f)).coerceAtLeast(40f).toInt()
            val circleParams = FrameLayout.LayoutParams(size, size)
            circleParams.gravity = Gravity.CENTER
            circle.layoutParams = circleParams
            circle.background = ContextCompat.getDrawable(this, R.drawable.circle_selected_bg)
            circle.alpha = (progressPercent / 100f).coerceAtLeast(0.25f)
            circle.backgroundTintList = ContextCompat.getColorStateList(this, R.color.card_blue)
            frameLayout.addView(circle)
        }
        
        val textView = TextView(this)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        return frameLayout
    }
}
