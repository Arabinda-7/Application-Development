package com.example.allinone

import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class HabitCalendarSection(
    private val viewPager: ViewPager2,
    private val dateHeader: TextView,
    private val onDateSelected: (String) -> Unit
) {
    private lateinit var weekAdapter: CalendarWeekAdapter
    private val weeks = mutableListOf<List<DayModel>>()
    private var initialPageIndex = 0

    fun setup() {
        val calendar = Calendar.getInstance()
        val habitColor = if (DataManager.globalHabitColor != -1) DataManager.globalHabitColor else android.graphics.Color.parseColor("#FF7A59")

        calendar.add(Calendar.WEEK_OF_YEAR, -52)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfDate.format(Date())
        val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())

        val totalWeeksCount = 105

        for (w in 0 until totalWeeksCount) {
            val weekDays = mutableListOf<DayModel>()
            for (d in 0 until 7) {
                val dateStr = sdfDate.format(calendar.time)
                val isSelected = dateStr == todayStr
                if (isSelected) initialPageIndex = w
                
                weekDays.add(DayModel(
                    date = calendar.time,
                    dayName = sdfDayName.format(calendar.time),
                    dayNumber = sdfDayNum.format(calendar.time),
                    dateString = dateStr,
                    isSelected = isSelected,
                    isToday = dateStr == todayStr
                ))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            weeks.add(weekDays)
        }

        weekAdapter = CalendarWeekAdapter(weeks, habitColor, R.layout.item_calendar_date_habit) { day ->
            weeks.flatten().forEach { it.isSelected = (it.dateString == day.dateString) }
            weekAdapter.notifyDataSetChanged()
            onDateSelected(day.dateString)
        }
        
        viewPager.adapter = weekAdapter
        viewPager.setCurrentItem(initialPageIndex, false)

        dateHeader.setOnClickListener {
            weeks.flatten().forEach { it.isSelected = (it.dateString == todayStr) }
            viewPager.setCurrentItem(initialPageIndex, true)
            weekAdapter.notifyDataSetChanged()
            onDateSelected(todayStr)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val firstDay = weeks[position][0]
                val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                dateHeader.text = sdfMonth.format(firstDay.date)
            }
        })
    }
}
