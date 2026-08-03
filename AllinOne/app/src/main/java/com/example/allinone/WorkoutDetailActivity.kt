package com.example.allinone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import com.example.allinone.data.model.Workout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WorkoutDetailActivity : BaseActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    private var workout: Workout? = null
    private var currentCalendar = Calendar.getInstance()
    private lateinit var calendarGrid: GridLayout
    private lateinit var tvMonth: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)

        setupKeyboardHandling(findViewById(R.id.workout_detail_root), findViewById(R.id.workout_detail_content_container))

        val workoutId = intent.getLongExtra("WORKOUT_ID", -1L)
        workout = viewModel.workouts.value.find { it.timestamp == workoutId }

        if (workout == null) {
            finish()
            return
        }

        calendarGrid = findViewById(R.id.calendar_grid)
        tvMonth = findViewById(R.id.tv_calendar_month)

        setupUI()
        setupCalendar()
        updateThemeVisuals()
    }

    private fun updateThemeVisuals() {
        val themeColor = if (workout?.color != -1) workout?.color ?: Color.parseColor("#FFFFB800") else Color.parseColor("#FFFFB800")
        findViewById<View>(R.id.header_bg_accent_workout).backgroundTintList = ColorStateList.valueOf(themeColor)
        
        val freqChip = findViewById<TextView>(R.id.tv_frequency_chip)
        freqChip.setTextColor(themeColor)
        freqChip.backgroundTintList = ColorStateList.valueOf(themeColor).withAlpha(40)
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.tv_workout_title).text = workout?.name
        findViewById<TextView>(R.id.tv_frequency_chip).text = workout?.frequency?.uppercase()
        findViewById<TextView>(R.id.tv_repeat_chip).text = if (workout?.repeatDays?.size == 7) "EVERYDAY" else "SPECIFIC DAYS"
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_edit_workout).setOnClickListener {
            val intent = Intent(this, AddWorkoutActivity::class.java).apply {
                putExtra("WORKOUT_ID", workout?.timestamp)
            }
            startActivity(intent)
            finish()
        }
        updateStats()
    }

    private fun updateStats() {
        val w = workout ?: return
        val totalCompletedDays = w.completedDates.size
        findViewById<TextView>(R.id.tv_finished_count).text = totalCompletedDays.toString()
        findViewById<TextView>(R.id.tv_streak_count).text = calculateStreak().toString()
        
        val creationDate = Date(w.timestamp)
        val daysSinceCreation = ((System.currentTimeMillis() - creationDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
        
        // Sum of all progress percentages / days since creation
        val totalProgressPoints = w.dailyProgress.values.sum()
        val rate = if (daysSinceCreation > 0) totalProgressPoints / daysSinceCreation else 0
        
        findViewById<TextView>(R.id.tv_rate_percent).text = "$rate%"
        findViewById<TextView>(R.id.tv_rate_fraction).text = "$totalCompletedDays/$daysSinceCreation workouts"
    }

    private fun calculateStreak(): Int {
        val dailyProgress = workout?.dailyProgress ?: return 0
        if (dailyProgress.isEmpty()) return 0
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0
        
        val today = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(calendar.time)
        
        // If neither today nor yesterday has progress, streak is 0
        if ((dailyProgress[today] ?: 0) == 0 && (dailyProgress[yesterday] ?: 0) == 0) return 0
        
        calendar.time = Date()
        if ((dailyProgress[today] ?: 0) == 0) calendar.add(Calendar.DAY_OF_YEAR, -1)
        
        while (true) {
            val key = sdf.format(calendar.time)
            if ((dailyProgress[key] ?: 0) > 0) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun setupCalendar() {
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
            val dateKey = sdfDate.format(dayCalendar.time)
            
            val progressPercent = workout?.dailyProgress?.get(dateKey) ?: 0
            calendarGrid.addView(createDayView(day.toString(), progressPercent, dateKey))
        }
    }

    private fun showProgressInputDialog(dateKey: String) {
        val w = workout ?: return
        val builder = AlertDialog.Builder(this, R.style.NumberPickerTheme)
        
        // Let's create a simple custom view for input
        val padding = (24 * resources.displayMetrics.density).toInt()
        val input = EditText(this).apply {
            hint = "Value (Max: ${w.target})"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(padding, padding, padding, padding)
            // Get current value if exists
            val currentVal = w.dailyProgress[dateKey]?.let { (it * w.target) / 100 } ?: 0
            setText(currentVal.toString())
        }

        builder.setTitle("Log Progress for $dateKey")
        builder.setMessage("Enter number of ${w.trackingMode} done:")
        builder.setView(input)

        builder.setPositiveButton("LOG") { _, _ ->
            val enteredValue = input.text.toString().toIntOrNull() ?: 0
            val targetValue = w.target.coerceAtLeast(1)
            val percentage = ((enteredValue.coerceIn(0, targetValue) * 100) / targetValue)
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            
            val updatedWorkout = if (percentage > 0) {
                w.copy(
                    dailyProgress = w.dailyProgress.toMutableMap().apply { put(dateKey, percentage) },
                    completedDates = w.completedDates.toMutableList().apply { if (!contains(dateKey)) add(dateKey) },
                    progress = if (dateKey == today) enteredValue.coerceIn(0, targetValue) else w.progress,
                    isCompleted = if (dateKey == today) percentage == 100 else w.isCompleted
                )
            } else {
                w.copy(
                    dailyProgress = w.dailyProgress.toMutableMap().apply { remove(dateKey) },
                    completedDates = w.completedDates.toMutableList().apply { remove(dateKey) },
                    progress = if (dateKey == today) 0 else w.progress,
                    isCompleted = if (dateKey == today) false else w.isCompleted
                )
            }
            
            viewModel.updateWorkout(updatedWorkout)
            workout = updatedWorkout
            updateStats()
            setupCalendar()
        }
        builder.setNegativeButton("CANCEL", null)
        builder.show()
    }

    private fun createSpacerView(): View {
        val view = View(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = (40 * resources.displayMetrics.density).toInt()
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        return view
    }

    private fun createDayView(day: String, progressPercent: Int, dateKey: String): View {
        val frameLayout = FrameLayout(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = (40 * resources.displayMetrics.density).toInt()
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params
        
        // Circular Progress Bar
        val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val s = (32 * resources.displayMetrics.density).toInt()
        val pbParams = FrameLayout.LayoutParams(s, s)
        pbParams.gravity = Gravity.CENTER
        progressBar.layoutParams = pbParams
        progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.circular_history_progress)
        progressBar.max = 100
        progressBar.progress = progressPercent
        progressBar.scaleX = -1f // Flip for anti-clockwise
        
        val themeColor = if (workout?.color != -1) workout?.color ?: Color.parseColor("#FFFFB800") else Color.parseColor("#FFFFB800")
        progressBar.progressTintList = ColorStateList.valueOf(themeColor)
        
        frameLayout.addView(progressBar)
        
        val textView = TextView(this)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        frameLayout.setOnClickListener {
            showProgressInputDialog(dateKey)
        }

        return frameLayout
    }
}
