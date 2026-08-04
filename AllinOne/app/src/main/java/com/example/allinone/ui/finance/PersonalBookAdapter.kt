package com.example.allinone.ui.finance

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.DataManager
import com.example.allinone.R
import com.example.allinone.data.model.PersonalLedgerEntry
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class PersonalBookAdapter(
    private val context: Context,
    private val entries: MutableList<PersonalLedgerEntry>,
    private var showHistory: Boolean,
    private val onSettle: (PersonalLedgerEntry) -> Unit,
    private val onEdit: (PersonalLedgerEntry) -> Unit,
    private val onDelete: (PersonalLedgerEntry) -> Unit,
    private val onUndo: (PersonalLedgerEntry) -> Unit,
    private val onToggleExpansion: (Int) -> Unit
) : RecyclerView.Adapter<PersonalBookAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun updateData(showHistory: Boolean) {
        this.showHistory = showHistory
        notifyDataSetChanged()
    }

    private fun getFilteredEntries() = entries.filter { it.isSettled == showHistory }
        .sortedByDescending { if (showHistory) it.settlementTimestamp ?: 0 else it.timestamp }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getFilteredEntries()[position]
        holder.tvType.text = entry.type.uppercase()
        val remaining = entry.amount - entry.paidAmount
        holder.tvAmount.text = "${DataManager.financeCurrency}${remaining.toInt()}"
        holder.tvDate.text = sdf.format(Date(entry.timestamp))
        holder.progressBar.progress = if (entry.amount > 0) ((entry.paidAmount / entry.amount) * 100).toInt() else 0

        val isOverdue = entry.dueDate?.let { it < System.currentTimeMillis() && !entry.isSettled } ?: false
        
        if (showHistory) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#0DFFFFFF"))
            holder.cardView.strokeColor = Color.parseColor("#00000000")
            holder.cardView.alpha = 0.6f
            holder.tvType.setTextColor(Color.parseColor("#80FFFFFF"))
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#1A1A1A"))
            holder.cardView.alpha = 1.0f
            holder.cardView.strokeColor = Color.parseColor(if (isOverdue) "#FF5252" else "#22FFFFFF")
            holder.cardView.strokeWidth = if (isOverdue) (2 * context.resources.displayMetrics.density).toInt() else (1 * context.resources.displayMetrics.density).toInt()
            
            val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
            holder.tvType.setTextColor(typeColor)
            holder.tvAmount.setTextColor(typeColor)
        }

        holder.btnSettle.setOnClickListener {
            onSettle(entry)
        }

        holder.itemView.setOnClickListener {
            entry.isExpanded = !entry.isExpanded
            onToggleExpansion(position)
        }

        holder.itemView.setOnLongClickListener {
            showEntryMenu(it, entry)
            true
        }
    }

    private fun showEntryMenu(anchor: View, entry: PersonalLedgerEntry) {
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.menu_ledger_item, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnUndo = menuView.findViewById<View>(R.id.menu_undo)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)
        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE

        btnEdit.visibility = if (showHistory) View.GONE else View.VISIBLE
        btnUndo.visibility = if (showHistory) View.VISIBLE else View.GONE

        btnUndo.setOnClickListener {
            onUndo(entry)
            popupWindow.dismiss()
        }

        btnEdit.setOnClickListener {
            popupWindow.dismiss()
            onEdit(entry)
        }

        btnDelete.setOnClickListener {
            onDelete(entry)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    override fun getItemCount() = getFilteredEntries().size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardView: MaterialCardView = v as MaterialCardView
        val tvType: TextView = v.findViewById(R.id.tv_ledger_type)
        val tvAmount: TextView = v.findViewById(R.id.tv_ledger_amount)
        val tvDate: TextView = v.findViewById(R.id.tv_ledger_date)
        val btnSettle: ImageView = v.findViewById(R.id.btn_settle_ledger)
        val progressBar: ProgressBar = v.findViewById(R.id.pb_debt_progress_circular)
    }
}
