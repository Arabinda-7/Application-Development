package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.allinone.data.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
        observeViewModel()

        if (intent.getBooleanExtra("SHOW_ADD_DIALOG", false)) {
            startActivity(Intent(this, AddTaskActivity::class.java).apply {
                putExtra("SECTION", viewModel.currentSection.value)
            })
        }
    }

    private fun initSections() {
        listSection = TaskListSection(this, findViewById(R.id.task_list)) {
            // Callback for data changes if needed
        }

        headerSection = TaskHeaderSection(
            findViewById(R.id.todo_root_layout),
            onBack = { if (viewModel.isDeleteMode.value) toggleDeleteMode(false) else finish() },
            onSearchChanged = { query ->
                viewModel.setSearchQuery(query)
            },
            onSettingsClicked = { anchor ->
                if (viewModel.isDeleteMode.value) {
                    listSection.deleteSelectedTasks()
                    toggleDeleteMode(false)
                } else {
                    showSettingsMenu(anchor)
                }
            }
        )

        filterSection = TaskFilterSection(this, findViewById(R.id.category_filter_group)) { filter ->
            viewModel.setCategoryFilter(filter)
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
            viewModel.setSection(section)
        }

        themeManager = TaskThemeManager(
            this,
            findViewById(R.id.task_aura_background),
            findViewById(R.id.btn_create_new_task)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        themeManager.applyTheme()
        
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.todo_root_layout), findViewById(R.id.task_content_container))

        findViewById<FloatingActionButton>(R.id.btn_create_new_task).setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                checkAndRequestPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
                    openAddTask()
                }
            } else {
                openAddTask()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { tasks ->
                        listSection.taskAdapter.updateTasks(tasks)
                    }
                }
                launch {
                    viewModel.currentSection.collect { section ->
                        findViewById<TextView>(R.id.tv_title).text = section.uppercase()
                        navigationSection.setup(section)
                        navigationSection.updateNavUI(section)
                    }
                }
                launch {
                    viewModel.currentCategoryFilter.collect { filter ->
                        filterSection.setup(filter)
                    }
                }
            }
        }
    }

    private fun openAddTask() {
        startActivity(Intent(this, AddTaskActivity::class.java).apply {
            putExtra("SECTION", viewModel.currentSection.value)
        })
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        viewModel.isDeleteMode.value = enabled
        listSection.setDeleteMode(enabled)
        headerSection.setSettingsIcon(if (enabled) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
    }

    private fun showSettingsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Clear Completed").setOnMenuItemClickListener {
            viewModel.clearCompletedTasks()
            true
        }
        popup.menu.add("Settings").setOnMenuItemClickListener {
            startActivity(Intent(this, TaskSettingsActivity::class.java))
            true
        }
        popup.show()
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                val orders = listOf("Tasks", "List") // Mock sections
                val currentIndex = orders.indexOf(viewModel.currentSection.value)
                val nextIndex = (currentIndex + 1) % orders.size
                viewModel.setSection(orders[nextIndex])
            }
            override fun onSwipeRight() {
                val orders = listOf("Tasks", "List")
                val currentIndex = orders.indexOf(viewModel.currentSection.value)
                val prevIndex = if (currentIndex <= 0) orders.size - 1 else currentIndex - 1
                viewModel.setSection(orders[prevIndex])
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        themeManager.applyTheme()
    }
}
