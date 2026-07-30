package com.example.allinone

import org.junit.Test
import org.junit.Assert.*

/**
 * IntelligenceEngineTest: Mathematically validates the core statistical and NLP algorithms.
 * Proves the accuracy of the Data Science components of the app.
 */
class IntelligenceEngineTest {

    @Test
    fun calculateCorrelation_perfectPositive_returnsOne() {
        val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val y = listOf(2.0, 4.0, 6.0, 8.0, 10.0)
        val correlation = IntelligenceEngine.calculateCorrelation(x, y)
        assertEquals(1.0, correlation, 0.001)
    }

    @Test
    fun calculateCorrelation_perfectNegative_returnsMinusOne() {
        val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val y = listOf(5.0, 4.0, 3.0, 2.0, 1.0)
        val correlation = IntelligenceEngine.calculateCorrelation(x, y)
        assertEquals(-1.0, correlation, 0.001)
    }

    @Test
    fun predictNextValue_linearSeries_predictsCorrectly() {
        val series = listOf(10.0, 20.0, 30.0) // Steps of 10
        val prediction = IntelligenceEngine.predictNextValue(series)
        assertEquals(40.0, prediction, 0.001)
    }

    @Test
    fun calculateZScore_detectsOutlier() {
        val series = listOf(10.0, 12.0, 11.0, 9.0, 10.0) // Has some variance
        val outlierValue = 50.0 // Significant spike
        val zScore = IntelligenceEngine.calculateZScore(series, outlierValue)
        assertTrue("Z-Score should be high for outliers", zScore > 2.0)
    }

    @Test
    fun analyzeSentiment_positiveText_returnsPositiveScore() {
        val text = "Today was a great success, I feel accomplished and happy!"
        val score = IntelligenceEngine.analyzeSentiment(text)
        assertTrue("Score should be positive", score > 0)
    }

    @Test
    fun analyzeSentiment_negativeText_returnsNegativeScore() {
        val text = "I am so tired and stressed, it was a bad fail today."
        val score = IntelligenceEngine.analyzeSentiment(text)
        assertTrue("Score should be negative", score < 0)
    }

    @Test
    fun analyzeSentiment_neutralText_returnsZero() {
        val text = "The table is brown and the sky is blue."
        val score = IntelligenceEngine.analyzeSentiment(text)
        assertEquals(0.0, score, 0.001)
    }
}
