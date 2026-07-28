package com.example.allinone

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class AddFinanceActivity : BaseActivity() {

    private var transactionIndex: Int = -1
    private var existingTransaction: Transaction? = null
    
    private lateinit var etAmount: EditText
    private lateinit var tvAmountHint: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var etCustomNote: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var btnSave: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    
    private var selectedCategoryName = "Other"
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_finance)

        transactionIndex = intent.getIntExtra("TRANSACTION_INDEX", -1)
        if (transactionIndex != -1 && transactionIndex < DataManager.transactions.size) {
            existingTransaction = DataManager.transactions[transactionIndex]
        }

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_finance_root), findViewById(R.id.add_finance_content_container))
    }

    private fun initViews() {
        etAmount = findViewById(R.id.et_trans_amount)
        tvAmountHint = findViewById(R.id.tv_amount_hint)
        chipGroup = findViewById(R.id.cg_trans_category)
        etCustomNote = findViewById(R.id.et_trans_custom_category)
        rgType = findViewById(R.id.rg_trans_type)
        btnSave = findViewById(R.id.btn_save_trans)
        tvDate = findViewById(R.id.tv_trans_date)
        tvTime = findViewById(R.id.tv_trans_time)
        
        findViewById<View>(R.id.btn_close_trans).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        if (existingTransaction != null) {
            findViewById<TextView>(R.id.tv_dialog_title).text = "Edit Transaction"
            btnSave.text = "SAVE"
            etAmount.setText(existingTransaction?.amount.toString())
            selectedCategoryName = existingTransaction?.category ?: "Other"
            if (selectedCategoryName == "Other") {
                etCustomNote.visibility = View.VISIBLE
                etCustomNote.setText(existingTransaction?.title)
            }
            when (existingTransaction?.type) {
                "Income" -> rgType.check(R.id.radio_income)
                "Saving" -> rgType.check(R.id.radio_saving)
                else -> rgType.check(R.id.radio_expense)
            }
            existingTransaction?.let { calendar.timeInMillis = it.timestamp }
        }

        val currency = DataManager.financeCurrency
        etAmount.hint = "${currency}0.00"

        val dateSdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        tvDate.text = dateSdf.format(calendar.time)
        tvTime.text = timeSdf.format(calendar.time)

        val categories = DataManager.financeCustomCategories
        val sortedCategories = categories.filter { it != "Other" }.sorted() + categories.filter { it == "Other" }

        sortedCategories.forEach { category ->
            val chip = Chip(this)
            chip.text = category; chip.isCheckable = true; chip.isChecked = (category == selectedCategoryName)
            chip.setChipBackgroundColorResource(R.color.chip_background); chip.setTextColor(Color.WHITE)
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategoryName = category
                    etCustomNote.visibility = if (category == "Other") View.VISIBLE else View.GONE
                }
            }
            chipGroup.addView(chip)
        }

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y); calendar.set(Calendar.MONTH, m); calendar.set(Calendar.DAY_OF_MONTH, d)
                tvDate.text = dateSdf.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvTime.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
                tvTime.text = timeSdf.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnSave.setOnClickListener { saveFinance() }
        validateInputs()
    }

    private fun validateInputs() {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val isValid = amount > 0
        btnSave.alpha = if (isValid) 1.0f else 0.3f
        btnSave.isEnabled = isValid
        val themeColor = if (DataManager.financeAddThemeColor != -1) DataManager.financeAddThemeColor else Color.parseColor("#1A73E8")
        if (isValid) btnSave.setTextColor(themeColor) else btnSave.setTextColor(Color.GRAY)
        tvAmountHint.visibility = if (isValid) View.GONE else View.VISIBLE
        if (!isValid) startPulseAnimation(tvAmountHint)
        tvAmountHint.setTextColor(themeColor)
    }

    private fun startPulseAnimation(view: View) {
        if (view.tag == "pulsing") return
        view.tag = "pulsing"
        view.animate().alpha(0.4f).setDuration(800).withEndAction {
            view.animate().alpha(1.0f).setDuration(800).withEndAction {
                view.tag = null
                if (view.visibility == View.VISIBLE) startPulseAnimation(view)
            }
        }.start()
    }

    private fun saveFinance() {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val finalTitle = if (selectedCategoryName == "Other") {
            etCustomNote.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Other Expense"
        } else { selectedCategoryName }

        if (amount > 0) {
            val type = when (rgType.checkedRadioButtonId) {
                R.id.radio_income -> "Income"; R.id.radio_saving -> "Saving"; else -> "Expense"
            }
            if (existingTransaction == null) {
                DataManager.transactions.add(0, Transaction(title = finalTitle, amount = amount, type = type, category = selectedCategoryName, timestamp = calendar.timeInMillis))
                DataManager.addActivity("Logged $type: ${DataManager.financeCurrency}$amount")
            } else {
                existingTransaction?.let {
                    it.title = finalTitle; it.amount = amount; it.type = type; it.category = selectedCategoryName; it.timestamp = calendar.timeInMillis
                }
                DataManager.addActivity("Updated $type: ${DataManager.financeCurrency}$amount")
            }
            DataManager.saveData(this); setResult(RESULT_OK); finish()
        }
    }
}
