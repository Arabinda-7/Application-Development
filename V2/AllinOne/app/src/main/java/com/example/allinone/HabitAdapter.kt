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
    private val onProgressChanged: (Boolean) -> Unit,
    private val onTimerStart: (Habit, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
    }

    private var isCompletedExpanded = true
    private var isDayOffExpanded = true
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
        val item = displayItems[position]
        return if (item is String) {
            TYPE_HEADER
        } else {
            val habit = item as Habit
            when (habit.frequency) {
                "Morning" -> R.layout.item_habit_morning
                "Afternoon" -> R.layout.item_habit_afternoon
                "Evening" -> R.layout.item_habit_evening
                else -> R.layout.item_habit_anytime
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_habit, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
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
            
            val isExpanded = if (headerText.startsWith("Day Off")) isDayOffExpanded else isCompletedExpanded
            holder.chevron.rotation = if (isExpanded) 180f else 0f
            
            holder.itemView.setOnClickListener {
                if (headerText.startsWith("Day Off")) {
                    isDayOffExpanded = !isDayOffExpanded
                } else {
                    isCompletedExpanded = !isCompletedExpanded
                }
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
            holder.habitCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
            holder.habitCard.strokeColor = themeColor
            holder.habitCard.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()

            // 4. Checkbox Tint (Always themed for border/fill)
            holder.habitCompleted.backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)

            UIUtils.safeSetImageResource(holder.habitIcon, habit.iconResId, R.drawable.ic_habit_tracker)

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

            if (habit.target > 1) {
                holder.habitCompleted.visibility = View.GONE
                holder.progressControls.visibility = View.VISIBLE
                
                val currentProgress = if (selectedDateString == todayDateString) habit.progress else habit.dailyProgress[selectedDateString] ?: 0
                holder.tvProgress.text = "$currentProgress/${habit.target}"
                
                holder.btnIncrement.setOnClickListener {
                    if (selectedDateString != todayDateString) return@setOnClickListener
                    if (habit.progress < habit.target) {
                        habit.progress++
                        habit.dailyProgress[selectedDateString] = habit.progress
                        if (habit.progress == habit.target) {
                            habit.isCompleted = true
                            if (!habit.completedDates.contains(selectedDateString)) {
                                habit.completedDates.add(selectedDateString)
                                triggerCompletionEffects(context)
                            }
                            applyFilterAndSort()
                        } else {
                            notifyItemChanged(position)
                        }
                        onProgressChanged(habit.isCompleted)
                    }
                }
                
                holder.btnDecrement.setOnClickListener {
                    if (selectedDateString != todayDateString) return@setOnClickListener
                    if (habit.progress > 0) {
                        habit.progress--
                        habit.dailyProgress[selectedDateString] = habit.progress
                        if (habit.progress < habit.target) {
                            val wasCompleted = habit.isCompleted
                            habit.isCompleted = false
                            habit.completedDates.remove(selectedDateString)
                            if (wasCompleted) applyFilterAndSort() else notifyItemChanged(position)
                        } else {
                            notifyItemChanged(position)
                        }
                        onProgressChanged(false)
                    }
                }
            } else {
                holder.habitCompleted.visibility = View.VISIBLE
                holder.progressControls.visibility = View.GONE

                holder.habitCompleted.setOnClickListener {
                    if (selectedDateString != todayDateString) {
                        holder.habitCompleted.isChecked = isCompleted
                        android.widget.Toast.makeText(context, "You can only mark habits for today!", android.widget.Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (isCompleted) {
                        holder.habitCompleted.isChecked = true
                        return@setOnClickListener
                    } else {
                        // Marking logic
                        if (holder.habitCompleted.isChecked) {
                            habit.isCompleted = true
                            habit.progress = habit.target
                            habit.dailyProgress[selectedDateString] = habit.target
                            
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
                            onProgressChanged(true)
                        }
                    }
                }
            }
        }
    }

    private fun showCustomMenu(anchor: View, habit: Habit) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.menu_habit_item, null)
        val isCompleted = isHabitCompletedOnSelectedDate(habit)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val dayOffBtn = menuView.findViewById<View>(R.id.menu_take_day_off)
        val actionText = menuView.findViewById<TextView>(R.id.tv_action_text)
        actionText.text = "TAKE DAY OFF"
        
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
            onProgressChanged(true)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(context, AddHabitActivity::class.java).apply {
                putExtra("HABIT_ID", habit.timestamp)
            }
            context.startActivity(intent)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allHabits.remove(habit)
            applyFilterAndSort()
            onProgressChanged(false)
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
            onProgressChanged(true)
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
            val isAvailableOnDate = DataManager.getTrackingDateString(habit.timestamp) <= selectedDateString
            matchesTime && matchesDay && isAvailableOnDate
        }

        val sorted = when (DataManager.habitSortOrder) {
            "Streak" -> filtered.sortedByDescending { it.completedDates.size }
            "Time" -> {
                val order = listOf("Morning", "Afternoon", "Evening", "Anytime")
                filtered.sortedBy { order.indexOf(it.frequency) }
            }
            else -> filtered.sortedByDescending { it.timestamp }
        }

        val activeHabits = sorted.filter { !isHabitCompletedOnSelectedDate(it) }
        val allCompleted = sorted.filter { isHabitCompletedOnSelectedDate(it) }
        
        val dayOffHabits = allCompleted.filter { it.isDayOff }
        val strictlyCompleted = allCompleted.filter { !it.isDayOff }

        displayItems.addAll(activeHabits)

        if (dayOffHabits.isNotEmpty()) {
            displayItems.add("Day Off ${dayOffHabits.size}")
            if (isDayOffExpanded) {
                displayItems.addAll(dayOffHabits)
            }
        }

        if (showCompleted && strictlyCompleted.isNotEmpty()) {
            displayItems.add("Completed ${strictlyCompleted.size}")
            if (isCompletedExpanded) {
                displayItems.addAll(strictlyCompleted)
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
        val progressControls: View = itemView.findViewById(R.id.progress_controls)
        val tvProgress: TextView = itemView.findViewById(R.id.tv_habit_progress)
        val btnIncrement: ImageButton = itemView.findViewById(R.id.btn_increment)
        val btnDecrement: ImageButton = itemView.findViewById(R.id.btn_decrement)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_header_title)
        val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)
    }
}
