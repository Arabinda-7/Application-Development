package com.example.allinone.feature.workout.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.R
import com.example.allinone.data.model.Workout

/**
 * WorkoutViewHolder: Handles view binding for Workout items.
 */
class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val workoutName: TextView? = itemView.findViewById(R.id.workout_name)
    val workoutDetails: TextView? = itemView.findViewById(R.id.workout_details)
    val workoutIcon: ImageView? = itemView.findViewById(R.id.workout_icon)

    fun bind(workout: Workout, callback: WorkoutCallback?) {
        workoutName?.text = WorkoutFormatter.formatTitle(workout.name)
        workoutDetails?.text = WorkoutFormatter.formatTargetText(workout.target, workout.trackingMode, workout.repsPerSet)

        itemView.setOnClickListener {
            callback?.onWorkoutClicked(workout)
        }
    }
}

/**
 * WorkoutHeaderViewHolder: Section headers for exercise categories.
 */
class WorkoutHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView? = itemView.findViewById(R.id.tv_header_title)
    val chevron: ImageView? = itemView.findViewById(R.id.iv_header_chevron)

    fun bind(headerText: String, isExpanded: Boolean, callback: WorkoutCallback?) {
        title?.text = headerText
        chevron?.rotation = if (isExpanded) 180f else 0f
        itemView.setOnClickListener {
            callback?.onHeaderClicked(headerText)
        }
    }
}
