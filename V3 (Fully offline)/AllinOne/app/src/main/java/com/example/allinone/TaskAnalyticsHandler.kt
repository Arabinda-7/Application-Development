package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object TaskAnalyticsHandler {
    fun show(context: Context) {
        val allTasks = DataManager.tasks
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

        val dialog = Dialog(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_analytics_simple, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        view.findViewById<TextView>(R.id.tv_analytics_content).text = message
        view.findViewById<View>(R.id.btn_close_analytics).setOnClickListener { dialog.dismiss() }

        if (context is BaseActivity) {
            context.showDialogSafe(dialog)
        } else {
            dialog.show()
        }
    }
}
