package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class PersonLedgerActivity : AppCompatActivity() {

    private lateinit var personName: String
    private lateinit var personEntries: MutableList<LedgerEntry>
    private lateinit var ledgerAdapter: LedgerAdapter
    
    private lateinit var tvOwe: TextView
    private lateinit var tvOwed: TextView
    private var showHistory: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_ledger)

        personName = intent.getStringExtra("personName") ?: ""
        if (personName.isEmpty()) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tv_person_title).text = personName.uppercase()
        tvOwe = findViewById(R.id.tv_person_owe)
        tvOwed = findViewById(R.id.tv_person_owed)

        val list = findViewById<RecyclerView>(R.id.person_ledger_list)
        list.layoutManager = LinearLayoutManager(this)
        
        updateEntries()
        
        ledgerAdapter = LedgerAdapter(personEntries, 
            onUpdate = {
                DataManager.saveData(this)
                updateEntries()
                updateSummary()
            },
            onShowMenu = { anchor, entry, isHistory, onAction ->
                showCustomLedgerMenu(anchor, entry, isHistory, onAction)
            },
            onAddPayment = { entry ->
                showAddPaymentDialog(this, entry)
            },
            onConfirmSettlement = { entry ->
                showConfirmationDialog(
                    title = "SETTLE ENTRY",
                    message = "Are you sure you want to mark this entry as settled?",
                    positiveButtonText = "SETTLE",
                    onConfirm = {
                        entry.isSettled = true
                        entry.settlementTimestamp = System.currentTimeMillis()
                        
                        val remaining = entry.amount - entry.paidAmount
                        if (remaining > 0) {
                            entry.paymentHistory.add(LedgerPayment(remaining))
                            entry.paidAmount = entry.amount
                        }
                        
                        DataManager.saveData(this)
                        updateEntries()
                        updateSummary()
                    }
                )
            }
        )
        list.adapter = ledgerAdapter

        updateSummary()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_person_settings).setOnClickListener {
            showPersonSettingsMenu(it)
        }

        findViewById<FloatingActionButton>(R.id.btn_add_to_person).setOnClickListener {
            showAddLedgerDialog(null)
        }
    }

    private fun showPersonSettingsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Toggle History")
        
        popup.setOnMenuItemClickListener {
            if (it.title == "Toggle History") {
                showHistory = !showHistory
                findViewById<View>(R.id.tv_history_label).visibility = if (showHistory) View.VISIBLE else View.GONE
                updateEntries()
            }
            true
        }
        popup.show()
    }

    private fun updateEntries() {
        personEntries = DataManager.ledgerEntries.filter { 
            it.personName == personName && it.isSettled == showHistory 
        }.sortedByDescending { if (showHistory) it.settlementTimestamp ?: 0 else it.timestamp }.toMutableList()

        if (::ledgerAdapter.isInitialized) {
            ledgerAdapter.notifyDataSetChanged()
        }
    }

    private fun updateSummary() {
        val totalOwe = personEntries.filter { it.type == "Borrowed" && !it.isSettled }.sumOf { it.amount - it.paidAmount }
        val totalOwed = personEntries.filter { it.type == "Lent" && !it.isSettled }.sumOf { it.amount - it.paidAmount }
        
        val currency = DataManager.financeCurrency
        tvOwe.text = String.format(Locale.US, "%s%.0f", currency, totalOwe)
        tvOwed.text = String.format(Locale.US, "%s%.0f", currency, totalOwed)
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry, isHistory: Boolean, onAction: () -> Unit) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnUndo = menuView.findViewById<View>(R.id.menu_undo)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)
        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        
        btnEdit.visibility = if (isHistory || entry.isSettled) View.GONE else View.VISIBLE
        btnUndo.visibility = if (isHistory || entry.isSettled) View.VISIBLE else View.GONE

        btnUndo.setOnClickListener {
            entry.isSettled = false
            DataManager.saveData(this)
            updateEntries()
            updateSummary()
            onAction()
            popupWindow.dismiss()
        }

        if (!isHistory && !entry.isSettled) {
            val paymentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
                val outValue = android.util.TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
                isClickable = true
                isFocusable = true
                
                val icon = ImageView(this@PersonLedgerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx())
                    setImageResource(android.R.drawable.ic_input_add)
                    imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
                }
                val text = TextView(this@PersonLedgerActivity).apply {
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
                    showAddPaymentDialog(this@PersonLedgerActivity, entry)
                }
            }
            (menuView as ViewGroup).getChildAt(0).let { (it as ViewGroup).addView(paymentLayout, 0) }
        }

        btnEdit.setOnClickListener {
            popupWindow.dismiss()
            showAddLedgerDialog(entry)
        }

        btnDelete.setOnClickListener {
            DataManager.ledgerEntries.remove(entry)
            DataManager.saveData(this)
            updateEntries()
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

        dialog.findViewById<View>(R.id.btn_pay_25).setOnClickListener { etInput.setText((remaining * 0.25).toInt().toString()) }
        dialog.findViewById<View>(R.id.btn_pay_50).setOnClickListener { etInput.setText((remaining * 0.50).toInt().toString()) }
        dialog.findViewById<View>(R.id.btn_pay_full).setOnClickListener { etInput.setText(remaining.toInt().toString()) }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val paid = etInput.text.toString().toDoubleOrNull() ?: 0.0
            if (paid > 0) {
                entry.paymentHistory.add(LedgerPayment(paid))
                entry.paidAmount += paid
                if (entry.paidAmount >= entry.amount) {
                    entry.isSettled = true
                    entry.settlementTimestamp = System.currentTimeMillis()
                }
                DataManager.saveData(this)
                updateEntries()
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

        // Hide Person name field in this context
        dialog.findViewById<View>(R.id.tv_person_label).visibility = View.GONE
        etName.visibility = View.GONE

        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"

        etName.setText(personName)
        etName.isEnabled = false // Locked to this person

        var selectedDueDate: Long? = existingEntry?.dueDate
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (existingEntry != null) {
            tvTitle.text = "Edit Ledger"
            etAmount.setText(existingEntry.amount.toString())
            etNote.setText(existingEntry.note)
            if (existingEntry.type == "Borrowed") rgType.check(R.id.radio_borrowed) else rgType.check(R.id.radio_lent)
            selectedDueDate?.let { tvDueDate.text = sdf.format(Date(it)) }
        }

        tvDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDueDate?.let { cal.timeInMillis = it }
            android.app.DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d); selectedDueDate = cal.timeInMillis
                tvDueDate.text = sdf.format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val type = if (rgType.checkedRadioButtonId == R.id.radio_borrowed) "Borrowed" else "Lent"
                if (existingEntry == null) {
                    val entry = LedgerEntry(personName = personName, amount = amount, type = type, note = etNote.text.toString().trim(), dueDate = selectedDueDate)
                    DataManager.ledgerEntries.add(0, entry)
                } else {
                    existingEntry.amount = amount; existingEntry.type = type; existingEntry.note = etNote.text.toString().trim(); existingEntry.dueDate = selectedDueDate
                }
                updateEntries(); DataManager.saveData(this); updateSummary(); dialog.dismiss()
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

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
