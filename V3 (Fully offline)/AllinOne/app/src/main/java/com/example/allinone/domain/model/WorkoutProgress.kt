package com.example.allinone.domain.model

data class WorkoutProgress(
    val dailyProgress: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val workoutsThisMonth: Int,
    val totalWorkoutsFinished: Int,
    val todayCaloriesBurned: Int
)

data class WorkoutAnalytics(
    val muscleDistribution: Map<String, Int>,
    val muscleRecovery: Map<String, Float>,
    val acwrStatus: ACWRData,
    val trainingStability: Float,
    val monthlyVolume: List<Float>
)

data class ACWRData(
    val acuteWorkload: List<Float>,
    val chronicWorkload: List<Float>
)
