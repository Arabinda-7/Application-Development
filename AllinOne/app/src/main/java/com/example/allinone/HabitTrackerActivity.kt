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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_tracker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.today_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, navBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_nav_mock)) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBars.bottom)
            insets
        }

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
        setupGridNavigation()
        setupCalendarViewPager()
        updateSectionProgress()
        setupGestureDetector()

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

        weekAdapter = CalendarWeekAdapter(weeks) { day ->
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
        currentTab = tab
        val todayLayout = findViewById<View>(R.id.today_layout)
        val historyLayout = findViewById<View>(R.id.history_layout)
        
        if (tab == "TODAY") {
            todayLayout.visibility = View.VISIBLE
            historyLayout.visibility = View.GONE
            updateNavUI("TODAY")
        } else {
            todayLayout.visibility = View.GONE
            historyLayout.visibility = View.VISIBLE
            updateNavUI("HISTORY")
            updateHistoryUI()
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

    private fun setupGridNavigation() {
        val historyLayout = findViewById<View>(R.id.history_layout)
        historyLayout.findViewById<View>(R.id.btn_prev_month).setOnClickListener {
            currentGridCalendar.add(Calendar.MONTH, -1)
            setupDynamicHistoryGrid()
        }
        historyLayout.findViewById<View>(R.id.btn_next_month).setOnClickListener {
            currentGridCalendar.add(Calendar.MONTH, 1)
            setupDynamicHistoryGrid()
        }
    }

    private fun updateHistoryUI() {
        setupDynamicHistoryGrid()
        updateTrendChart()
        updatePerformanceCard(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()))
    }

    private fun updateSectionProgress() {
        val progress = DataManager.getHabitProgress()
        sectionProgressBar.progress = progress
        sectionProgressText.text = "$progress%"
    }

    private fun setupDynamicHistoryGrid() {
        val grid = findViewById<GridLayout>(R.id.history_dynamic_grid) ?: return
        val tvMonth = findViewById<TextView>(R.id.tv_grid_month) ?: return
        
        grid.removeAllViews()

        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = sdfMonth.format(currentGridCalendar.time).uppercase()
        
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

    private fun updateTrendChart() {
        val container = findViewById<LinearLayout>(R.id.trend_chart_container) ?: return
        container.removeAllViews()

        val last7Days = DataManager.getLastSevenDaysDetailedProgress() // List<Pair<H%, W%>>
        val cal = Calendar.getInstance()
        val dayLetters = mutableListOf<String>()
        for (i in 0 until 7) {
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(cal.time)
            dayLetters.add(0, dayName.take(1).uppercase())
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        last7Days.forEachIndexed { index, pair ->
            val barLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            barLayout.layoutParams = params
            barLayout.orientation = LinearLayout.VERTICAL
            barLayout.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

            val barsContainer = LinearLayout(this)
            barsContainer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1f)
            barsContainer.orientation = LinearLayout.HORIZONTAL
            barsContainer.gravity = Gravity.BOTTOM

            // Habit Bar
            val habitBar = View(this)
            val hHeight = (pair.first * 1.5 * resources.displayMetrics.density).toInt().coerceAtLeast(10)
            val hParams = LinearLayout.LayoutParams((14 * resources.displayMetrics.density).toInt(), hHeight)
            hParams.setMargins(6, 0, 6, 0)
            habitBar.layoutParams = hParams
            habitBar.background = ContextCompat.getDrawable(this, R.drawable.bar_habit)
            barsContainer.addView(habitBar)

            // Workout Bar
            val workoutBar = View(this)
            val wHeight = (pair.second * 1.5 * resources.displayMetrics.density).toInt().coerceAtLeast(10)
            val wParams = LinearLayout.LayoutParams((14 * resources.displayMetrics.density).toInt(), wHeight)
            wParams.setMargins(6, 0, 6, 0)
            workoutBar.layoutParams = wParams
            workoutBar.background = ContextCompat.getDrawable(this, R.drawable.bar_workout)
            barsContainer.addView(workoutBar)

            barLayout.addView(barsContainer)

            val tvDay = TextView(this)
            tvDay.text = dayLetters[index]
            tvDay.setTextColor(Color.parseColor("#4DFFFFFF"))
            tvDay.textSize = 10f
            tvDay.gravity = Gravity.CENTER
            tvDay.setPadding(0, 8, 0, 0)
            barLayout.addView(tvDay)

            container.addView(barLayout)
        }
    }

    private var tempRepeatType = "SPECIFIC_DAYS"
    private var tempRepeatDays = mutableListOf(0, 1, 2, 3, 4, 5, 6)
    private var tempRepeatCount = 1

    fun showAddHabitDialog(existingHabit: Habit? = null) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_habit)

        val nameInput = dialog.findViewById<EditText>(R.id.habit_name_input)
        val btnClose = dialog.findViewById<View>(R.id.btn_close)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save)
        val iconPreview = dialog.findViewById<ImageView>(R.id.icon_preview)
        val colorPreview = dialog.findViewById<View>(R.id.color_preview)
        val headerAccent = dialog.findViewById<View>(R.id.header_bg_accent)
        val tvNameHint = dialog.findViewById<TextView>(R.id.tv_name_hint)
        val tvScheduleHint = dialog.findViewById<TextView>(R.id.tv_schedule_hint)

        // Day Selector Direct Logic
        val dayViews = listOf(R.id.day_0_direct, R.id.day_1_direct, R.id.day_2_direct, R.id.day_3_direct, R.id.day_4_direct, R.id.day_5_direct, R.id.day_6_direct)
            .map { dialog.findViewById<TextView>(it) }
        
        tempRepeatDays = existingHabit?.repeatDays?.toMutableList() ?: mutableListOf(0, 1, 2, 3, 4, 5, 6)
        tempRepeatType = "SPECIFIC_DAYS"

        fun validateInputs() {
            val name = nameInput.text.toString().trim()
            val isNameValid = name.isNotEmpty()
            val isScheduleValid = tempRepeatDays.isNotEmpty()
            val isAllValid = isNameValid && isScheduleValid

            btnSave.alpha = if (isAllValid) 1.0f else 0.3f
            btnSave.isEnabled = isAllValid
            
            tvNameHint.visibility = if (isNameValid) View.GONE else View.VISIBLE
            tvScheduleHint.visibility = if (isScheduleValid) View.GONE else View.VISIBLE
            
            if (!isNameValid) startPulseAnimation(tvNameHint)
            if (!isScheduleValid) startPulseAnimation(tvScheduleHint)
        }

        fun refreshDayButtons() {
            dayViews.forEachIndexed { index, tv ->
                val isSelected = tempRepeatDays.contains(index)
                tv.backgroundTintList = ColorStateList.valueOf(if (isSelected) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                tv.alpha = if (isSelected) 1.0f else 0.5f
            }
            validateInputs()
        }
        refreshDayButtons()

        dayViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                if (tempRepeatDays.contains(index)) { if (tempRepeatDays.size > 1) tempRepeatDays.remove(index) }
                else { tempRepeatDays.add(index) }
                refreshDayButtons()
            }
        }

        nameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Frequency Cards Logic
        val cardMorning = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_morning)
        val cardAfternoon = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_afternoon)
        val cardEvening = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_evening)
        val cardAnytime = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_anytime)
        val freqCards = mapOf("Morning" to cardMorning, "Afternoon" to cardAfternoon, "Evening" to cardEvening, "Anytime" to cardAnytime)

        var selectedFrequency = existingHabit?.frequency ?: "Anytime"

        fun refreshFreqCards() {
            freqCards.forEach { (type, card) ->
                val isActive = type == selectedFrequency
                card.setCardBackgroundColor(if (isActive) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                card.alpha = if (isActive) 1.0f else 0.6f
            }
        }
        refreshFreqCards()

        freqCards.forEach { (type, card) ->
            card.setOnClickListener { selectedFrequency = type; refreshFreqCards() }
        }

        val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN, Color.YELLOW, Color.LTGRAY)
        var selectedColor = existingHabit?.color ?: colors[0]
        var selectedIcon = existingHabit?.iconResId ?: R.drawable.ic_habit_tracker

        fun updateThemeVisuals() {
            iconPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
            colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
            headerAccent.backgroundTintList = ColorStateList.valueOf(selectedColor)
            if (btnSave.isEnabled) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
            tvNameHint.setTextColor(selectedColor)
            tvScheduleHint.setTextColor(selectedColor)
        }

        if (existingHabit != null) {
            nameInput.setText(existingHabit.name)
            btnSave.text = "Update"
            iconPreview.setImageResource(selectedIcon)
        }
        
        updateThemeVisuals()
        validateInputs()

        dialog.findViewById<View>(R.id.card_habit_icon).setOnClickListener {
            showIconSelectionDialog { icon ->
                selectedIcon = icon
                iconPreview.setImageResource(selectedIcon)
            }
        }

        colorPreview.setOnClickListener {
            val currentIndex = colors.indexOf(selectedColor)
            selectedColor = colors[(currentIndex + 1) % colors.size]
            updateThemeVisuals()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        
        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (existingHabit == null) {
                habits.add(Habit(name, false, selectedFrequency, color = selectedColor, iconResId = selectedIcon, repeatType = "SPECIFIC_DAYS", repeatDays = tempRepeatDays.toList(), repeatCount = 1))
            } else {
                existingHabit.name = name
                existingHabit.frequency = selectedFrequency
                existingHabit.color = selectedColor
                existingHabit.iconResId = selectedIcon
                existingHabit.repeatDays = tempRepeatDays.toList()
            }
            habitAdapter.sortHabits()
            DataManager.saveData(this)
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun startPulseAnimation(view: View) {
        if (view.tag == "pulsing") return
        view.tag = "pulsing"
        view.animate().alpha(0.4f).setDuration(800).withEndAction {
            view.animate().alpha(1.0f).setDuration(800).withEndAction {
                view.tag = null
                if (view.visibility == View.VISIBLE) startPulseAnimation(view)
            }
        }.start()
    }

    private fun showIconSelectionDialog(onSelected: (Int) -> Unit) {
        val icons = listOf(
            R.drawable.ic_habit_tracker, R.drawable.ic_water, R.drawable.ic_sleep,
            R.drawable.ic_book, R.drawable.ic_meditation, R.drawable.ic_notes,
            R.drawable.ic_fitness, R.drawable.ic_finance, R.drawable.icons8_coffee_100,
            R.drawable.icons8_yoga_100, R.drawable.icons8_clock_100, R.drawable.icons8_laptop_100,
            R.drawable.icons8_typing_100, R.drawable.icons8_sun_50_apng, R.drawable.icons8_health_100_3,
            R.drawable.icons8_clock_100_2, R.drawable.icons8_search_100, R.drawable.icons8_income_100,
            R.drawable.icons8_bookmark_100, R.drawable.icons8_coffee_100_2, R.drawable.icons8_coffee_100_3,
            R.drawable.icons8_coffee_100_4, R.drawable.icons8_coffee_100_5, R.drawable.icons8_drinking_100,
            R.drawable.icons8_drinking_50, R.drawable.icons8_health_100, R.drawable.icons8_health_100_4,
            R.drawable.icons8_health_100_9, R.drawable.icons8_health_100_11, R.drawable.icons8_health_100_12,
            R.drawable.icons8_health_100_13, R.drawable.icons8_heart_health_100, R.drawable.icons8_yoga_100_3,
            R.drawable.icons8_pilates_100, R.drawable.icons8_artistic_gymnastics_100, R.drawable.icons8_walking_100_3
        )
        
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_premium_icon_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val gridLayout = dialog.findViewById<GridLayout>(R.id.premium_icon_grid)
        gridLayout.columnCount = 5
        val btnClose = dialog.findViewById<View>(R.id.btn_close_picker)

        icons.forEach { iconRes ->
            val iconView = ImageView(this)
            val s = (52 * resources.displayMetrics.density).toInt() // Slightly smaller for 5 columns
            val params = GridLayout.LayoutParams()
            params.width = s; params.height = s; params.setMargins(6, 6, 6, 6)
            iconView.layoutParams = params
            
            iconView.setImageResource(iconRes)
            iconView.setPadding(12, 12, 12, 12)
            iconView.background = ContextCompat.getDrawable(this, R.drawable.circle_selected_bg)
            iconView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#22FFFFFF"))
            iconView.imageTintList = ColorStateList.valueOf(Color.WHITE)
            
            iconView.setOnClickListener {
                onSelected(iconRes)
                dialog.dismiss()
            }
            gridLayout.addView(iconView)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
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
        updateSectionProgress()
    }

    private fun updateNavUI(active: String) {
        val todayColor = if (active == "TODAY") ContextCompat.getColor(this, R.color.chip_selected) else ContextCompat.getColor(this, R.color.text_secondary)
        val historyColor = if (active == "HISTORY") ContextCompat.getColor(this, R.color.chip_selected) else ContextCompat.getColor(this, R.color.text_secondary)
        
        findViewById<ImageView>(R.id.iv_today).setColorFilter(todayColor)
        findViewById<TextView>(R.id.tv_today_nav).setTextColor(todayColor)
        findViewById<ImageView>(R.id.iv_history).setColorFilter(historyColor)
        findViewById<TextView>(R.id.tv_history_nav).setTextColor(historyColor)
    }
}
