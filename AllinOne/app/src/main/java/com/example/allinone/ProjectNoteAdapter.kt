package com.example.allinone

import android.graphics.Paint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ProjectNoteAdapter(
    private var notes: MutableList<Note>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<ProjectNoteAdapter.ProjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.project_note_item, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val note = notes[position]
        val context = holder.itemView.context
        val isCompleted = note.status == "Completed"

        holder.title.text = note.title
        holder.content.text = note.content
        
        // Visual Completion Feedback
        if (isCompleted) {
            holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.card.alpha = 0.5f
            holder.card.strokeWidth = 2
            holder.card.strokeColor = Color.parseColor("#2EC4B6")
        } else {
            holder.title.paintFlags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.card.alpha = 1.0f
            holder.card.strokeWidth = 0
        }
        
        // Deadline/Date
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        if (note.deadline != null) {
            val daysLeft = ((note.deadline!! - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
            val deadlineStr = sdf.format(Date(note.deadline!!))
            if (daysLeft < 0) {
                holder.date.text = "OVERDUE: $deadlineStr"
                holder.date.setTextColor(Color.RED)
            } else {
                holder.date.text = "$daysLeft days left | $deadlineStr"
                holder.date.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
        } else {
            holder.date.text = sdf.format(Date(note.timestamp))
            holder.date.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        // Card Color
        val color = if (note.color != -1) note.color else ContextCompat.getColor(context, R.color.primary_blue)
        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.chip_background))
        holder.title.setTextColor(color)

        // Pin
        holder.ivPin.visibility = if (note.isPinned) View.VISIBLE else View.GONE
        
        // Completion Check
        holder.ivCompletedCheck.visibility = if (note.status == "Completed") View.VISIBLE else View.GONE

        // Priority
        val priorityText = when (note.priority) {
            2 -> "HIGH"
            1 -> "MED"
            else -> "LOW"
        }
        val priorityColor = when (note.priority) {
            2 -> Color.RED
            1 -> Color.parseColor("#FFB800") // Yellow/Gold
            else -> Color.parseColor("#2EC4B6") // Teal
        }
        holder.tvPriorityBadge.text = priorityText
        holder.tvPriorityBadge.setTextColor(priorityColor)
        holder.tvPriorityBadge.backgroundTintList = ColorStateList.valueOf(priorityColor).withAlpha(40)

        // Status Badge
        holder.statusBadge.text = note.status.uppercase()
        val statusColor = when (note.status) {
            "Completed" -> Color.parseColor("#2EC4B6")
            "In Progress" -> Color.parseColor("#1A73E8")
            "On Hold" -> Color.parseColor("#FF7A59")
            else -> Color.GRAY
        }
        holder.statusBadge.backgroundTintList = ColorStateList.valueOf(statusColor.apply { 
            // Add transparency for badge background if needed, but here we just use the color
        }).withAlpha(60)
        holder.statusBadge.setTextColor(statusColor)

        // Progress
        holder.containerProgress.visibility = View.VISIBLE
        holder.progressBar.progress = if (note.status == "Completed") 100 else note.progress
        holder.tvProgressPercent.text = "${holder.progressBar.progress}%"
        holder.progressBar.progressTintList = ColorStateList.valueOf(statusColor)

        holder.itemView.setOnClickListener {
            (context as? ProjectActivity)?.showProjectDetailsDialog(note)
        }

        holder.ivHistory.setOnClickListener {
            (context as? ProjectActivity)?.showProjectHistoryDialog(note)
        }

        holder.itemView.setOnLongClickListener {
            (context as? ProjectActivity)?.showProjectMenu(it, note)
            true
        }
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes.toMutableList()
        notifyDataSetChanged()
    }

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.project_note_card)
        val title: TextView = itemView.findViewById(R.id.tv_note_title)
        val content: TextView = itemView.findViewById(R.id.tv_note_content)
        val date: TextView = itemView.findViewById(R.id.tv_note_date)
        val statusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        val ivPin: ImageView = itemView.findViewById(R.id.iv_pin)
        val ivCompletedCheck: ImageView = itemView.findViewById(R.id.iv_completed_check)
        val ivHistory: ImageView = itemView.findViewById(R.id.iv_history_icon)
        val tvPriorityBadge: TextView = itemView.findViewById(R.id.tv_priority_badge)
        val containerProgress: LinearLayout = itemView.findViewById(R.id.container_progress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val tvProgressPercent: TextView = itemView.findViewById(R.id.tv_progress_percent)
    }
}
