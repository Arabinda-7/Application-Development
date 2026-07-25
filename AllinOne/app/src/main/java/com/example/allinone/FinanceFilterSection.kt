package com.example.allinone

import android.widget.RadioGroup

class FinanceFilterSection(
    private val radioGroup: RadioGroup,
    private val onFilterChanged: (String) -> Unit
) {
    fun setup() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chip_filter_expense -> "Expense"
                R.id.chip_filter_income -> "Income"
                R.id.chip_filter_saving -> "Saving"
                else -> "All"
            }
            onFilterChanged(filter)
        }
    }
}
