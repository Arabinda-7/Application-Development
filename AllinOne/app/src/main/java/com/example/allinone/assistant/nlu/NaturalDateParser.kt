package com.example.allinone.assistant.nlu

import java.util.Calendar

object NaturalDateParser {

    fun parseToTimestamp(input: String): Long? {
        val lower = input.lowercase().trim()
        val calendar = Calendar.getInstance()

        return when {
            lower.contains("today") -> {
                calendar.timeInMillis
            }
            lower.contains("tomorrow") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            lower.contains("yesterday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            lower.contains("next week") -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.timeInMillis
            }
            else -> {
                // Regex pattern matching: "in X days"
                val inDaysRegex = Regex("""in (\d+) days?""")
                val match = inDaysRegex.find(lower)
                if (match != null) {
                    val days = match.groupValues[1].toIntOrNull() ?: 0
                    calendar.add(Calendar.DAY_OF_YEAR, days)
                    calendar.timeInMillis
                } else null
            }
        }
    }
}
