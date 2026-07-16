package com.example.allinone

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private val allHabits: MutableList<Habit>,
    private val onProgressChanged: () -> Unit,
    private val onTimerStart: (Habit, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HABIT = 0
        private const val TYPE_HEADER = 1
    }

    private var isCompletedExpanded = true
    private var displayItems = mutableListOf<Any>()
    private var currentFilter = "All"
    private var selectedDayIndex = 6
    private var selectedDateString = DataManager.getTrackingDateString()
    private val todayDateString get() = DataManager.getTrackingDateString()
    private var showCompleted = DataManager.habitShowCompleted

    init {
        applyFilterAndSort()
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) TYPE_HEADER else TYPE_HABIT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list_item, parent, false)
            HabitViewHolder(view)
        }
    }

    private fun isHabitCompletedOnSelectedDate(habit: Habit): Boolean {
        return if (selectedDateString == todayDateString) {
            habit.isCompleted
        } else {
            habit.completedDates.contains(selectedDateString)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val headerText = displayItems[position] as String
            holder.title.text = headerText
            holder.chevron.rotation = if (isCompletedExpanded) 180f else 0f
            holder.itemView.setOnClickListener {
                isCompletedExpanded = !isCompletedExpanded
                applyFilterAndSort()
            }
        } else if (holder is HabitViewHolder) {
            val habit = displayItems[position] as Habit
            val context = holder.itemView.context
            val isCompleted = isHabitCompletedOnSelectedDate(habit)

            holder.habitName.text = UIUtils.formatTitleCase(habit.name)
            holder.habitCompleted.isChecked = isCompleted
            
            // Advanced Design Binding
            val themeColor = if (habit.color != -1) habit.color else android.graphics.Color.parseColor("#1A73E8")
            
            // 1. Accent Bar Color
            holder.itemView.findViewById<View>(R.id.accent_bar).backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
            
            // 2. Icon Container Tint
            holder.itemView.findViewById<View>(R.id.icon_container).backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor).withAlpha(20)

            // 3. Card Styling (Glassmorphic)
            holder.habitCard.setCardBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))
            holder.habitCard.strokeColor = themeColor
            holder.habitCard.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()

            if (habit.iconResId != -1) {
                holder.habitIcon.setImageResource(habit.iconResId)
            }

            holder.habitCard.setOnClickListener {
                val intent = Intent(context, HabitDetailActivity::class.java).apply {
                    putExtra("HABIT_NAME", habit.name)
                    putExtra("HABIT_ID", habit.timestamp)
                }
                context.startActivity(intent)
            }

            holder.habitCard.setOnLongClickListener {
                showCustomMenu(it, habit)
                true
            }
            
            holder.habitName.setOnClickListener {
                val intent = Intent(context, HabitDetailActivity::class.java).apply {
                    putExtra("HABIT_NAME", habit.name)
                    putExtra("HABIT_ID", habit.timestamp)
                }
                context.startActivity(intent)
            }

            updateVisuals(holder, isCompleted)

            holder.habitCompleted.setOnClickListener {
                if (selectedDateString != todayDateString) {
                    holder.habitCompleted.isChecked = isCompleted
                    android.widget.Toast.makeText(context, "You can only mark habits for today!", android.widget.Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isCompleted) {
                    holder.habitCompleted.isChecked = true
                    return@setOnClickListener
                }
                
                if (holder.habitCompleted.isChecked) {
                    if (selectedDateString == todayDateString) {
                        habit.isCompleted = true
                        habit.progress = habit.target
                    }
                    
                    if (!habit.completedDates.contains(selectedDateString)) {
                        habit.completedDates.add(selectedDateString)
                        triggerCompletionEffects(context)
                        DataManager.addActivity("Finished Ritual: ${habit.name}")
                        
                        // Award XP
                        if (DataManager.addXP(context, 10)) {
                            android.widget.Toast.makeText(context, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    applyFilterAndSort()
                    onProgressChanged()
                }
            }
        }
    }

    private fun showCustomMenu(anchor: View, habit: Habit) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val isCompleted = isHabitCompletedOnSelectedDate(habit)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val dayOffBtn = menuView.findViewById<View>(R.id.menu_take_day_off)
        dayOffBtn.visibility = if (isCompleted || selectedDateString != todayDateString) View.GONE else View.VISIBLE
        dayOffBtn.setOnClickListener {
            habit.isCompleted = true
            habit.isDayOff = true
            habit.isExpanded = false
            
            if (!habit.completedDates.contains(todayDateString)) {
                habit.completedDates.add(todayDateString)
                if (DataManager.addXP(context, 5)) {
                    android.widget.Toast.makeText(context, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            applyFilterAndSort()
            onProgressChanged()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? HabitTrackerActivity)?.showAddHabitDialog(habit)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allHabits.remove(habit)
            applyFilterAndSort()
            onProgressChanged()
            popupWindow.dismiss()
        }

        val undoView = menuView.findViewById<View>(R.id.menu_undo)
        undoView.visibility = if (isCompleted) View.VISIBLE else View.GONE
        undoView.setOnClickListener {
            if (selectedDateString == todayDateString) {
                habit.isCompleted = false
                habit.isDayOff = false
                habit.progress = 0
            }
            
            habit.completedDates.remove(selectedDateString)

            applyFilterAndSort()
            onProgressChanged()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    fun filter(filterType: String, dayIndex: Int? = null, dateString: String? = null) {
        currentFilter = filterType
        if (dayIndex != null) selectedDayIndex = dayIndex
        if (dateString != null) selectedDateString = dateString
        applyFilterAndSort()
    }

    fun sortHabits() {
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        displayItems.clear()

        val filtered = allHabits.filter { habit ->
            val matchesTime = if (currentFilter == "All") true else habit.frequency == currentFilter
            val matchesDay = if (habit.repeatType == "SPECIFIC_DAYS") {
                habit.repeatDays.contains(selectedDayIndex)
            } else {
                true 
            }
            matchesTime && matchesDay
        }

        val activeHabits = filtered.filter { !isHabitCompletedOnSelectedDate(it) }.sortedByDescending { it.timestamp }
        val completedHabits = filtered.filter { isHabitCompletedOnSelectedDate(it) }.sortedByDescending { it.timestamp }

        displayItems.addAll(activeHabits)

        if (showCompleted && completedHabits.isNotEmpty()) {
            displayItems.add("Completed ${completedHabits.size}")
            if (isCompletedExpanded) {
                displayItems.addAll(completedHabits)
            }
        }

        notifyDataSetChanged()
    }

    private fun triggerCompletionEffects(context: android.content.Context) {
        if (DataManager.habitCompletionHaptics) {
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
        }
        
        if (DataManager.habitCompletionSound) {
            try {
                val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val r = android.media.RingtoneManager.getRingtone(context, notification)
                r.play()
            } catch (e: Exception) {}
        }
    }

    private fun updateVisuals(holder: HabitViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.habitName.paintFlags = holder.habitName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.mainContainer.alpha = 0.5f
        } else {
            holder.habitName.paintFlags = holder.habitName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.mainContainer.alpha = 1.0f
        }
    }

    fun setShowCompleted(show: Boolean) {
        showCompleted = show
        applyFilterAndSort()
    }

    override fun getItemCount() = displayItems.size

    fun setBulkMode(enabled: Boolean) {
        // Implement bulk mode if needed
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainContainer: View = itemView
        val habitName: TextView = itemView.findViewById(R.id.habit_name)
        val habitCompleted: CheckBox = itemView.findViewById(R.id.habit_completed)
        val habitCard: MaterialCardView = itemView.findViewById(R.id.habit_card)
        val habitIcon: ImageView = itemView.findViewById(R.id.habit_icon)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_header_title)
        val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)
    }
}
