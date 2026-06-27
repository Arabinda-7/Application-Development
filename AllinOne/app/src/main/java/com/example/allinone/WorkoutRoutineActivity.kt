package com.example.allinone

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRoutineActivity : AppCompatActivity() {

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
                if (!workout.completedDates.contains(today)) workout.completedDates.add(today)
                workoutAdapter.sortWorkouts()
                DataManager.saveData(this)
                currentlyTimingWorkoutPosition = -1
                updateHistoryUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_routine)

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

        val btnCreate = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_create_new_workout)
        if (DataManager.workoutAddThemeColor != -1) {
            btnCreate.backgroundTintList = android.content.res.ColorStateList.valueOf(DataManager.workoutAddThemeColor)
        }
        btnCreate.setOnClickListener { showAddWorkoutDialog(null) }

        setupHeaderLogic()
        setupFooterLogic()
        setupGridNavigation()
        setupCalendarViewPager()
        updateSectionProgress()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_workout_settings).setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 10f

            // Set Primary Action to Analytics/Balance
            val balanceBtn = menuView.findViewById<View>(R.id.menu_action_primary)
            balanceBtn.visibility = View.VISIBLE
            menuView.findViewById<TextView>(R.id.tv_action_primary).text = "MUSCLE BALANCE"
            menuView.findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_fitness)
            
            balanceBtn.setOnClickListener {
                showWorkoutStatsDialog()
                popupWindow.dismiss()
            }

            // Toggle Show/Hide Completed
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

            // Hide task-specific items
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
        val trackingGroup = dialog.findViewById<RadioGroup>(R.id.tracking_mode_group)
        val targetInput = dialog.findViewById<EditText>(R.id.target_input)
        val chipGroup = dialog.findViewById<ChipGroup>(R.id.muscle_chip_group)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_workout)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_workout)
        if (DataManager.workoutAddThemeColor != -1) {
            btnSave.setTextColor(DataManager.workoutAddThemeColor)
        }
        val iconPreview = dialog.findViewById<ImageView>(R.id.icon_preview_workout)
        val colorPreview = dialog.findViewById<View>(R.id.color_preview_workout)
        val cardRepeat = dialog.findViewById<View>(R.id.card_repeat_workout)
        val tvRepeatSummary = dialog.findViewById<TextView>(R.id.tv_repeat_summary_workout)
        
        val muscleGroups = DataManager.workoutMuscleGroups
        val selectedMuscleGroups = existingWorkout?.muscleGroups?.toMutableList() ?: mutableListOf("General")

        // Populate Muscle Group Chips
        muscleGroups.forEach { group ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = group
            chip.isCheckable = true
            chip.isChecked = selectedMuscleGroups.contains(group)
            
            // Style the chip for dark theme
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(Color.WHITE)
            chip.setCheckable(true)
            chip.setCheckedIconVisible(true)
            chip.setCheckedIconTintResource(R.color.white)
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!selectedMuscleGroups.contains(group)) selectedMuscleGroups.add(group)
                } else {
                    selectedMuscleGroups.remove(group)
                }
            }
            chipGroup.addView(chip)
        }

        val radioAnytime = dialog.findViewById<RadioButton>(R.id.radio_anytime_workout)
        val radioMorning = dialog.findViewById<RadioButton>(R.id.radio_morning_workout)
        val radioAfternoon = dialog.findViewById<RadioButton>(R.id.radio_afternoon_workout)
        val radioEvening = dialog.findViewById<RadioButton>(R.id.radio_evening_workout)
        val frequencyRadios = listOf(radioAnytime, radioMorning, radioAfternoon, radioEvening)
        frequencyRadios.forEach { rb -> rb.setOnClickListener { updateRadioSelection(frequencyRadios, rb) } }
        val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN, Color.YELLOW, Color.LTGRAY)
        var selectedColor = existingWorkout?.color ?: colors[0]
        var selectedIcon = existingWorkout?.iconResId ?: android.R.drawable.ic_menu_directions
        var tempRepeatType = existingWorkout?.repeatType ?: "SPECIFIC_DAYS"
        var tempRepeatDays = existingWorkout?.repeatDays?.toMutableList() ?: mutableListOf(0, 1, 2, 3, 4, 5, 6)
        var tempRepeatCount = existingWorkout?.repeatCount ?: 1
        
        fun updateSummary() {
            tvRepeatSummary.text = when (tempRepeatType) {
                "SPECIFIC_DAYS" -> if (tempRepeatDays.size == 7) "Everyday" else "Specific days"
                "WEEKLY" -> "$tempRepeatCount days per week"
                else -> "Everyday"
            }
        }
        updateSummary()

        if (existingWorkout == null) {
            when(DataManager.workoutDefaultMode) {
                "Sets" -> trackingGroup.check(R.id.radio_sets)
                "Timer" -> trackingGroup.check(R.id.radio_timer)
                else -> trackingGroup.check(R.id.radio_reps)
            }
        }

        if (existingWorkout != null) {
            nameInput.setText(existingWorkout.name); targetInput.setText(existingWorkout.target.toString())
            btnSave.text = "Save"
            when (existingWorkout.trackingMode) { "Sets" -> trackingGroup.check(R.id.radio_sets); "Reps" -> trackingGroup.check(R.id.radio_reps); "Timer" -> trackingGroup.check(R.id.radio_timer) }
            when (existingWorkout.frequency) { "Morning" -> updateRadioSelection(frequencyRadios, radioMorning); "Afternoon" -> updateRadioSelection(frequencyRadios, radioAfternoon); "Evening" -> updateRadioSelection(frequencyRadios, radioEvening); else -> updateRadioSelection(frequencyRadios, radioAnytime) }
            iconPreview.setImageResource(selectedIcon); iconPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor); colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }
        dialog.findViewById<View>(R.id.card_workout_icon).setOnClickListener { showIconSelectionDialog { icon -> selectedIcon = icon; iconPreview.setImageResource(selectedIcon) } }
        colorPreview.setOnClickListener { val currentIndex = colors.indexOf(selectedColor); selectedColor = colors[(currentIndex + 1) % colors.size]; iconPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor); colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor) }
        cardRepeat.setOnClickListener { showWorkoutDaysDialog { updateSummary() } }
        btnClose.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val name = nameInput.text.toString()
            if (name.isNotEmpty()) {
                val trackingMode = when (trackingGroup.checkedRadioButtonId) { R.id.radio_sets -> "Sets"; R.id.radio_reps -> "Reps"; else -> "Timer" }
                val frequency = when { radioMorning.isChecked -> "Morning"; radioAfternoon.isChecked -> "Afternoon"; radioEvening.isChecked -> "Evening"; else -> "Anytime" }
                val target = targetInput.text.toString().toIntOrNull() ?: 0
                
                // Final selection check
                val finalSelection = if (selectedMuscleGroups.isEmpty()) listOf("General") else selectedMuscleGroups.toList()
                
                if (existingWorkout == null) workouts.add(Workout(name, false, trackingMode, target, frequency = frequency, color = selectedColor, iconResId = selectedIcon, muscleGroups = finalSelection, repeatType = tempRepeatType, repeatDays = tempRepeatDays.toList(), repeatCount = tempRepeatCount))
                else { existingWorkout.name = name; existingWorkout.target = target; existingWorkout.trackingMode = trackingMode; existingWorkout.frequency = frequency; existingWorkout.color = selectedColor; existingWorkout.iconResId = selectedIcon; existingWorkout.muscleGroups = finalSelection; existingWorkout.repeatType = tempRepeatType; existingWorkout.repeatDays = tempRepeatDays.toList(); existingWorkout.repeatCount = tempRepeatCount }
                workoutAdapter.sortWorkouts(); DataManager.saveData(this); dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showIconSelectionDialog(onSelected: (Int) -> Unit) {
        val icons = listOf(R.drawable.ic_fitness, android.R.drawable.ic_menu_directions, android.R.drawable.ic_menu_upload, android.R.drawable.ic_menu_view, android.R.drawable.ic_menu_myplaces, android.R.drawable.ic_lock_power_off, android.R.drawable.ic_media_play, android.R.drawable.ic_menu_compass)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_picker, null)
        val gridLayout = dialogView.findViewById<GridLayout>(R.id.icon_grid)
        val pickerDialog = AlertDialog.Builder(this).setTitle("Select Icon").setView(dialogView).create()
        icons.forEach { iconRes ->
            val iconView = ImageView(this); val params = GridLayout.LayoutParams(); params.width = 120; params.height = 120; params.setMargins(16, 16, 16, 16); iconView.layoutParams = params; iconView.setImageResource(iconRes); iconView.setPadding(24, 24, 24, 24); iconView.setBackgroundResource(R.drawable.circle_selected_bg); iconView.backgroundTintList = ContextCompat.getColorStateList(this, R.color.chip_background); iconView.imageTintList = ContextCompat.getColorStateList(this, R.color.white); iconView.setOnClickListener { onSelected(iconRes); pickerDialog.dismiss() }; gridLayout.addView(iconView)
        }
        pickerDialog.show()
    }

    private fun showWorkoutDaysDialog(onDismiss: () -> Unit) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_workout_days)
        val btnBack = dialog.findViewById<ImageView>(R.id.btn_back_workout_days)
        val cardSpecific = dialog.findViewById<View>(R.id.card_specific_days_workout)
        val cardWeekly = dialog.findViewById<View>(R.id.card_days_per_week_workout)
        val tvDaysPerWeek = dialog.findViewById<TextView>(R.id.tv_days_per_week_workout)
        val ivCheckSpecific = dialog.findViewById<ImageView>(R.id.iv_check_specific_workout)
        val ivRadioWeek = dialog.findViewById<ImageView>(R.id.iv_radio_week_workout)
        val dayViews = listOf(R.id.day_0_workout, R.id.day_1_workout, R.id.day_2_workout, R.id.day_3_workout, R.id.day_4_workout, R.id.day_5_workout, R.id.day_6_workout).map { dialog.findViewById<TextView>(it) }
        
        var tempRepeatType = "SPECIFIC_DAYS"
        var tempRepeatDays = mutableListOf(0, 1, 2, 3, 4, 5, 6)
        var tempRepeatCount = 1

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

    private fun updateRadioSelection(list: List<RadioButton>, selected: RadioButton) { list.forEach { it.isChecked = false }; selected.isChecked = true }

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

        // Hide unused rows
        view.findViewById<View>(R.id.item_haptics)?.visibility = View.GONE
        view.findViewById<View>(R.id.item_grace_period)?.visibility = View.GONE

        title.text = "Workout Settings"
        
        // Find labels via tags or index safely
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
