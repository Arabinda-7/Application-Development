package com.example.allinone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarWeekAdapter(
    private val weeks: List<List<DayModel>>,
    private val onDaySelected: (DayModel) -> Unit
) : RecyclerView.Adapter<CalendarWeekAdapter.WeekViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val rv = RecyclerView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(context, 7)
        }
        return WeekViewHolder(rv)
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        holder.bind(weeks[position])
    }

    override fun getItemCount() = weeks.size

    inner class WeekViewHolder(private val rv: RecyclerView) : RecyclerView.ViewHolder(rv) {
        fun bind(days: List<DayModel>) {
            rv.adapter = CalendarDayAdapter(days) { selectedDay ->
                // Clear selection in other weeks if necessary
                // For simplicity, we assume one selection at a time globally
                onDaySelected(selectedDay)
            }
        }
    }
}
