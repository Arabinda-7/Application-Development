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
    val options: List<String>? = null,
    val selectedIndex: Int? = null,
    val onOptionSelected: ((Int) -> Unit)? = null,
    val isHeader: Boolean = false,
    val action: () -> Unit = {}
)

class ConfigAdapter(
    private val items: List<ConfigItem>,
    private val onAnyChange: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 0
    private val TYPE_HEADER = 1

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isHeader) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_settings_header, parent, false))
        } else {
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_config_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        
        if (holder is ItemViewHolder) {
            holder.title.text = item.title
            holder.summary.text = item.summary
            
            holder.switch.visibility = if (item.isToggle) View.VISIBLE else View.GONE
            holder.switch.isChecked = item.isChecked
            
            holder.chevron.visibility = if (item.isToggle || item.options != null) View.GONE else View.VISIBLE
            
            holder.itemView.setOnClickListener {
                if (item.options != null && item.onOptionSelected != null) {
                    showOptionsDialog(holder.itemView.context, item)
                } else {
                    item.action()
                    if (item.isToggle) {
                        item.isChecked = !item.isChecked
                        holder.switch.isChecked = item.isChecked
                    }
                    onAnyChange()
                }
            }
        } else if (holder is HeaderViewHolder) {
            holder.title.text = item.title.uppercase()
        }
    }

    private fun showOptionsDialog(context: android.content.Context, item: ConfigItem) {
        val dialog = android.app.Dialog(context)
        dialog.setContentView(R.layout.dialog_settings_selection)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val container = dialog.findViewById<ViewGroup>(R.id.options_container)
        val btnCancel = dialog.findViewById<View>(R.id.btn_cancel)

        title.text = item.title.uppercase()
        container.removeAllViews()

        item.options?.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_settings_selection, container, false) as TextView
            itemView.text = option
            
            if (item.selectedIndex == index) {
                itemView.setBackgroundResource(R.drawable.item_selection_highlight)
                itemView.setTypeface(null, android.graphics.Typeface.BOLD)
                itemView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icons8_checkmark_100, 0)
                itemView.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                
                // Scale down the checkmark icon a bit
                val drawables = itemView.compoundDrawables
                drawables[2]?.let { 
                    val size = (18 * context.resources.displayMetrics.density).toInt()
                    it.setBounds(0, 0, size, size)
                    itemView.setCompoundDrawables(null, null, it, null)
                }
            } else {
                itemView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }

            itemView.setOnClickListener {
                item.onOptionSelected?.invoke(index)
                onAnyChange()
                dialog.dismiss()
            }
            container.addView(itemView)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun getItemCount() = items.size

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tv_config_title)
        val summary: TextView = v.findViewById(R.id.tv_config_summary)
        val switch: SwitchCompat = v.findViewById(R.id.sw_config_toggle)
        val chevron: View = v.findViewById(R.id.iv_chevron)
    }

    class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tv_header_title)
    }
}