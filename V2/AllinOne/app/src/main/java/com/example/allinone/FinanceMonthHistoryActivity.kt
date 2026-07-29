package com.example.allinone

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels

class FinanceMonthHistoryActivity : BaseActivity() {

    private val viewModel: FinanceHistoryViewModel by viewModels()
    
    private lateinit var selectorSection: FinanceHistorySelectorSection
    private lateinit var detailsSection: FinanceHistoryDetailsSection
    private lateinit var themeManager: FinanceHistoryThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_month_history)

        val initialYear = intent.getIntExtra("year", viewModel.selectedYear)
        val initialMonth = intent.getIntExtra("month", viewModel.currentMonthIndex)
        
        viewModel.selectedYear = initialYear
        viewModel.currentMonthIndex = initialMonth

        initSections()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.month_history_root), findViewById(R.id.month_history_content_container), 12)
    }

    private fun initSections() {
        selectorSection = FinanceHistorySelectorSection(
            this,
            findViewById(R.id.rg_month_selector),
            findViewById(R.id.tv_current_year),
            onMonthSelected = { index ->
                viewModel.currentMonthIndex = index
                detailsSection.setCurrentItem(index)
            },
            onYearClicked = { showYearPickerDialog() }
        )

        detailsSection = FinanceHistoryDetailsSection(this, findViewById(R.id.vp_month_details)) { index ->
            viewModel.currentMonthIndex = index
            selectorSection.updateSelection(index)
        }

        themeManager = FinanceHistoryThemeManager(findViewById(R.id.month_history_aura_background))
    }

    private fun setupLogic() {
        selectorSection.setup()
        detailsSection.setup(viewModel.selectedYear)
        themeManager.applyTheme()

        selectorSection.updateYear(viewModel.selectedYear)
        selectorSection.updateSelection(viewModel.currentMonthIndex)
        detailsSection.setCurrentItem(viewModel.currentMonthIndex)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun showYearPickerDialog() {
        val years = viewModel.availableYears.map { it.toString() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select Year")
            .setItems(years) { _, which ->
                val year = viewModel.availableYears[which]
                viewModel.selectedYear = year
                selectorSection.updateYear(year)
                detailsSection.setup(year)
                detailsSection.setCurrentItem(viewModel.currentMonthIndex)
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        themeManager.applyTheme()
    }
}
