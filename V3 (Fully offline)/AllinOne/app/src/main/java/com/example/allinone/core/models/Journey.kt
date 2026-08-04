package com.example.allinone.core.models

import java.util.*

data class Journey(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String, // HABIT, WORKOUT
    val bannerRes: Int,
    val keyResults: List<JourneyResult>,
    val expectations: List<String>,
    val phases: List<JourneyPhase>,
    val habitsToCreate: List<String> = emptyList(),
    val workoutsToCreate: List<String> = emptyList()
)

data class JourneyResult(
    val title: String,
    val iconEmoji: String
)

data class JourneyPhase(
    val dayRange: String,
    val title: String,
    val description: String
)
