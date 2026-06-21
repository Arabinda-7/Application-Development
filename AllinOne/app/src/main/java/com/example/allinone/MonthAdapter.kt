package com.example.allinone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class MonthItem(
    val name: String,
    val icon: Int,
    val color: String
)

class MonthAdapter(
    private val items: List<MonthItem>,
    private val onMonthClick: (String) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month_grid, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val item = items[position]
        
        holder.cardView.setCardBackgroundColor(Color.parseColor(item.color))
        holder.iconView.setImageResource(item.icon)
        holder.nameView.text = item.name.uppercase().take(3)
        
        holder.itemView.setOnClickListener { onMonthClick(item.name) }
    }

    override fun getItemCount() = items.size

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val iconView: ImageView = itemView.findViewById(R.id.iv_month_icon)
        val nameView: TextView = itemView.findViewById(R.id.tv_month_name)
    }
}
