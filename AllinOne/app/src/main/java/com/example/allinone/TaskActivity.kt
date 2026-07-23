package com.example.allinone

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : BaseActivity() {

    private val allTasks = DataManager.tasks
    private lateinit var taskAdapter: TaskAdapter
    private var isDeleteMode = false
    private lateinit var gestureDetector: android.view.GestureDetector
    
    private var currentCategoryFilter = "All"
    private var currentSearchQuery = ""
    private var currentSection = DataManager.taskDefaultSection

    private lateinit var navTasks: View
    private lateinit var navTodo: View
    private lateinit var ivTasksIcon: ImageView
    private lateinit var tvTasksLabel: TextView
    private lateinit var ivTodoIcon: ImageView
    private lateinit var tvTodoLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        navTasks = findViewById(R.id.nav_tasks)
        navTodo = findViewById(R.id.nav_todo_list)
        
        ivTasksIcon = findViewById(R.id.iv_tasks_icon)
        tvTasksLabel = findViewById(R.id.tv_tasks_label)
        ivTodoIcon = findViewById(R.id.iv_todo_icon)
        tvTodoLabel = findViewById(R.id.tv_todo_label)

        val taskList = findViewById<RecyclerView>(R.id.task_list)
        taskList.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(allTasks) { 
            DataManager.saveData(this)
        }
        taskAdapter.setShowCompleted(DataManager.taskShowCompleted)
        taskAdapter.setSortOrder(DataManager.taskSortOrder)
        taskList.adapter = taskAdapter

        setupHeader()
        setupFilters()
        setupBottomNavigation()
        applySectionTheme()
        setupSwipeActions(taskList)
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.todo_root_layout), findViewById(R.id.task_content_container))
        updateDynamicBackground()

        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_task)
        btnCreate.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
            val intent = Intent(this, AddTaskActivity::class.java).apply {
                putExtra("SECTION", currentSection)
            }
            startActivity(intent)
        }

        if (intent.getBooleanExtra("SHOW_ADD_DIALOG", false)) {
            val intentAdd = Intent(this, AddTaskActivity::class.java).apply {
                putExtra("SECTION", currentSection)
            }
            startActivity(intentAdd)
        }
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation()
        applyFilters()
        applySectionTheme()
        updateDynamicBackground()
    }

    private fun updateDynamicBackground() {
        val auraView = findViewById<View>(R.id.task_aura_background) ?: return
        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                adjustAlpha(taskColor, 0.4f),
                Color.BLACK
            )
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setupHeader() {
        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(Date())

        findViewById<View>(R.id.btn_back).setOnClickListener {
            if (isDeleteMode) toggleDeleteMode(false) else finish()
        }

        val btnSearch = findViewById<ImageButton>(R.id.btn_task_search)
        val etSearch = findViewById<EditText>(R.id.et_task_search)
        
        btnSearch.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                etSearch.visibility = View.GONE
                etSearch.text.clear()
                currentSearchQuery = ""
                applyFilters()
            } else {
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString()
                applyFilters()
            }
        })

        findViewById<ImageButton>(R.id.btn_task_settings).setOnClickListener {
            if (isDeleteMode) {
                taskAdapter.deleteSelectedTasks(this)
                toggleDeleteMode(false)
            } else {
                showSettingsMenu(it)
            }
        }
    }

    private fun setupFilters() {
        val radioGroup = findViewById<RadioGroup>(R.id.category_filter_group)
        radioGroup.removeAllViews()

        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        val allCategories = mutableListOf("All")
        allCategories.addAll(DataManager.taskCustomCategories)

        allCategories.forEachIndexed { index, category ->
            val rb = RadioButton(this).apply {
                id = index + 1000 
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38f, resources.displayMetrics).toInt()
                val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
                params.setMargins(margin, 0, margin, 0)
                layoutParams = params
                
                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                setPadding(padding, 0, padding, 0)
                
                val darkenedColor = UIUtils.darkenColor(taskColor, 0.5f)

                // Dynamic Background
                val checkedDrawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 19f * resources.displayMetrics.density
                    setColor(darkenedColor)
                }
                val uncheckedDrawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 19f * resources.displayMetrics.density
                    setColor(Color.TRANSPARENT)
                    setStroke(Math.round(1.5f * resources.displayMetrics.density), taskColor)
                }
                val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
                    addState(intArrayOf(), uncheckedDrawable)
                }
                background = stateListDrawable

                buttonDrawable = null
                gravity = Gravity.CENTER
                text = category.uppercase()
                setTextColor(Color.WHITE)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                
                isChecked = currentCategoryFilter == category
            }
            radioGroup.addView(rb)
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRb = group.findViewById<RadioButton>(checkedId)
            if (checkedRb != null) {
                currentCategoryFilter = allCategories[checkedId - 1000]
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        taskAdapter.filter(currentCategoryFilter, currentSearchQuery)
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (DataManager.taskVisibleSections.size > 1) {
                    val currentIndex = DataManager.taskVisibleSections.indexOf(currentSection)
                    val nextIndex = (currentIndex + 1) % DataManager.taskVisibleSections.size
                    switchSection(DataManager.taskVisibleSections[nextIndex])
                }
            }

            override fun onSwipeRight() {
                if (DataManager.taskVisibleSections.size > 1) {
                    val currentIndex = DataManager.taskVisibleSections.indexOf(currentSection)
                    val prevIndex = if (currentIndex <= 0) DataManager.taskVisibleSections.size - 1 else currentIndex - 1
                    switchSection(DataManager.taskVisibleSections[prevIndex])
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun setupSwipeActions(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTaskAt(position) ?: return
                
                if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe Right -> Complete
                    task.isCompleted = true
                    taskAdapter.updateDisplayList()
                    DataManager.saveData(this@TaskActivity)
                } else {
                    // Swipe Left -> Delete
                    allTasks.remove(task)
                    taskAdapter.updateDisplayList()
                    DataManager.saveData(this@TaskActivity)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    private fun showSettingsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // Clear Completed
        menuView.findViewById<View>(R.id.menu_clear_completed).setOnClickListener {
            val completedCount = allTasks.count { it.isCompleted }
            if (completedCount > 0) {
                allTasks.removeAll { it.isCompleted }
                taskAdapter.updateDisplayList()
                DataManager.saveData(this)
                Toast.makeText(this, "Cleared $completedCount completed tasks", Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }

        // Toggle Show/Hide Completed
        val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        tvToggle.text = if (DataManager.taskShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
        ivToggle.setImageResource(if (DataManager.taskShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        menuView.findViewById<View>(R.id.menu_toggle_completed).setOnClickListener {
            DataManager.taskShowCompleted = !DataManager.taskShowCompleted
            taskAdapter.setShowCompleted(DataManager.taskShowCompleted)
            DataManager.saveData(this)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener { 
            showAdvancedSettingsDialog()
            popupWindow.dismiss() 
        }
        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun showAdvancedSettingsDialog() {
        startActivity(Intent(this, TaskSettingsActivity::class.java))
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        taskAdapter.setDeleteMode(enabled)
        val btnSettings = findViewById<ImageButton>(R.id.btn_task_settings)
        btnSettings.setImageResource(if (enabled) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
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

    private fun setupBottomNavigation() {
        val footer = findViewById<LinearLayout>(R.id.bottom_navigation_tasks)

        // 1. Remove all to prepare for re-ordering
        footer.removeAllViews()

        // 2. Add back in the order specified by DataManager.taskVisibleSections
        DataManager.taskVisibleSections.forEach { section ->
            val viewToAdd = when (section) {
                "Tasks" -> navTasks
                "List" -> navTodo
                else -> null
            }
            viewToAdd?.let {
                if (it.parent != null) (it.parent as ViewGroup).removeView(it)
                footer.addView(it)
            }
        }

        // 3. Auto-switch to default if current is hidden
        if (!DataManager.taskVisibleSections.contains(currentSection)) {
            currentSection = DataManager.taskDefaultSection
        }

        // Show/Hide based on settings (re-redundant if using addView, but keeps visibility logic)
        navTasks.visibility = if (DataManager.taskVisibleSections.contains("Tasks")) View.VISIBLE else View.GONE
        navTodo.visibility = if (DataManager.taskVisibleSections.contains("List")) View.VISIBLE else View.GONE

        // Dynamic Visibility: Hide the entire footer if only one section is enabled
        if (DataManager.taskVisibleSections.size > 1) {
            footer.visibility = View.VISIBLE
        } else {
            footer.visibility = View.GONE
            val onlyVisible = DataManager.taskVisibleSections.firstOrNull() ?: "Tasks"
            if (currentSection != onlyVisible) switchSection(onlyVisible)
        }

        if (!DataManager.taskVisibleSections.contains(currentSection)) {
            val firstVisible = DataManager.taskVisibleSections.firstOrNull() ?: "Tasks"
            switchSection(firstVisible)
        }

        navTasks.setOnClickListener { switchSection("Tasks") }
        navTodo.setOnClickListener { switchSection("List") }
        
        updateNavUI()
    }

    private fun switchSection(section: String) {
        if (section == currentSection && DataManager.taskVisibleSections.size <= 1) return

        val root = findViewById<ViewGroup>(R.id.task_content_container)
        androidx.transition.TransitionManager.beginDelayedTransition(root, androidx.transition.AutoTransition())
        
        val sections = DataManager.taskVisibleSections.toMutableList()

        if (section == currentSection) {
            // Double Click: Reset to original order
            val originalOrder = listOf("Tasks", "List")
            val resetOrder = originalOrder.filter { sections.contains(it) }
            
            DataManager.taskVisibleSections.clear()
            DataManager.taskVisibleSections.addAll(resetOrder)
        } else {
            // Reorder: Move current section to the first position
            if (sections.contains(section)) {
                sections.remove(section)
                sections.add(0, section)
                DataManager.taskVisibleSections.clear()
                DataManager.taskVisibleSections.addAll(sections)
            }
        }

        currentSection = section
        setupBottomNavigation() // Redraw footer with new order
        taskAdapter.setSection(section)
        findViewById<TextView>(R.id.tv_title).text = section.uppercase()
        updateNavUI()
    }

    private fun updateNavUI() {
        val navs = mapOf(
            "Tasks" to Pair(ivTasksIcon, tvTasksLabel),
            "List" to Pair(ivTodoIcon, tvTodoLabel)
        )

        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)

        navs.forEach { (sec, views) ->
            val isActive = sec == currentSection
            val color = if (isActive) taskColor else inactiveColor
            
            views.first.setColorFilter(color)
            views.second.setTextColor(color)
        }
    }

    private fun applySectionTheme() {
        // Refresh filters to pick up color change
        setupFilters()
        
        // Sync Nav UI
        updateNavUI()

        // Synchronize FAB color (50% darker)
        val taskColor = if (DataManager.globalTaskColor != -1) DataManager.globalTaskColor else Color.parseColor("#2EC4B6")
        val darkenedFabColor = UIUtils.darkenColor(taskColor, 0.5f)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_task).backgroundTintList = 
            android.content.res.ColorStateList.valueOf(darkenedFabColor)
    }


    private fun showTaskAnalyticsDialog() {
        val total = allTasks.size
        val completed = allTasks.count { it.isCompleted }
        val pending = total - completed
        val highPriorityPending = allTasks.count { !it.isCompleted && it.priority == 2 }
        val completionRate = if (total > 0) (completed * 100) / total else 0
        
        val message = """
            Total Tasks: $total
            Completed: $completed
            Pending: $pending
            
            Completion Rate: $completionRate%
            Urgent Tasks Pending: $highPriorityPending
        """.trimIndent()

        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_analytics_simple, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        view.findViewById<TextView>(R.id.tv_analytics_content).text = message
        view.findViewById<View>(R.id.btn_close_analytics).setOnClickListener { dialog.dismiss() }

        showDialogSafe(dialog)
    }


}
