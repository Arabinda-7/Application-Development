package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FinanceActivity : BaseActivity() {

    private val viewModel: FinanceViewModel by viewModels()
    
    private lateinit var summarySection: FinanceSummarySection
    private lateinit var listSection: FinanceListSection
    private lateinit var filterSection: FinanceFilterSection
    private lateinit var themeManager: FinanceThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        initSections()
        setupLogic()

        if (intent.getBooleanExtra("SHOW_ADD_DIALOG", false)) {
            startActivity(Intent(this, AddFinanceActivity::class.java))
        }
    }

    private fun initSections() {
        summarySection = FinanceSummarySection(findViewById(R.id.finance_summary))
        
        listSection = FinanceListSection(
            this,
            findViewById(R.id.finance_list),
            findViewById(R.id.layout_empty_state)
        ) {
            updateAllUI()
        }

        filterSection = FinanceFilterSection(findViewById(R.id.rg_finance_filters)) { filter ->
            viewModel.currentFilter = filter
            viewModel.allMonthTransactions = listSection.loadCurrentMonthTransactions(filter).toMutableList()
            summarySection.update(viewModel.allMonthTransactions)
        }

        themeManager = FinanceThemeManager(
            this,
            findViewById(R.id.finance_aura_background),
            listOf(
                findViewById<View>(R.id.finance_summary).findViewById(R.id.card_budget),
                findViewById<View>(R.id.finance_summary).findViewById(R.id.card_spent),
                findViewById<View>(R.id.finance_summary).findViewById(R.id.card_remain),
                findViewById<View>(R.id.finance_summary).findViewById(R.id.card_top_expenses),
                findViewById<View>(R.id.finance_summary).findViewById(R.id.card_savings)
            ),
            listOf(
                findViewById(R.id.chip_filter_all),
                findViewById(R.id.chip_filter_expense),
                findViewById(R.id.chip_filter_income),
                findViewById(R.id.chip_filter_saving)
            ),
            findViewById(R.id.btn_create_new_finance)
        )
    }

    private fun setupLogic() {
        filterSection.setup()
        themeManager.applyTheme()
        updateAllUI()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btn_finance_settings).setOnClickListener { showSettingsMenu(it) }

        findViewById<View>(R.id.btn_finance_ledger).apply {
            visibility = if (DataManager.isFinanceLedgerEnabled) View.VISIBLE else View.GONE
            setOnClickListener { startActivity(Intent(this@FinanceActivity, LedgerActivity::class.java)) }
        }

        findViewById<FloatingActionButton>(R.id.btn_create_new_finance).setOnClickListener {
            startActivity(Intent(this, AddFinanceActivity::class.java))
        }

        findViewById<View>(R.id.finance_summary).findViewById<View>(R.id.card_budget).setOnClickListener {
            showSetBudgetDialog()
        }

        findViewById<View>(R.id.finance_summary).findViewById<View>(R.id.card_savings).setOnClickListener {
            showSetSavingsGoalDialog()
        }
        
        setupKeyboardHandling(findViewById(R.id.finance_root_layout), findViewById(R.id.finance_content_container), 12)
    }

    private fun updateAllUI() {
        viewModel.allMonthTransactions = listSection.loadCurrentMonthTransactions(viewModel.currentFilter).toMutableList()
        summarySection.update(viewModel.allMonthTransactions)
    }

    private fun showSettingsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_finance, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_action_primary).apply {
            visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_action_primary).text = "HISTORY"
            findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_history)
            setOnClickListener {
                startActivity(Intent(this@FinanceActivity, FinanceHistoryActivity::class.java))
                popupWindow.dismiss()
            }
        }
        
        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            startActivity(Intent(this, FinanceSettingsActivity::class.java))
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget_finance)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val etBudget = dialog.findViewById<EditText>(R.id.et_budget_amount)
        etBudget.hint = "${DataManager.financeCurrency}0.00"
        etBudget.setText(DataManager.monthlyBudget.toInt().toString())
        
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.monthlyBudget = etBudget.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this)
            updateAllUI()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_close_budget).setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_ledger_finance)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etGoal = dialog.findViewById<EditText>(R.id.et_ledger_amount)
        val etName = dialog.findViewById<EditText>(R.id.et_person_name)
        dialog.findViewById<TextView>(R.id.tv_person_label).text = "GOAL FOR"
        dialog.findViewById<View>(R.id.rg_ledger_type).visibility = View.GONE
        dialog.findViewById<View>(R.id.tv_ledger_due_date).visibility = View.GONE
        dialog.findViewById<View>(R.id.et_ledger_note).visibility = View.GONE

        dialog.findViewById<TextView>(R.id.tv_dialog_title).text = "SET SAVINGS GOAL"
        etGoal.hint = "${DataManager.financeCurrency}0.00"
        etGoal.setText(DataManager.monthlySavingsGoal.toInt().toString())
        etName.setText(DataManager.financeSavingsGoalName)

        dialog.findViewById<View>(R.id.btn_save_ledger).setOnClickListener {
            DataManager.monthlySavingsGoal = etGoal.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.financeSavingsGoalName = etName.text.toString()
            DataManager.saveData(this)
            updateAllUI()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_close_ledger).setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }

    override fun onResume() {
        super.onResume()
        updateAllUI()
        themeManager.applyTheme()
        findViewById<View>(R.id.btn_finance_ledger).visibility = if (DataManager.isFinanceLedgerEnabled) View.VISIBLE else View.GONE
    }
}
