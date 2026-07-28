package com.example.allinone

import android.widget.RadioButton
import android.widget.RadioGroup

class WorkoutFilterSection(
    private val filterGroup: RadioGroup,
    private val onFilterChanged: (String) -> Unit
) {
    fun setup() {
        filterGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            val filter = if (checkedRadioButton != null) {
                val text = checkedRadioButton.text.toString()
                if (text == "ALL") "All" else UIUtils.formatTitleCase(text)
            } else {
                "All"
            }
            onFilterChanged(filter)
        }
    }
}
