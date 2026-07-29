package com.example.allinone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class LedgerHistoryListSection(
    private val recyclerView: RecyclerView,
    private val onLongClick: (View, LedgerEntry) -> Unit,
    private val onDelete: (LedgerEntry) -> Unit
) {
    private val adapter: HistoryAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        adapter = HistoryAdapter(mutableListOf(), onLongClick, onDelete)
        recyclerView.adapter = adapter
    }

    fun update() {
        val settled = DataManager.ledgerEntries.filter { it.isSettled }
            .sortedByDescending { it.settlementTimestamp ?: it.timestamp }
        adapter.updateEntries(settled)
    }

    fun setDeleteMode(enabled: Boolean) {
        adapter.isDeleteMode = enabled
        adapter.notifyDataSetChanged()
    }

    class HistoryAdapter(
        private var entries: List<LedgerEntry>,
        private val onLongClick: (View, LedgerEntry) -> Unit,
        private val onDeleteClick: (LedgerEntry) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        var isDeleteMode: Boolean = false

        fun updateEntries(newEntries: List<LedgerEntry>) {
            this.entries = newEntries
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_history, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]
            holder.tvName.text = entry.personName
            holder.tvType.text = entry.type.uppercase()
            holder.tvAmount.text = "${DataManager.financeCurrency}${entry.amount.toInt()}"
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            holder.tvDate.text = "Logged: ${sdf.format(Date(entry.timestamp))}"
            
            entry.settlementTimestamp?.let {
                holder.tvSettledDate.text = "Settled: ${sdf.format(Date(it))}"
                holder.tvSettledDate.visibility = View.VISIBLE
            } ?: run { holder.tvSettledDate.visibility = View.GONE }

            val color = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
            holder.tvType.setTextColor(color)
            holder.ivStatus.setColorFilter(color)

            if (isDeleteMode) {
                holder.ivStatus.setImageResource(android.R.drawable.ic_menu_delete)
                holder.ivStatus.setColorFilter(Color.parseColor("#FF5252"))
                holder.ivStatus.setOnClickListener { onDeleteClick(entry) }
            } else {
                holder.ivStatus.setImageResource(R.drawable.icons8_checked_checkbox_100)
                holder.ivStatus.setColorFilter(color)
                holder.ivStatus.setOnClickListener(null)
            }

            holder.itemView.setOnLongClickListener {
                onLongClick(it, entry)
                true
            }
        }

        override fun getItemCount() = entries.size

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tv_person_name)
            val tvType: TextView = v.findViewById(R.id.tv_ledger_type)
            val tvAmount: TextView = v.findViewById(R.id.tv_ledger_amount)
            val tvDate: TextView = v.findViewById(R.id.tv_ledger_date)
            val tvSettledDate: TextView = v.findViewById(R.id.tv_settled_date)
            val ivStatus: ImageView = v.findViewById(R.id.iv_ledger_status)
        }
    }
}
