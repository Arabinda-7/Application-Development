package com.example.allinone

import android.widget.RadioGroup

class HabitFilterSection(
    private val filterGroup: RadioGroup,
    private val onFilterChanged: (String) -> Unit
) {
    fun setup() {
        filterGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chip_morning -> "Morning"
                R.id.chip_afternoon -> "Afternoon"
                R.id.chip_evening -> "Evening"
                else -> "All"
            }
            onFilterChanged(filter)
        }
    }
}
