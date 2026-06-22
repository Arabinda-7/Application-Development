package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ToDoListActivity : AppCompatActivity() {

    private val tasks = DataManager.tasks
    private lateinit var taskAdapter: TaskAdapter
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)

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
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Delete")
        popup.setOnMenuItemClickListener { item ->
            if (item.title == "Delete") {
                toggleDeleteMode(true)
            }
            true
        }
        popup.show()
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        taskAdapter.setDeleteMode(enabled)
        val btnSettings = findViewById<ImageButton>(R.id.btn_task_settings)
        if (enabled) {
            btnSettings.setImageResource(android.R.drawable.ic_menu_delete)
        } else {
            btnSettings.setImageResource(android.R.drawable.ic_menu_manage)
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
