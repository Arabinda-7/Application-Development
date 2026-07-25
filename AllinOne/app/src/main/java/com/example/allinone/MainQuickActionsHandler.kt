package com.example.allinone

import android.content.Context
import android.content.Intent

class MainQuickActionsHandler(private val context: Context) {
    fun quickAddTask() {
        val intent = Intent(context, AddTaskActivity::class.java)
        context.startActivity(intent)
    }

    fun quickAddExpense() {
        val intent = Intent(context, AddFinanceActivity::class.java)
        context.startActivity(intent)
    }

    fun quickAddNote() {
        val intent = Intent(context, AddNoteActivity::class.java)
        context.startActivity(intent)
    }
}
