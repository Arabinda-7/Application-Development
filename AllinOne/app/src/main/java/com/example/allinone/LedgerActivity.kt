package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import java.text.SimpleDateFormat
import java.util.*
import com.example.allinone.data.model.PersonalLedgerEntry as LedgerEntry
import com.example.allinone.data.model.LedgerPayment

class LedgerActivity : BaseActivity() {

    private val viewModel: FinanceLedgerViewModel by viewModels()
    
    private lateinit var summarySection: FinanceLedgerSummarySection
    private lateinit var listSection: FinanceLedgerListSection
    private lateinit var themeManager: FinanceLedgerThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ledger)

        initSections()
        setupLogic()
    }

    private fun initSections() {
        summarySection = FinanceLedgerSummarySection(findViewById(R.id.ledger_root_layout))
        
        listSection = FinanceLedgerListSection(
            this,
            findViewById(R.id.ledger_list),
            onUpdate = { updateAllUI() },
            onShowMenu = { anchor, entry, isHistory, onAction -> showCustomLedgerMenu(anchor, entry, isHistory, onAction) },
            onAddPayment = { entry -> showAddPaymentDialog(this, entry) },
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
                        updateAllUI()
                    }
                )
            }
        )

        themeManager = FinanceLedgerThemeManager(
            findViewById(R.id.ledger_aura_background),
            findViewById(R.id.btn_add_ledger),
            listOf(
                findViewById(R.id.card_total_borrowed),
                findViewById(R.id.card_total_lent),
                findViewById(R.id.card_net_balance)
            )
        )
    }

    private fun setupLogic() {
        updateAllUI()
        themeManager.applyTheme()
        setupKeyboardHandling(findViewById(R.id.ledger_root_layout), findViewById(R.id.ledger_content_container), 12)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_ledger_history).setOnClickListener {
            startActivity(Intent(this, LedgerHistoryActivity::class.java))
        }
        findViewById<View>(R.id.btn_ledger_settings).setOnClickListener { showLedgerSettingsDialog() }
        findViewById<View>(R.id.btn_add_ledger).setOnClickListener { showAddLedgerDialog() }
    }

    private fun updateAllUI() {
        val active = listSection.updateActiveEntries()
        summarySection.update(active)
    }

    private fun showLedgerSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_ledger_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<View>(R.id.item_people_ledger).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PersonalLedgerHubActivity::class.java))
        }
        dialog.findViewById<View>(R.id.btn_close_settings).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry, isHistory: Boolean, onAction: () -> Unit) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_ledger_item, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        menuView.findViewById<View>(R.id.menu_undo).apply {
            visibility = if (isHistory) View.VISIBLE else View.GONE
            setOnClickListener {
                entry.isSettled = false
                DataManager.saveData(this@LedgerActivity)
                updateAllUI()
                onAction()
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_edit).apply {
            visibility = if (isHistory) View.GONE else View.VISIBLE
            setOnClickListener {
                popupWindow.dismiss()
                showAddLedgerDialog(entry)
            }
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            DataManager.ledgerEntries.removeIf { it.id == entry.id }
            DataManager.saveData(this)
            updateAllUI()
            onAction()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    private fun showAddPaymentDialog(context: Context, entry: LedgerEntry) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_payment_simple)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etInput = dialog.findViewById<EditText>(R.id.et_payment_amount)
        val tvRemaining = dialog.findViewById<TextView>(R.id.tv_remaining_label)
        val remaining = entry.amount - entry.paidAmount
        tvRemaining.text = "Amount remaining: ${DataManager.financeCurrency}${remaining.toInt()}"
        
        dialog.findViewById<View>(R.id.btn_pay_full).setOnClickListener { etInput.setText(remaining.toInt().toString()) }
        dialog.findViewById<View>(R.id.btn_close_payment).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btn_confirm_payment).setOnClickListener {
            val paid = etInput.text.toString().toDoubleOrNull() ?: 0.0
            if (paid > 0) {
                entry.paymentHistory.add(LedgerPayment(paid))
                entry.paidAmount += paid
                if (entry.paidAmount >= entry.amount) {
                    entry.isSettled = true
                    entry.settlementTimestamp = System.currentTimeMillis()
                }
                DataManager.saveData(this)
                updateAllUI()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showAddLedgerDialog(existingEntry: LedgerEntry? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_ledger_main)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etAmount = dialog.findViewById<EditText>(R.id.et_ledger_amount)
        val etName = dialog.findViewById<EditText>(R.id.et_person_name)
        val etNote = dialog.findViewById<EditText>(R.id.et_ledger_note)
        val tvDueDate = dialog.findViewById<TextView>(R.id.tv_ledger_due_date)
        val rgType = dialog.findViewById<RadioGroup>(R.id.rg_ledger_type)
        
        var selectedDueDate: Long? = existingEntry?.dueDate
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (existingEntry != null) {
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
                cal.set(y, m, d); selectedDueDate = cal.timeInMillis; tvDueDate.text = sdf.format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dialog.findViewById<View>(R.id.btn_save_ledger).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val name = etName.text.toString().trim()
            if (name.isNotEmpty() && amount > 0) {
                val type = if (rgType.checkedRadioButtonId == R.id.radio_borrowed) "Borrowed" else "Lent"
                if (existingEntry == null) {
                    DataManager.ledgerEntries.add(0, LedgerEntry(personName = name, amount = amount, type = type, note = etNote.text.toString().trim(), dueDate = selectedDueDate))
                } else {
                    existingEntry.apply { personName = name; this.amount = amount; this.type = type; note = etNote.text.toString().trim(); dueDate = selectedDueDate }
                }
                updateAllUI(); DataManager.saveData(this); dialog.dismiss()
            }
        }
        dialog.findViewById<View>(R.id.btn_close_ledger).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showConfirmationDialog(title: String, message: String, positiveButtonText: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_ledger)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        dialog.findViewById<TextView>(R.id.btn_confirm_positive).apply { text = positiveButtonText; setOnClickListener { onConfirm(); dialog.dismiss() } }
        dialog.findViewById<View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        updateAllUI()
        themeManager.applyTheme()
    }
}
