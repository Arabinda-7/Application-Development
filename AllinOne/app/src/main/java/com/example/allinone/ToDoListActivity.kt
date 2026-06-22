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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class ToDoListActivity : AppCompatActivity() {

    private val allTasks = DataManager.tasks
    private lateinit var taskAdapter: TaskAdapter
    private var isDeleteMode = false
    
    private var currentCategoryFilter = "All"
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)

        val taskList = findViewById<RecyclerView>(R.id.task_list)
        taskList.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(allTasks) { 
            DataManager.saveData(this)
        }
        taskList.adapter = taskAdapter

        setupHeader()
        setupFilters()
        setupSwipeActions(taskList)

        findViewById<View>(R.id.btn_create_new_task).setOnClickListener { 
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
            showAddTaskDialog(null) 
        }
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
        val chipGroup = findViewById<ChipGroup>(R.id.category_filter_chips)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentCategoryFilter = when (checkedIds.firstOrNull()) {
                R.id.chip_cat_personal -> "Personal"
                R.id.chip_cat_work -> "Work"
                R.id.chip_cat_shopping -> "Shopping"
                else -> "All"
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        taskAdapter.filter(currentCategoryFilter, currentSearchQuery)
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
                    DataManager.saveData(this@ToDoListActivity)
                } else {
                    // Swipe Left -> Delete
                    allTasks.remove(task)
                    taskAdapter.updateDisplayList()
                    DataManager.saveData(this@ToDoListActivity)
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

        val deleteBtn = menuView.findViewById<View>(R.id.menu_action_primary)
        deleteBtn.visibility = View.VISIBLE
        deleteBtn.setOnClickListener {
            toggleDeleteMode(true)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener { popupWindow.dismiss() }
        popupWindow.showAsDropDown(anchor, -150, 0)
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
        val rgPriority = view.findViewById<RadioGroup>(R.id.rg_priority)
        val spinnerCat = view.findViewById<Spinner>(R.id.spinner_category)
        val containerSubtasks = view.findViewById<LinearLayout>(R.id.container_subtasks)
        val etNewSubtask = view.findViewById<EditText>(R.id.et_new_subtask)
        val btnAddSubtask = view.findViewById<ImageButton>(R.id.btn_add_subtask)
        val tvReminder = view.findViewById<TextView>(R.id.tv_reminder_summary)
        val colorPreview = view.findViewById<View>(R.id.task_color_preview)
        val btnSave = view.findViewById<TextView>(R.id.btn_save_task)

        // Setup Category Spinner
        val categories = listOf("General", "Personal", "Work", "Shopping")
        spinnerCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        var selectedPriority = existingTask?.priority ?: 0
        var selectedReminder: Long? = existingTask?.reminderTime
        var selectedColor = existingTask?.color ?: ContextCompat.getColor(this, R.color.card_blue)
        val tempSubtasks = existingTask?.subtasks?.toMutableList() ?: mutableListOf()

        // Initial UI State
        existingTask?.let {
            etName.setText(it.name)
            spinnerCat.setSelection(categories.indexOf(it.category))
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
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        renderSubtasks(containerSubtasks, tempSubtasks)

        // Listeners
        rgPriority.setOnCheckedChangeListener { _, id ->
            selectedPriority = when (id) {
                R.id.rb_priority_medium -> 1
                R.id.rb_priority_high -> 2
                else -> 0
            }
            updatePriorityAlpha(view, id)
        }

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
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
                val task = existingTask ?: Task(name)
                task.name = name
                task.priority = selectedPriority
                task.category = spinnerCat.selectedItem.toString()
                task.color = selectedColor
                task.reminderTime = selectedReminder
                task.subtasks.clear()
                task.subtasks.addAll(tempSubtasks)
                
                if (existingTask == null) allTasks.add(0, task)
                
                // Schedule Reminder
                selectedReminder?.let { time ->
                    if (time > System.currentTimeMillis()) {
                        scheduleReminder(task)
                    }
                }

                taskAdapter.updateDisplayList()
                DataManager.saveData(this)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Task Name is required", Toast.LENGTH_SHORT).show()
                etName.requestFocus()
            }
        }

        dialog.show()
    }

    private fun scheduleReminder(task: Task) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", task.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, task.timestamp.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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
        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                onTimeSelected(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
