package com.example.allinone

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

data class MonthItem(
    val name: String,
    val icon: Int,
    val color: String
)

class MonthAdapter(
    private val items: List<MonthItem>,
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    private val onMonthClick: (String) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    fun updateYear(year: Int) {
        currentYear = year
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month_grid, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        holder.cardView.setCardBackgroundColor(Color.parseColor(item.color))
        holder.iconView.setImageResource(item.icon)
        holder.nameView.text = item.name.uppercase().take(3)

        // Feature: At-a-Glance Summary & Progress Ring
        val sdfMonth = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, position)
        val monthKey = sdfMonth.format(cal.time)

        val budget = DataManager.monthlyBudgets[monthKey] ?: 0.0
        val monthlyTransactions = DataManager.transactions.filter {
            SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(it.timestamp)) == monthKey
        }
        val spent = monthlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount }

        if (spent > 0) {
            holder.summaryView.text = String.format(Locale.US, "%s%.0f", DataManager.financeCurrency, spent)
            holder.summaryView.visibility = View.VISIBLE
            
            if (budget > 0) {
                val progress = ((spent / budget) * 100).toInt().coerceIn(0, 100)
                holder.progressRing.progress = progress
                
                // Feature: Financial Health Tinting
                val tintColor = if (spent > budget) Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
                holder.progressRing.progressTintList = ColorStateList.valueOf(tintColor)
            } else {
                holder.progressRing.progress = 0
            }
        } else {
            holder.summaryView.visibility = View.GONE
            holder.progressRing.progress = 0
        }

        // Feature: Current Month Highlight
        val now = Calendar.getInstance()
        if (currentYear == now.get(Calendar.YEAR) && position == now.get(Calendar.MONTH)) {
            holder.cardView.strokeWidth = (2 * context.resources.displayMetrics.density).toInt()
            holder.cardView.strokeColor = Color.parseColor("#1A73E8")
        } else {
            holder.cardView.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
            holder.cardView.strokeColor = Color.parseColor("#33FFFFFF")
        }

        // Feature: Interactive Scale Animation
        holder.itemView.setOnClickListener {
            holder.itemView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                holder.itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                onMonthClick(item.name)
            }.start()
        }
    }

    override fun getItemCount() = items.size

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val iconView: ImageView = itemView.findViewById(R.id.iv_month_icon)
        val nameView: TextView = itemView.findViewById(R.id.tv_month_name)
        val summaryView: TextView = itemView.findViewById(R.id.tv_month_summary)
        val progressRing: ProgressBar = itemView.findViewById(R.id.month_progress_ring)
    }
}

