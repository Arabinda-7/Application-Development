package com.example.allinone

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private val allHabits: MutableList<Habit>,
    private val onProgressChanged: () -> Unit,
    private val onTimerStart: (Habit, Int) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var displayHabits = allHabits.toMutableList()
    private var isBulkMode = false
    private var currentFilter = "All"
    private var selectedDayIndex = 6
    private var selectedDateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private val todayDateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private var showCompleted = DataManager.habitShowCompleted

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_list_item, parent, false)
        return HabitViewHolder(view)
    }

    private fun isHabitCompletedOnSelectedDate(habit: Habit): Boolean {
        return if (selectedDateString == todayDateString) {
            habit.isCompleted
        } else {
            habit.completedDates.contains(selectedDateString)
        }
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = displayHabits[position]
        val context = holder.itemView.context
        val isCompleted = isHabitCompletedOnSelectedDate(habit)

        holder.habitName.text = habit.name
        holder.habitCompleted.isChecked = isCompleted
        
        if (habit.isDayOff && selectedDateString == todayDateString) {
            holder.habitFrequency.text = "DAY OFF"
        } else {
            val details = when (habit.trackingMode) {
                "Timer" -> "${habit.target}s"
                else -> if (habit.target > 0) "${habit.progress}/${habit.target} reps" else habit.frequency
            }
            holder.habitFrequency.text = details
        }

        val cardColor = if (habit.color != -1) habit.color else ContextCompat.getColor(context, R.color.card_blue)
        holder.habitCard.setCardBackgroundColor(cardColor)

        if (habit.iconResId != -1) {
            holder.habitIcon.setImageResource(habit.iconResId)
        }

        // Expansion Logic
        holder.expandableControls.visibility = if (habit.isExpanded) View.VISIBLE else View.GONE
        holder.expandChevron.rotation = if (habit.isExpanded) 270f else 90f

        holder.habitCard.setOnClickListener {
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
            habit.isExpanded = !habit.isExpanded
            notifyItemChanged(position)
        }
        
        holder.habitName.setOnClickListener {
            val intent = Intent(context, HabitDetailActivity::class.java).apply {
                putExtra("HABIT_NAME", habit.name)
                putExtra("HABIT_ID", habit.timestamp)
            }
            context.startActivity(intent)
        }

        // Timer Logic
        if (habit.trackingMode == "Timer") {
            holder.btnStartTimer.visibility = View.VISIBLE
            holder.btnStartTimer.setOnClickListener {
                if (!isCompleted) {
                    onTimerStart(habit, allHabits.indexOf(habit))
                }
            }
        } else {
            holder.btnStartTimer.visibility = View.GONE
        }

        updateVisuals(holder, isCompleted)

        holder.habitCompleted.setOnClickListener {
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
                }
                
                applyFilterAndSort()
                onProgressChanged()
            }
        }

        holder.editButton.setOnClickListener { showCustomMenu(it, position) }
    }

    private fun showCustomMenu(anchor: View, position: Int) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val habit = displayHabits[position]
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
        displayHabits = allHabits.filter { habit ->
            val matchesTime = if (currentFilter == "All") true else habit.frequency == currentFilter
            val matchesDay = if (habit.repeatType == "SPECIFIC_DAYS") {
                habit.repeatDays.contains(selectedDayIndex)
            } else {
                true 
            }
            
            val isCompleted = isHabitCompletedOnSelectedDate(habit)
            val matchesVisibility = if (showCompleted) true else !isCompleted

            matchesTime && matchesDay && matchesVisibility
        }.toMutableList()
        
        // Sorting logic based on Settings
        if (DataManager.habitSortOrder == "Streak") {
            displayHabits.sortWith(compareByDescending<Habit> { it.completedDates.size }.thenBy { it.timestamp })
        } else {
            // Default: Time based sort (Morning -> Afternoon -> Evening -> Anytime)
            val order = listOf("Morning", "Afternoon", "Evening", "Anytime")
            displayHabits.sortWith(compareBy<Habit> { order.indexOf(it.frequency) }.thenBy { it.timestamp })
        }

        // Keep completed items at the bottom if desired? 
        // Current logic puts completed at the top due to `isHabitCompletedOnSelectedDate(it)` sort.
        // Actually, users usually prefer completed at bottom. Let's move them to bottom.
        displayHabits.sortBy { isHabitCompletedOnSelectedDate(it) }

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
            holder.mainContainer.alpha = 0.6f
        } else {
            holder.habitName.paintFlags = holder.habitName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.mainContainer.alpha = 1.0f
        }
    }

    fun setShowCompleted(show: Boolean) {
        showCompleted = show
        applyFilterAndSort()
    }

    override fun getItemCount() = displayHabits.size

    fun setBulkMode(enabled: Boolean) {
        isBulkMode = enabled
        if (!enabled) allHabits.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    fun markSelectedDone(context: android.content.Context) {
        displayHabits.filter { it.isSelected }.forEach { habit ->
            if (selectedDateString == todayDateString) {
                habit.isCompleted = true
                habit.progress = habit.target
            }
            if (!habit.completedDates.contains(selectedDateString)) {
                habit.completedDates.add(selectedDateString)
                DataManager.addActivity("Bulk Finished: ${habit.name}")
            }
        }
        setBulkMode(false)
        onProgressChanged()
        DataManager.saveData(context)
    }

    fun markSelectedDayOff(context: android.content.Context) {
        displayHabits.filter { it.isSelected }.forEach { habit ->
            if (selectedDateString == todayDateString) {
                habit.isCompleted = true
                habit.isDayOff = true
                habit.progress = habit.target
            }
            if (!habit.completedDates.contains(selectedDateString)) {
                habit.completedDates.add(selectedDateString)
                DataManager.addActivity("Bulk Finished: ${habit.name}")
            }
        }
        setBulkMode(false)
        onProgressChanged()
        DataManager.saveData(context)
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainContainer: View = itemView
        val habitName: TextView = itemView.findViewById(R.id.habit_name)
        val habitCompleted: CheckBox = itemView.findViewById(R.id.habit_completed)
        val habitFrequency: TextView = itemView.findViewById(R.id.habit_frequency)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_habit_button)
        val habitCard: MaterialCardView = itemView.findViewById(R.id.habit_card)
        val habitIcon: ImageView = itemView.findViewById(R.id.habit_icon)
        val expandChevron: ImageView = itemView.findViewById(R.id.iv_expand_chevron_habit)
        val expandableControls: LinearLayout = itemView.findViewById(R.id.expandable_controls_habit)
        val btnStartTimer: MaterialCardView = itemView.findViewById(R.id.btn_start_timer_habit)
    }
}
