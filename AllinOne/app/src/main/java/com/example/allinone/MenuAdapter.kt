package com.example.allinone

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class MenuItem(
    val title: String, 
    val icon: Int, 
    val activity: Class<*>, 
    var progress: Int = 0, 
    var showProgress: Boolean = true,
    var colorResId: Int = R.color.chip_background
)

class MenuAdapter(private val context: Context, private val menuItems: MutableList<MenuItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.grid_item_layout, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.menuTitle.text = menuItem.title
        holder.menuIcon.setImageResource(menuItem.icon)
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, menuItem.colorResId))
        
        if (menuItem.showProgress) {
            holder.menuProgressPercentage.visibility = View.VISIBLE
            holder.menuProgressPercentage.text = "${menuItem.progress}%"
        } else {
            holder.menuProgressPercentage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, menuItem.activity)
            context.startActivity(intent)
        }
    }
    
    fun updateProgress(title: String, progress: Int) {
        val index = menuItems.indexOfFirst { it.title == title }
        if (index != -1) {
            menuItems[index].progress = progress
            notifyItemChanged(index)
        }
    }

    override fun getItemCount() = menuItems.size

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        val menuIcon: ImageView = itemView.findViewById(R.id.menu_icon)
        val menuTitle: TextView = itemView.findViewById(R.id.menu_title)
        val menuProgressPercentage: TextView = itemView.findViewById(R.id.menu_progress_percentage)
    }
}
