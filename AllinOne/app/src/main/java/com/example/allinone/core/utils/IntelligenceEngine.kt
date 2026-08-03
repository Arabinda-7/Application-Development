package com.example.allinone.core.utils

import kotlin.math.sqrt

/**
 * IntelligenceEngine: The mathematical core of the All in One Assistant.
 * Provides on-device statistical analysis and NLP capabilities.
 */
object IntelligenceEngine {

    /**
     * Calculates the Pearson Correlation Coefficient between two lists of numbers.
     */
    fun calculateCorrelation(x: List<Double>, y: List<Double>): Double {
        if (x.size != y.size || x.isEmpty()) return 0.0

        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { it.first * it.second }
        val sumX2 = x.sumOf { it * it }
        val sumY2 = y.sumOf { it * it }

        val numerator = (n * sumXY) - (sumX * sumY)
        val denominator = sqrt(((n * sumX2) - (sumX * sumX)) * ((n * sumY2) - (sumY * sumY)))

        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    /**
     * Simple Linear Regression to predict the next value in a series.
     */
    fun predictNextValue(series: List<Double>): Double {
        if (series.size < 2) return series.lastOrNull() ?: 0.0
        
        val n = series.size
        val x = List(n) { it.toDouble() }
        val y = series

        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { it.first * it.second }
        val sumX2 = x.sumOf { it * it }

        val m = ((n * sumXY) - (sumX * sumY)) / ((n * sumX2) - (sumX * sumX))
        val b = (sumY - (m * sumX)) / n

        return (m * n) + b
    }

    /**
     * Calculates the Z-Score of the latest value to detect anomalies.
     */
    fun calculateZScore(series: List<Double>, value: Double): Double {
        if (series.size < 3) return 0.0
        val mean = series.average()
        val stdDev = sqrt(series.map { Math.pow(it - mean, 2.0) }.average())
        return if (stdDev == 0.0) 0.0 else (value - mean) / stdDev
    }

    /**
     * Lexicon-based Sentiment Analysis.
     */
    fun analyzeSentiment(text: String): Double {
        val positiveWords = setOf(
            "great", "awesome", "accomplished", "happy", "productive", 
            "success", "win", "good", "energized", "focused", "motivated",
            "grateful", "love", "excellent", "progress", "achieved"
        )
        val negativeWords = setOf(
            "tired", "sad", "fail", "bad", "unproductive", "stressed", 
            "angry", "procrastinating", "stuck", "boring", "waste", 
            "difficult", "exhausted", "lazy", "regret", "poor"
        )

        val words = text.lowercase().split(Regex("\\s+"))
        var score = 0
        var totalHits = 0

        words.forEach { word ->
            when {
                positiveWords.contains(word) -> { score++; totalHits++ }
                negativeWords.contains(word) -> { score--; totalHits++ }
            }
        }
        return if (totalHits == 0) 0.0 else score.toDouble() / totalHits
    }

    /**
     * Calculates the user's "Velocity" (Tasks completed per day).
     */
    fun calculateTaskVelocity(completionTimestamps: List<Long>, days: Int = 14): Double {
        if (completionTimestamps.isEmpty()) return 0.0
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val count = completionTimestamps.count { it >= cutoff }
        return count.toDouble() / days
    }

    /**
     * Calculates a priority score for a task using weighted heuristic scoring.
     */
    fun calculatePriorityScore(
        priority: Int, 
        ageInDays: Int, 
        daysUntilDeadline: Int?, 
        subtaskCount: Int
    ): Double {
        val priorityComponent = (priority + 1) * 20.0
        val urgencyComponent = if (daysUntilDeadline == null) {
            ageInDays.toDouble().coerceAtMost(20.0)
        } else {
            val proximity = (14 - daysUntilDeadline).coerceAtLeast(0)
            proximity * 5.0
        }
        val complexityComponent = (subtaskCount * 5.0).coerceAtMost(20.0)
        return priorityComponent + urgencyComponent + complexityComponent
    }
}
