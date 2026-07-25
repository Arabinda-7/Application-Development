package com.example.allinone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ProjectHistoryAdapter(private val history: List<ProjectHistory>) :
    RecyclerView.Adapter<ProjectHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = history[position]
        holder.tvAction.text = item.action
        holder.tvDescription.text = item.description
        
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(item.timestamp))
    }

    override fun getItemCount(): Int = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAction: TextView = itemView.findViewById(R.id.tv_history_action)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_history_description)
        val tvTime: TextView = itemView.findViewById(R.id.tv_history_time)
    }
}
