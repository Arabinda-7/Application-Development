package com.example.allinone

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class HabitListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val createButton: MaterialCardView,
    private val onDataChanged: () -> Unit
) {
    private val habitAdapter: HabitAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        habitAdapter = HabitAdapter(DataManager.habits, { immediate ->
            DataManager.saveData(context, immediate)
            onDataChanged()
        }, { _, _ -> })
        recyclerView.adapter = habitAdapter
        
        if (DataManager.habitAddThemeColor != -1) {
            createButton.strokeColor = DataManager.habitAddThemeColor
        }
        createButton.setOnClickListener {
            context.startActivity(Intent(context, AddHabitActivity::class.java))
        }
    }

    fun applyFilter(filter: String, dayIndex: Int, dateString: String) {
        habitAdapter.filter(filter, dayIndex, dateString)
    }

    fun setShowCompleted(show: Boolean) {
        habitAdapter.setShowCompleted(show)
    }
}
