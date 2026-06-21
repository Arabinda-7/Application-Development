package com.example.allinone

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
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

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_create_new_task).setOnClickListener { showAddTaskDialog(null) }
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

        dialogView.findViewById<View>(R.id.color_selection_row).setOnClickListener {
            val colors = listOf(
                ContextCompat.getColor(this, R.color.card_blue),
                ContextCompat.getColor(this, R.color.card_orange),
                ContextCompat.getColor(this, R.color.card_green),
                Color.MAGENTA, Color.RED, Color.CYAN
            )
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<View>(R.id.btn_save_task).setOnClickListener {
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                if (existingTask == null) {
                    tasks.add(Task(name, color = selectedColor))
                } else {
                    existingTask.name = name
                    existingTask.color = selectedColor
                }
                taskAdapter.sortTasks()
                DataManager.saveData(this)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
