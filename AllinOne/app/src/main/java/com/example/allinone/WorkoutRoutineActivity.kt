package com.example.allinone

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutSettings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WorkoutRoutineActivity : BaseActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    
    private lateinit var progressSection: WorkoutProgressSection
    private lateinit var calendarSection: WorkoutCalendarSection
    private lateinit var filterSection: WorkoutFilterSection
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
                val workout = viewModel.workouts.value[viewModel.currentlyTimingWorkoutPosition]
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                
                val updatedWorkout = workout.copy(
                    isCompleted = true,
                    progress = workout.target,
                    dailyProgress = workout.dailyProgress.toMutableMap().apply { put(today, 100) },
                    completedDates = workout.completedDates.toMutableList().apply { 
                        if (!contains(today)) add(today) 
                    }
                )
                
                if (!workout.completedDates.contains(today)) {
                    viewModel.addActivity("Finished Workout: ${workout.name}")
                    viewModel.addXP(25)
                }
                
                viewModel.updateWorkout(updatedWorkout)
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

        filterSection = WorkoutFilterSection(findViewById(R.id.filter_chips)) { filter ->
            viewModel.selectedTimeFilter = filter
            applyFilters()
        }

        listSection = WorkoutListSection(
            this,
            findViewById(R.id.workout_list),
            viewModel,
            onTimerStart = { workout, position -> startTimerForWorkout(workout, position) }
        ) {
            updateAllUI()
        }

        navigationSection = WorkoutNavigationSection(
            this,
            findViewById(R.id.workout_content_container),
            findViewById(R.id.today_layout),
            findViewById(R.id.history_layout),
            findViewById(R.id.history_compose_view)
        ) { tab ->
            viewModel.currentTab = tab
            if (tab == "HISTORY") {
                historyGridSection.setup(viewModel.currentGridCalendar)
                performanceSection.update(viewModel.currentlySelectedHistoryDate)
                updateHistoryUI()
            }
        }

        themeManager = WorkoutThemeManager(
            this,
            findViewById(R.id.workout_aura_background),
            findViewById(R.id.filter_chips),
            findViewById(R.id.btn_create_new_workout),
            findViewById(R.id.section_progress_bar),
            historyCards = listOf(
                findViewById(R.id.history_card_streak),
                findViewById(R.id.history_card_workouts),
                findViewById(R.id.history_card_efficiency)
            ),
            historyValues = listOf(
                findViewById(R.id.history_current_streak),
                findViewById(R.id.history_workouts_finished),
                findViewById(R.id.history_efficiency)
            )
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
        setupKeyboardHandling(findViewById(R.id.workout_root_layout), findViewById(R.id.workout_content_container))
        setupBackNavigation()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.workouts.collect { newList ->
                        listSection.updateWorkouts(newList)
                        applyFilters()
                        updateAllUI()
                    }
                }
                launch {
                    viewModel.workoutSettings.collect { settings ->
                        listSection.updateSettings(settings)
                        setupDynamicFilterChips(settings)
                    }
                }
            }
        }

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
        updateHistoryUI()
        if (viewModel.currentTab == "HISTORY") {
            historyGridSection.setup(viewModel.currentGridCalendar)
            performanceSection.update(viewModel.currentlySelectedHistoryDate)
        }
    }

    private fun updateHistoryUI() {
        val streaks = viewModel.getStreaks()
        findViewById<TextView>(R.id.history_current_streak)?.text = streaks.first.toString()
        findViewById<TextView>(R.id.history_best_streak)?.text = "Best Streak: ${streaks.second}"
        
        findViewById<TextView>(R.id.history_workouts_finished)?.text = viewModel.getTotalFinished().toString()
        findViewById<TextView>(R.id.history_workouts_this_month)?.text = "This month: ${viewModel.getWorkoutsThisMonth()}"
        
        findViewById<TextView>(R.id.history_efficiency)?.text = "${viewModel.getGlobalCompletionRate()}%"
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
            val current = viewModel.workoutSettings.value
            viewModel.updateSettings(current.copy(showCompleted = !current.showCompleted))
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

    private fun setupDynamicFilterChips(settings: WorkoutSettings) {
        val filterGroup = findViewById<RadioGroup>(R.id.filter_chips)
        // Keep ALL chip, remove others
        val allChip = findViewById<View>(R.id.chip_all)
        filterGroup.removeAllViews()
        filterGroup.addView(allChip)

        if (settings.filterType == "TIME") {
            addFilterChip(filterGroup, "MORNING")
            addFilterChip(filterGroup, "AFTERNOON")
            addFilterChip(filterGroup, "EVENING")
        } else {
            settings.muscleGroups.forEach { muscle ->
                addFilterChip(filterGroup, muscle.uppercase())
            }
        }
    }

    private fun addFilterChip(group: RadioGroup, label: String) {
        val rb = RadioButton(this)
        val params = RadioGroup.LayoutParams((80 * resources.displayMetrics.density).toInt(), (38 * resources.displayMetrics.density).toInt())
        params.setMargins((2 * resources.displayMetrics.density).toInt(), (2 * resources.displayMetrics.density).toInt(), (2 * resources.displayMetrics.density).toInt(), (2 * resources.displayMetrics.density).toInt())
        rb.layoutParams = params
        rb.background = ContextCompat.getDrawable(this, R.drawable.filter_chip_bg)
        rb.buttonDrawable = null
        rb.text = label
        rb.gravity = android.view.Gravity.CENTER
        rb.setTextColor(Color.WHITE)
        rb.textSize = 10f
        rb.typeface = android.graphics.Typeface.DEFAULT_BOLD
        rb.id = View.generateViewId()
        group.addView(rb)
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

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.currentTab == "HISTORY") {
                    navigationSection.switchTab("TODAY")
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
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
        updateHistoryUI()
        navigationSection.updateNavUI(viewModel.currentTab)
    }

    override fun onStop() {
        super.onStop()
        listSection.workoutAdapter.collapseAll()
    }
}
