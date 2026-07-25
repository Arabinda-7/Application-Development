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
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : BaseActivity() {

    private var taskIndex: Int = -1
    private var existingTask: Task? = null
    private var currentSection: String = "Tasks"
    
    private lateinit var etName: EditText
    private lateinit var tvNameHint: TextView
    private lateinit var rgPriority: RadioGroup
    private lateinit var chipGroupCat: ChipGroup
    private lateinit var containerSubtasks: LinearLayout
    private lateinit var etNewSubtask: EditText
    private lateinit var btnSave: TextView
    private lateinit var tvReminder: TextView
    private lateinit var btnSetReminder: View
    private lateinit var ivReminderIcon: ImageView

    private var selectedCategory = "General"
    private var selectedPriority = 0
    private var selectedReminder: Long? = null
    private val tempSubtasks = mutableListOf<Subtask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskIndex = intent.getIntExtra("TASK_INDEX", -1)
        currentSection = intent.getStringExtra("SECTION") ?: "Tasks"
        
        if (taskIndex != -1 && taskIndex < DataManager.tasks.size) {
            existingTask = DataManager.tasks[taskIndex]
        }

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_task_root), findViewById(R.id.add_task_content_container))
    }

    private fun initViews() {
        etName = findViewById(R.id.task_name_input)
        tvNameHint = findViewById(R.id.tv_name_hint)
        rgPriority = findViewById(R.id.rg_priority)
        chipGroupCat = findViewById(R.id.category_chip_group)
        containerSubtasks = findViewById(R.id.container_subtasks)
        etNewSubtask = findViewById(R.id.et_new_subtask)
        tvReminder = findViewById(R.id.tv_reminder_summary)
        btnSetReminder = findViewById(R.id.btn_set_reminder)
        ivReminderIcon = findViewById(R.id.iv_reminder_button_icon)
        btnSave = findViewById(R.id.btn_save_task)
        
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        selectedCategory = existingTask?.category ?: "General"
        selectedPriority = existingTask?.priority ?: 0
        selectedReminder = existingTask?.reminderTime
        tempSubtasks.addAll(existingTask?.subtasks ?: mutableListOf())

        if (existingTask != null) {
            etName.setText(existingTask?.name)
            btnSave.text = "UPDATE"
            when (selectedPriority) {
                0 -> rgPriority.check(R.id.rb_priority_low)
                1 -> rgPriority.check(R.id.rb_priority_medium)
                2 -> rgPriority.check(R.id.rb_priority_high)
            }
            updateReminderUI()
            updatePriorityAlpha(rgPriority.checkedRadioButtonId)
        }

        // Categories
        DataManager.taskCustomCategories.forEach { cat ->
            val chip = Chip(this)
            chip.text = cat
            chip.isCheckable = true
            chip.isChecked = (cat == selectedCategory)
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(Color.WHITE)
            chip.setOnCheckedChangeListener { _, isChecked -> if (isChecked) selectedCategory = cat }
            chipGroupCat.addView(chip)
        }

        rgPriority.setOnCheckedChangeListener { _, id ->
            selectedPriority = when (id) {
                R.id.rb_priority_medium -> 1
                R.id.rb_priority_high -> 2
                else -> 0
            }
            updatePriorityAlpha(id)
        }

        findViewById<View>(R.id.btn_add_subtask).setOnClickListener {
            val subName = etNewSubtask.text.toString().trim()
            if (subName.isNotEmpty()) {
                tempSubtasks.add(Subtask(subName))
                renderSubtasks()
                etNewSubtask.text.clear()
            }
        }

        findViewById<View>(R.id.btn_set_reminder).setOnClickListener {
            showReminderPicker { time ->
                selectedReminder = time
                updateReminderUI()
            }
        }

        btnSave.setOnClickListener { saveTask() }
        
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: Editable?) {}
        })

        renderSubtasks()
        validateInputs()
    }

    private fun validateInputs() {
        val name = etName.text.toString().trim()
        val isValid = name.isNotEmpty()
        
        btnSave.alpha = if (isValid) 1.0f else 0.3f
        btnSave.isEnabled = isValid
        
        val themeColor = if (DataManager.taskAddThemeColor != -1) DataManager.taskAddThemeColor else ContextCompat.getColor(this, R.color.primary_blue)
        if (isValid) btnSave.setTextColor(themeColor) else btnSave.setTextColor(Color.GRAY)
        
        tvNameHint.visibility = if (isValid) View.GONE else View.VISIBLE
        if (!isValid) startPulseAnimation(tvNameHint)
        tvNameHint.setTextColor(themeColor)
    }

    private fun updatePriorityAlpha(checkedId: Int) {
        listOf(R.id.rb_priority_low, R.id.rb_priority_medium, R.id.rb_priority_high).forEach { id ->
            findViewById<View>(id).alpha = if (id == checkedId) 1.0f else 0.3f
        }
    }

    private fun renderSubtasks() {
        containerSubtasks.removeAllViews()
        tempSubtasks.forEach { subtask ->
            val subView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_multiple_choice, containerSubtasks, false)
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
            containerSubtasks.addView(subView)
        }
    }

    private fun updateReminderUI() {
        if (selectedReminder == null) {
            tvReminder.text = "Set reminder"
            ivReminderIcon.visibility = View.VISIBLE
            btnSetReminder.setBackgroundResource(R.drawable.bg_dialog_rounded)
            btnSetReminder.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#20FFFFFF"))
        } else {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            tvReminder.text = sdf.format(Date(selectedReminder!!))
            ivReminderIcon.visibility = View.GONE
            btnSetReminder.background = null
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

    private fun saveTask() {
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
                DataManager.tasks.add(0, task)
                DataManager.addActivity("Captured Task: $name")
            } else {
                DataManager.addActivity("Updated Task: $name")
            }
            
            selectedReminder?.let { time ->
                if (time > System.currentTimeMillis()) {
                    scheduleReminder(task)
                }
            }

            DataManager.saveData(this)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun scheduleReminder(task: Task) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", task.name)
            putExtra("TASK_TIMESTAMP", task.timestamp)
        }
        val requestCode = (task.timestamp % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderTime!!, pendingIntent)
    }
}
