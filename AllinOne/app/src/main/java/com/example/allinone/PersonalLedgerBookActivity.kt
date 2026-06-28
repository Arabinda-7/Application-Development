package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PersonalLedgerBookActivity : AppCompatActivity() {

    private lateinit var ledger: PersonalLedger
    private lateinit var adapter: PersonalBookAdapter
    private var showHistory: Boolean = false
    
    private lateinit var tvOwe: TextView
    private lateinit var tvOwed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_ledger)

        val ledgerId = intent.getStringExtra("ledgerId") ?: ""
        ledger = DataManager.personalLedgers.find { it.id == ledgerId } ?: run { finish(); return }

        findViewById<TextView>(R.id.tv_person_title).text = ledger.personName.uppercase()
        tvOwe = findViewById(R.id.tv_person_owe)
        tvOwed = findViewById(R.id.tv_person_owed)

        val list = findViewById<RecyclerView>(R.id.person_ledger_list)
        list.layoutManager = LinearLayoutManager(this)
        
        adapter = PersonalBookAdapter()
        list.adapter = adapter

        updateSummary()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_person_settings).setOnClickListener {
            showPersonSettingsMenu(it)
        }

        findViewById<View>(R.id.btn_calculate_balance).setOnClickListener {
            showCalculateConfirmation()
        }

        findViewById<View>(R.id.btn_add_to_person).setOnClickListener {
            showAddEntryDialog()
        }
    }

    private fun showPersonSettingsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Toggle History")
        popup.menu.add("Delete Personal Ledger")
        
        popup.setOnMenuItemClickListener {
            when (it.title) {
                "Toggle History" -> {
                    showHistory = !showHistory
                    findViewById<View>(R.id.tv_history_label).visibility = if (showHistory) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, if (showHistory) "Showing History" else "Showing Active Ledger", Toast.LENGTH_SHORT).show()
                }
                "Delete Personal Ledger" -> {
                    showConfirmationDialog(
                        "DELETE BOOK",
                        "Are you sure you want to delete this entire personal ledger book for ${ledger.personName}?",
                        "DELETE",
                        onConfirm = {
                            DataManager.personalLedgers.remove(ledger)
                            DataManager.saveData(this)
                            finish()
                        }
                    )
                }
            }
            true
        }
        popup.show()
    }

    private fun updateSummary() {
        val activeEntries = ledger.entries.filter { !it.isSettled }
        val owe = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
        val owed = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
        
        val currency = DataManager.financeCurrency
        tvOwe.text = String.format(Locale.US, "%s%.0f", currency, owe)
        tvOwed.text = String.format(Locale.US, "%s%.0f", currency, owed)
    }

    private fun showCalculateConfirmation() {
        val activeEntries = ledger.entries.filter { !it.isSettled }
        if (activeEntries.isEmpty()) {
            Toast.makeText(this, "No active entries to calculate", Toast.LENGTH_SHORT).show()
            return
        }

        val totalOwe = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
        val totalOwed = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
        
        val currency = DataManager.financeCurrency
        val message = "Summary: Lent ${currency}${totalOwed.toInt()} | Borrowed ${currency}${totalOwe.toInt()}\n\n" +
            if (totalOwe == totalOwed) {
                "Balance is equal. Both will be settled. Proceed?"
            } else {
                val net = totalOwed - totalOwe
                val netType = if (net > 0) "Lent" else "Borrowed"
                "Covered entries will be settled and the remainder will update the latest entry. Proceed?"
            }

        showConfirmationDialog(
            "CHECK & CALCULATE",
            message,
            "CALCULATE",
            onConfirm = { performAutoReconciliation(totalOwe, totalOwed) }
        )
    }

    private fun performAutoReconciliation(totalOwe: Double, totalOwed: Double) {
        val now = System.currentTimeMillis()
        val activeEntries = ledger.entries.filter { !it.isSettled }.sortedBy { it.timestamp }
        
        val smallerSideAmount = if (totalOwed > totalOwe) totalOwe else totalOwed
        val typeToSettle = if (totalOwed > totalOwe) "Borrowed" else "Lent"
        
        // 1. Settle all entries of the smaller side
        activeEntries.filter { it.type == typeToSettle }.forEach {
            it.isSettled = true
            it.settlementTimestamp = now
            it.note += " (Offset via Check)"
        }

        // 2. Partial settlement of the larger side (FIFO)
        var remainingToOffset = smallerSideAmount
        val largerSideType = if (typeToSettle == "Borrowed") "Lent" else "Borrowed"
        
        activeEntries.filter { it.type == largerSideType }.forEach { entry ->
            if (remainingToOffset > 0) {
                val availableForOffset = entry.amount - entry.paidAmount
                val offsetApplied = Math.min(availableForOffset, remainingToOffset)
                
                entry.paidAmount += offsetApplied
                entry.paymentHistory.add(LedgerPayment(offsetApplied, now))
                remainingToOffset -= offsetApplied
                
                if (entry.paidAmount >= entry.amount) {
                    entry.isSettled = true
                    entry.settlementTimestamp = now
                }
            }
        }

        DataManager.saveData(this)
        updateSummary()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Balance reconciled successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showAddEntryDialog(existingEntry: PersonalLedgerEntry? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_ledger)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_ledger_amount)
        val etName = dialog.findViewById<EditText>(R.id.et_person_name)
        val etNote = dialog.findViewById<EditText>(R.id.et_ledger_note)
        val tvDueDate = dialog.findViewById<TextView>(R.id.tv_ledger_due_date)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_ledger_type)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_ledger)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_ledger)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)

        dialog.findViewById<View>(R.id.tv_person_label).visibility = View.GONE
        etName.visibility = View.GONE

        if (existingEntry != null) {
            tvTitle.text = "Edit Entry"
            etAmount.setText(existingEntry.amount.toString())
            etNote.setText(existingEntry.note)
            if (existingEntry.type == "Borrowed") rgType.check(R.id.radio_borrowed) else rgType.check(R.id.radio_lent)
            existingEntry.dueDate?.let { tvDueDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) }
        }

        var selectedDueDate: Long? = existingEntry?.dueDate
        tvDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDueDate?.let { cal.timeInMillis = it }
            android.app.DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d); selectedDueDate = cal.timeInMillis
                tvDueDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val type = if (rgType.checkedRadioButtonId == R.id.radio_borrowed) "Borrowed" else "Lent"
                if (existingEntry == null) {
                    val entry = PersonalLedgerEntry(amount = amount, type = type, note = etNote.text.toString().trim(), dueDate = selectedDueDate)
                    ledger.entries.add(0, entry)
                } else {
                    existingEntry.amount = amount; existingEntry.type = type; existingEntry.note = etNote.text.toString().trim(); existingEntry.dueDate = selectedDueDate
                }
                DataManager.saveData(this); updateSummary(); adapter.notifyDataSetChanged(); dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showConfirmationDialog(title: String, message: String, positiveButtonText: String = "PROCEED", onConfirm: () -> Unit) {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_confirm_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_confirm_message)
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_confirm_negative)
        val btnPositive = dialog.findViewById<TextView>(R.id.btn_confirm_positive)
        tvTitle.text = title; tvMessage.text = message; btnPositive.text = positiveButtonText
        btnNegative.setOnClickListener { dialog.dismiss() }
        btnPositive.setOnClickListener { onConfirm(); dialog.dismiss() }
        dialog.show()
    }

    inner class PersonalBookAdapter : RecyclerView.Adapter<PersonalBookAdapter.ViewHolder>() {
        
        private fun getFilteredEntries() = ledger.entries.filter { it.isSettled == showHistory }
            .sortedByDescending { if (showHistory) it.settlementTimestamp ?: 0 else it.timestamp }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ledger_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = getFilteredEntries()[position]
            holder.tvType.text = entry.type.uppercase()
            val remaining = entry.amount - entry.paidAmount
            holder.tvAmount.text = "${DataManager.financeCurrency}${remaining.toInt()}"
            holder.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
            holder.progressBar.progress = if (entry.amount > 0) ((entry.paidAmount / entry.amount) * 100).toInt() else 0

            val isOverdue = entry.dueDate != null && entry.dueDate!! < System.currentTimeMillis() && !entry.isSettled
            
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
                holder.cardView.strokeWidth = if (isOverdue) (2 * resources.displayMetrics.density).toInt() else (1 * resources.displayMetrics.density).toInt()
                
                val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
                holder.tvType.setTextColor(typeColor)
                holder.tvAmount.setTextColor(typeColor)
            }

            holder.btnSettle.setOnClickListener {
                entry.isSettled = true
                entry.settlementTimestamp = System.currentTimeMillis()
                DataManager.saveData(this@PersonalLedgerBookActivity)
                updateSummary()
                notifyDataSetChanged()
            }

            holder.itemView.setOnClickListener {
                entry.isExpanded = !entry.isExpanded
                notifyItemChanged(position)
            }

            holder.itemView.setOnLongClickListener {
                showEntryMenu(it, entry)
                true
            }
        }

        private fun showEntryMenu(anchor: View, entry: PersonalLedgerEntry) {
            val inflater = LayoutInflater.from(this@PersonalLedgerBookActivity)
            val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
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
                entry.isSettled = false
                DataManager.saveData(this@PersonalLedgerBookActivity)
                updateSummary()
                notifyDataSetChanged()
                popupWindow.dismiss()
                Toast.makeText(this@PersonalLedgerBookActivity, "Entry moved back to active", Toast.LENGTH_SHORT).show()
            }

            btnEdit.setOnClickListener {
                popupWindow.dismiss()
                showAddEntryDialog(entry)
            }

            btnDelete.setOnClickListener {
                ledger.entries.remove(entry)
                DataManager.saveData(this@PersonalLedgerBookActivity)
                updateSummary()
                notifyDataSetChanged()
                popupWindow.dismiss()
                Toast.makeText(this@PersonalLedgerBookActivity, "Entry deleted", Toast.LENGTH_SHORT).show()
            }

            popupWindow.showAsDropDown(anchor, 150, -100)
        }

        override fun getItemCount() = getFilteredEntries().size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val cardView: com.google.android.material.card.MaterialCardView = v as com.google.android.material.card.MaterialCardView
            val tvType: TextView = v.findViewById(R.id.tv_ledger_type)
            val tvAmount: TextView = v.findViewById(R.id.tv_ledger_amount)
            val tvDate: TextView = v.findViewById(R.id.tv_ledger_date)
            val btnSettle: ImageView = v.findViewById(R.id.btn_settle_ledger)
            val progressBar: ProgressBar = v.findViewById(R.id.pb_debt_progress_circular)
        }
    }
}
