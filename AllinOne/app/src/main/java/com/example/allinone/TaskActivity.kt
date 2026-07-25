package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskActivity : BaseActivity() {

    private val viewModel: TaskViewModel by viewModels()
    
    private lateinit var listSection: TaskListSection
    private lateinit var headerSection: TaskHeaderSection
    private lateinit var filterSection: TaskFilterSection
    private lateinit var navigationSection: TaskNavigationSection
    private lateinit var themeManager: TaskThemeManager
    private lateinit var gestureDetector: android.view.GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        initSections()
        setupLogic()

        if (intent.getBooleanExtra("SHOW_ADD_DIALOG", false)) {
            startActivity(Intent(this, AddTaskActivity::class.java).apply {
                putExtra("SECTION", viewModel.currentSection)
            })
        }
    }

    private fun initSections() {
        listSection = TaskListSection(this, findViewById(R.id.task_list)) {
            // Callback for data changes if needed
        }

        headerSection = TaskHeaderSection(
            findViewById(R.id.todo_root_layout),
            onBack = { if (viewModel.isDeleteMode) toggleDeleteMode(false) else finish() },
            onSearchChanged = { query ->
                viewModel.currentSearchQuery = query
                applyFilters()
            },
            onSettingsClicked = { anchor ->
                if (viewModel.isDeleteMode) {
                    listSection.deleteSelectedTasks()
                    toggleDeleteMode(false)
                } else {
                    showSettingsMenu(anchor)
                }
            }
        )

        filterSection = TaskFilterSection(this, findViewById(R.id.category_filter_group)) { filter ->
            viewModel.currentCategoryFilter = filter
            applyFilters()
        }

        navigationSection = TaskNavigationSection(
            findViewById(R.id.bottom_navigation_tasks),
            findViewById(R.id.nav_tasks),
            findViewById(R.id.nav_todo_list),
            findViewById(R.id.iv_tasks_icon),
            findViewById(R.id.tv_tasks_label),
            findViewById(R.id.iv_todo_icon),
            findViewById(R.id.tv_todo_label)
        ) { section ->
            viewModel.currentSection = section
            listSection.setSection(section)
            findViewById<TextView>(R.id.tv_title).text = section.uppercase()
            navigationSection.setup(section)
            navigationSection.updateNavUI(section)
        }

        themeManager = TaskThemeManager(
            this,
            findViewById(R.id.task_aura_background),
            findViewById(R.id.btn_create_new_task)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        filterSection.setup(viewModel.currentCategoryFilter)
        navigationSection.setup(viewModel.currentSection)
        themeManager.applyTheme()
        listSection.setSection(viewModel.currentSection)
        findViewById<TextView>(R.id.tv_title).text = viewModel.currentSection.uppercase()
        
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.todo_root_layout), findViewById(R.id.task_content_container))

        findViewById<FloatingActionButton>(R.id.btn_create_new_task).setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
            startActivity(Intent(this, AddTaskActivity::class.java).apply {
                putExtra("SECTION", viewModel.currentSection)
            })
        }
    }

    private fun applyFilters() {
        listSection.applyFilters(viewModel.currentCategoryFilter, viewModel.currentSearchQuery)
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        viewModel.isDeleteMode = enabled
        listSection.setDeleteMode(enabled)
        headerSection.setSettingsIcon(if (enabled) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
    }

    private fun showSettingsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_task, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_clear_completed).setOnClickListener {
            val completedCount = DataManager.tasks.count { it.isCompleted }
            if (completedCount > 0) {
                DataManager.tasks.removeAll { it.isCompleted }
                listSection.taskAdapter.updateDisplayList()
                DataManager.saveData(this)
                Toast.makeText(this, "Cleared $completedCount completed tasks", Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }

        val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        tvToggle.text = if (DataManager.taskShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
        ivToggle.setImageResource(if (DataManager.taskShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        menuView.findViewById<View>(R.id.menu_toggle_completed).setOnClickListener {
            DataManager.taskShowCompleted = !DataManager.taskShowCompleted
            listSection.taskAdapter.setShowCompleted(DataManager.taskShowCompleted)
            DataManager.saveData(this)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener { 
            startActivity(Intent(this, TaskSettingsActivity::class.java))
            popupWindow.dismiss() 
        }
        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (DataManager.taskVisibleSections.size > 1) {
                    val currentIndex = DataManager.taskVisibleSections.indexOf(viewModel.currentSection)
                    val nextIndex = (currentIndex + 1) % DataManager.taskVisibleSections.size
                    navigationSection.setup(DataManager.taskVisibleSections[nextIndex]) // Should trigger callback
                }
            }
            override fun onSwipeRight() {
                if (DataManager.taskVisibleSections.size > 1) {
                    val currentIndex = DataManager.taskVisibleSections.indexOf(viewModel.currentSection)
                    val prevIndex = if (currentIndex <= 0) DataManager.taskVisibleSections.size - 1 else currentIndex - 1
                    navigationSection.setup(DataManager.taskVisibleSections[prevIndex])
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
        navigationSection.setup(viewModel.currentSection)
        applyFilters()
        themeManager.applyTheme()
    }
}
