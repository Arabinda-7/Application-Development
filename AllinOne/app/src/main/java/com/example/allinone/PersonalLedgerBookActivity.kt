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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.core.utils.LedgerMathHelper
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.data.model.*
import com.example.allinone.ui.finance.PersonalBookAdapter
import java.text.SimpleDateFormat
import java.util.*

class PersonalLedgerBookActivity : BaseActivity() {

    private lateinit var ledger: PersonalLedger
    private lateinit var adapter: PersonalBookAdapter
    private var showHistory: Boolean = false
    
    private lateinit var tvOwe: TextView
    private lateinit var tvOwed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_book)

        val ledgerId = intent.getStringExtra("ledgerId") ?: ""
        ledger = DataManager.personalLedgers.find { it.id == ledgerId } ?: run { finish(); return }

        findViewById<TextView>(R.id.tv_person_title).text = ledger.personName.uppercase()
        tvOwe = findViewById(R.id.tv_person_owe)
        tvOwed = findViewById(R.id.tv_person_owed)

        val list = findViewById<RecyclerView>(R.id.person_ledger_list)
        list.layoutManager = LinearLayoutManager(this)
        
        adapter = PersonalBookAdapter(
            context = this,
            entries = ledger.entries,
            showHistory = showHistory,
            onSettle = { entry ->
                entry.isSettled = true
                entry.settlementTimestamp = System.currentTimeMillis()
                DataManager.saveData(this)
                updateSummary()
                adapter.notifyDataSetChanged()
            },
            onEdit = { entry -> showAddEntryDialog(entry) },
            onDelete = { entry ->
                ledger.entries.remove(entry)
                DataManager.saveData(this)
                updateSummary()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            },
            onUndo = { entry ->
                entry.isSettled = false
                DataManager.saveData(this)
                updateSummary()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Entry moved back to active", Toast.LENGTH_SHORT).show()
            },
            onToggleExpansion = { position -> adapter.notifyItemChanged(position) }
        )
        list.adapter = adapter

        updateSummary()

        setupKeyboardHandling(findViewById(R.id.person_ledger_root), findViewById(R.id.person_ledger_content_container))

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
        applySectionTheme()
        updateDynamicBackground()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        applySectionTheme()
        updateDynamicBackground()
    }

    private fun applySectionTheme() {
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        val darkenedFabColor = UIUtils.darkenColor(financeColor, 0.5f)
        
        findViewById<FloatingActionButton>(R.id.btn_add_to_person).backgroundTintList = 
            android.content.res.ColorStateList.valueOf(darkenedFabColor)
            
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_person_owe).strokeColor = Color.parseColor("#FF5252")
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_person_owed).strokeColor = Color.parseColor("#4CAF50")
    }

    private fun updateDynamicBackground() {
        val auraView = findViewById<View>(R.id.person_ledger_aura_background) ?: return
        val financeColor = if (DataManager.globalFinanceColor != -1) DataManager.globalFinanceColor else Color.parseColor("#E91E63")
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                adjustAlpha(financeColor, 0.4f),
                Color.BLACK
            )
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
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
                    adapter.updateData(showHistory)
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
        val (owe, owed) = LedgerMathHelper.calculateActiveSums(ledger.entries)
        
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

        val (totalOwe, totalOwed) = LedgerMathHelper.calculateActiveSums(ledger.entries)
        
        val currency = DataManager.financeCurrency
        val message = "Summary: Lent ${currency}${totalOwed.toInt()} | Borrowed ${currency}${totalOwe.toInt()}\n\n" +
            if (totalOwe == totalOwed) {
                "Balance is equal. Both will be settled. Proceed?"
            } else {
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
        if (LedgerMathHelper.performAutoReconciliation(ledger.entries, totalOwe, totalOwed)) {
            DataManager.saveData(this)
            updateSummary()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Balance reconciled successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddEntryDialog(existingEntry: PersonalLedgerEntry? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_ledger_person)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_ledger_amount)
        val etNote = dialog.findViewById<EditText>(R.id.et_ledger_note)
        val tvDueDate = dialog.findViewById<TextView>(R.id.tv_ledger_due_date)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_ledger_type)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_ledger)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_ledger)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)

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
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_confirm_ledger)
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

}
