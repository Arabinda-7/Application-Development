package com.example.allinone

import android.app.Activity
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
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

class WorkoutRoutineActivity : BaseActivity() {

    private val workouts = DataManager.workouts
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var weekAdapter: CalendarWeekAdapter
    private lateinit var sectionProgressBar: android.widget.ProgressBar
    private lateinit var sectionProgressText: TextView
    private var currentlyTimingWorkoutPosition: Int = -1
    private var selectedTimeFilter: String = "All"
    private var selectedDateString: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    
    private var currentGridCalendar = Calendar.getInstance()

    private val timerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (currentlyTimingWorkoutPosition != -1) {
                val workout = workouts[currentlyTimingWorkoutPosition]
                workout.isCompleted = true
                workout.progress = workout.target
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                if (!workout.completedDates.contains(today)) {
                    workout.completedDates.add(today)
                    DataManager.addActivity("Finished Workout: ${workout.name}")
                }
                workoutAdapter.sortWorkouts()
                DataManager.saveData(this)
                currentlyTimingWorkoutPosition = -1
                updateHistoryUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_routine)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.today_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val offset = (12 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, statusBars.top - offset, v.paddingRight, v.paddingBottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val offset = (12 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, statusBars.top - offset, v.paddingRight, navBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_nav_mock)) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBars.bottom)
            insets
        }

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(Date())

        val workoutList = findViewById<RecyclerView>(R.id.workout_list)
        workoutList.layoutManager = LinearLayoutManager(this)

        sectionProgressBar = findViewById(R.id.section_progress_bar)
        sectionProgressText = findViewById(R.id.tv_section_progress_percentage)

        workoutAdapter = WorkoutAdapter(workouts, { 
            DataManager.saveData(this)
            updateHistoryUI()
            updateSectionProgress()
            if (DataManager.workoutAutoRestTimer) {
                startRestTimer()
            }
        }, { workout, position -> startTimerForWorkout(workout, position) })
        workoutList.adapter = workoutAdapter

        val btnCreate = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_create_new_workout)
        if (DataManager.workoutAddThemeColor != -1) {
            btnCreate.strokeColor = DataManager.workoutAddThemeColor
        }
        btnCreate.setOnClickListener { showAddWorkoutDialog(null) }

        setupHeaderLogic()
        setupFooterLogic()
        setupGridNavigation()
        setupCalendarViewPager()
        updateSectionProgress()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back_history).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_workout_settings).setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 10f

            val balanceBtn = menuView.findViewById<View>(R.id.menu_action_primary)
            balanceBtn.visibility = View.VISIBLE
            menuView.findViewById<TextView>(R.id.tv_action_primary).text = "MUSCLE BALANCE"
            menuView.findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_fitness)
            
            balanceBtn.setOnClickListener {
                showWorkoutStatsDialog()
                popupWindow.dismiss()
            }

            val menuToggle = menuView.findViewById<View>(R.id.menu_toggle_completed)
            val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
            val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
            
            menuToggle.visibility = View.VISIBLE
            tvToggle.text = if (DataManager.workoutShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
            ivToggle.setImageResource(if (DataManager.workoutShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

            menuToggle.setOnClickListener {
                DataManager.workoutShowCompleted = !DataManager.workoutShowCompleted
                workoutAdapter.setShowCompleted(DataManager.workoutShowCompleted)
                DataManager.saveData(this)
                popupWindow.dismiss()
            }

            menuView.findViewById<View>(R.id.menu_clear_completed).visibility = View.GONE

            menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
                showWorkoutSettingsDialog()
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(it, -150, 0)
        }
    }

    private fun setupCalendarViewPager() {
        val vpCalendar = findViewById<ViewPager2>(R.id.vp_calendar)
        val weeks = mutableListOf<List<DayModel>>()
        val calendar = Calendar.getInstance()
        
        calendar.add(Calendar.WEEK_OF_YEAR, -52)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfDate.format(Date())
        val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())

        var initialPageIndex = 0
        val totalWeeksCount = 105 

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
            workoutAdapter.filter(selectedTimeFilter, dayIndex, selectedDateString)
        } catch (e: Exception) {
            workoutAdapter.filter(selectedTimeFilter, 0, selectedDateString)
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
        findViewById<TextView>(R.id.history_workouts_finished).text = DataManager.getTotalWorkoutsFinished().toString()
        findViewById<TextView>(R.id.history_efficiency).text = "${DataManager.getGlobalCompletionRate()}%"
        setupDynamicHistoryGrid()
        updateSectionProgress()
    }

    private fun updateSectionProgress() {
        val progress = DataManager.getWorkoutProgress()
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
                if (workouts.isNotEmpty()) (workouts.count { it.isCompleted } * 100) / workouts.size else 0
            } else {
                val historyData = DataManager.history[dateKey]
                if (historyData != null && historyData.totalWorkouts > 0) {
                    (historyData.workoutsCompleted * 100) / historyData.totalWorkouts
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

        val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val s = (32 * resources.displayMetrics.density).toInt()
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
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)

        return frameLayout
    }

    private fun startTimerForWorkout(workout: Workout, position: Int) {
        currentlyTimingWorkoutPosition = position
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("WORKOUT_NAME", workout.name)
            putExtra("TIMER_DURATION", workout.target)
        }
        timerActivityResultLauncher.launch(intent)
    }

    fun showAddWorkoutDialog(existingWorkout: Workout? = null) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_workout)

        val nameInput = dialog.findViewById<EditText>(R.id.workout_name_input)
        val targetInput = dialog.findViewById<EditText>(R.id.target_input)
        val chipGroup = dialog.findViewById<ChipGroup>(R.id.muscle_chip_group)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_workout)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_workout)
        val headerAccent = dialog.findViewById<View>(R.id.header_bg_accent_workout)
        val iconPreview = dialog.findViewById<ImageView>(R.id.icon_preview_workout)
        val colorPreview = dialog.findViewById<View>(R.id.color_preview_workout)
        val tvNameHint = dialog.findViewById<TextView>(R.id.tv_name_hint_workout)
        val tvScheduleHint = dialog.findViewById<TextView>(R.id.tv_schedule_hint_workout)
        val tvTargetHint = dialog.findViewById<TextView>(R.id.tv_target_hint_workout)

        val dayViews = listOf(R.id.day_0_direct_workout, R.id.day_1_direct_workout, R.id.day_2_direct_workout, R.id.day_3_direct_workout, R.id.day_4_direct_workout, R.id.day_5_direct_workout, R.id.day_6_direct_workout)
            .map { dialog.findViewById<TextView>(it) }
        
        var tempRepeatDays = existingWorkout?.repeatDays?.toMutableList() ?: mutableListOf(0, 1, 2, 3, 4, 5, 6)

        fun validateInputs() {
            val name = nameInput.text.toString().trim()
            val target = targetInput.text.toString().toIntOrNull() ?: 0
            val isNameValid = name.isNotEmpty()
            val isScheduleValid = tempRepeatDays.isNotEmpty()
            val isTargetValid = target > 0
            
            val isAllValid = isNameValid && isScheduleValid && isTargetValid

            btnSave.alpha = if (isAllValid) 1.0f else 0.3f
            btnSave.isEnabled = isAllValid
            
            tvNameHint.visibility = if (isNameValid) View.GONE else View.VISIBLE
            tvScheduleHint.visibility = if (isScheduleValid) View.GONE else View.VISIBLE
            tvTargetHint.visibility = if (isTargetValid) View.GONE else View.VISIBLE

            if (!isNameValid) startPulseAnimation(tvNameHint)
            if (!isScheduleValid) startPulseAnimation(tvScheduleHint)
            if (!isTargetValid) startPulseAnimation(tvTargetHint)
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

        targetInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val cardReps = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_mode_reps)
        val cardSets = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_mode_sets)
        val cardTimer = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_mode_timer)
        val modeCards = mapOf("Reps" to cardReps, "Sets" to cardSets, "Timer" to cardTimer)
        
        var selectedMode = existingWorkout?.trackingMode ?: DataManager.workoutDefaultMode

        fun refreshModeCards() {
            modeCards.forEach { (mode, card) ->
                val isActive = mode == selectedMode
                card.setCardBackgroundColor(if (isActive) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                card.alpha = if (isActive) 1.0f else 0.6f
            }
        }
        refreshModeCards()
        modeCards.forEach { (mode, card) -> card.setOnClickListener { selectedMode = mode; refreshModeCards() } }

        val cardMorning = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_morning_workout)
        val cardAfternoon = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_afternoon_workout)
        val cardEvening = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_evening_workout)
        val cardAnytime = dialog.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_anytime_workout)
        val freqCards = mapOf("Morning" to cardMorning, "Afternoon" to cardAfternoon, "Evening" to cardEvening, "Anytime" to cardAnytime)

        var selectedFrequency = existingWorkout?.frequency ?: "Anytime"

        fun refreshFreqCards() {
            freqCards.forEach { (type, card) ->
                val isActive = type == selectedFrequency
                card.setCardBackgroundColor(if (isActive) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                card.alpha = if (isActive) 1.0f else 0.6f
            }
        }
        refreshFreqCards()
        freqCards.forEach { (type, card) -> card.setOnClickListener { selectedFrequency = type; refreshFreqCards() } }

        val muscleGroups = DataManager.workoutMuscleGroups
        val selectedMuscleGroups = existingWorkout?.muscleGroups?.toMutableList() ?: mutableListOf("General")
        muscleGroups.forEach { group ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = group; chip.isCheckable = true; chip.isChecked = selectedMuscleGroups.contains(group)
            chip.setChipBackgroundColorResource(R.color.chip_background); chip.setTextColor(Color.WHITE)
            chip.setCheckedIconVisible(true); chip.setCheckedIconTintResource(R.color.white)
            chip.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { if (!selectedMuscleGroups.contains(group)) selectedMuscleGroups.add(group) } else { selectedMuscleGroups.remove(group) } }
            chipGroup.addView(chip)
        }

        val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN, Color.YELLOW, Color.LTGRAY)
        var selectedColor = existingWorkout?.color ?: colors[0]
        var selectedIcon = existingWorkout?.iconResId ?: R.drawable.icons8_exercise_100

        fun updateThemeVisuals() {
            iconPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
            colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
            headerAccent.backgroundTintList = ColorStateList.valueOf(selectedColor)
            if (btnSave.isEnabled) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
            tvNameHint.setTextColor(selectedColor)
            tvScheduleHint.setTextColor(selectedColor)
            tvTargetHint.setTextColor(selectedColor)
        }

        if (existingWorkout != null) {
            nameInput.setText(existingWorkout.name); targetInput.setText(existingWorkout.target.toString())
            btnSave.text = "Update"; iconPreview.setImageResource(selectedIcon)
        }
        
        updateThemeVisuals()
        validateInputs()

        dialog.findViewById<View>(R.id.card_workout_icon).setOnClickListener { showIconSelectionDialog { icon -> selectedIcon = icon; iconPreview.setImageResource(selectedIcon) } }
        colorPreview.setOnClickListener { val currentIndex = colors.indexOf(selectedColor); selectedColor = colors[(currentIndex + 1) % colors.size]; updateThemeVisuals() }
        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val target = targetInput.text.toString().toIntOrNull() ?: 0
            val finalMuscleSelection = if (selectedMuscleGroups.isEmpty()) listOf("General") else selectedMuscleGroups.toList()
            if (existingWorkout == null) {
                workouts.add(Workout(name, false, selectedMode, target, frequency = selectedFrequency, color = selectedColor, iconResId = selectedIcon, muscleGroups = finalMuscleSelection, repeatType = "SPECIFIC_DAYS", repeatDays = tempRepeatDays.toList(), repeatCount = 1))
            } else {
                existingWorkout.name = name; existingWorkout.target = target; existingWorkout.trackingMode = selectedMode; existingWorkout.frequency = selectedFrequency; existingWorkout.color = selectedColor; existingWorkout.iconResId = selectedIcon; existingWorkout.muscleGroups = finalMuscleSelection; existingWorkout.repeatDays = tempRepeatDays.toList()
            }
            workoutAdapter.sortWorkouts(); DataManager.saveData(this); dialog.dismiss()
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
            R.drawable.icons8_exercise_100, R.drawable.icons8_exercise_100_2, R.drawable.icons8_exercise_100_3,
            R.drawable.icons8_exercise_100_4, R.drawable.icons8_exercise_100_5, R.drawable.icons8_exercise_100_6,
            R.drawable.icons8_exercise_100_7, R.drawable.icons8_exercise_100_8, R.drawable.icons8_exercise_100_9,
            R.drawable.icons8_exercise_100_10, R.drawable.icons8_exercise_100_11, R.drawable.icons8_exercise_100_12,
            R.drawable.icons8_exercise_100_13, R.drawable.icons8_exercise_100_14, R.drawable.icons8_exercise_100_15,
            R.drawable.icons8_exercise_100_16, R.drawable.icons8_exercise_100_17, R.drawable.icons8_exercise_100_18,
            R.drawable.icons8_exercise_100_20, R.drawable.icons8_exercise_100_21, R.drawable.icons8_exercise_100_22,
            R.drawable.icons8_exercise_100_23, R.drawable.icons8_exercise_100_25, R.drawable.icons8_exercise_100_26,
            R.drawable.icons8_exercise_100_27, R.drawable.icons8_exercise_100_28, R.drawable.icons8_exercise_100_29,
            R.drawable.icons8_exercise_100_30, R.drawable.icons8_exercise_100_31, R.drawable.icons8_exercise_100_32,
            R.drawable.icons8_exercise_100_33, R.drawable.icons8_exercise_100_34, R.drawable.icons8_exercise_100_36,
            R.drawable.icons8_exercise_100_37, R.drawable.icons8_exercise_100_38, R.drawable.icons8_exercise_100_39,
            R.drawable.icons8_exercise_100_40, R.drawable.icons8_exercise_100_41, R.drawable.icons8_exercise_100_43,
            R.drawable.icons8_exercise_100_44, R.drawable.icons8_exercise_100_45, R.drawable.icons8_exercise_100_47,
            R.drawable.icons8_exercise_100_48, R.drawable.icons8_dumbbell_100, R.drawable.icons8_deadlift_100,
            R.drawable.icons8_plank_100, R.drawable.icons8_skipping_rope_100_2, R.drawable.icons8_treadmill_100_2,
            R.drawable.icons8_warm_up_100, R.drawable.icons8_pilates_100, R.drawable.icons8_triceps_100,
            R.drawable.icons8_yoga_100, R.drawable.icons8_hand_grip_100_2, R.drawable.icons8_walking_100_3,
            R.drawable.icons8_artistic_gymnastics_100, R.drawable.icons8_heart_health_100, R.drawable.icons8_dog_training_100
        )
        
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_premium_icon_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val gridLayout = dialog.findViewById<GridLayout>(R.id.premium_icon_grid)
        gridLayout.columnCount = 5
        val title = dialog.findViewById<TextView>(R.id.tv_picker_title)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_picker)

        title.text = "SELECT WORKOUT ICON"

        icons.forEach { iconRes ->
            val iconView = ImageView(this)
            val s = (52 * resources.displayMetrics.density).toInt()
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
        dialog.show()
    }

    private fun showWorkoutSettingsDialog() {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_habit_settings, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = view.findViewById<TextView>(R.id.tv_settings_title)
        val itemMuscle = view.findViewById<View>(R.id.item_default_tab) 
        val tvMuscleSummary = view.findViewById<TextView>(R.id.tv_default_tab_summary)
        
        val itemRest = view.findViewById<View>(R.id.item_vacation_mode)
        val swRest = view.findViewById<SwitchCompat>(R.id.iv_vacation_check)
        
        val itemUnit = view.findViewById<View>(R.id.item_day_reset)
        val tvUnitSummary = view.findViewById<TextView>(R.id.tv_day_reset_summary)
        
        val itemReadiness = view.findViewById<View>(R.id.item_bulk_mode)
        val ivReadiness = view.findViewById<View>(R.id.iv_bulk_check)
        
        val itemDefaultMode = view.findViewById<View>(R.id.item_sort_order)
        val tvDefaultModeSummary = view.findViewById<TextView>(R.id.tv_sort_summary)
        
        val itemRestDuration = view.findViewById<View>(R.id.item_sound)
        val btnClose = view.findViewById<View>(R.id.btn_close_settings)

        view.findViewById<View>(R.id.item_haptics)?.visibility = View.GONE
        view.findViewById<View>(R.id.item_grace_period)?.visibility = View.GONE

        title.text = "Workout Settings"
        
        fun setLabel(container: View?, text: String) {
            (container as? ViewGroup)?.let { vg ->
                for (i in 0 until vg.childCount) {
                    val child = vg.getChildAt(i)
                    if (child is TextView && child.id != R.id.tv_default_tab_summary && 
                        child.id != R.id.tv_sort_summary && child.id != R.id.tv_day_reset_summary &&
                        child.id != R.id.tv_grace_summary) {
                        child.text = text
                        return
                    }
                    if (child is ViewGroup) setLabel(child, text)
                }
            }
        }

        setLabel(itemMuscle, "Manage Muscle Groups")
        tvMuscleSummary?.text = "Add or remove body part tags"
        
        setLabel(itemRest, "Auto-Rest Timer")
        swRest?.isChecked = DataManager.workoutAutoRestTimer

        setLabel(itemUnit, "Workout Unit")
        tvUnitSummary?.text = "Current: ${DataManager.workoutWeightUnit} (Tap to change)"

        setLabel(itemReadiness, "Workout Readiness")
        ivReadiness?.visibility = View.GONE

        setLabel(itemDefaultMode, "Default Tracking Mode")
        tvDefaultModeSummary?.text = "Current: ${DataManager.workoutDefaultMode}"

        setLabel(itemRestDuration, "Rest Duration")
        val tvRestDurationSummary = (itemRestDuration as? ViewGroup)?.findViewById<TextView>(R.id.tv_sort_summary) 
                                     ?: (itemRestDuration as? ViewGroup)?.findViewById<TextView>(R.id.tv_default_tab_summary)
                                     ?: (itemRestDuration as? ViewGroup)?.getChildAt(0).let { (it as? ViewGroup)?.getChildAt(1) as? TextView }
        
        tvRestDurationSummary?.text = "Current: ${DataManager.workoutRestDuration}s (Tap to cycle)"
        view.findViewById<View>(R.id.iv_sound_check)?.visibility = View.GONE

        itemMuscle?.setOnClickListener { showManageMuscleGroupsDialog() }
        
        itemRest?.setOnClickListener {
            DataManager.workoutAutoRestTimer = !DataManager.workoutAutoRestTimer
            swRest?.isChecked = DataManager.workoutAutoRestTimer
            DataManager.saveData(this)
        }

        itemUnit?.setOnClickListener {
            DataManager.workoutWeightUnit = if (DataManager.workoutWeightUnit == "Kg") "Lb" else "Kg"
            DataManager.saveData(this)
            tvUnitSummary?.text = "Current: ${DataManager.workoutWeightUnit} (Tap to change)"
            android.widget.Toast.makeText(this, "Unit changed to ${DataManager.workoutWeightUnit}", android.widget.Toast.LENGTH_SHORT).show()
        }

        itemReadiness?.setOnClickListener { showWorkoutReadinessDialog() }

        itemDefaultMode?.setOnClickListener {
            val modes = listOf("Reps", "Sets", "Timer")
            val next = modes[(modes.indexOf(DataManager.workoutDefaultMode) + 1) % modes.size]
            DataManager.workoutDefaultMode = next
            tvDefaultModeSummary?.text = "Current: $next"
            DataManager.saveData(this)
        }

        itemRestDuration?.setOnClickListener {
            val durations = listOf(30, 60, 90, 120, 180)
            val currentIdx = durations.indexOf(DataManager.workoutRestDuration)
            val next = durations[(if (currentIdx == -1) 1 else currentIdx + 1) % durations.size]
            DataManager.workoutRestDuration = next
            tvRestDurationSummary?.text = "Current: ${next}s (Tap to cycle)"
            DataManager.saveData(this)
        }

        btnClose?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showManageMuscleGroupsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)

        title.text = "Manage Muscle Groups"

        fun refresh() {
            container.removeAllViews()
            DataManager.workoutMuscleGroups.forEach { group ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
                itemView.findViewById<TextView>(R.id.tv_header_title).text = group
                itemView.findViewById<View>(R.id.iv_header_chevron).visibility = View.GONE
                itemView.setOnLongClickListener {
                    DataManager.workoutMuscleGroups.remove(group)
                    DataManager.saveData(this)
                    refresh()
                    true
                }
                container.addView(itemView)
            }
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.workoutMuscleGroups.contains(name)) {
                DataManager.workoutMuscleGroups.add(name)
                DataManager.saveData(this)
                refresh()
                etNew.text.clear()
            }
        }

        refresh()
        dialog.show()
    }

    private fun showWorkoutStatsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnClose = dialog.findViewById<TextView>(R.id.btn_save_budget)

        title.text = "HEALTH & TRAINING STATS"
        etInput.visibility = View.GONE
        
        val muscleStats = mutableMapOf<String, Int>()
        workouts.forEach { workout ->
            workout.muscleGroups.forEach { group ->
                muscleStats[group] = (muscleStats[group] ?: 0) + 1
            }
        }
        
        val calories = DataManager.getTodayCaloriesBurned()
        val sb = StringBuilder()
        sb.append("🔥 Calories Burned Today: $calories kcal\n\n")
        
        if (muscleStats.isEmpty()) {
            sb.append("No workouts tracked yet. Assign muscle groups to see balance!")
        } else {
            sb.append("Muscle Group Distribution:\n\n")
            DataManager.workoutMuscleGroups.forEach { group ->
                val count = muscleStats[group] ?: 0
                sb.append("$group: $count sessions\n")
            }
        }
        subtext.text = sb.toString()
        btnClose.text = "CLOSE"
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showWorkoutReadinessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnAction = dialog.findViewById<TextView>(R.id.btn_save_budget)

        title.text = "READINESS CHECK"
        etInput.visibility = View.GONE
        subtext.text = "How are you feeling today?\n\n1. Did you sleep 7+ hours?\n2. Do you have high energy?"
        btnAction.text = "START SURVEY"

        var step = 1
        var score = 0

        fun finishSurvey() {
            step = 3
            title.text = "YOUR SCORE: $score%"
            subtext.text = if (score >= 100) "You are fully ready! Crush it!" 
                          else if (score >= 50) "Proceed with caution. Maybe a lighter session?"
                          else "Recovery might be better today. Consider a rest day."
            btnAction.text = "CLOSE"
        }

        fun nextStep() { 
            step = 2
            subtext.text = "Step 2: Check your energy levels." 
        }

        btnAction.setOnClickListener {
            when (step) {
                1 -> {
                    AlertDialog.Builder(this).setTitle("Sleep").setMessage("Did you sleep well?")
                        .setPositiveButton("Yes") { _, _ -> score += 50; nextStep() }
                        .setNegativeButton("No") { _, _ -> nextStep() }.show()
                }
                2 -> {
                    AlertDialog.Builder(this).setTitle("Energy").setMessage("Ready for heavy lifting?")
                        .setPositiveButton("Yes") { _, _ -> score += 50; finishSurvey() }
                        .setNegativeButton("No") { _, _ -> finishSurvey() }.show()
                }
                else -> dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun startRestTimer() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnClose = dialog.findViewById<TextView>(R.id.btn_save_budget)

        val duration = DataManager.workoutRestDuration
        title.text = "REST TIMER"
        etInput.visibility = View.GONE
        btnClose.text = "SKIP"
        
        val timer = object : android.os.CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = (millisUntilFinished / 1000).toInt()
                subtext.text = "Rest for $timeLeft seconds..."
            }
            override fun onFinish() {
                dialog.dismiss()
                android.widget.Toast.makeText(this@WorkoutRoutineActivity, "Rest finished! Back to work!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        timer.start()

        btnClose.setOnClickListener { timer.cancel(); dialog.dismiss() }
        dialog.show()
    }
}
