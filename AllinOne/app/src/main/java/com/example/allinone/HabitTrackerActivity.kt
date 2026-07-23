package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class HabitTrackerActivity : BaseActivity() {

    private val habits = DataManager.habits
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var weekAdapter: CalendarWeekAdapter
    private lateinit var sectionProgressBar: android.widget.ProgressBar
    private lateinit var sectionProgressText: TextView
    private lateinit var gestureDetector: android.view.GestureDetector
    private var selectedTimeFilter: String = "All"
    private var selectedDateString: String = DataManager.getTrackingDateString()
    
    private var currentGridCalendar = Calendar.getInstance()
    private var currentTab = "TODAY"

    private lateinit var todayLayout: View
    private lateinit var historyLayout: View
    private lateinit var historyComposeView: ComposeView
    private lateinit var ivToday: ImageView
    private lateinit var tvTodayNav: TextView
    private lateinit var ivHistory: ImageView
    private lateinit var tvHistoryNav: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_tracker)

        todayLayout = findViewById(R.id.today_layout)
        historyLayout = findViewById(R.id.history_layout)
        historyComposeView = findViewById(R.id.history_compose_view)
        
        ivToday = findViewById(R.id.iv_today)
        tvTodayNav = findViewById(R.id.tv_today_nav)
        ivHistory = findViewById(R.id.iv_history)
        tvHistoryNav = findViewById(R.id.tv_history_nav)

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(DataManager.getTrackingCalendar().time)

        val habitList = findViewById<RecyclerView>(R.id.habit_list)
        habitList.layoutManager = LinearLayoutManager(this)
        
        sectionProgressBar = findViewById(R.id.section_progress_bar)
        sectionProgressText = findViewById(R.id.tv_section_progress_percentage)
        
        habitAdapter = HabitAdapter(habits, { 
            DataManager.saveData(this)
            updateHistoryUI()
            updateSectionProgress()
        }, { _, _ -> })
        habitList.adapter = habitAdapter

        val btnCreate = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_create_new_habit)
        if (DataManager.habitAddThemeColor != -1) {
            btnCreate.strokeColor = DataManager.habitAddThemeColor
        }
        btnCreate.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        setupHeaderLogic()
        setupFooterLogic()
        setupCalendarViewPager()
        applySectionTheme()
        updateSectionProgress()
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.habit_tracker_root))
        updateDynamicBackground()
        setupComposeHistory()

        // Apply Default Startup Tab
        if (DataManager.habitDefaultTab == "HISTORY") {
            switchTab("HISTORY")
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back_history).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_habit_settings).setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 10f

            // Toggle Show/Hide Completed
            val menuToggle = menuView.findViewById<View>(R.id.menu_toggle_completed)
            val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
            val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
            
            menuToggle.visibility = View.VISIBLE
            tvToggle.text = if (DataManager.habitShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
            ivToggle.setImageResource(if (DataManager.habitShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

            menuToggle.setOnClickListener {
                DataManager.habitShowCompleted = !DataManager.habitShowCompleted
                habitAdapter.setShowCompleted(DataManager.habitShowCompleted)
                DataManager.saveData(this)
                popupWindow.dismiss()
            }

            // History Option
            val historyBtn = menuView.findViewById<View>(R.id.menu_action_primary)
            historyBtn.visibility = View.VISIBLE
            historyBtn.setOnClickListener {
                switchTab("HISTORY")
                popupWindow.dismiss()
            }

            // Hide task-specific items
            menuView.findViewById<View>(R.id.menu_clear_completed).visibility = View.GONE

            menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
                showHabitSettingsDialog()
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(it, -150, 0)
        }
    }

    private fun setupCalendarViewPager() {
        val vpCalendar = findViewById<ViewPager2>(R.id.vp_calendar)
        val weeks = mutableListOf<List<DayModel>>()
        val calendar = Calendar.getInstance()
        
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")

        // Start from 52 weeks ago to 52 weeks ahead (approx 2 years)
        calendar.add(Calendar.WEEK_OF_YEAR, -52)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfDate.format(Date())
        val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())

        var initialPageIndex = 0
        val totalWeeksCount = 105 // 52 past + today week + 52 future

        for (w in 0 until totalWeeksCount) {
            val weekDays = mutableListOf<DayModel>()
            for (d in 0 until 7) {
                val dateStr = sdfDate.format(calendar.time)
                val isSelected = dateStr == todayStr
                if (isSelected) initialPageIndex = w
                
                weekDays.add(DayModel(
                    date = calendar.time,
                    dayName = sdfDayName.format(calendar.time),
                    dayNumber = sdfDayNum.format(calendar.time),
                    dateString = dateStr,
                    isSelected = isSelected,
                    isToday = dateStr == todayStr
                ))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            weeks.add(weekDays)
        }

        weekAdapter = CalendarWeekAdapter(weeks, habitColor) { day ->
            selectedDateString = day.dateString
            weeks.flatten().forEach { it.isSelected = (it.dateString == day.dateString) }
            weekAdapter.notifyDataSetChanged()
            applyFilters()
        }
        
        vpCalendar.adapter = weekAdapter
        vpCalendar.setCurrentItem(initialPageIndex, false)

        val dateHeader = findViewById<TextView>(R.id.tv_date)
        dateHeader.setOnClickListener {
            selectedDateString = todayStr
            weeks.flatten().forEach { it.isSelected = (it.dateString == todayStr) }
            vpCalendar.setCurrentItem(initialPageIndex, true)
            weekAdapter.notifyDataSetChanged()
            applyFilters()
        }

        vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val firstDay = weeks[position][0]
                val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                dateHeader.text = sdfMonth.format(firstDay.date)
            }
        })
    }

    private fun setupHeaderLogic() {
        findViewById<RadioGroup>(R.id.filter_chips).setOnCheckedChangeListener { _, checkedId ->
            selectedTimeFilter = when (checkedId) {
                R.id.chip_morning -> "Morning"
                R.id.chip_afternoon -> "Afternoon"
                R.id.chip_evening -> "Evening"
                else -> "All"
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        try {
            val selectedDate = sdf.parse(selectedDateString) ?: Date()
            calendar.time = selectedDate
            val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) - 1) // 0=Sun
            habitAdapter.filter(selectedTimeFilter, dayIndex, selectedDateString)
        } catch (e: Exception) {
            habitAdapter.filter(selectedTimeFilter, 0, selectedDateString)
        }
    }

    private fun setupFooterLogic() {
        findViewById<View>(R.id.nav_today).setOnClickListener { switchTab("TODAY") }
        findViewById<View>(R.id.nav_history).setOnClickListener { switchTab("HISTORY") }
    }

    private fun switchTab(tab: String) {
        val root = findViewById<ViewGroup>(R.id.habit_content_container)
        androidx.transition.TransitionManager.beginDelayedTransition(root, androidx.transition.AutoTransition())
        currentTab = tab
        
        if (tab == "TODAY") {
            todayLayout.visibility = View.VISIBLE
            historyLayout.visibility = View.GONE
            historyComposeView.visibility = View.GONE
            updateNavUI("TODAY")
        } else {
            todayLayout.visibility = View.GONE
            historyLayout.visibility = View.GONE
            historyComposeView.visibility = View.VISIBLE
            updateNavUI("HISTORY")
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (currentTab == "TODAY") switchTab("HISTORY")
            }

            override fun onSwipeRight() {
                if (currentTab == "HISTORY") switchTab("TODAY")
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun updateHistoryUI() {
        findViewById<TextView>(R.id.history_current_streak)?.text = DataManager.getCurrentStreak().toString()
        findViewById<TextView>(R.id.history_workouts_finished)?.text = DataManager.getTotalHabitsFinished().toString()
        findViewById<TextView>(R.id.history_efficiency)?.text = "${DataManager.getGlobalCompletionRate()}%"
        setupDynamicHistoryGrid()
        updateSectionProgress()
    }

    private fun updateSectionProgress() {
        val progress = DataManager.getHabitProgress()
        sectionProgressBar.progress = progress
        sectionProgressText.text = "$progress%"
    }

    private fun setupComposeHistory() {
        val composeView = findViewById<ComposeView>(R.id.history_compose_view) ?: return
        composeView.setContent {
            var selectedDate by remember { mutableStateOf(DataManager.getTrackingDateString()) }
            var currentMonth by remember { 
                mutableStateOf(Calendar.getInstance().apply { 
                    try {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(selectedDate)
                        if (date != null) time = date
                    } catch (e: Exception) {}
                }) 
            }
            
            val performanceData = remember(selectedDate) {
                DataManager.getDayHistory(selectedDate)
            }
            
            val trendData = remember { DataManager.getLastSevenDaysDetailedProgress() }
            val habitColor = if (DataManager.globalHabitColor != -1) ComposeColor(DataManager.globalHabitColor) else ComposeColor(0xFFFF7A59)

            PerformanceDashboardScreen(
                onBack = { switchTab("TODAY") },
                title = "HABIT HISTORY",
                onDateSelected = { selectedDate = it },
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                onMonthChanged = { currentMonth = it.clone() as Calendar },
                onShowPicker = {
                    val dialog = android.app.DatePickerDialog(
                        this,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day)
                            currentMonth = cal.clone() as Calendar
                            selectedDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
                        },
                        currentMonth.get(Calendar.YEAR),
                        currentMonth.get(Calendar.MONTH),
                        currentMonth.get(Calendar.DAY_OF_MONTH)
                    )
                    dialog.show()
                },
                performanceData = performanceData,
                trendData = trendData,
                currentMood = null,
                overrideColor = habitColor,
                isWorkoutContext = false,
                showPerformanceCard = false,
                showTrendCard = false,
                showBackgroundAura = false
            )
        }
    }

    private fun setupDynamicHistoryGrid() {
        val grid = findViewById<GridLayout>(R.id.history_dynamic_grid) ?: return
        val tvMonth = findViewById<TextView>(R.id.tv_grid_month) ?: return
        
        val childCount = grid.childCount
        if (childCount > 7) {
            grid.removeViews(7, childCount - 7)
        }

        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = sdfMonth.format(currentGridCalendar.time)
        
        val displayMonth = currentGridCalendar.get(Calendar.MONTH)
        val displayYear = currentGridCalendar.get(Calendar.YEAR)
        
        val tempCal = currentGridCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfDate.format(Date())

        for (i in 0 until firstDayOfWeek) {
            grid.addView(createSpacerView())
        }

        for (day in 1..daysInMonth) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(displayYear, displayMonth, day)
            val dateKey = sdfDate.format(dayCalendar.time)
            
            val progress = if (dateKey == todayStr) {
                DataManager.getTotalDailyProgress()
            } else {
                val historyData = DataManager.history[dateKey]
                if (historyData != null && (historyData.totalHabits + historyData.totalWorkouts) > 0) {
                    ((historyData.habitsCompleted + historyData.workoutsCompleted) * 100) / (historyData.totalHabits + historyData.totalWorkouts)
                } else 0
            }
            
            grid.addView(createDayView(day.toString(), progress, dateKey))
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

    private var currentlySelectedDate: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    private fun createDayView(day: String, progressPercent: Int, dateKey: String): View {
        val frameLayout = FrameLayout(this)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = (48 * resources.displayMetrics.density).toInt()
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        frameLayout.layoutParams = params

        if (dateKey == currentlySelectedDate) {
            frameLayout.setBackgroundResource(R.drawable.history_calendar_selected_bg)
        }

        // Circular Progress Bar
        val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val s = (36 * resources.displayMetrics.density).toInt()
        val pbParams = FrameLayout.LayoutParams(s, s)
        pbParams.gravity = Gravity.CENTER
        progressBar.layoutParams = pbParams
        progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.circular_history_progress)
        progressBar.max = 100
        progressBar.progress = progressPercent
        progressBar.scaleX = -1f
        frameLayout.addView(progressBar)

        val textView = TextView(this)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.textSize = 13f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        frameLayout.setOnClickListener {
            currentlySelectedDate = dateKey
            setupDynamicHistoryGrid()
            updatePerformanceCard(dateKey)
        }

        return frameLayout
    }

    private fun updatePerformanceCard(dateKey: String) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val historyData = if (dateKey == today) {
            val hCount = habits.size
            val hComp = habits.count { it.isCompleted }
            val wCount = DataManager.workouts.size
            val wComp = DataManager.workouts.count { it.isCompleted }
            DayHistory(hComp, hCount, wComp, wCount)
        } else {
            DataManager.history[dateKey] ?: DayHistory(0, 0, 0, 0)
        }

        val sdfInput = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val formattedDate = try {
            sdfOutput.format(sdfInput.parse(dateKey) ?: Date())
        } catch (e: Exception) { "" }

        findViewById<TextView>(R.id.tv_performance_title)?.text = "PERFORMANCE FOR ${formattedDate.uppercase()}"

        val totalItems = historyData.totalHabits + historyData.totalWorkouts
        val totalCompleted = historyData.habitsCompleted + historyData.workoutsCompleted
        val overallPercent = if (totalItems > 0) (totalCompleted * 100) / totalItems else 0

        findViewById<TextView>(R.id.tv_overall_percentage)?.text = "$overallPercent%"

        findViewById<TextView>(R.id.tv_habits_stat_label)?.text = "[H] Habits (${historyData.habitsCompleted}/${historyData.totalHabits})"
        val hPercent = if (historyData.totalHabits > 0) (historyData.habitsCompleted * 100) / historyData.totalHabits else 0
        findViewById<TextView>(R.id.tv_habits_stat_percent)?.text = "$hPercent%"
        findViewById<ProgressBar>(R.id.pb_habits_history)?.progress = hPercent

        findViewById<TextView>(R.id.tv_workouts_stat_label)?.text = "[W] Workouts (${historyData.workoutsCompleted}/${historyData.totalWorkouts})"
        val wPercent = if (historyData.totalWorkouts > 0) (historyData.workoutsCompleted * 100) / historyData.totalWorkouts else 0
        findViewById<TextView>(R.id.tv_workouts_stat_percent)?.text = "$wPercent%"
        findViewById<ProgressBar>(R.id.pb_workouts_history)?.progress = wPercent

        findViewById<TextView>(R.id.tv_total_stat_label)?.text = "Σ Total Performance ($totalCompleted/$totalItems)"
        findViewById<TextView>(R.id.tv_total_stat_percent)?.text = "$overallPercent%"
        findViewById<ProgressBar>(R.id.pb_total_history)?.progress = overallPercent
    }



    private fun showBehavioralInsightsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget) // Re-use simple dialog structure
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)

        title.text = "BEHAVIORAL INSIGHTS"
        etInput.visibility = View.GONE
        
        val stats = DataManager.getHabitPerformanceByFrequency()
        val peak = stats.maxByOrNull { it.value }
        val moodInsight = DataManager.getMoodCorrelationData()
        
        val sb = StringBuilder()
        if (peak == null || peak.value <= 0) {
            sb.append("Not enough data yet. Keep tracking your habits to see your peak performance times!\n")
        } else {
            sb.append("Your Peak Performance Time: ${peak.key.uppercase()}\n\n")
            stats.forEach { (freq, score) ->
                if (score >= 0) {
                    sb.append("$freq Habits: $score% Completion\n")
                }
            }
        }

        if (moodInsight != null) {
            sb.append("\n${moodInsight}")
        }
        
        subtext.text = sb.toString()
        
        (btnSave as? TextView)?.text = "CLOSE"
        btnSave.setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }

    private fun showHabitSettingsDialog() {
        startActivity(Intent(this, HabitSettingsActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        // Ensure sorting is applied if it changed in settings
        applyFilters()
        applySectionTheme()
        updateSectionProgress()
        updateDynamicBackground()
    }

    private fun applySectionTheme() {
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")
        
        val chips = listOf<RadioButton>(
            findViewById(R.id.chip_all),
            findViewById(R.id.chip_morning),
            findViewById(R.id.chip_afternoon),
            findViewById(R.id.chip_evening)
        )

        chips.forEach { chip ->
            val checkedDrawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 19f * resources.displayMetrics.density
                setColor(habitColor)
            }
            
            val uncheckedDrawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 19f * resources.displayMetrics.density
                setColor(Color.TRANSPARENT)
                setStroke(Math.round(1.5f * resources.displayMetrics.density), habitColor)
            }

            val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                addState(intArrayOf(), uncheckedDrawable)
            }
            
            chip.background = stateListDrawable

            // Synchronize Text Color
            val textColorStateList = android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(Color.WHITE, habitColor)
            )
            chip.setTextColor(textColorStateList)
        }

        // Apply to Create Button
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_create_new_habit).strokeColor = habitColor
        
        // Apply to Progress Bar
        sectionProgressBar.progressTintList = ColorStateList.valueOf(habitColor)

        // Sync Nav UI
        updateNavUI(currentTab)
    }

    private fun updateDynamicBackground() {
        val auraView = findViewById<View>(R.id.habit_aura_background) ?: return
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                adjustAlpha(habitColor, 0.4f),
                Color.BLACK
            )
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun updateNavUI(active: String) {
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")
        val todayColor = if (active == "TODAY") habitColor else ContextCompat.getColor(this, R.color.text_secondary)
        val historyColor = if (active == "HISTORY") habitColor else ContextCompat.getColor(this, R.color.text_secondary)
        
        ivToday.setColorFilter(todayColor)
        tvTodayNav.setTextColor(todayColor)
        ivHistory.setColorFilter(historyColor)
        tvHistoryNav.setTextColor(historyColor)
    }
}
