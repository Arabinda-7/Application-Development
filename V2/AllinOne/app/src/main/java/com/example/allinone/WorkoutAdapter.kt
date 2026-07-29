package com.example.allinone

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class WorkoutAdapter(
    private val allWorkouts: MutableList<Workout>,
    private val onProgressChanged: (Boolean) -> Unit,
    private val onTimerStart: (Workout, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
    }

    private var isCompletedExpanded = true
    private var isDayOffExpanded = true
    private var displayItems = mutableListOf<Any>()
    private var currentFilter = "All"
    private var selectedDayIndex = 0
    private var selectedDateString = DataManager.getTrackingDateString()
    private val todayDateString get() = DataManager.getTrackingDateString()
    private var showCompleted = DataManager.workoutShowCompleted

    init {
        applyFilterAndSort()
    }

    override fun getItemViewType(position: Int): Int {
        val item = displayItems[position]
        return if (item is String) {
            TYPE_HEADER
        } else {
            val workout = item as Workout
            when (workout.frequency) {
                "Morning" -> R.layout.item_workout_morning
                "Afternoon" -> R.layout.item_workout_afternoon
                "Evening" -> R.layout.item_workout_evening
                else -> R.layout.item_workout_anytime
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_workout, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            WorkoutViewHolder(view)
        }
    }

    private fun isWorkoutCompletedOnSelectedDate(workout: Workout): Boolean {
        return if (selectedDateString == todayDateString) {
            workout.isCompleted
        } else {
            workout.completedDates.contains(selectedDateString)
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
        } else if (holder is WorkoutViewHolder) {
            val workout = displayItems[position] as Workout
            val context = holder.itemView.context
            val isCompleted = isWorkoutCompletedOnSelectedDate(workout)

            holder.workoutName.text = UIUtils.formatTitleCase(workout.name)
            
            if (selectedDateString == todayDateString) {
                if (workout.isDayOff) {
                    holder.workoutDetails.text = "DAY OFF"
                } else {
                    val details = when (workout.trackingMode) {
                        "Timer" -> "${workout.target}s"
                        "Sets" -> "${workout.progress}/${workout.target} Sets (×${workout.repsPerSet})"
                        else -> "${workout.progress}/${workout.target} ${workout.trackingMode}"
                    }
                    holder.workoutDetails.text = details
                }
            } else {
                if (isCompleted) {
                    holder.workoutDetails.text = if (workout.trackingMode == "Sets") "${workout.target} Sets × ${workout.repsPerSet} DONE" else "COMPLETED"
                } else {
                    val unit = when (workout.trackingMode) {
                        "Timer" -> "s"
                        "Sets" -> " Sets (×${workout.repsPerSet})"
                        else -> " ${workout.trackingMode}"
                    }
                    holder.workoutDetails.text = "0/${workout.target}$unit"
                }
            }

            // Advanced Design Binding
            val themeColor = if (workout.color != -1) workout.color else android.graphics.Color.parseColor("#1A73E8")
            
            // 1. Accent Bar Color
            holder.itemView.findViewById<View>(R.id.accent_bar_workout).backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
            
            // 2. Icon Container Tint
            holder.itemView.findViewById<View>(R.id.icon_container_workout).backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor).withAlpha(20)

            // 3. Card Styling (Glassmorphic)
            holder.workoutCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
            holder.workoutCard.strokeColor = themeColor
            holder.workoutCard.strokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()

            // 4. Action Panel Dynamic Styling
            holder.itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_roller_container).strokeColor = themeColor
            holder.btnFinishSelection.setCardBackgroundColor(android.graphics.Color.argb(30, android.graphics.Color.red(themeColor), android.graphics.Color.green(themeColor), android.graphics.Color.blue(themeColor)))
            holder.btnFinishSelection.strokeColor = themeColor
            holder.tvSelectedNumCircle.backgroundTintList = android.content.res.ColorStateList.valueOf(themeColor)
            holder.btnFinishAll.setCardBackgroundColor(themeColor)
            
            UIUtils.safeSetImageResource(holder.workoutIcon, workout.iconResId, R.drawable.ic_workout_routine)

            // Expansion Logic with Smooth Transition
            val shouldShowControls = workout.isExpanded && !isCompleted && selectedDateString == todayDateString
            holder.expandableControls.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
            holder.expandChevron.rotation = if (workout.isExpanded) 180f else 0f

            holder.workoutCard.setOnClickListener {
                if (isCompleted || selectedDateString != todayDateString) {
                    val intent = Intent(context, WorkoutDetailActivity::class.java).apply {
                        putExtra("WORKOUT_NAME", workout.name)
                        putExtra("WORKOUT_ID", workout.timestamp)
                    }
                    context.startActivity(intent)
                } else {
                    val wasExpanded = workout.isExpanded
                    if (!wasExpanded) {
                        // Collapse others
                        displayItems.forEach { 
                            if (it is Workout && it.isExpanded) {
                                it.isExpanded = false
                            }
                        }
                    }
                    TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                    workout.isExpanded = !wasExpanded
                    notifyDataSetChanged() // Use notifyDataSetChanged to refresh all items to show collapses
                }
            }

            holder.workoutCard.setOnLongClickListener {
                showCustomMenu(it, workout)
                true
            }
            
            holder.workoutName.setOnClickListener {
                val intent = Intent(context, WorkoutDetailActivity::class.java).apply {
                    putExtra("WORKOUT_NAME", workout.name)
                    putExtra("WORKOUT_ID", workout.timestamp)
                }
                context.startActivity(intent)
            }

            holder.expandChevron.setOnClickListener {
                if (selectedDateString != todayDateString) {
                    android.widget.Toast.makeText(context, "You can only track workouts for today!", android.widget.Toast.LENGTH_SHORT).show()
                } else if (!isCompleted) {
                    val wasExpanded = workout.isExpanded
                    if (!wasExpanded) {
                        // Collapse others
                        displayItems.forEach { 
                            if (it is Workout && it.isExpanded) {
                                it.isExpanded = false
                            }
                        }
                    }
                    TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                    workout.isExpanded = !wasExpanded
                    notifyDataSetChanged()
                }
            }

            // Context-aware UI based on tracking mode
            if (workout.trackingMode == "Timer") {
                holder.layoutRepsControls.visibility = View.GONE
                holder.btnStartTimer.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
                holder.btnStartTimer.setOnClickListener {
                    if (!isCompleted) {
                        onTimerStart(workout, allWorkouts.indexOf(workout))
                    }
                }
            } else {
                holder.layoutRepsControls.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
                holder.btnStartTimer.visibility = View.GONE
                
                val remainingReps = (workout.target - workout.progress).coerceAtLeast(0)

                if (remainingReps > 0) {
                    holder.numberPicker.minValue = 1
                    holder.numberPicker.maxValue = remainingReps
                    holder.numberPicker.wrapSelectorWheel = false
                    holder.numberPicker.value = 1
                } else {
                    holder.numberPicker.minValue = 0
                    holder.numberPicker.maxValue = 0
                    holder.numberPicker.value = 0
                }
                
                updateFinishSelectionUI(holder, holder.numberPicker.value, workout.trackingMode)

                holder.numberPicker.setOnValueChangedListener { _, _, newVal ->
                    updateFinishSelectionUI(holder, newVal, workout.trackingMode)
                }

                holder.btnFinishSelection.setOnClickListener {
                    val addedValue = holder.numberPicker.value
                    if (addedValue > 0) {
                        workout.progress += addedValue
                        
                        // Track historical partial progress
                        val progressPercent = (workout.progress * 100) / workout.target.coerceAtLeast(1)
                        workout.dailyProgress[todayDateString] = progressPercent

                        if (workout.progress >= workout.target) {
                            workout.isCompleted = true
                            workout.isExpanded = false
                            if (!workout.completedDates.contains(todayDateString)) {
                                workout.completedDates.add(todayDateString)
                                DataManager.addActivity("Finished Workout: ${workout.name}")
                                if (DataManager.addXP(context, 25)) {
                                    android.widget.Toast.makeText(context, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                        workout.isExpanded = false
                        applyFilterAndSort()
                        onProgressChanged(workout.isCompleted)
                    }
                }

                holder.btnFinishAll.setOnClickListener {
                    workout.progress = workout.target
                    workout.isCompleted = true
                    workout.dailyProgress[todayDateString] = 100
                    TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                    workout.isExpanded = false
                    
                    if (!workout.completedDates.contains(todayDateString)) {
                        workout.completedDates.add(todayDateString)
                        DataManager.addActivity("Finished Workout: ${workout.name}")
                        if (DataManager.addXP(context, 25)) {
                            android.widget.Toast.makeText(context, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    applyFilterAndSort()
                    onProgressChanged(true)
                }
            }

            updateVisuals(holder, isCompleted)
        }
    }

    private fun updateFinishSelectionUI(holder: WorkoutViewHolder, value: Int, mode: String) {
        holder.tvSelectedNumCircle.text = value.toString()
        val unit = if (mode == "Reps") "REPS" else if (mode == "Sets") "SETS" else mode
        holder.tvFinishRepsLabel.text = "FINISH $value ${unit.uppercase()}"
    }

    private fun showCustomMenu(anchor: View, workout: Workout) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.menu_workout_item, null)
        val isCompleted = isWorkoutCompletedOnSelectedDate(workout)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val dayOffBtn = menuView.findViewById<View>(R.id.menu_take_day_off)
        val actionText = menuView.findViewById<TextView>(R.id.tv_action_text)
        actionText.text = "TAKE DAY OFF"
        menuView.findViewById<ImageView>(R.id.iv_action_icon).setImageResource(R.drawable.icons8_coffee_100)
        
        dayOffBtn.visibility = if (isCompleted || selectedDateString != todayDateString) View.GONE else View.VISIBLE
        dayOffBtn.setOnClickListener {
            workout.isCompleted = true
            workout.isDayOff = true
            workout.isExpanded = false
            workout.dailyProgress[todayDateString] = 100
            
            if (!workout.completedDates.contains(todayDateString)) {
                workout.completedDates.add(todayDateString)
                if (DataManager.addXP(context, 10)) {
                    android.widget.Toast.makeText(context, "LEVEL UP! You are now Level ${DataManager.userLevel}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            applyFilterAndSort()
            onProgressChanged(true)
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(context, AddWorkoutActivity::class.java).apply {
                putExtra("WORKOUT_ID", workout.timestamp)
            }
            context.startActivity(intent)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allWorkouts.remove(workout)
            applyFilterAndSort()
            onProgressChanged(false)
            popupWindow.dismiss()
        }

        val undoView = menuView.findViewById<View>(R.id.menu_undo)
        undoView.visibility = if (isCompleted) View.VISIBLE else View.GONE
        undoView.setOnClickListener {
            if (selectedDateString == todayDateString) {
                workout.isCompleted = false
                workout.progress = 0
                workout.isDayOff = false
                workout.dailyProgress.remove(todayDateString)
            }
            
            workout.completedDates.remove(selectedDateString)
            workout.dailyProgress.remove(selectedDateString)

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

    fun sortWorkouts() {
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        displayItems.clear()

        val filtered = allWorkouts.filter { workout ->
            val matchesFilter = if (currentFilter == "All") {
                true
            } else {
                if (DataManager.workoutFilterType == "TIME") {
                    workout.frequency == currentFilter
                } else {
                    workout.muscleGroups.contains(currentFilter)
                }
            }
            
            val matchesDay = if (workout.repeatType == "SPECIFIC_DAYS") {
                workout.repeatDays.contains(selectedDayIndex)
            } else {
                true 
            }
            val isAvailableOnDate = DataManager.getTrackingDateString(workout.timestamp) <= selectedDateString
            matchesFilter && matchesDay && isAvailableOnDate
        }

        val activeWorkouts = filtered.filter { !isWorkoutCompletedOnSelectedDate(it) }.sortedByDescending { it.timestamp }
        val allCompleted = filtered.filter { isWorkoutCompletedOnSelectedDate(it) }.sortedByDescending { it.timestamp }
        
        val dayOffWorkouts = allCompleted.filter { it.isDayOff }
        val strictlyCompleted = allCompleted.filter { !it.isDayOff }

        displayItems.addAll(activeWorkouts)

        if (dayOffWorkouts.isNotEmpty()) {
            displayItems.add("Day Off ${dayOffWorkouts.size}")
            if (isDayOffExpanded) {
                displayItems.addAll(dayOffWorkouts)
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

    fun setShowCompleted(show: Boolean) {
        showCompleted = show
        applyFilterAndSort()
    }

    fun collapseAll() {
        allWorkouts.forEach { it.isExpanded = false }
        applyFilterAndSort()
    }

    private fun updateVisuals(holder: WorkoutViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.workoutName.paintFlags = holder.workoutName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.mainContainer.alpha = 0.5f
        } else {
            holder.workoutName.paintFlags = holder.workoutName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.mainContainer.alpha = 1.0f
        }
    }

    override fun getItemCount() = displayItems.size

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainContainer: View = itemView
        val expandChevron: ImageView = itemView.findViewById(R.id.iv_expand_chevron)
        val workoutCard: MaterialCardView = itemView.findViewById(R.id.workout_card)
        val workoutName: TextView = itemView.findViewById(R.id.workout_name)
        val workoutDetails: TextView = itemView.findViewById(R.id.workout_details)
        val workoutIcon: ImageView = itemView.findViewById(R.id.workout_icon)
        
        val expandableControls: LinearLayout = itemView.findViewById(R.id.expandable_controls)
        val layoutRepsControls: LinearLayout = itemView.findViewById(R.id.layout_reps_controls)
        val numberPicker: NumberPicker = itemView.findViewById(R.id.np_workout_progress)
        val btnFinishSelection: MaterialCardView = itemView.findViewById(R.id.btn_finish_selection)
        val tvSelectedNumCircle: TextView = itemView.findViewById(R.id.tv_selected_num_circle)
        val tvFinishRepsLabel: TextView = itemView.findViewById(R.id.tv_finish_reps_label)
        val btnFinishAll: MaterialCardView = itemView.findViewById(R.id.btn_finish_all)
        val btnStartTimer: MaterialCardView = itemView.findViewById(R.id.btn_start_timer_workout)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_header_title)
        val chevron: ImageView = itemView.findViewById(R.id.iv_header_chevron)
    }
}
