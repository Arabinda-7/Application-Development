package com.example.allinone

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

import com.example.allinone.data.model.PersonalLedgerEntry

class LedgerAdapter(
    private var entries: List<PersonalLedgerEntry>,
    private val onUpdate: () -> Unit,
    private val onShowMenu: (View, PersonalLedgerEntry, Boolean, () -> Unit) -> Unit,
    private val onAddPayment: (PersonalLedgerEntry) -> Unit,
    private val onConfirmSettlement: (PersonalLedgerEntry) -> Unit
) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    fun updateEntries(newEntries: List<PersonalLedgerEntry>) {
        this.entries = newEntries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_main, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvName.text = entry.personName
        holder.tvType.text = entry.type.uppercase()
        
        val remaining = entry.amount - entry.paidAmount
        holder.tvAmount.text = "${DataManager.financeCurrency}${remaining.toInt()}"
        holder.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
        
        val progress = if (entry.amount > 0) ((entry.paidAmount / entry.amount) * 100).toInt() else 0
        holder.progressBar.progress = progress

        val isOverdue = entry.dueDate?.let { it < System.currentTimeMillis() && !entry.isSettled } ?: false
        val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
        
        holder.cardView.setCardBackgroundColor(Color.TRANSPARENT)
        holder.cardView.strokeWidth = (1.5f * holder.itemView.resources.displayMetrics.density).toInt()
        
        if (isOverdue) {
            holder.cardView.strokeColor = Color.parseColor("#FF5252")
            holder.cardView.strokeWidth = (2.5f * holder.itemView.resources.displayMetrics.density).toInt()
        } else {
            holder.cardView.strokeColor = typeColor
        }

        if (entry.isExpanded && entry.paymentHistory.isNotEmpty()) {
            holder.historyContainer.visibility = View.VISIBLE
            holder.historyContainer.removeAllViews()
            
            val title = TextView(holder.itemView.context).apply {
                text = "PAYMENT LOG"
                setTextColor(Color.parseColor("#80FFFFFF"))
                textSize = 10f
                setPadding(0, 8, 0, 8)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            holder.historyContainer.addView(title)

            entry.paymentHistory.forEach { payment ->
                val row = LinearLayout(holder.itemView.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                    
                    val tvDate = TextView(holder.itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(payment.timestamp))
                        setTextColor(Color.parseColor("#B0B0B0"))
                        textSize = 11f
                    }
                    val tvAmt = TextView(holder.itemView.context).apply {
                        text = "+${DataManager.financeCurrency}${payment.amount.toInt()}"
                        setTextColor(Color.parseColor("#4CAF50"))
                        textSize = 11f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    addView(tvDate)
                    addView(tvAmt)
                }
                holder.historyContainer.addView(row)
            }
        } else {
            holder.historyContainer.visibility = View.GONE
        }

        if (entry.paidAmount > 0 && !entry.isSettled) {
            holder.tvNote.text = "Paid: ${DataManager.financeCurrency}${entry.paidAmount.toInt()} / ${DataManager.financeCurrency}${entry.amount.toInt()}\n${entry.note}"
            holder.tvNote.visibility = View.VISIBLE
        } else if (entry.note.isNotEmpty()) {
            holder.tvNote.text = entry.note
            holder.tvNote.visibility = View.VISIBLE
        } else {
            holder.tvNote.visibility = View.GONE
        }

        if (entry.dueDate != null && !entry.isSettled) {
            val sdfDue = SimpleDateFormat("MMM dd", Locale.getDefault())
            entry.dueDate?.let { dueDate ->
                holder.tvDueDate.text = if (isOverdue) "OVERDUE: ${sdfDue.format(Date(dueDate))}" else "DUE: ${sdfDue.format(Date(dueDate))}"
                holder.tvDueDate.visibility = View.VISIBLE
                holder.tvDueDate.setTextColor(if (isOverdue) Color.RED else Color.parseColor("#FFB800"))
            } ?: run { holder.tvDueDate.visibility = View.GONE }
        } else {
            holder.tvDueDate.visibility = View.GONE
        }

        holder.tvType.setTextColor(typeColor)
        holder.tvAmount.setTextColor(typeColor)

        holder.tvAmount.setOnClickListener { onAddPayment(entry) }
        holder.tvName.setOnClickListener {
            val intent = Intent(holder.itemView.context, PersonLedgerActivity::class.java).apply {
                putExtra("personName", entry.personName)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.btnSettle.setOnClickListener { onConfirmSettlement(entry) }
        holder.itemView.setOnClickListener {
            entry.isExpanded = !entry.isExpanded
            notifyItemChanged(position)
        }
        holder.itemView.setOnLongClickListener {
            onShowMenu(it, entry, false) {}
            true
        }
    }

    override fun getItemCount(): Int = entries.size

    class LedgerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view as MaterialCardView
        val tvName: TextView = view.findViewById(R.id.tv_person_name)
        val tvType: TextView = view.findViewById(R.id.tv_ledger_type)
        val tvAmount: TextView = view.findViewById(R.id.tv_ledger_amount)
        val tvNote: TextView = view.findViewById(R.id.tv_ledger_note)
        val tvDueDate: TextView = view.findViewById(R.id.tv_due_date_label)
        val tvDate: TextView = view.findViewById(R.id.tv_ledger_date)
        val btnSettle: ImageView = view.findViewById(R.id.btn_settle_ledger)
        val progressBar: ProgressBar = view.findViewById(R.id.pb_debt_progress_circular)
        val historyContainer: LinearLayout = view.findViewById(R.id.container_payment_history)
    }
}
