package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HabitTrackerActivity : BaseActivity() {

    private val viewModel: HabitTrackerViewModel by viewModels()
    
    private lateinit var progressSection: HabitProgressSection
    private lateinit var calendarSection: HabitCalendarSection
    private lateinit var filterSection: HabitFilterSection
    private lateinit var listSection: HabitListSection
    private lateinit var navigationSection: HabitNavigationSection
    private lateinit var themeManager: HabitThemeManager
    private lateinit var historyGridSection: HabitHistoryGridSection
    private lateinit var performanceSection: HabitPerformanceSection
    private lateinit var composeHandler: HabitHistoryComposeHandler
    private lateinit var gestureDetector: android.view.GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_tracker)

        initSections()
        setupLogic()
        
        if (DataManager.habitDefaultTab == "HISTORY") {
            navigationSection.switchTab("HISTORY")
        }
    }

    private fun initSections() {
        progressSection = HabitProgressSection(
            findViewById(R.id.section_progress_bar),
            findViewById(R.id.tv_section_progress_percentage)
        )

        calendarSection = HabitCalendarSection(
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

        listSection = HabitListSection(
            this,
            findViewById(R.id.habit_list),
            findViewById(R.id.btn_create_new_habit)
        ) {
            updateAllUI()
        }

        navigationSection = HabitNavigationSection(
            this,
            findViewById(R.id.habit_content_container),
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

        themeManager = HabitThemeManager(
            this,
            findViewById(R.id.habit_aura_background),
            listOf(
                findViewById(R.id.chip_all),
                findViewById(R.id.chip_morning),
                findViewById(R.id.chip_afternoon),
                findViewById(R.id.chip_evening)
            ),
            findViewById(R.id.btn_create_new_habit),
            findViewById(R.id.section_progress_bar)
        )

        historyGridSection = HabitHistoryGridSection(
            this,
            findViewById(R.id.history_dynamic_grid),
            findViewById(R.id.tv_grid_month)
        ) { date ->
            viewModel.currentlySelectedHistoryDate = date
            historyGridSection.setSelectedDate(date)
            historyGridSection.setup(viewModel.currentGridCalendar)
            performanceSection.update(date)
        }

        performanceSection = HabitPerformanceSection(findViewById(R.id.history_layout))

        composeHandler = HabitHistoryComposeHandler(findViewById(R.id.history_compose_view)) {
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
        setupKeyboardHandling(findViewById(R.id.habit_tracker_root))

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back_history).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_habit_settings).setOnClickListener { showSettingsPopup(it) }
    }

    private fun applyFilters() {
        listSection.applyFilter(
            viewModel.selectedTimeFilter,
            viewModel.getDayIndex(viewModel.selectedDateString),
            viewModel.selectedDateString
        )
    }

    private fun updateAllUI() {
        progressSection.update()
        if (viewModel.currentTab == "HISTORY") {
            historyGridSection.setup(viewModel.currentGridCalendar)
            performanceSection.update(viewModel.currentlySelectedHistoryDate)
        }
    }

    private fun showSettingsPopup(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_habit, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val menuToggle = menuView.findViewById<View>(R.id.menu_toggle_completed)
        val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        menuToggle.visibility = View.VISIBLE
        tvToggle.text = if (DataManager.habitShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
        ivToggle.setImageResource(if (DataManager.habitShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        menuToggle.setOnClickListener {
            DataManager.habitShowCompleted = !DataManager.habitShowCompleted
            listSection.setShowCompleted(DataManager.habitShowCompleted)
            DataManager.saveData(this)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_action_primary).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                navigationSection.switchTab("HISTORY")
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            startActivity(Intent(this, HabitSettingsActivity::class.java))
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
