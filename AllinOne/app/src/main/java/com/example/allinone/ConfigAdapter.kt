package com.example.allinone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

data class ConfigItem(
    val title: String,
    val summary: String = "",
    val isToggle: Boolean = false,
    var isChecked: Boolean = false,
    val action: () -> Unit
)

class ConfigAdapter(
    private val items: List<ConfigItem>,
    private val onAnyChange: () -> Unit
) : RecyclerView.Adapter<ConfigAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_config_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.summary.text = item.summary
        
        holder.switch.visibility = if (item.isToggle) View.VISIBLE else View.GONE
        holder.switch.isChecked = item.isChecked
        
        holder.chevron.visibility = if (item.isToggle) View.GONE else View.VISIBLE
        
        holder.itemView.setOnClickListener {
            item.action()
            if (item.isToggle) {
                item.isChecked = !item.isChecked
                holder.switch.isChecked = item.isChecked
            }
            onAnyChange()
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tv_config_title)
        val summary: TextView = v.findViewById(R.id.tv_config_summary)
        val switch: SwitchCompat = v.findViewById(R.id.sw_config_toggle)
        val chevron: View = v.findViewById(R.id.iv_chevron)
    }
}