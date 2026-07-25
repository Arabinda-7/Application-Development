package com.example.allinone

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRoutineActivity : BaseActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    
    private lateinit var progressSection: WorkoutProgressSection
    private lateinit var calendarSection: WorkoutCalendarSection
    private lateinit var filterSection: HabitFilterSection // Re-using FilterSection as logic is same
    private lateinit var listSection: WorkoutListSection
    private lateinit var navigationSection: WorkoutNavigationSection
    private lateinit var themeManager: WorkoutThemeManager
    private lateinit var historyGridSection: WorkoutHistoryGridSection
    private lateinit var performanceSection: WorkoutPerformanceSection
    private lateinit var composeHandler: WorkoutHistoryComposeHandler
    private lateinit var gestureDetector: android.view.GestureDetector

    private val timerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (viewModel.currentlyTimingWorkoutPosition != -1) {
                val workout = DataManager.workouts[viewModel.currentlyTimingWorkoutPosition]
                workout.isCompleted = true
                workout.progress = workout.target
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                if (!workout.completedDates.contains(today)) {
                    workout.completedDates.add(today)
                    DataManager.addActivity("Finished Workout: ${workout.name}")
                    if (DataManager.addXP(this, 25)) {
                        android.widget.Toast.makeText(this, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                listSection.workoutAdapter.sortWorkouts()
                DataManager.saveData(this)
                viewModel.currentlyTimingWorkoutPosition = -1
                updateHistoryUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_routine)

        initSections()
        setupLogic()
    }

    private fun initSections() {
        progressSection = WorkoutProgressSection(
            findViewById(R.id.section_progress_bar),
            findViewById(R.id.tv_section_progress_percentage)
        )

        calendarSection = WorkoutCalendarSection(
            findViewById(R.id.vp_calendar),
            findViewById(R.id.tv_date)
        ) { date ->
            viewModel.selectedDateString = date
            applyFilters()
        }

        filterSection = HabitFilterSection(findViewById(R.id.filter_chips)) { filter ->
            viewModel.selectedTimeFilter = filter
            applyFilters()
        }

        listSection = WorkoutListSection(
            this,
            findViewById(R.id.workout_list),
            onTimerStart = { workout, position -> startTimerForWorkout(workout, position) }
        ) {
            updateAllUI()
        }

        navigationSection = WorkoutNavigationSection(
            this,
            findViewById(R.id.workout_content_container),
            findViewById(R.id.today_layout),
            findViewById(R.id.history_layout),
            findViewById(R.id.history_compose_view),
            findViewById(R.id.iv_today),
            findViewById(R.id.tv_today_nav),
            findViewById(R.id.iv_history),
            findViewById(R.id.tv_history_nav)
        ) { tab ->
            viewModel.currentTab = tab
            if (tab == "HISTORY") {
                historyGridSection.setup(viewModel.currentGridCalendar)
                performanceSection.update(viewModel.currentlySelectedHistoryDate)
            }
        }

        themeManager = WorkoutThemeManager(
            this,
            findViewById(R.id.workout_aura_background),
            listOf(
                findViewById(R.id.chip_all),
                findViewById(R.id.chip_morning),
                findViewById(R.id.chip_afternoon),
                findViewById(R.id.chip_evening)
            ),
            findViewById(R.id.btn_create_new_workout),
            findViewById(R.id.section_progress_bar)
        )

        historyGridSection = WorkoutHistoryGridSection(
            this,
            findViewById(R.id.history_dynamic_grid),
            findViewById(R.id.tv_grid_month)
        ) { date ->
            viewModel.currentlySelectedHistoryDate = date
            historyGridSection.setSelectedDate(date)
            historyGridSection.setup(viewModel.currentGridCalendar)
            performanceSection.update(date)
        }

        performanceSection = WorkoutPerformanceSection(findViewById(R.id.history_layout))

        composeHandler = WorkoutHistoryComposeHandler(findViewById(R.id.history_compose_view)) {
            navigationSection.switchTab("TODAY")
        }
    }

    private fun setupLogic() {
        calendarSection.setup()
        filterSection.setup()
        navigationSection.setup()
        themeManager.applyTheme()
        progressSection.update()
        composeHandler.setup()
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.workout_root_layout))

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back_history).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_workout_settings).setOnClickListener { showSettingsPopup(it) }

        findViewById<View>(R.id.btn_create_new_workout).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
        }
    }

    private fun applyFilters() {
        val calendar = Calendar.getInstance()
        try { 
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(viewModel.selectedDateString)?.let { calendar.time = it } 
        } catch (e: Exception) {}
        val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) - 1)
        listSection.applyFilter(viewModel.selectedTimeFilter, dayIndex, viewModel.selectedDateString)
    }

    private fun updateAllUI() {
        progressSection.update()
        if (viewModel.currentTab == "HISTORY") {
            historyGridSection.setup(viewModel.currentGridCalendar)
            performanceSection.update(viewModel.currentlySelectedHistoryDate)
        }
    }

    private fun updateHistoryUI() {
        findViewById<TextView>(R.id.history_current_streak)?.text = DataManager.getWorkoutStreak().toString()
        findViewById<TextView>(R.id.history_workouts_finished)?.text = DataManager.getTotalWorkoutsFinished().toString()
        findViewById<TextView>(R.id.history_efficiency)?.text = "${DataManager.getGlobalCompletionRate("WORKOUTS")}%"
        updateAllUI()
    }

    private fun startTimerForWorkout(workout: Workout, position: Int) {
        viewModel.currentlyTimingWorkoutPosition = position
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("WORKOUT_NAME", workout.name)
            putExtra("TIMER_DURATION", workout.target)
        }
        timerActivityResultLauncher.launch(intent)
    }

    private fun showSettingsPopup(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_workout, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val menuToggle = menuView.findViewById<View>(R.id.menu_toggle_completed)
        val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        menuToggle.visibility = View.VISIBLE
        tvToggle.text = if (DataManager.workoutShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
        ivToggle.setImageResource(if (DataManager.workoutShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        menuToggle.setOnClickListener {
            DataManager.workoutShowCompleted = !DataManager.workoutShowCompleted
            listSection.workoutAdapter.setShowCompleted(DataManager.workoutShowCompleted)
            DataManager.saveData(this)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_action_primary).apply {
            visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_action_primary).text = "HISTORY"
            findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_history)
            setOnClickListener {
                navigationSection.switchTab("HISTORY")
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            startActivity(Intent(this, WorkoutSettingsActivity::class.java))
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (viewModel.currentTab == "TODAY") navigationSection.switchTab("HISTORY")
            }
            override fun onSwipeRight() {
                if (viewModel.currentTab == "HISTORY") navigationSection.switchTab("TODAY")
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        applyFilters()
        themeManager.applyTheme()
        progressSection.update()
        navigationSection.updateNavUI(viewModel.currentTab)
    }
}
