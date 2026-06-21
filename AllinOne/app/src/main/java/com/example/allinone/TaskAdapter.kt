package com.example.allinone

import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name
        holder.taskCompleted.isChecked = task.isCompleted

        val context = holder.itemView.context
        val cardColor = if (task.color != -1) {
            task.color
        } else {
            ContextCompat.getColor(context, R.color.card_blue)
        }
        holder.taskCard.setCardBackgroundColor(cardColor)

        updateVisuals(holder, task.isCompleted)

        holder.taskCompleted.setOnClickListener {
            if (holder.taskCompleted.isChecked) {
                task.isCompleted = true
                sortTasks()
                DataManager.saveData(context)
                onProgressChanged()
            } else {
                holder.taskCompleted.isChecked = true
                AlertDialog.Builder(context)
                    .setTitle("Confirm")
                    .setMessage("Mark as incomplete?")
                    .setPositiveButton("Yes") { _, _ ->
                        task.isCompleted = false
                        sortTasks()
                        DataManager.saveData(context)
                        onProgressChanged()
                    }
                    .setNegativeButton("No") { _, _ -> holder.taskCompleted.isChecked = true }
                    .show()
            }
        }

        holder.editButton.setOnClickListener { showCustomMenu(it, position) }
    }

    private fun showCustomMenu(anchor: View, position: Int) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val task = tasks[position]

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        // Task doesn't have "Day Off", so hide it
        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? ToDoListActivity)?.showAddTaskDialog(task)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            tasks.remove(task)
            notifyDataSetChanged()
            onProgressChanged()
            popupWindow.dismiss()
        }

        val undoView = menuView.findViewById<View>(R.id.menu_undo)
        if (task.isCompleted) {
            undoView.visibility = View.VISIBLE
            undoView.setOnClickListener {
                task.isCompleted = false
                sortTasks()
                onProgressChanged()
                popupWindow.dismiss()
            }
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    fun sortTasks() {
        tasks.sortWith(compareBy({ it.isCompleted }, { it.timestamp }))
        notifyDataSetChanged()
    }

    private fun updateVisuals(holder: TaskViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.taskName.paintFlags = holder.taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.6f
        } else {
            holder.taskName.paintFlags = holder.taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount() = tasks.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskCompleted: CheckBox = itemView.findViewById(R.id.task_completed)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_task_button)
        val taskCard: MaterialCardView = itemView.findViewById(R.id.task_card)
    }
}
