package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FinanceSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_section_settings)

        settingsList = findViewById(R.id.settings_list)
        tvTitle = findViewById(R.id.tv_title)
        
        tvTitle.text = "FINANCE SETTINGS"
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Configuration", isHeader = true))
        settings.add(ConfigItem("Primary Currency", "Current: ${DataManager.financeCurrency}", options = listOf("₹", "$", "€", "£", "¥"), selectedIndex = listOf("₹", "$", "€", "£", "¥").indexOf(DataManager.financeCurrency), onOptionSelected = { index ->
            val symbols = listOf("₹", "$", "€", "£", "¥")
            DataManager.financeCurrency = symbols[index]
            loadSettings()
        }))
        
        settings.add(ConfigItem("Manage Categories", "Edit spending labels") { 
            showManageCategoriesDialog()
        })

        settings.add(ConfigItem("Planning", isHeader = true))
        settings.add(ConfigItem("Monthly Budget", "Current: ${DataManager.financeCurrency}${DataManager.monthlyBudget}") { 
            showSetBudgetDialog()
        })
        
        settings.add(ConfigItem("Savings Goal", "Current: ${DataManager.financeCurrency}${DataManager.monthlySavingsGoal}") { 
            showSetSavingsGoalDialog()
        })

        settings.add(ConfigItem("Modules", isHeader = true))
        settings.add(ConfigItem("Ledger System", "Enable person-based debt tracking", isToggle = true, isChecked = DataManager.isFinanceLedgerEnabled) {
            DataManager.isFinanceLedgerEnabled = !DataManager.isFinanceLedgerEnabled
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showManageCategoriesDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_manage_categories)
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        
        fun refresh() {
            container.removeAllViews()
            DataManager.financeCustomCategories.forEach { c ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = c
                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { 
                    DataManager.financeCustomCategories.remove(c)
                    DataManager.saveData(this)
                    refresh() 
                }
                container.addView(iv)
            }
        }
        
        dialog.findViewById<View>(R.id.btn_add_category).setOnClickListener {
            val n = et.text.toString().trim()
            if (n.isNotEmpty()) { 
                DataManager.financeCustomCategories.add(n)
                DataManager.saveData(this)
                et.text.clear()
                refresh() 
            }
        }
        refresh()
        showDialogSafe(dialog)
    }

    private fun showSetBudgetDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setText(DataManager.monthlyBudget.toString())
        
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.monthlyBudget = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this)
            dialog.dismiss()
            loadSettings()
        }
        showDialogSafe(dialog)
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this); dialog.setContentView(R.layout.dialog_set_budget)
        dialog.findViewById<TextView>(R.id.tv_settings_title).text = "Savings Goal"
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        et.setHint("Goal Amount...")
        et.setText(DataManager.monthlySavingsGoal.toString())
        
        dialog.findViewById<View>(R.id.btn_save_budget).setOnClickListener {
            DataManager.monthlySavingsGoal = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this)
            dialog.dismiss()
            loadSettings()
        }
        showDialogSafe(dialog)
    }
}