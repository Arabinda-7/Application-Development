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
    private val onProgressChanged: () -> Unit,
    private val onTimerStart: (Workout, Int) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var displayWorkouts = allWorkouts.toMutableList()
    private var currentFilter = "All"
    private var selectedDayIndex = 0
    private var selectedDateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private val todayDateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_item, parent, false)
        return WorkoutViewHolder(view)
    }

    private fun isWorkoutCompletedOnSelectedDate(workout: Workout): Boolean {
        return if (selectedDateString == todayDateString) {
            workout.isCompleted
        } else {
            workout.completedDates.contains(selectedDateString)
        }
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = displayWorkouts[position]
        val context = holder.itemView.context
        val isCompleted = isWorkoutCompletedOnSelectedDate(workout)

        holder.workoutName.text = workout.name
        
        if (workout.isDayOff && selectedDateString == todayDateString) {
            holder.workoutDetails.text = "DAY OFF"
        } else {
            val details = when (workout.trackingMode) {
                "Timer" -> "${workout.target}s"
                else -> "${workout.progress}/${workout.target} ${workout.trackingMode}"
            }
            holder.workoutDetails.text = details
        }

        val cardColor = if (workout.color != -1) {
            workout.color
        } else {
            ContextCompat.getColor(context, R.color.card_blue)
        }
        holder.workoutCard.setCardBackgroundColor(cardColor)

        if (workout.iconResId != -1) {
            holder.workoutIcon.setImageResource(workout.iconResId)
        }

        // Expansion Logic with Smooth Transition
        // Force hide controls if completed or not expanded to prevent accidental showing
        val shouldShowControls = workout.isExpanded && !isCompleted && selectedDateString == todayDateString
        holder.expandableControls.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
        holder.expandChevron.rotation = if (workout.isExpanded) 270f else 90f

        holder.workoutCard.setOnClickListener {
            if (isCompleted || selectedDateString != todayDateString) {
                val intent = Intent(context, WorkoutDetailActivity::class.java).apply {
                    putExtra("WORKOUT_NAME", workout.name)
                    putExtra("WORKOUT_ID", workout.timestamp)
                }
                context.startActivity(intent)
            } else {
                TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                workout.isExpanded = !workout.isExpanded
                notifyItemChanged(position)
            }
        }
        
        holder.workoutName.setOnClickListener {
            val intent = Intent(context, WorkoutDetailActivity::class.java).apply {
                putExtra("WORKOUT_NAME", workout.name)
                putExtra("WORKOUT_ID", workout.timestamp)
            }
            context.startActivity(intent)
        }

        holder.expandChevron.setOnClickListener {
            if (!isCompleted && selectedDateString == todayDateString) {
                TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                workout.isExpanded = !workout.isExpanded
                notifyItemChanged(position)
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

            // Finish Selected Reps
            holder.btnFinishSelection.setOnClickListener {
                val addedValue = holder.numberPicker.value
                if (addedValue > 0) {
                    workout.progress += addedValue
                    if (workout.progress >= workout.target) {
                        workout.isCompleted = true
                        workout.isExpanded = false
                        if (!workout.completedDates.contains(todayDateString)) {
                            workout.completedDates.add(todayDateString)
                        }
                    }
                    // Collapse the spinner after finishing task
                    TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                    workout.isExpanded = false
                    applyFilterAndSort()
                    onProgressChanged()
                }
            }

            // Finish All
            holder.btnFinishAll.setOnClickListener {
                workout.progress = workout.target
                workout.isCompleted = true
                // Collapse the spinner after finishing task
                TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
                workout.isExpanded = false
                
                if (!workout.completedDates.contains(todayDateString)) {
                    workout.completedDates.add(todayDateString)
                }
                
                applyFilterAndSort()
                onProgressChanged()
            }
        }

        holder.editButton.setOnClickListener { showCustomMenu(it, position) }
        
        updateVisuals(holder, isCompleted)
    }

    private fun updateFinishSelectionUI(holder: WorkoutViewHolder, value: Int, mode: String) {
        holder.tvSelectedNumCircle.text = value.toString()
        holder.tvFinishRepsLabel.text = "$value ${mode.uppercase()}"
    }

    private fun showCustomMenu(anchor: View, position: Int) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val workout = displayWorkouts[position]
        val isCompleted = isWorkoutCompletedOnSelectedDate(workout)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val dayOffBtn = menuView.findViewById<View>(R.id.menu_take_day_off)
        dayOffBtn.visibility = if (isCompleted || selectedDateString != todayDateString) View.GONE else View.VISIBLE
        dayOffBtn.setOnClickListener {
            workout.isCompleted = true
            workout.isDayOff = true
            workout.isExpanded = false
            
            if (!workout.completedDates.contains(todayDateString)) {
                workout.completedDates.add(todayDateString)
            }
            
            applyFilterAndSort()
            onProgressChanged()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? WorkoutRoutineActivity)?.showAddWorkoutDialog(workout)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allWorkouts.remove(workout)
            applyFilterAndSort()
            onProgressChanged()
            popupWindow.dismiss()
        }

        val undoView = menuView.findViewById<View>(R.id.menu_undo)
        undoView.visibility = if (isCompleted) View.VISIBLE else View.GONE
        undoView.setOnClickListener {
            if (selectedDateString == todayDateString) {
                workout.isCompleted = false
                workout.progress = 0
                workout.isDayOff = false
            }
            
            workout.completedDates.remove(selectedDateString)

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

    fun sortWorkouts() {
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        displayWorkouts = allWorkouts.filter { workout ->
            val matchesTime = if (currentFilter == "All") true else workout.frequency == currentFilter
            val matchesDay = if (workout.repeatType == "SPECIFIC_DAYS") {
                workout.repeatDays.contains(selectedDayIndex)
            } else {
                true 
            }
            matchesTime && matchesDay
        }.toMutableList()
        displayWorkouts.sortWith(compareBy({ isWorkoutCompletedOnSelectedDate(it) }, { it.timestamp }))
        notifyDataSetChanged()
    }

    private fun updateVisuals(holder: WorkoutViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.workoutName.paintFlags = holder.workoutName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.mainContainer.alpha = 0.6f
        } else {
            holder.workoutName.paintFlags = holder.workoutName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.mainContainer.alpha = 1.0f
        }
    }

    override fun getItemCount() = displayWorkouts.size

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainContainer: View = itemView
        val expandChevron: ImageView = itemView.findViewById(R.id.iv_expand_chevron)
        val workoutCard: MaterialCardView = itemView.findViewById(R.id.workout_card)
        val workoutName: TextView = itemView.findViewById(R.id.workout_name)
        val workoutDetails: TextView = itemView.findViewById(R.id.workout_details)
        val workoutIcon: ImageView = itemView.findViewById(R.id.workout_icon)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_workout_button)
        
        val expandableControls: LinearLayout = itemView.findViewById(R.id.expandable_controls)
        val layoutRepsControls: LinearLayout = itemView.findViewById(R.id.layout_reps_controls)
        val numberPicker: NumberPicker = itemView.findViewById(R.id.np_workout_progress)
        val btnFinishSelection: MaterialCardView = itemView.findViewById(R.id.btn_finish_selection)
        val tvSelectedNumCircle: TextView = itemView.findViewById(R.id.tv_selected_num_circle)
        val tvFinishRepsLabel: TextView = itemView.findViewById(R.id.tv_finish_reps_label)
        val btnFinishAll: MaterialCardView = itemView.findViewById(R.id.btn_finish_all)
        val btnStartTimer: MaterialCardView = itemView.findViewById(R.id.btn_start_timer_workout)
    }
}
