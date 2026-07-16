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
import android.view.LayoutInflater
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import java.util.*

class PerformanceHistoryActivity : BaseActivity() {

    private var currentCalendar = Calendar.getInstance()
    private lateinit var tvMonthYear: TextView
    private lateinit var grid: GridLayout
    private var selectedDateString: String? = null
    private lateinit var gestureDetector: android.view.GestureDetector

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

        selectedDateString = DataManager.getTrackingDateString()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.iv_calendar_icon)?.setOnClickListener {
            showDatePicker()
        }

        findViewById<View>(R.id.tv_month_year)?.setOnClickListener {
            showDatePicker()
        }

        setupGestureDetector()
        
        // Use the month year text or icon for swiping is fine, but header is most reliable
        findViewById<View>(R.id.history_header)?.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            v.performClick()
            true
        }

        grid.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Don't consume so clicks still work on days
        }

        setupGrid()
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swipe Right -> Previous Month
                        currentCalendar.add(Calendar.MONTH, -1)
                        setupGrid()
                    } else {
                        // Swipe Left -> Next Month
                        currentCalendar.add(Calendar.MONTH, 1)
                        setupGrid()
                    }
                    return true
                }
                return false
            }
        })
    }

    private fun showDatePicker() {
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                currentCalendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                selectedDateString = sdf.format(currentCalendar.time)
                setupGrid()
                showDailyDetails(selectedDateString!!)
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        showDialogSafe(datePicker)
    }

    private fun setupTrendGraph() {
        val container = findViewById<LinearLayout>(R.id.container_trend_bars) ?: return
        val labelContainer = findViewById<LinearLayout>(R.id.layout_trend_labels)
        
        container.removeAllViews()
        labelContainer?.removeAllViews()

        val trendData = DataManager.getLastSevenDaysDetailedProgress()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("E", Locale.getDefault()) // E.g. "Mon"
        val todayStr = sdf.format(Date())

        trendData.forEachIndexed { index, pair ->
            val hScore = pair.first
            val wScore = pair.second

            val barCalendar = DataManager.getTrackingCalendar()
            barCalendar.add(Calendar.DAY_OF_YEAR, -(6 - index))
            val dateKey = sdf.format(barCalendar.time)
            val isToday = dateKey == todayStr

            val dayContainer = LinearLayout(this).apply {
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
                p.weight = 1f
                val margin = (4 * resources.displayMetrics.density).toInt()
                p.setMargins(margin, 0, margin, 0) // Density-aware horizontal margins between dates
                layoutParams = p
                gravity = Gravity.BOTTOM
                orientation = LinearLayout.HORIZONTAL
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedDateString = dateKey
                    setupGrid()
                    showDailyDetails(dateKey)
                }
            }

            // 1. Habit Bar (Dark Blue)
            val hBar = View(this).apply {
                val h = (hScore * resources.displayMetrics.density * 0.8).toInt().coerceAtLeast(4)
                val p = LinearLayout.LayoutParams(0, h)
                p.weight = 1f
                p.setMargins(4, 0, 2, 0)
                layoutParams = p

                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = 4f * resources.displayMetrics.density
                shape.setColor(if (hScore >= 100) Color.parseColor("#1A73E8") else Color.parseColor("#991A73E8")) // Increased alpha for visibility
                background = shape
            }

            // 2. Workout Bar (Cyan/Teal)
            val wBar = View(this).apply {
                val h = (wScore * resources.displayMetrics.density * 0.8).toInt().coerceAtLeast(4)
                val p = LinearLayout.LayoutParams(0, h)
                p.weight = 1f
                p.setMargins(2, 0, 4, 0)
                layoutParams = p

                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = 4f * resources.displayMetrics.density
                shape.setColor(if (wScore >= 100) Color.parseColor("#2EC4B6") else Color.parseColor("#992EC4B6")) // Increased alpha for visibility
                background = shape
            }

            dayContainer.addView(hBar)
            dayContainer.addView(wBar)
            container.addView(dayContainer)

            // Day Label (M, T, W...)
            val tvLabel = TextView(this).apply {
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                p.weight = 1f
                layoutParams = p
                text = sdfDay.format(barCalendar.time).first().toString() // "M", "T"...
                setTextColor(if (isToday) Color.parseColor("#1A73E8") else Color.GRAY)
                textSize = 10f
                gravity = Gravity.CENTER
                typeface = if (isToday) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            }
            labelContainer?.addView(tvLabel)
        }
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

            grid.addView(createDayView(day.toString(), progress, dateKey))
        }

        setupTrendGraph()

        // Show current selection if exists
        selectedDateString?.let { showDailyDetails(it) }
    }

    private fun showDailyDetails(dateKey: String) {
        val card = findViewById<View>(R.id.card_daily_details) ?: return
        val tvMainScore = findViewById<TextView>(R.id.tv_detail_main_score)
        val tvDateTitle = findViewById<TextView>(R.id.tv_detail_date_title)
        val containerBars = findViewById<LinearLayout>(R.id.container_detail_bars)

        card.visibility = View.VISIBLE
        containerBars.removeAllViews()

        val sdfInput = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        try {
            val date = sdfInput.parse(dateKey)
            tvDateTitle.text = "PERFORMANCE FOR ${sdfOutput.format(date!!).uppercase()}"
        } catch (e: Exception) {
            tvDateTitle.text = "DAILY DETAILS"
        }

        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        if (dateKey == todayStr) {
            val score = DataManager.getTotalDailyProgress()
            tvMainScore.text = "$score%"
            
            // Current day real-time breakdown
            val habitScore = DataManager.getHabitProgress()
            val workoutScore = DataManager.getWorkoutProgress()
            
            val todayIndex = (DataManager.getTrackingCalendar().get(Calendar.DAY_OF_WEEK) - 1)
            val hTotal = DataManager.habits.filter { it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) }.size
            val hCompleted = DataManager.habits.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) && it.isCompleted }.size
            
            val wTotal = DataManager.workouts.filter { it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex) }.size
            val wCompleted = DataManager.workouts.filter { (it.repeatType != "SPECIFIC_DAYS" || it.repeatDays.contains(todayIndex)) && it.isCompleted }.size
            
            addDetailRow(containerBars, "Habits", habitScore, "$hCompleted/$hTotal")
            addDetailRow(containerBars, "Workouts", workoutScore, "$wCompleted/$wTotal")
            
            val totalItems = hTotal + wTotal
            val totalDone = hCompleted + wCompleted
            addDetailRow(containerBars, "Total Performance", score, "$totalDone/$totalItems")
        } else {
            val dayData = DataManager.history[dateKey]
            if (dayData != null) {
                val total = dayData.totalHabits + dayData.totalWorkouts
                val score = if (total > 0) ((dayData.habitsCompleted + dayData.workoutsCompleted) * 100) / total else 0
                tvMainScore.text = "$score%"
                
                val hScore = if (dayData.totalHabits > 0) (dayData.habitsCompleted * 100) / dayData.totalHabits else 0
                val wScore = if (dayData.totalWorkouts > 0) (dayData.workoutsCompleted * 100) / dayData.totalWorkouts else 0
                
                addDetailRow(containerBars, "Habits Completed", hScore, "${dayData.habitsCompleted}/${dayData.totalHabits}")
                addDetailRow(containerBars, "Workouts Completed", wScore, "${dayData.workoutsCompleted}/${dayData.totalWorkouts}")
                addDetailRow(containerBars, "Daily Mastery", score, "${dayData.habitsCompleted + dayData.workoutsCompleted}/$total")
            } else {
                tvMainScore.text = "0%"
                containerBars.addView(TextView(this).apply {
                    text = "No data recorded for this day."
                    setTextColor(Color.GRAY)
                    textSize = 12f
                })
            }
        }
    }

    private fun addDetailRow(container: LinearLayout, label: String, score: Int, countLabel: String? = null) {
        val row = LayoutInflater.from(this).inflate(R.layout.item_performance_stat_row, container, false)
        val tvLabel = row.findViewById<TextView>(R.id.tv_stat_label)
        val tvValue = row.findViewById<TextView>(R.id.tv_stat_value)
        val pb = row.findViewById<android.widget.ProgressBar>(R.id.pb_stat_progress)

        tvLabel.text = if (countLabel != null) "$label ($countLabel)" else label
        tvValue.text = "$score%"
        pb.progress = score
        container.addView(row)
    }

    private fun createSpacerView(): View {
        return View(this).apply {
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = (48 * resources.displayMetrics.density).toInt()
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams = params
        }
    }

    private fun createDayView(day: String, progressPercent: Int, dateKey: String): View {
        val frameLayout = FrameLayout(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = (48 * resources.displayMetrics.density).toInt()
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params

        val isSelected = selectedDateString == dateKey
        if (isSelected) {
            frameLayout.setBackgroundResource(R.drawable.selected_day_bg)
        }

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

        // Day Number or Mood
        val textView = TextView(this)
        val mood = DataManager.dailyMoods[dateKey]
        textView.text = mood ?: day
        textView.setTextColor(Color.WHITE)
        textView.textSize = if (mood != null) 18f else 14f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        // Today Underline
        val todayStr = DataManager.getTrackingDateString()
        if (dateKey == todayStr) {
            val underline = View(this).apply {
                val h = (3 * resources.displayMetrics.density).toInt()
                val w = (16 * resources.displayMetrics.density).toInt()
                val p = FrameLayout.LayoutParams(w, h)
                p.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                p.bottomMargin = (2 * resources.displayMetrics.density).toInt()
                layoutParams = p

                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = h / 2f
                shape.setColor(Color.parseColor("#1A73E8"))
                background = shape
            }
            frameLayout.addView(underline)
        }

        frameLayout.setOnClickListener {
            selectedDateString = dateKey
            setupGrid() // Redraw to highlight
            showDailyDetails(dateKey)
        }

        return frameLayout
    }
}
