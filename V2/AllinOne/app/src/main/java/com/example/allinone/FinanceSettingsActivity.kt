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
        setContentView(R.layout.activity_section_settings_finance)

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
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories_finance)
        
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val et = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        accentLine?.setBackgroundColor(accentColor)
        root?.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        
        fun refresh() {
            container.removeAllViews()
            DataManager.financeCustomCategories.forEach { c ->
                val iv = LayoutInflater.from(this).inflate(R.layout.item_category_manage_finance, container, false)
                iv.findViewById<TextView>(R.id.tv_category_name).text = c
                
                // Item Background
                iv.findViewById<View>(R.id.item_container)?.background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#2A2A2A"))
                    cornerRadius = radius * 0.5f
                }

                iv.findViewById<View>(R.id.btn_remove_category).setOnClickListener { 
                    DataManager.financeCustomCategories.remove(c)
                    DataManager.saveData(this)
                    refresh() 
                }
                container.addView(iv)
            }
        }
        
        btnAdd.setOnClickListener {
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
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget_finance)
        
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        (btnSave.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = radius * 0.5f
        }

        et.setText(DataManager.monthlyBudget.toString())
        
        btnSave.setOnClickListener {
            DataManager.monthlyBudget = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this)
            dialog.dismiss()
            loadSettings()
        }
        showDialogSafe(dialog)
    }

    private fun showSetSavingsGoalDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget_finance)
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_settings_title)
        val et = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)
        val accentLine = dialog.findViewById<View>(R.id.title_accent_line)
        val root = dialog.findViewById<View>(R.id.dialog_root)

        val accentColor = if (DataManager.appAccentColor != -1) DataManager.appAccentColor else android.graphics.Color.parseColor("#1A73E8")
        val radius = DataManager.appBorderRadius.toFloat() * resources.displayMetrics.density

        tvTitle.text = "SAVINGS GOAL"
        accentLine.setBackgroundColor(accentColor)
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (DataManager.appThemeMode == "OLED") android.graphics.Color.BLACK else android.graphics.Color.parseColor("#121212"))
            cornerRadius = radius
        }
        (btnSave.background as? android.graphics.drawable.GradientDrawable)?.let {
            it.setColor(accentColor)
            it.cornerRadius = radius * 0.5f
        }

        et.setHint("Goal Amount...")
        et.setText(DataManager.monthlySavingsGoal.toString())
        
        btnSave.setOnClickListener {
            DataManager.monthlySavingsGoal = et.text.toString().toDoubleOrNull() ?: 0.0
            DataManager.saveData(this)
            dialog.dismiss()
            loadSettings()
        }
        showDialogSafe(dialog)
    }
}