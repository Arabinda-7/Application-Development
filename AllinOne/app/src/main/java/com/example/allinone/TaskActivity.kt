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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.todo_root_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation_tasks)) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBars.bottom)
            insets
        }

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
        setupSwipeActions(taskList)
        setupGestureDetector()

        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_task)
        if (DataManager.taskAddThemeColor != -1) {
            btnCreate.backgroundTintList = android.content.res.ColorStateList.valueOf(DataManager.taskAddThemeColor)
        }
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
        applyFilters()
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

        val allCategories = mutableListOf("All")
        allCategories.addAll(DataManager.taskCustomCategories)

        allCategories.forEachIndexed { index, category ->
            val rb = RadioButton(this).apply {
                id = index + 1000 // Unique ID for RadioGroup management
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, resources.displayMetrics).toInt()
                val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
                params.setMargins(margin, 0, margin, 0)
                layoutParams = params
                
                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                setPadding(padding, 0, padding, 0)
                
                background = ContextCompat.getDrawable(this@TaskActivity, R.drawable.filter_chip_bg)
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

    fun showAddTaskDialog(existingTask: Task? = null) {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etName = view.findViewById<EditText>(R.id.task_name_input)
        val tvNameHint = view.findViewById<TextView>(R.id.tv_name_hint)
        val rgPriority = view.findViewById<RadioGroup>(R.id.rg_priority)
        val chipGroupCat = view.findViewById<ChipGroup>(R.id.category_chip_group)
        val containerSubtasks = view.findViewById<LinearLayout>(R.id.container_subtasks)
        val etNewSubtask = view.findViewById<EditText>(R.id.et_new_subtask)
        val btnAddSubtask = view.findViewById<ImageButton>(R.id.btn_add_subtask)
        val tvReminder = view.findViewById<TextView>(R.id.tv_reminder_summary)
        val btnSave = view.findViewById<TextView>(R.id.btn_save_task)

        fun validateInputs() {
            val name = etName.text.toString().trim()
            val isValid = name.isNotEmpty()
            
            btnSave.alpha = if (isValid) 1.0f else 0.3f
            btnSave.isEnabled = isValid
            
            tvNameHint.visibility = if (isValid) View.GONE else View.VISIBLE
            if (!isValid) startPulseAnimation(tvNameHint)
            
            val themeColor = if (DataManager.taskAddThemeColor != -1) DataManager.taskAddThemeColor else ContextCompat.getColor(this, R.color.primary_blue)
            if (isValid) btnSave.setTextColor(themeColor) else btnSave.setTextColor(Color.GRAY)
            tvNameHint.setTextColor(themeColor)
        }

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Setup Category Chips
        val categories = DataManager.taskCustomCategories
        var selectedCategory = existingTask?.category ?: "General"

        categories.forEach { cat ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = cat
            chip.isCheckable = true
            chip.isChecked = (cat == selectedCategory)
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(Color.WHITE)
            chip.setOnCheckedChangeListener { _, isChecked -> if (isChecked) selectedCategory = cat }
            chipGroupCat.addView(chip)
        }

        var selectedPriority = existingTask?.priority ?: 0
        var selectedReminder: Long? = existingTask?.reminderTime
        val tempSubtasks = existingTask?.subtasks?.toMutableList() ?: mutableListOf()

        // Initial UI State
        existingTask?.let {
            etName.setText(it.name)
            when (it.priority) {
                0 -> rgPriority.check(R.id.rb_priority_low)
                1 -> rgPriority.check(R.id.rb_priority_medium)
                2 -> rgPriority.check(R.id.rb_priority_high)
            }
            updateReminderUI(tvReminder, selectedReminder)
            updatePriorityAlpha(view, when(it.priority) {
                1 -> R.id.rb_priority_medium
                2 -> R.id.rb_priority_high
                else -> R.id.rb_priority_low
            })
        }
        renderSubtasks(containerSubtasks, tempSubtasks)
        validateInputs()

        // Listeners
        rgPriority.setOnCheckedChangeListener { _, id ->
            selectedPriority = when (id) {
                R.id.rb_priority_medium -> 1
                R.id.rb_priority_high -> 2
                else -> 0
            }
            updatePriorityAlpha(view, id)
        }

        btnAddSubtask.setOnClickListener {
            val subName = etNewSubtask.text.toString()
            if (subName.isNotEmpty()) {
                tempSubtasks.add(Subtask(subName))
                renderSubtasks(containerSubtasks, tempSubtasks)
                etNewSubtask.text.clear()
            }
        }

        view.findViewById<View>(R.id.btn_set_reminder).setOnClickListener {
            showReminderPicker { time ->
                selectedReminder = time
                updateReminderUI(tvReminder, selectedReminder)
            }
        }

        if (existingTask != null) {
            btnSave.text = "Update"
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                val task = existingTask ?: Task(name, section = currentSection)
                task.name = name
                task.priority = selectedPriority
                task.category = selectedCategory
                task.reminderTime = selectedReminder
                task.subtasks.clear()
                task.subtasks.addAll(tempSubtasks)
                
                if (existingTask == null) {
                    allTasks.add(0, task)
                    DataManager.addActivity("Captured Task: $name")
                } else {
                    DataManager.addActivity("Updated Task: $name")
                }
                
                // Schedule Reminder
                selectedReminder?.let { time ->
                    if (time > System.currentTimeMillis()) {
                        scheduleReminder(task)
                    }
                }

                taskAdapter.updateDisplayList()
                DataManager.saveData(this)
                
                if (intent.getBooleanExtra("SHOW_ADD_DIALOG", false)) {
                    finish()
                } else {
                    dialog.dismiss()
                }
            }
        }

        showDialogSafe(dialog)
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
        val navTasks = findViewById<View>(R.id.nav_tasks)
        val navTodo = findViewById<View>(R.id.nav_todo_list)
        val footer = findViewById<View>(R.id.bottom_navigation_tasks)

        // Show/Hide based on settings
        val showTasks = DataManager.taskVisibleSections.contains("Tasks")
        val showTodo = DataManager.taskVisibleSections.contains("List")

        navTasks.visibility = if (showTasks) View.VISIBLE else View.GONE
        navTodo.visibility = if (showTodo) View.VISIBLE else View.GONE

        // Dynamic Visibility: Hide the entire footer if only one section is enabled
        if (DataManager.taskVisibleSections.size > 1) {
            footer.visibility = View.VISIBLE
        } else {
            footer.visibility = View.GONE
            // If only one is visible, ensure we switch to it
            val onlyVisible = DataManager.taskVisibleSections.firstOrNull() ?: "Tasks"
            if (currentSection != onlyVisible) switchSection(onlyVisible)
        }

        // If current section is hidden, switch to the first visible one
        if (!DataManager.taskVisibleSections.contains(currentSection)) {
            val firstVisible = DataManager.taskVisibleSections.firstOrNull() ?: "Tasks"
            switchSection(firstVisible)
        }

        navTasks.setOnClickListener { switchSection("Tasks") }
        navTodo.setOnClickListener { switchSection("List") }
        
        updateNavUI()
    }

    private fun switchSection(section: String) {
        currentSection = section
        taskAdapter.setSection(section)
        findViewById<TextView>(R.id.tv_title).text = section.uppercase()
        updateNavUI()
    }

    private fun updateNavUI() {
        val navs = mapOf(
            "Tasks" to Pair(findViewById<ImageView>(R.id.iv_tasks_icon), findViewById<TextView>(R.id.tv_tasks_label)),
            "List" to Pair(findViewById<ImageView>(R.id.iv_todo_icon), findViewById<TextView>(R.id.tv_todo_label))
        )

        val activeColor = ContextCompat.getColor(this, R.color.chip_selected)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)

        navs.forEach { (sec, views) ->
            val isActive = sec == currentSection
            val color = if (isActive) activeColor else inactiveColor
            
            views.first.setColorFilter(color)
            views.second.setTextColor(color)
        }
    }

    private fun showManageCategoriesDialog() {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_manage_categories, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = view.findViewById<LinearLayout>(R.id.categories_container)
        val etNewCategory = view.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = view.findViewById<ImageButton>(R.id.btn_add_category)

        fun render() {
            container.removeAllViews()
            DataManager.taskCustomCategories.forEach { category ->
                val catView = layoutInflater.inflate(R.layout.item_category_manage, container, false)
                catView.findViewById<TextView>(R.id.tv_category_name).text = category
                catView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
                    if (DataManager.taskCustomCategories.size > 1) {
                        DataManager.taskCustomCategories.remove(category)
                        DataManager.saveData(this)
                        render()
                        setupFilters() // Refresh main screen chips
                    } else {
                        Toast.makeText(this, "At least one category required", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(catView)
            }
        }

        btnAdd.setOnClickListener {
            val name = etNewCategory.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.taskCustomCategories.contains(name)) {
                DataManager.taskCustomCategories.add(name)
                DataManager.saveData(this)
                etNewCategory.text.clear()
                render()
                setupFilters() // Refresh main screen chips
            }
        }

        render()
        showDialogSafe(dialog)
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

    private fun showManageSectionsDialog(type: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_sections)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.container_section_switches)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_sections)
        
        val options = if (type == "TASK") listOf("Tasks", "List") else listOf("Notes", "Questions", "Daily", "Stories")
        val currentVisible = if (type == "TASK") DataManager.taskVisibleSections else DataManager.noteVisibleSections
        val tempSelection = currentVisible.toMutableList()

        options.forEach { option ->
            val switch = SwitchCompat(this).apply {
                text = option
                setTextColor(Color.WHITE)
                textSize = 16f
                isChecked = tempSelection.contains(option)
                setPadding(0, 24, 0, 24)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (!tempSelection.contains(option)) tempSelection.add(option)
                    } else {
                        if (tempSelection.size > 1) {
                            tempSelection.remove(option)
                        } else {
                            this.isChecked = true
                            Toast.makeText(this@TaskActivity, "At least one section must be visible", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            container.addView(switch)
        }

        btnSave.setOnClickListener {
            if (type == "TASK") {
                DataManager.taskVisibleSections.clear()
                DataManager.taskVisibleSections.addAll(tempSelection)
            } else {
                DataManager.noteVisibleSections.clear()
                DataManager.noteVisibleSections.addAll(tempSelection)
            }
            DataManager.saveData(this)
            setupBottomNavigation() // Refresh current screen
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun scheduleReminder(task: Task) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        
        // Android 12+ check for exact alarm permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", task.name)
            putExtra("TASK_TIMESTAMP", task.timestamp)
        }
        
        // Use a more unique request code for PendingIntent
        val requestCode = (task.timestamp % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderTime!!, pendingIntent)
    }

    private fun updatePriorityAlpha(root: View, checkedId: Int) {
        listOf(R.id.rb_priority_low, R.id.rb_priority_medium, R.id.rb_priority_high).forEach { id ->
            root.findViewById<View>(id).alpha = if (id == checkedId) 1.0f else 0.3f
        }
    }

    private fun renderSubtasks(container: LinearLayout, subtasks: MutableList<Subtask>) {
        container.removeAllViews()
        subtasks.forEach { subtask ->
            val subView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_multiple_choice, container, false)
            val ctView = subView as CheckedTextView
            ctView.text = subtask.name
            ctView.setTextColor(Color.WHITE)
            ctView.textSize = 14f
            ctView.isChecked = subtask.isCompleted
            ctView.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(Color.WHITE))
            ctView.setOnClickListener {
                subtask.isCompleted = !subtask.isCompleted
                ctView.isChecked = subtask.isCompleted
            }
            container.addView(subView)
        }
    }

    private fun updateReminderUI(tv: TextView, time: Long?) {
        if (time == null) {
            tv.text = "Set reminder"
        } else {
            val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            tv.text = sdf.format(Date(time))
        }
    }

    private fun showReminderPicker(onTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            val timePicker = TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                onTimeSelected(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            showDialogSafe(timePicker)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        showDialogSafe(datePicker)
    }
}
