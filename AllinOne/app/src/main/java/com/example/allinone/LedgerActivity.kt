package com.example.allinone

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
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

class LedgerActivity : AppCompatActivity() {

    private val allEntries = DataManager.ledgerEntries
    private val activeEntries = mutableListOf<LedgerEntry>()
    private lateinit var ledgerAdapter: LedgerAdapter
    
    private lateinit var tvTotalBorrowed: TextView
    private lateinit var tvTotalLent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ledger)

        tvTotalBorrowed = findViewById(R.id.tv_total_borrowed)
        tvTotalLent = findViewById(R.id.tv_total_lent)

        val ledgerList = findViewById<RecyclerView>(R.id.ledger_list)
        ledgerList.layoutManager = LinearLayoutManager(this)
        
        updateActiveEntries()
        ledgerAdapter = LedgerAdapter(activeEntries, 
            onUpdate = {
                DataManager.saveData(this)
                updateActiveEntries()
                updateSummary()
            },
            onShowMenu = { anchor, entry, isHistory, onAction ->
                showCustomLedgerMenu(anchor, entry, isHistory, onAction)
            },
            onConfirmSettlement = { entry ->
                showConfirmationDialog(
                    title = "SETTLE ENTRY",
                    message = "Are you sure you want to mark this entry as settled?",
                    positiveButtonText = "SETTLE",
                    onConfirm = {
                        entry.isSettled = true
                        entry.settlementTimestamp = System.currentTimeMillis()
                        DataManager.saveData(this)
                        updateActiveEntries()
                        updateSummary()
                    }
                )
            }
        )
        ledgerList.adapter = ledgerAdapter

        updateSummary()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_add_ledger).setOnClickListener { showAddLedgerDialog() }
        findViewById<View>(R.id.btn_ledger_history).setOnClickListener { 
            startActivity(Intent(this, LedgerHistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateActiveEntries()
        updateSummary()
    }

    private fun updateActiveEntries() {
        activeEntries.clear()
        activeEntries.addAll(allEntries.filter { !it.isSettled }.sortedByDescending { it.timestamp })
        if (::ledgerAdapter.isInitialized) {
            ledgerAdapter.notifyDataSetChanged()
        }
    }

    private fun updateSummary() {
        val activeBorrow = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
        val activeLent = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
        
        val currency = DataManager.financeCurrency
        tvTotalBorrowed.text = String.format(Locale.US, "%s%.0f", currency, activeBorrow)
        tvTotalLent.text = String.format(Locale.US, "%s%.0f", currency, activeLent)
    }

    private fun showLedgerHistoryDialog() {
        val dialog = Dialog(this, R.style.SeamlessDialog)
        dialog.setContentView(R.layout.dialog_project_history)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val historyList = dialog.findViewById<RecyclerView>(R.id.history_list)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_history)
        val title = dialog.findViewById<TextView>(R.id.tv_history_title)

        title.text = "SETTLED LEDGER"
        historyList.layoutManager = LinearLayoutManager(this)
        
        val settledEntries = allEntries.filter { it.isSettled }.sortedByDescending { it.timestamp }.toMutableList()
        
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_history, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val entry = settledEntries[position]
                val v = holder.itemView
                val tvName = v.findViewById<TextView>(R.id.tv_person_name)
                val tvAmount = v.findViewById<TextView>(R.id.tv_ledger_amount)
                
                tvName.text = entry.personName
                tvName.paintFlags = tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                
                v.findViewById<TextView>(R.id.tv_ledger_type).text = entry.type.uppercase()
                tvAmount.text = "${DataManager.financeCurrency}${entry.amount.toInt()}"
                v.findViewById<TextView>(R.id.tv_ledger_date).text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
                
                val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
                tvAmount.setTextColor(typeColor)

                v.setOnLongClickListener {
                    showCustomLedgerMenu(it, entry, true) {
                        settledEntries.remove(entry)
                        notifyDataSetChanged()
                        updateActiveEntries()
                        updateSummary()
                    }
                    true
                }
            }

            override fun getItemCount(): Int = settledEntries.size
        }
        historyList.adapter = adapter

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry, isHistory: Boolean, onAction: () -> Unit) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnUndo = menuView.findViewById<View>(R.id.menu_undo)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)
        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)
        val btnDayOff = menuView.findViewById<View>(R.id.menu_take_day_off)
        val btnHide = menuView.findViewById<View>(R.id.menu_hide_unhide)

        btnDayOff.visibility = View.GONE
        btnHide.visibility = View.GONE
        btnEdit.visibility = if (isHistory) View.GONE else View.VISIBLE
        btnUndo.visibility = if (isHistory) View.VISIBLE else View.GONE

        btnUndo.setOnClickListener {
            entry.isSettled = false
            DataManager.saveData(this)
            updateActiveEntries()
            updateSummary()
            onAction()
            popupWindow.dismiss()
        }

        if (!isHistory) {
            val paymentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
                val outValue = android.util.TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
                isClickable = true
                isFocusable = true
                
                val icon = ImageView(this@LedgerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx())
                    setImageResource(android.R.drawable.ic_input_add)
                    imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
                }
                val text = TextView(this@LedgerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        marginStart = 16.dpToPx()
                    }
                    text = "ADD PAYMENT"
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                addView(icon)
                addView(text)
                
                setOnClickListener {
                    popupWindow.dismiss()
                    showAddPaymentDialog(this@LedgerActivity, entry)
                }
            }
            (menuView as ViewGroup).getChildAt(0).let { (it as ViewGroup).addView(paymentLayout, 0) }
        }

        btnEdit.setOnClickListener {
            popupWindow.dismiss()
            showAddLedgerDialog(entry)
        }

        btnDelete.setOnClickListener {
            allEntries.remove(entry)
            DataManager.saveData(this)
            updateActiveEntries()
            updateSummary()
            onAction()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    private fun showAddPaymentDialog(context: Context, entry: LedgerEntry) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_payment)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etInput = dialog.findViewById<EditText>(R.id.et_payment_amount)
        val tvRemaining = dialog.findViewById<TextView>(R.id.tv_remaining_label)
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_confirm_payment)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_payment)

        val remaining = entry.amount - entry.paidAmount
        tvRemaining.text = "Amount remaining: ${DataManager.financeCurrency}${remaining.toInt()}"
        etInput.hint = "${DataManager.financeCurrency}0"

        dialog.findViewById<View>(R.id.btn_pay_25).setOnClickListener {
            etInput.setText((remaining * 0.25).toInt().toString())
        }
        dialog.findViewById<View>(R.id.btn_pay_50).setOnClickListener {
            etInput.setText((remaining * 0.50).toInt().toString())
        }
        dialog.findViewById<View>(R.id.btn_pay_full).setOnClickListener {
            etInput.setText(remaining.toInt().toString())
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val paid = etInput.text.toString().toDoubleOrNull() ?: 0.0
            if (paid > 0) {
                entry.paidAmount += paid
                if (entry.paidAmount >= entry.amount) {
                    entry.isSettled = true
                    entry.settlementTimestamp = System.currentTimeMillis()
                }
                DataManager.saveData(this)
                updateActiveEntries()
                updateSummary()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showAddLedgerDialog(existingEntry: LedgerEntry? = null) {
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

        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"

        var selectedDueDate: Long? = existingEntry?.dueDate
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (existingEntry != null) {
            tvTitle.text = "Edit Ledger"
            etAmount.setText(existingEntry.amount.toString())
            etName.setText(existingEntry.personName)
            etNote.setText(existingEntry.note)
            if (existingEntry.type == "Borrowed") rgType.check(R.id.radio_borrowed) else rgType.check(R.id.radio_lent)
            selectedDueDate?.let { tvDueDate.text = sdf.format(Date(it)) }
        }

        tvDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDueDate?.let { cal.timeInMillis = it }
            android.app.DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDueDate = cal.timeInMillis
                tvDueDate.text = sdf.format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val name = etName.text.toString().trim()
            if (name.isNotEmpty() && amount > 0) {
                val type = if (rgType.checkedRadioButtonId == R.id.radio_borrowed) "Borrowed" else "Lent"
                
                if (existingEntry == null) {
                    val entry = LedgerEntry(
                        personName = name,
                        amount = amount,
                        type = type,
                        note = etNote.text.toString().trim(),
                        dueDate = selectedDueDate
                    )
                    allEntries.add(0, entry)
                } else {
                    existingEntry.personName = name
                    existingEntry.amount = amount
                    existingEntry.type = type
                    existingEntry.note = etNote.text.toString().trim()
                    existingEntry.dueDate = selectedDueDate
                }
                
                updateActiveEntries()
                DataManager.saveData(this)
                updateSummary()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Name and Amount required", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String = "PROCEED",
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_confirm_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_confirm_message)
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_confirm_negative)
        val btnPositive = dialog.findViewById<TextView>(R.id.btn_confirm_positive)

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = positiveButtonText

        btnNegative.setOnClickListener { dialog.dismiss() }
        btnPositive.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}

class LedgerAdapter(
    private val entries: MutableList<LedgerEntry>,
    private val onUpdate: () -> Unit,
    private val onShowMenu: (View, LedgerEntry, Boolean, () -> Unit) -> Unit,
    private val onConfirmSettlement: (LedgerEntry) -> Unit
) : RecyclerView.Adapter<LedgerAdapter.LedgerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ledger_list_item, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvName.text = entry.personName
        holder.tvType.text = entry.type.uppercase()
        
        val remaining = entry.amount - entry.paidAmount
        holder.tvAmount.text = "${DataManager.financeCurrency}${remaining.toInt()}"
        holder.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
        
        if (entry.paidAmount > 0 && !entry.isSettled) {
            holder.tvNote.text = "Paid: ${DataManager.financeCurrency}${entry.paidAmount.toInt()} / ${DataManager.financeCurrency}${entry.amount.toInt()}\n${entry.note}"
            holder.tvNote.visibility = View.VISIBLE
        } else if (entry.note.isNotEmpty()) {
            holder.tvNote.text = entry.note
            holder.tvNote.visibility = View.VISIBLE
        } else {
            holder.tvNote.visibility = View.GONE
        }

        // Due Date Logic
        if (entry.dueDate != null && !entry.isSettled) {
            val sdfDue = SimpleDateFormat("MMM dd", Locale.getDefault())
            val isOverdue = entry.dueDate!! < System.currentTimeMillis()
            holder.tvDueDate.text = if (isOverdue) "OVERDUE: ${sdfDue.format(Date(entry.dueDate!!))}" else "DUE: ${sdfDue.format(Date(entry.dueDate!!))}"
            holder.tvDueDate.visibility = View.VISIBLE
            holder.tvDueDate.setTextColor(if (isOverdue) Color.RED else Color.parseColor("#FFB800"))
        } else {
            holder.tvDueDate.visibility = View.GONE
        }

        val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
        holder.tvType.setTextColor(typeColor)
        holder.tvAmount.setTextColor(typeColor)

        // Settlement Logic
        if (entry.isSettled) {
            holder.tvName.paintFlags = holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.5f
            holder.btnSettle.setImageResource(android.R.drawable.checkbox_on_background)
            holder.btnSettle.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            holder.tvName.paintFlags = holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1.0f
            holder.btnSettle.setImageResource(android.R.drawable.checkbox_off_background)
            holder.btnSettle.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        holder.btnSettle.setOnClickListener {
            onConfirmSettlement(entry)
        }

        holder.itemView.setOnLongClickListener {
            onShowMenu(it, entry, false) {}
            true
        }
    }

    override fun getItemCount(): Int = entries.size

    class LedgerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_person_name)
        val tvType: TextView = view.findViewById(R.id.tv_ledger_type)
        val tvAmount: TextView = view.findViewById(R.id.tv_ledger_amount)
        val tvNote: TextView = view.findViewById(R.id.tv_ledger_note)
        val tvDueDate: TextView = view.findViewById(R.id.tv_due_date_label)
        val tvDate: TextView = view.findViewById(R.id.tv_ledger_date)
        val btnSettle: ImageView = view.findViewById(R.id.btn_settle_ledger)
    }
}
