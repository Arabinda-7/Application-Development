package com.example.allinone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

data class DayModel(
    val date: Date,
    val dayName: String,
    val dayNumber: String,
    val dateString: String,
    var isSelected: Boolean = false
)

class CalendarDayAdapter(
    private val days: List<DayModel>,
    private val onDaySelected: (DayModel) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_date, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount() = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tv_day_number)
        private val viewCircle: View = itemView.findViewById(R.id.view_selected_circle)
        private val viewLine: View = itemView.findViewById(R.id.view_selection_line)

        fun bind(day: DayModel) {
            tvDayNumber.text = day.dayNumber
            
            if (day.isSelected) {
                tvDayNumber.setTypeface(null, android.graphics.Typeface.BOLD)
                viewCircle.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.chip_background)
                viewLine.visibility = View.VISIBLE
            } else {
                tvDayNumber.setTypeface(null, android.graphics.Typeface.NORMAL)
                viewCircle.backgroundTintList = ContextCompat.getColorStateList(itemView.context, android.R.color.transparent)
                viewLine.visibility = View.INVISIBLE
            }

            itemView.setOnClickListener {
                if (!day.isSelected) {
                    days.forEach { it.isSelected = false }
                    day.isSelected = true
                    notifyDataSetChanged()
                    onDaySelected(day)
                }
            }
        }
    }
}
