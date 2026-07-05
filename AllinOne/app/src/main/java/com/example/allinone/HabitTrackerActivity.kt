package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
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

class HabitTrackerActivity : AppCompatActivity() {

    private val habits = DataManager.habits
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var weekAdapter: CalendarWeekAdapter
    private lateinit var sectionProgressBar: android.widget.ProgressBar
    private lateinit var sectionProgressText: TextView
    private var selectedTimeFilter: String = "All"
    private var selectedDateString: String = DataManager.getTrackingDateString()
    
    private var currentGridCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_tracker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.today_layout)) { v, insets ->
            val topPadding = (8 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, topPadding, v.paddingRight, v.paddingBottom)
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_layout)) { v, insets ->
            val topPadding = (8 * resources.displayMetrics.density).toInt()
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, topPadding, v.paddingRight, navBars.bottom)
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

        val btnCreate = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_create_new_habit)
        if (DataManager.habitAddThemeColor != -1) {
            btnCreate.backgroundTintList = android.content.res.ColorStateList.valueOf(DataManager.habitAddThemeColor)
        }
        btnCreate.setOnClickListener { showAddHabitDialog(null) }

        setupHeaderLogic()
        setupFooterLogic()
        setupGridNavigation()
        setupCalendarViewPager()
        updateSectionProgress()

        // Apply Default Startup Tab
        if (DataManager.habitDefaultTab == "HISTORY") {
            findViewById<View>(R.id.today_layout).visibility = View.GONE
            findViewById<View>(R.id.history_layout).visibility = View.VISIBLE
            updateNavUI("HISTORY")
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
                    isSelected = isSelected
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

        vpCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val firstDay = weeks[position][0]
                val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                findViewById<TextView>(R.id.tv_date).text = sdfMonth.format(firstDay.date)
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
        val todayLayout = findViewById<View>(R.id.today_layout)
        val historyLayout = findViewById<View>(R.id.history_layout)
        val navToday = findViewById<View>(R.id.nav_today)
        val navHistory = findViewById<View>(R.id.nav_history)
        val ivToday = findViewById<ImageView>(R.id.iv_today)
        val tvTodayNav = findViewById<TextView>(R.id.tv_today_nav)
        val ivHistory = findViewById<ImageView>(R.id.iv_history)
        val tvHistoryNav = findViewById<TextView>(R.id.tv_history_nav)

        navToday.setOnClickListener {
            todayLayout.visibility = View.VISIBLE
            historyLayout.visibility = View.GONE
            ivToday.imageTintList = ContextCompat.getColorStateList(this, R.color.chip_selected)
            tvTodayNav.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
            ivHistory.imageTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)
            tvHistoryNav.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        navHistory.setOnClickListener {
            todayLayout.visibility = View.GONE
            historyLayout.visibility = View.VISIBLE
            ivHistory.imageTintList = ContextCompat.getColorStateList(this, R.color.chip_selected)
            tvHistoryNav.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
            ivToday.imageTintList = ContextCompat.getColorStateList(this, R.color.text_secondary)
            tvTodayNav.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            updateHistoryUI()
        }
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
        findViewById<TextView>(R.id.history_current_streak).text = DataManager.getCurrentStreak().toString()
        findViewById<TextView>(R.id.history_habits_finished).text = DataManager.getTotalHabitsFinished().toString()
        findViewById<TextView>(R.id.history_completion_rate).text = "${DataManager.getGlobalCompletionRate()}%"
        setupDynamicHistoryGrid()
        updateSectionProgress()
    }

    private fun updateSectionProgress() {
        val progress = DataManager.getHabitProgress()
        sectionProgressBar.progress = progress
        sectionProgressText.text = "$progress%"
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
                if (habits.isNotEmpty()) (habits.count { it.isCompleted } * 100) / habits.size else 0
            } else {
                val historyData = DataManager.history[dateKey]
                if (historyData != null && historyData.totalHabits > 0) {
                    (historyData.habitsCompleted * 100) / historyData.totalHabits
                } else 0
            }
            
            grid.addView(createDayView(day.toString(), progress))
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
        params.height = 120
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
        progressBar.scaleX = -1f // Flip horizontally for anti-clockwise fill
        frameLayout.addView(progressBar)

        val textView = TextView(this)
        textView.text = day
        textView.setTextColor(Color.WHITE)
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        return frameLayout
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
        var selectedIcon = existingHabit?.iconResId ?: android.R.drawable.ic_menu_directions

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
        dialog.show()
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
        val btnClose = dialog.findViewById<View>(R.id.btn_close_picker)

        icons.forEach { iconRes ->
            val iconView = ImageView(this)
            val s = (64 * resources.displayMetrics.density).toInt() // Slightly smaller for better grid fit
            val params = GridLayout.LayoutParams()
            params.width = s; params.height = s; params.setMargins(8, 8, 8, 8)
            iconView.layoutParams = params
            
            iconView.setImageResource(iconRes)
            iconView.setPadding(16, 16, 16, 16)
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
        dialog.show()
    }

    private fun updateRepeatSummary(textView: TextView) {
        textView.text = when (tempRepeatType) {
            "SPECIFIC_DAYS" -> if (tempRepeatDays.size == 7) "Everyday" else "Specific days"
            "WEEKLY" -> "$tempRepeatCount days per week"
            else -> "Everyday"
        }
    }

    private fun showHabitDaysDialog(onDismiss: () -> Unit) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_habit_days)
        val btnBack = dialog.findViewById<ImageView>(R.id.btn_back)
        val cardSpecific = dialog.findViewById<View>(R.id.card_specific_days)
        val cardWeekly = dialog.findViewById<View>(R.id.card_days_per_week)
        val tvDaysPerWeek = dialog.findViewById<TextView>(R.id.tv_days_per_week)
        val ivCheckSpecific = dialog.findViewById<ImageView>(R.id.iv_check_specific)
        val ivRadioWeek = dialog.findViewById<ImageView>(R.id.iv_radio_week)
        val dayViews = listOf(R.id.day_0, R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6).map { dialog.findViewById<TextView>(it) }

        fun refreshUI() {
            ivCheckSpecific.visibility = if (tempRepeatType == "SPECIFIC_DAYS") View.VISIBLE else View.INVISIBLE
            ivRadioWeek.setImageResource(if (tempRepeatType == "WEEKLY") android.R.drawable.radiobutton_on_background else android.R.drawable.radiobutton_off_background)
            tvDaysPerWeek.text = "$tempRepeatCount day${if (tempRepeatCount > 1) "s" else ""} per week"
            dayViews.forEachIndexed { index, tv ->
                val isSelected = tempRepeatType == "SPECIFIC_DAYS" && tempRepeatDays.contains(index)
                tv.backgroundTintList = android.content.res.ColorStateList.valueOf(if (isSelected) ContextCompat.getColor(this, R.color.chip_selected) else Color.TRANSPARENT)
                tv.setTextColor(if (isSelected) Color.WHITE else Color.GRAY)
            }
        }
        refreshUI()
        cardSpecific.setOnClickListener { tempRepeatType = "SPECIFIC_DAYS"; refreshUI() }
        dayViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                if (tempRepeatType != "SPECIFIC_DAYS") { tempRepeatType = "SPECIFIC_DAYS"; tempRepeatDays.clear() }
                if (tempRepeatDays.contains(index)) { if (tempRepeatDays.size > 1) tempRepeatDays.remove(index) } else tempRepeatDays.add(index)
                refreshUI()
            }
        }
        cardWeekly.setOnClickListener {
            if (tempRepeatType == "WEEKLY") {
                val pickerDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_number_picker, null)
                val numberPicker = pickerDialogView.findViewById<NumberPicker>(R.id.number_picker)
                numberPicker.minValue = 1; numberPicker.maxValue = 7; numberPicker.value = tempRepeatCount
                AlertDialog.Builder(this).setTitle("Select days per week").setView(pickerDialogView).setPositiveButton("OK") { _, _ -> tempRepeatCount = numberPicker.value; refreshUI() }.setNegativeButton("Cancel", null).show()
            } else { tempRepeatType = "WEEKLY"; refreshUI() }
        }
        btnBack.setOnClickListener { onDismiss(); dialog.dismiss() }
        dialog.show()
    }

    private fun updateRadioSelection(list: List<RadioButton>, selected: RadioButton) {
        list.forEach { it.isChecked = false }
        selected.isChecked = true
    }

    private fun showHabitSettingsDialog() {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_habit_settings, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val itemDefaultTab = view.findViewById<View>(R.id.item_default_tab)
        val tvDefaultSummary = view.findViewById<TextView>(R.id.tv_default_tab_summary)
        val itemSort = view.findViewById<View>(R.id.item_sort_order)
        val tvSortSummary = view.findViewById<TextView>(R.id.tv_sort_summary)
        val itemVacation = view.findViewById<View>(R.id.item_vacation_mode)
        val swVacation = view.findViewById<SwitchCompat>(R.id.iv_vacation_check)
        val itemSound = view.findViewById<View>(R.id.item_sound)
        val swSound = view.findViewById<SwitchCompat>(R.id.iv_sound_check)
        val itemHaptics = view.findViewById<View>(R.id.item_haptics)
        val swHaptics = view.findViewById<SwitchCompat>(R.id.iv_haptics_check)
        val itemDayReset = view.findViewById<View>(R.id.item_day_reset)
        val tvDayReset = view.findViewById<TextView>(R.id.tv_day_reset_summary)
        val itemBulk = view.findViewById<View>(R.id.item_bulk_mode)
        val swBulk = view.findViewById<SwitchCompat>(R.id.iv_bulk_check)
        val itemGrace = view.findViewById<View>(R.id.item_grace_period)
        val tvGrace = view.findViewById<TextView>(R.id.tv_grace_summary)
        val btnClose = view.findViewById<View>(R.id.btn_close_settings)

        // Behavioral Insights Button (NEW - moved from home page)
        val btnInsights = TextView(this).apply {
            text = "VIEW BEHAVIORAL INSIGHTS"
            setTextColor(ContextCompat.getColor(this@HabitTrackerActivity, R.color.chip_selected))
            textSize = 12f
            setPadding(0, 20, 0, 20)
            gravity = Gravity.CENTER
            setOnClickListener {
                showBehavioralInsightsDialog()
            }
        }
        view.findViewById<LinearLayout>(R.id.container_settings_items).addView(btnInsights, 0)

        // Initial State
        tvDefaultSummary.text = "Current: ${DataManager.habitDefaultTab}"
        tvSortSummary.text = "Current: ${DataManager.habitSortOrder}"
        swVacation.isChecked = DataManager.habitVacationMode
        swSound.isChecked = DataManager.habitCompletionSound
        swHaptics.isChecked = DataManager.habitCompletionHaptics
        swBulk.isChecked = DataManager.habitBulkMode
        tvGrace.text = "Allowed misses: ${DataManager.habitGraceDaysAllowed} days"
        
        fun updateResetSummary() {
            val hour = DataManager.habitDayResetHour
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            tvDayReset.text = "Ends at: $displayHour:00 $amPm"
        }
        updateResetSummary()

        itemDefaultTab.setOnClickListener {
            DataManager.habitDefaultTab = if (DataManager.habitDefaultTab == "TODAY") "HISTORY" else "TODAY"
            tvDefaultSummary.text = "Current: ${DataManager.habitDefaultTab}"
            DataManager.saveData(this)
        }

        itemSort.setOnClickListener {
            DataManager.habitSortOrder = if (DataManager.habitSortOrder == "Time") "Streak" else "Time"
            tvSortSummary.text = "Current: ${DataManager.habitSortOrder}"
            DataManager.saveData(this)
            applyFilters() // Refresh list with new sort
        }

        itemVacation.setOnClickListener {
            DataManager.habitVacationMode = !DataManager.habitVacationMode
            swVacation.isChecked = DataManager.habitVacationMode
            DataManager.saveData(this)
        }

        itemSound.setOnClickListener {
            DataManager.habitCompletionSound = !DataManager.habitCompletionSound
            swSound.isChecked = DataManager.habitCompletionSound
            DataManager.saveData(this)
        }

        itemHaptics.setOnClickListener {
            DataManager.habitCompletionHaptics = !DataManager.habitCompletionHaptics
            swHaptics.isChecked = DataManager.habitCompletionHaptics
            DataManager.saveData(this)
        }

        itemDayReset.setOnClickListener {
            val options = listOf(0, 1, 2, 3, 4) // 12 AM to 4 AM
            val current = DataManager.habitDayResetHour
            val next = options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]
            DataManager.habitDayResetHour = next
            DataManager.saveData(this)
            updateResetSummary()
            
            // Update the main date display
            val dateTextViewMain = findViewById<TextView>(R.id.tv_date)
            val sdfMain = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateTextViewMain.text = sdfMain.format(DataManager.getTrackingCalendar().time)
            
            selectedDateString = DataManager.getTrackingDateString()
            applyFilters()
        }

        itemBulk.setOnClickListener {
            DataManager.habitBulkMode = !DataManager.habitBulkMode
            swBulk.isChecked = DataManager.habitBulkMode
            DataManager.saveData(this)
            // Just update adapter state, no bar in UI anymore as requested
            habitAdapter.setBulkMode(DataManager.habitBulkMode)
        }

        itemGrace.setOnClickListener {
            val options = listOf(0, 1, 2, 3)
            val next = options[(options.indexOf(DataManager.habitGraceDaysAllowed).coerceAtLeast(0) + 1) % options.size]
            DataManager.habitGraceDaysAllowed = next
            DataManager.saveData(this)
            tvGrace.text = "Allowed misses: $next days"
            updateHistoryUI() // Streaks might change
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
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
        
        if (peak == null || peak.value <= 0) {
            subtext.text = "Not enough data yet. Keep tracking your habits to see your peak performance times!"
        } else {
            val sb = StringBuilder()
            sb.append("Your Peak Performance Time: ${peak.key.uppercase()}\n\n")
            stats.forEach { (freq, score) ->
                if (score >= 0) {
                    sb.append("$freq Habits: $score% Completion\n")
                }
            }
            subtext.text = sb.toString()
        }
        
        (btnSave as? TextView)?.text = "CLOSE"
        btnSave.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Ensure sorting is applied if it changed in settings
        applyFilters()
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
