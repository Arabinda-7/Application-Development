package com.example.allinone

import android.os.Bundle
import android.view.View
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
        
        settings.add(ConfigItem("Primary Currency", "Current: ${DataManager.financeCurrency}") {
            val symbols = listOf("₹", "$", "€", "£", "¥")
            DataManager.financeCurrency = symbols[(symbols.indexOf(DataManager.financeCurrency) + 1) % symbols.size]
            loadSettings()
        })
        
        settings.add(ConfigItem("Manage Categories", "Edit spending labels") { 
            showUpcomingFeatureDialog("Manage Categories")
        })
        
        settings.add(ConfigItem("Monthly Budget", "Current: ${DataManager.financeCurrency}${DataManager.monthlyBudget}") { 
            showUpcomingFeatureDialog("Set Budget")
        })
        
        settings.add(ConfigItem("Savings Goal", "Current: ${DataManager.financeCurrency}${DataManager.monthlySavingsGoal}") { 
            showUpcomingFeatureDialog("Set Savings Goal")
        })
        
        settings.add(ConfigItem("Ledger System", "Enable person-based debt tracking", isToggle = true, isChecked = DataManager.isFinanceLedgerEnabled) {
            DataManager.isFinanceLedgerEnabled = !DataManager.isFinanceLedgerEnabled
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showUpcomingFeatureDialog(name: String) {
        android.widget.Toast.makeText(this, "$name: Feature in transition", android.widget.Toast.LENGTH_SHORT).show()
    }
}