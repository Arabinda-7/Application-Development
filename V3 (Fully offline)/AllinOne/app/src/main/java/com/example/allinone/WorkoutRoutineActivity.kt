package com.example.allinone

import android.app.Activity
import android.content.Intent
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
import com.example.allinone.ui.workout.WorkoutUiHandler
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
            viewModel.completeWorkoutWithTimer(viewModel.currentlyTimingWorkoutPosition)
            viewModel.currentlyTimingWorkoutPosition = -1
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
                        WorkoutUiHandler.setupDynamicFilterChips(this@WorkoutRoutineActivity, findViewById(R.id.filter_chips), settings)
                    }
                }
                launch {
                    viewModel.stats.collect { stats ->
                        updateHistoryUI(stats)
                    }
                }
            }
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back_history).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_workout_settings).setOnClickListener { 
            WorkoutUiHandler.showSettingsPopup(this, it, viewModel) {
                navigationSection.switchTab("HISTORY")
            }
        }

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

    private fun updateHistoryUI(stats: WorkoutStats) {
        findViewById<TextView>(R.id.history_current_streak)?.text = stats.streaks.first.toString()
        findViewById<TextView>(R.id.history_best_streak)?.text = "Best Streak: ${stats.streaks.second}"
        
        findViewById<TextView>(R.id.history_workouts_finished)?.text = stats.totalFinished.toString()
        findViewById<TextView>(R.id.history_workouts_this_month)?.text = "This month: ${stats.workoutsThisMonth}"
        
        findViewById<TextView>(R.id.history_efficiency)?.text = "${stats.completionRate}%"
    }

    private fun startTimerForWorkout(workout: Workout, position: Int) {
        viewModel.currentlyTimingWorkoutPosition = position
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("WORKOUT_NAME", workout.name)
            putExtra("TIMER_DURATION", workout.target)
        }
        timerActivityResultLauncher.launch(intent)
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
        navigationSection.updateNavUI(viewModel.currentTab)
    }

    override fun onStop() {
        super.onStop()
        listSection.workoutAdapter.collapseAll()
    }
}
