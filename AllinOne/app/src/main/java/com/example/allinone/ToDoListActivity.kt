package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ToDoListActivity : AppCompatActivity() {

    private val tasks = DataManager.tasks
    private lateinit var taskAdapter: TaskAdapter
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(Date())

        val taskList = findViewById<RecyclerView>(R.id.task_list)
        taskList.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(tasks) { 
            DataManager.saveData(this)
        }
        taskList.adapter = taskAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener {
            if (isDeleteMode) {
                toggleDeleteMode(false)
            } else {
                finish()
            }
        }

        findViewById<View>(R.id.btn_create_new_task).setOnClickListener { showAddTaskDialog(null) }

        val btnSettings = findViewById<ImageButton>(R.id.btn_task_settings)
        btnSettings.setOnClickListener {
            if (isDeleteMode) {
                // Delete selected tasks
                taskAdapter.deleteSelectedTasks(this)
                toggleDeleteMode(false)
            } else {
                showSettingsMenu(it)
            }
        }
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

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            popupWindow.dismiss()
            // Open settings if needed
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        taskAdapter.setDeleteMode(enabled)
        val btnSettings = findViewById<ImageButton>(R.id.btn_task_settings)
        if (enabled) {
            btnSettings.setImageResource(android.R.drawable.ic_menu_delete)
        } else {
            btnSettings.setImageResource(R.drawable.baseline_settings_24)
        }
    }

    fun showAddTaskDialog(existingTask: Task? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editText = dialogView.findViewById<EditText>(R.id.task_name_input)
        val colorPreview = dialogView.findViewById<View>(R.id.task_color_preview)
        
        var selectedColor = existingTask?.color ?: ContextCompat.getColor(this, R.color.card_blue)
        
        if (existingTask != null) {
            editText.setText(existingTask.name)
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        colorPreview.setOnClickListener {
            val colors = listOf(
                ContextCompat.getColor(this, R.color.card_blue),
                ContextCompat.getColor(this, R.color.card_orange),
                ContextCompat.getColor(this, R.color.card_green),
                Color.MAGENTA, Color.RED, Color.CYAN
            )
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnSave = dialogView.findViewById<TextView>(R.id.btn_save_task)
        if (existingTask != null) {
            btnSave.text = "Update"
        }

        btnSave.setOnClickListener {
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                if (existingTask == null) {
                    tasks.add(Task(name, isCompleted = false, color = selectedColor))
                } else {
                    existingTask.name = name
                    existingTask.color = selectedColor
                }
                taskAdapter.updateDisplayList()
                DataManager.saveData(this)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
