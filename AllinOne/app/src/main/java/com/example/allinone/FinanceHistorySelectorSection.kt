package com.example.allinone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class FinanceHistorySelectorSection(
    private val context: Context,
    private val radioGroup: RadioGroup,
    private val tvYear: TextView,
    private val onMonthSelected: (Int) -> Unit,
    private val onYearClicked: () -> Unit
) {
    private val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

    fun setup() {
        radioGroup.removeAllViews()
        val inflater = LayoutInflater.from(context)
        monthNames.forEachIndexed { index, name ->
            val rb = inflater.inflate(R.layout.item_month_tab, radioGroup, false) as RadioButton
            rb.id = View.generateViewId()
            rb.text = name
            rb.setOnClickListener { onMonthSelected(index) }
            radioGroup.addView(rb)
        }

        tvYear.setOnClickListener { onYearClicked() }
    }

    fun updateSelection(index: Int) {
        val rb = radioGroup.getChildAt(index) as? RadioButton
        rb?.isChecked = true
    }

    fun updateYear(year: Int) {
        tvYear.text = year.toString()
    }
}
