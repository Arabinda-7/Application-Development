package com.example.allinone

data class Workout(
    var name: String,
    var isCompleted: Boolean,
    var trackingMode: String = "Reps",
    var target: Int = 0,
    var progress: Int = 0,
    var frequency: String = "Anytime",
    var isDayOff: Boolean = false,
    var color: Int = -1,
    var iconResId: Int = -1,
    var repeatType: String = "SPECIFIC_DAYS",
    var repeatDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6), // 0=Sun, 6=Sat
    var repeatCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    var isExpanded: Boolean = false,
    var completedDates: MutableList<String> = mutableListOf()
)
